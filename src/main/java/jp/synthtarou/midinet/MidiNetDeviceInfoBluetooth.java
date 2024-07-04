package jp.synthtarou.midinet;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import androidx.annotation.NonNull;

import jp.kshoji.blemidi.device.MidiInputDevice;
import jp.kshoji.blemidi.device.MidiOutputDevice;
import jp.kshoji.blemidi.listener.OnMidiDeviceAttachedListener;
import jp.synthtarou.midinet.patchlib.OnMidiInputEventStreamForBLE;
import jp.synthtarou.midinet.task.SingleTaskFlag;

public class MidiNetDeviceInfoBluetooth extends MidiNetDeviceInfo {
    BluetoothDevice _bluetooth;

    public MidiNetDeviceInfoBluetooth(@NonNull MidiNetServiceBluetooth service, @NonNull String name, @NonNull String uuid, @NonNull BluetoothDevice bluetooth) {
        super(service, name, uuid);
        _bluetooth = bluetooth;
    }

    @Override
    public boolean prepareInput(MidiNetStream handler) {
        _onRead = handler;
        if (_input != null) {
            _input.start();
            return true;
        }
        prepareDevice();
        return false;
    }

    @Override
    public void processOutput(@NonNull byte[] data, int offset, int length) {
        if (length == 0 || _outputOpened == false) {
            return;
        }
        try {
            if (_output != null) {
                _output.sendMidiMessage(data, offset, length);
            }
        }catch(Throwable ex) {
            recordIOException(ex);
        }
    }

    @Override
    public boolean prepareOutput() {
        if (_output != null) {
            _output.start();
            return true;
        }
        prepareDevice();
        if (_output != null) {
            _output.start();
            return true;
        }
        return false;
    }

    public void prepareDevice() {
        MidiNetServiceBluetooth service = (MidiNetServiceBluetooth) _service;
        DeviceAttachListener listener = new DeviceAttachListener();
        service._provider.setOnMidiDeviceAttachedListener(listener);
        service._provider.connectGatt(_bluetooth);
        //多重起動してしまうが、問題なさそうな？
    }

    MidiInputDevice _input;
    MidiOutputDevice _output;

    @Override
    public void closeInput() {
        super.closeInput();
        if (_input != null) {
            _input.stop();;
        }
        if (_inputOpened == false && _outputOpened == false) {
            //closeDeviceConnection();
        }
    }

    @Override
    public void closeOutput() {
        super.closeOutput();
        if (_output != null) {
            _output.stop();
        }
        if (_inputOpened == false && _outputOpened == false) {
            //closeDeviceConnection();
        }
    }

    class DeviceAttachListener implements OnMidiDeviceAttachedListener {
        SingleTaskFlag _flag = new SingleTaskFlag();

        DeviceAttachListener() {
            _flag.started();
        }

        @Override
        public void onMidiInputDeviceAttached(@NonNull MidiInputDevice midiInputDevice) {
            _input = midiInputDevice;
            _input.start();
            _service._manager.fireReaderOpened(_service, MidiNetDeviceInfoBluetooth.this);
            midiInputDevice.start();
            midiInputDevice.setOnMidiInputEventListener(new OnMidiInputEventStreamForBLE() {
                @Override
                public void onReceivedData(@NonNull MidiInputDevice sender, @NonNull byte[] data, int offset, int count) {
                    processInput(data, offset, count);
                }
            });
            _inputOpened = true;
        }

        @Override
        public void onMidiOutputDeviceAttached(@NonNull MidiOutputDevice midiOutputDevice) {
            _output = midiOutputDevice;
            _output.start();
            _outputOpened = true;
            _service._manager.fireWriterOpened(_service, MidiNetDeviceInfoBluetooth.this);
        }
    };

    public void closeDeviceConnection() {
        MidiNetServiceBluetooth ble = (MidiNetServiceBluetooth)_service;
        try {
            if(_input != null) {
                ble._provider.disconnectDevice(_input);
            }
        }catch(Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        }
        _input = null;
        try {
            if (_output != null) {
                ble._provider.disconnectDevice(_output);
            }
        }catch(Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        }
        _output = null;
    }

    public int getSortOrder() {
        return 100;
    }
}
