package jp.synthtarou.midinet;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 複数あるドライバの基底クラス
 * この１つ下に、デバイス一覧がつながっている
 * デバイスの下のポートは、IN1OUT1としてあつかう、2つ以上あっても認識しない
 * MIDIなど、開く前からわかるもののみ、INとOUTの有無をチェックしていて、
 * 開かないとわからないものはチェックしない
 */
public abstract class MidiNetService {
    static String TAG = "MidiNetService";

    String _prefix;
    MidiNetManager _manager;

    List<MidiNetDeviceInfo> _listDevices = Collections.synchronizedList(new ArrayList<>());

    MidiNetService(String prefix, MidiNetManager manager) {
        _manager = manager;
        if (prefix == null || prefix.isEmpty()) {
            _prefix = "";
        }
        else {
            _prefix = "(" + prefix +") ";
        }
    }

    public MidiNetDeviceInfo findDeviceInfoByUUID(String uuid) {
        synchronized (_listDevices) {
            for (MidiNetDeviceInfo seek : _listDevices) {
                if (seek._uuidInService.equals(uuid)) {
                    return seek;
                }
            }
        }
        return null;
    }

    public void recordDeviceDetected(MidiNetDeviceInfo info) {
        _listDevices.add(info);
        _manager.fireDeviceDetected(this, info);
    }

    public void recordDeviceLostDetected(MidiNetDeviceInfo info) {
        _manager.fireDeviceLostDectected(this, info);
    }

    protected void terminateAllDevices() {
        List<MidiNetDeviceInfo> copy;
        synchronized (_listDevices) {
            copy = new ArrayList<>(_listDevices);
        }
        for (MidiNetDeviceInfo info : copy) {
            info.closeInput();
            info.closeOutput();
            info.closeDeviceConnection();
        }
    }

    public abstract void startEnumerate();
    public abstract void startDeepScan();
    public abstract void stopDeepScan();
}
