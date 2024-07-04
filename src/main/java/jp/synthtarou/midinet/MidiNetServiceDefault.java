package jp.synthtarou.midinet;

import android.content.Context;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiDeviceStatus;
import android.media.midi.MidiManager;
import android.os.Build;

import jp.synthtarou.midinet.task.SingleTaskFlag;
import jp.synthtarou.midinet.task.SingleTaskQueue;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 普通のAndroidMIDIAPIを用いる
 * 複数デバイス・アプリサービスに対応MidiNetService
 */
public class MidiNetServiceDefault extends MidiNetService {
    static String TAG = "MidiNetServiceDefault";
    MidiManager _manager;
    MidiManager.DeviceCallback _callback;

    public MidiNetServiceDefault(MidiNetManager manager, Context context) {
        super("", manager);
        _manager = (MidiManager) context.getSystemService(Context.MIDI_SERVICE);
    }

    @Override
    public void startEnumerate() {
        Set<MidiDeviceInfo> infoList;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            infoList = _manager.getDevicesForTransport(MidiManager.TRANSPORT_MIDI_BYTE_STREAM);
        } else {
            infoList = new HashSet<>();
            Collections.addAll(infoList, _manager.getDevices());
        }
        for (MidiDeviceInfo device : infoList) {
            String name = device.getProperties().getString(MidiDeviceInfo.PROPERTY_NAME);
            MidiNetDeviceInfo info = findDeviceInfoByUUID(name);
            if (info == null) {
                info = new MidiNetDeviceInfoDefault(this, name, name, device);
                info._hasOutput= device.getOutputPortCount() > 0;
                info._hasInput= device.getInputPortCount() > 0;
                recordDeviceDetected(info);
            }
        }
        _callback = new MidiManager.DeviceCallback() {
            @Override
            public void onDeviceAdded(MidiDeviceInfo device) {
                String name = device.getProperties().getString(MidiDeviceInfo.PROPERTY_NAME);
                MidiNetDeviceInfo info;
                info = new MidiNetDeviceInfoDefault(MidiNetServiceDefault.this, name, name, device);
                info._hasOutput= device.getOutputPortCount() > 0;
                info._hasInput= device.getInputPortCount() > 0;
                recordDeviceDetected(info);
            }

            @Override
            public void onDeviceRemoved(MidiDeviceInfo info) {
                String propName = info.getProperties().getString(MidiDeviceInfo.PROPERTY_NAME);
                if (propName != null) {
                    synchronized (_listDevices) {
                        for (MidiNetDeviceInfo seek : _listDevices) {
                            if (seek._name.equals(propName)) {
                                recordDeviceLostDetected(seek);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onDeviceStatusChanged(MidiDeviceStatus status) {
            }
        };
        _manager.registerDeviceCallback(_callback, null);
    }

    public void startDeepScan() {
        return;
    }


    @Override
    public void terminateAllDevices() {
        super.terminateAllDevices();
        _manager.unregisterDeviceCallback(_callback);
    }

    long _lastPrepared = 0;

    @Override
    public void stopDeepScan(){
    }
}
