package jp.synthtarou.midinet;

import android.content.Context;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiDeviceStatus;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.os.Build;
import android.util.Log;

import jp.synthtarou.midinet.task.SingleTaskFlag;
import jp.synthtarou.midinet.task.SingleTaskQueue;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 普通のAndroidMIDIAPIを用いる
 * 複数デバイス・アプリサービスに対応Driverの一種
 */
public class MidiNetDriverDefault extends MidiNetDriver {
    static String TAG = "MidiNetDriverDefault";
    MidiManager _manager;
    MidiManager.DeviceCallback _callback;

    public MidiNetDriverDefault(MidiNetManager manager, Context context) {
        super("", manager);
        _manager = (MidiManager) context.getSystemService(Context.MIDI_SERVICE);
    }

    @Override
    protected void mainEnumerateDevices() {
        Set<MidiDeviceInfo> infoList;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            infoList = _manager.getDevicesForTransport(MidiManager.TRANSPORT_MIDI_BYTE_STREAM);
        } else {
            infoList = new HashSet<>();
            Collections.addAll(infoList, _manager.getDevices());
        }

        for (MidiDeviceInfo seek : infoList) {
            String name = seek.getProperties().getString(MidiDeviceInfo.PROPERTY_NAME);
            //String ble = seek.getProperties().getString(MidiDeviceInfo.PROPERTY_BLUETOOTH_DEVICE);
            MidiNetDeviceInfo info = recordDeviceDetected(name, name, seek);
            info._portCountReder = seek.getOutputPortCount();
            info._portCountWriter = seek.getInputPortCount();
        }
        _callback = new MidiManager.DeviceCallback() {
            @Override
            public void onDeviceAdded(MidiDeviceInfo device) {
                String name = device.getProperties().getString(MidiDeviceInfo.PROPERTY_NAME);
                //String ble = device.getProperties().getString(MidiDeviceInfo.PROPERTY_BLUETOOTH_DEVICE);
                MidiNetDeviceInfo info = recordDeviceDetected(name, name, device);
                info._portCountReder = device.getOutputPortCount();
                info._portCountWriter = device.getInputPortCount();
            }

            @Override
            public void onDeviceRemoved(MidiDeviceInfo info) {
                for (MidiNetDeviceInfo seek : _connectedDevices) {
                    if (seek._name.equals(info.getProperties().getString(MidiDeviceInfo.PROPERTY_NAME))) {
                        recordDeviceLostDetected(seek);
                        break;
                    }
                }
            }

            @Override
            public void onDeviceStatusChanged(MidiDeviceStatus status) {
            }
        };
        _manager.registerDeviceCallback(_callback, null);
    }

    @Override
    public void disconnectImpl(MidiNetDeviceInfo info) {
        if (info._disconnected == false) {
            info._disconnected = true;
            MidiDeviceInfo deviceInfo = (MidiDeviceInfo) info._infoObject;
            MidiDevice device = (MidiDevice) info._connectedObject;

            try {
                device.close();
            } catch (Throwable ex) {

            }
            info._connectedObject = null;
        }
    }

    @Override
    public void terminate() {
        super.terminate();
        _manager.unregisterDeviceCallback(_callback);
    }

    public MidiDevice prepareDevice(MidiNetDeviceInfo info) {
        MidiDeviceInfo deviceInfo = (MidiDeviceInfo) info._infoObject;
        MidiDevice device = (MidiDevice) info._connectedObject;
        if (device != null) {
            return device;
        }
        SingleTaskFlag flag = new SingleTaskFlag();
        SingleTaskFlag f2 = SingleTaskQueue.getMainLooper().push(() -> {
            try {
                MidiManager manager = _manager;
                manager.openDevice(deviceInfo, new MidiManager.OnDeviceOpenedListener() {
                    @Override
                    public void onDeviceOpened(MidiDevice device) {
                        try {
                            info._connectedObject = device;
                            info._infoObject = device.getInfo();
                            flag.done();
                        }catch(Throwable ex) {
                            flag.done(ex);
                        }
                    }
                }, null);
            }catch(Throwable ex) {
                flag.done(ex);
            }
        });
        if (flag.awaitResult(5000) == false) {
            Throwable ex  = flag.awaitThrowable(0);
            Log.e(TAG, "WhyFalse", ex);
            return null;
        }
        return (MidiDevice) info._connectedObject;
    }
    @Override
    public MidiNetStream getOrCreateReader(MidiNetDeviceInfo info, MidiNetStream handler) {
        if (info._onRead != null) {
            return info._onRead;
        }
        //TODO manager reconnect
        MidiNetStream onRead = getReader(info);
        if (onRead == null) {
            MidiDevice device = (MidiDevice) info._connectedObject;
            if (device == null){
                device = prepareDevice(info);
                if (device == null) {
                    return null;
                }
            }

            mapReader.put(info._uuidInDriver, handler);
        }
        registerReader(info, onRead);
        return onRead;
    }
    @Override
    public MidiNetStream getOrCreateWriter(MidiNetDeviceInfo info) {
        if (info._onWrite != null) {
            return info._onWrite;
        }
        //TODO manager reconnect
        MidiNetStream onWrite = getWriter(info);
        if (onWrite == null) {
            MidiDeviceInfo deviceInfo = (MidiDeviceInfo) info._infoObject;
            MidiDevice device = (MidiDevice) info._connectedObject;
            if (device == null){
                device = prepareDevice(info);
                if (device == null) {
                    return null;
                }
            }

            MidiInputPort outgoing = device.openInputPort(0);
            onWrite = new MidiNetStream() {
                @Override
                public void receivedData(byte[] data, int offset, int length) {
                    try {
                        outgoing.send(data, offset, length);
                    }catch(IOException ex) {
                        info.markBreak();
                    }
                }
            };
        }
        registerWriter(info, onWrite);
        return onWrite;
    }

    @Override
    public void stopScan(){
    }
}
