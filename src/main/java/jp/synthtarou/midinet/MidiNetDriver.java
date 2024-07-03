package jp.synthtarou.midinet;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 複数あるドライバの基底クラス
 * この１つ下に、デバイス一覧がつながっている
 * デバイスの下のポートは、IN1OUT1としてあつかう、2つ以上あっても認識しない
 * MIDIなど、開く前からわかるもののみ、INとOUTの有無をチェックしていて、
 * 開かないとわからないものはチェックしない
 */
public abstract class MidiNetDriver {
    static String TAG = "MidiNetDriver";

    String _prefix;
    MidiNetManager _manager;
    ArrayList<MidiNetDeviceInfo> _connectedDevices = new ArrayList<>();
    MidiNetDriver(String prefix, MidiNetManager manager) {
        _manager = manager;
        if (prefix == null || prefix.isEmpty()) {
            _prefix = "";
        }
        else {
            _prefix = "(" + prefix +") ";
        }
    }
    public synchronized MidiNetDeviceInfo recordDeviceDetected(String name, String uuid, Object object) {
        Log.e(TAG, "recordDeviceDetected " + _prefix + name);
        for (MidiNetDeviceInfo seek : _connectedDevices) {
            if (seek._uuidInDriver.equals(uuid)) {
                seek._name = name;
                seek._infoObject = object;
                if (seek._disconnected) {
                    seek._disconnected = false;
                }
                return seek;
            }
        }
        name = _prefix + name;
        MidiNetDeviceInfo info = new MidiNetDeviceInfo(this, name, uuid, object);
        _connectedDevices.add(info);
        _manager.fireDeviceDetected(this, info);
        return info;
    }

    public synchronized void recordDeviceLostDetected(MidiNetDeviceInfo info) {
        _connectedDevices.remove(info);
        _manager.fireDeviceLostDectected(this, info);
    }

    public void postEnumerateDevice() {
        //SingleTaskQueue.getMainLooper().push(() -> {
            mainEnumerateDevices();
        //});
    }

    protected abstract void mainEnumerateDevices();

    protected void terminate() {
        for (MidiNetDeviceInfo info : _connectedDevices) {
            disconnectImpl(info);
        }
    }

    public abstract void disconnectImpl(MidiNetDeviceInfo info);

    public abstract MidiNetStream getOrCreateReader(MidiNetDeviceInfo info, MidiNetStream handler);
    public abstract MidiNetStream getOrCreateWriter(MidiNetDeviceInfo info);

    protected MidiNetStream getReader(MidiNetDeviceInfo info) {
        return mapReader.get(info._uuidInDriver);
    }
    protected void registerReader(MidiNetDeviceInfo info, MidiNetStream reader) {
        mapReader.put(info._uuidInDriver, reader);
    }
    protected void removeReader(MidiNetDeviceInfo info) {
        mapReader.remove(info._uuidInDriver);
    }

    protected MidiNetStream getWriter(MidiNetDeviceInfo info) {
        return mapWriter.get(info._uuidInDriver);
    }
    protected void registerWriter(MidiNetDeviceInfo info, MidiNetStream writer) {
        mapWriter.put(info._uuidInDriver, writer);
    }
    protected void removeWriter(MidiNetDeviceInfo info) {
        mapReader.remove(info._uuidInDriver);
    }

    HashMap<String, MidiNetStream> mapReader = new HashMap<>();
    HashMap<String, MidiNetStream> mapWriter = new HashMap<>();

    public ArrayList<MidiNetDeviceInfo> listDevice() {
        return _connectedDevices;
    }
    public abstract void stopScan();
}
