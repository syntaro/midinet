package jp.synthtarou.midinet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.NonNull;

import jp.synthtarou.midinet.task.SingleTaskFlag;
import jp.synthtarou.midinet.libs.GrantPermissionTask;
import jp.synthtarou.midinet.libs.TheUUID;

import java.util.Set;

import jp.kshoji.blemidi.central.BleMidiCentralProvider;
import jp.kshoji.blemidi.device.MidiInputDevice;
import jp.kshoji.blemidi.device.MidiOutputDevice;
import jp.kshoji.blemidi.listener.OnMidiDeviceAttachedListener;
import jp.kshoji.blemidi.listener.OnMidiDeviceDetachedListener;
import jp.synthtarou.midinet.patchlib.OnMidiInputEventStreamForBLE;
import jp.kshoji.blemidi.listener.OnMidiScanStatusListener;

/**
 * Bluetooth（セントラル用）のMidiNetDriver
 * 複数デバイスに対応
 */
public class MidiNetDriverBluetooth extends MidiNetDriver {
    BleMidiCentralProvider _provider;
    static String TAG = "MidiNetDriverDefault";
    MidiManager.DeviceCallback _callback;

    Activity _context;

    Runnable _runnable;
    public void addConnectionListener(Runnable runnable) {
        _runnable = runnable;
    }

    @SuppressLint("NewApi")
    public MidiNetDriverBluetooth(MidiNetManager manager, Activity context) {
        super("BLE", manager);
        _context = context;

        _provider = new BleMidiCentralProvider(context);
        _provider.setAutoStartInputDevice(false);
        _provider.setRequestPairing(false);

        _provider.setOnMidiScanStatusListener(new OnMidiScanStatusListener() {
            @Override
            public void onMidiScanStatusChanged(boolean isScanning) {
            }
        });
        _provider.setOnMidiDeviceAttachedListener(new OnMidiDeviceAttachedListener() {
            @Override
            public void onMidiInputDeviceAttached(@NonNull MidiInputDevice midiInputDevice) {
                if (_runnable != null) {
                    _runnable.run();
                }
            }

            @Override
            public void onMidiOutputDeviceAttached(@NonNull MidiOutputDevice midiOutputDevice) {
                if (_runnable != null) {
                    _runnable.run();
                }
            }
        });
        _provider.setOnMidiDeviceDetachedListener(new OnMidiDeviceDetachedListener() {
            @Override
            public void onMidiInputDeviceDetached(@NonNull MidiInputDevice midiInputDevice) {
                Log.e(TAG, "onMidiInputDeviceDetached " + midiInputDevice.getDeviceAddress() + "/" + midiInputDevice.getDeviceName());
                Log.e(TAG, "midiInput " + _provider.getMidiInputDevices());
                Log.e(TAG, "midiOutput " + _provider.getMidiOutputDevices());
            }

            @Override
            public void onMidiOutputDeviceDetached(@NonNull MidiOutputDevice midiOutputDevice) {
                Log.e(TAG, "onMidiOutputDeviceDetached " + midiOutputDevice.getDeviceAddress() + "/" + midiOutputDevice.getDeviceName());
                Log.e(TAG, "midiInput " + _provider.getMidiInputDevices());
                Log.e(TAG, "midiOutput " + _provider.getMidiOutputDevices());
            }
        });
        new GrantPermissionTask(android.Manifest.permission.BLUETOOTH_CONNECT, _context).grantPermission();
        new GrantPermissionTask(android.Manifest.permission.BLUETOOTH_SCAN, _context).grantPermission();
        new GrantPermissionTask(android.Manifest.permission.BLUETOOTH_ADMIN, _context).grantPermission();
        new GrantPermissionTask(Manifest.permission.ACCESS_FINE_LOCATION, _context).grantPermission();
        new GrantPermissionTask(android.Manifest.permission.ACCESS_COARSE_LOCATION, _context).grantPermission();
        _provider.setAutoStartInputDevice(true);
    }

