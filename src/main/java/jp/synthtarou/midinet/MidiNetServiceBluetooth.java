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
 * Bluetooth（セントラル用）のMidiNetService
 * 複数デバイスに対応
 */
public class MidiNetServiceBluetooth extends MidiNetService {
    BleMidiCentralProvider _provider;
    static String TAG = "MidiNetServiceBluetooth";
    MidiManager.DeviceCallback _callback;

    Activity _context;

    Runnable _runnable;
    public void addConnectionListener(Runnable runnable) {
        _runnable = runnable;
    }

    boolean _usable = false;

    @SuppressLint("NewApi")
    public MidiNetServiceBluetooth(MidiNetManager manager, Activity context) {
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
        _usable = new GrantPermissionTask(android.Manifest.permission.BLUETOOTH_CONNECT, _context).grantPermission();
        _usable &= new GrantPermissionTask(android.Manifest.permission.BLUETOOTH_SCAN, _context).grantPermission();
        _usable &= new GrantPermissionTask(android.Manifest.permission.BLUETOOTH_ADMIN, _context).grantPermission();
        _usable &= new GrantPermissionTask(Manifest.permission.ACCESS_FINE_LOCATION, _context).grantPermission();
        _usable &= new GrantPermissionTask(android.Manifest.permission.ACCESS_COARSE_LOCATION, _context).grantPermission();
    }

    @Override
    public void startDeepScan() {
        _provider.stopScanDevice();
        _provider.setRequestPairing(true);
        _provider.startScanDevice(30000);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void startEnumerate() {
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
                    MidiNetDeviceInfo info = new MidiNetDeviceInfoBluetooth(this, name, address, device);
                    info._hasOutput = true;
                    info._hasInput = true;
                    recordDeviceDetected(info);
                    break;
                }
            }
        }

    }

    @Override
    public void terminateAllDevices() {
        super.terminateAllDevices();
    }

    public void connectGatt(BluetoothDevice device) {
        _provider.connectGatt(device);
    }
    @Override
    public void stopDeepScan() {
        _provider.stopScanDevice();
    }

    MidiNetStream _onRead = null;

}