    public void launchScanner() {
        _provider.setRequestPairing(true);
        _provider.startScanDevice(30000);
    }
    @SuppressLint("MissingPermission")
    @Override
    protected void mainEnumerateDevices() {
        BluetoothAdapter adapter = TheUUID.getAdapter();
        Set<BluetoothDevice> already = adapter.getBondedDevices();

        for (BluetoothDevice device : already) {
            ParcelUuid[] uuid = device.getUuids();
            if (uuid == null) {
                continue;
            }
            for (int i = 0; i < uuid.length; ++i) {
                if (TheUUID.MIDI_SERVICE.equals(uuid[i])) {
                    String name = device.getName();
                    String address = device.getAddress();
                    MidiNetDeviceInfo info = recordDeviceDetected(name, address, device);
                    info._portCountWriter = 1;
                    info._portCountReder = 1;
                    break;
                }
            }
        }

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
    }

    class DeviceAttachListener implements OnMidiDeviceAttachedListener {
        MidiNetDeviceInfo _info;
        SingleTaskFlag _flag = new SingleTaskFlag();
        boolean _isTargetWriter;
        DeviceAttachListener(MidiNetDeviceInfo info, boolean targetIsWriter) {
            _flag.started();
            _info = info;
            _isTargetWriter = targetIsWriter;
        }

        @Override
        public void onMidiInputDeviceAttached(@NonNull MidiInputDevice midiInputDevice) {
            MidiNetStream cussion = new MidiNetStream() {
                @Override
                public void receivedData(byte[] data, int offset, int length) {
                    if (_onRead != null) {
                        _onRead.receivedData(data, offset, length);
                    }
                }
            };
            registerReader(_info, cussion);
            _manager.fireReaderOpened(MidiNetDriverBluetooth.this, _info);
            midiInputDevice.start();
            midiInputDevice.setOnMidiInputEventListener(new OnMidiInputEventStreamForBLE() {
                @Override
                public void onReceivedData(@NonNull MidiInputDevice sender, @NonNull byte[] data, int offset, int count) {
                    cussion.receivedData(data, offset, count);
                }
            });
            synchronized (MidiNetDriverBluetooth.this) {
                MidiNetDriverBluetooth.this.notifyAll();
            }
        }

        @Override
        public void onMidiOutputDeviceAttached(@NonNull MidiOutputDevice midiOutputDevice) {
            midiOutputDevice.start();
            MidiNetStream writer = new MidiNetStream() {
                @Override
                public void receivedData(byte[] data, int offset, int length) {
                    midiOutputDevice.sendMidiMessage(data, offset, length);
                }
            };
            registerWriter(_info, writer);
            _manager.fireWriterOpened(MidiNetDriverBluetooth.this, _info);
            synchronized (MidiNetDriverBluetooth.this) {
                MidiNetDriverBluetooth.this.notifyAll();
            }
        }
    };

    public void prepareDevice(MidiNetDeviceInfo info, boolean isTargetWriter) {
        if (isTargetWriter) {
            if (getWriter(info) != null) {
                return;
            }
        }
        else {
            if (getReader(info) != null) {
                return;
            }
        }
        BluetoothDevice device = (BluetoothDevice) info._infoObject;
        DeviceAttachListener listener = new DeviceAttachListener(info, isTargetWriter);
        _provider.setOnMidiDeviceAttachedListener(listener);
        _provider.connectGatt(device);
        if (isTargetWriter) {
            synchronized (this) {
                if (getWriter(info)== null) {
                    try {
                        MidiNetDriverBluetooth.this.wait(3000);
                    }catch(InterruptedException ex) {

                    }
                }
            }
        }
        else {
            synchronized (this) {
                if (getReader(info) == null) {
                    try {
                        MidiNetDriverBluetooth.this.wait(3000);
                    }catch(InterruptedException ex) {

                    }
                }
            }
        }
    }

    @Override
    public MidiNetStream getOrCreateReader(MidiNetDeviceInfo info, MidiNetStream handler) {
        prepareDevice(info, false);
        _onRead = handler;
        return mapReader.get(info._uuidInDriver);
    }

    @Override
    public MidiNetStream getOrCreateWriter(MidiNetDeviceInfo info) {
        prepareDevice(info, true);
        return mapWriter.get(info._uuidInDriver);
    }

    public void connectGatt(BluetoothDevice device) {
        _provider.connectGatt(device);
    }
    @Override
    public void stopScan() {
        _provider.stopScanDevice();
    }

    MidiNetStream _onRead = null;
}
