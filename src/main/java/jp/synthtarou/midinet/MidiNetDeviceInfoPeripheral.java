package jp.synthtarou.midinet;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import jp.kshoji.blemidi.listener.OnMidiDeviceAttachedListener;
import jp.kshoji.blemidi.listener.OnMidiDeviceDetachedListener;
import jp.synthtarou.midinet.patchlib.OnMidiInputEventStreamForBLE;
import jp.kshoji.blemidi.peripheral.BleMidiPeripheralProvider;
import jp.kshoji.blemidi.device.MidiInputDevice;
import jp.kshoji.blemidi.device.MidiOutputDevice;

/**
 * MidiNetDriverPeripheralのデバイス情報（公開用）
 * 公開用の名前などを保持していないので、でっちあげる
 */
public class MidiNetDeviceInfoPeripheral extends MidiNetDeviceInfo {
    static String TAG = "MidiNetServicePeripheralDevice";
    Context _context;
    BleMidiPeripheralProvider _provider;

    public MidiNetDeviceInfoPeripheral(MidiNetServicePeripheral service, Context context) {
        super(service, "Be BLE Device", "ble2");
        _context = context;
    }

    jp.kshoji.blemidi.device.MidiInputDevice _midiInputDevice;
    jp.kshoji.blemidi.device.MidiOutputDevice _midiOutputDevice;

    public void startAdvertising() {
        Log.e(TAG, "startAdvertising");
        if (_provider != null) {
            _provider.startAdvertising();
            return;
        }
        _provider = new BleMidiPeripheralProvider(_context);
        _provider.setAutoStartDevice(true);
        _provider.setOnMidiDeviceAttachedListener(new OnMidiDeviceAttachedListener() {
            @Override
            public void onMidiInputDeviceAttached(@NonNull MidiInputDevice midiInputDevice) {
                _midiInputDevice = midiInputDevice;
                _inputOpened = true;

                _midiInputDevice.setOnMidiInputEventListener(new OnMidiInputEventStreamForBLE() {
                    @Override
                    public void onReceivedData(@NonNull MidiInputDevice sender, @NonNull byte[] data, int offset, int count) {
                        if (sender == _midiInputDevice) {
                            if (_onRead != null){
                                _onRead.receivedData(data, offset, count);
                            }
                        }
                    }
                });
                _service._manager.fireReaderOpened(_service, MidiNetDeviceInfoPeripheral.this);
            }

            @Override
            public void onMidiOutputDeviceAttached(@NonNull MidiOutputDevice midiOutputDevice) {
                _midiOutputDevice = midiOutputDevice;
                _outputOpened = true;
                _service._manager.fireWriterOpened(_service, MidiNetDeviceInfoPeripheral.this);
            }
        });
        _provider.setOnMidiDeviceDetachedListener(new OnMidiDeviceDetachedListener() {
            @Override
            public void onMidiInputDeviceDetached(@NonNull MidiInputDevice midiInputDevice) {
                _midiInputDevice = null;
                _inputOpened = false;
                _service._manager.fireReaderClosed(_service, MidiNetDeviceInfoPeripheral.this);
            }

            @Override
            public void onMidiOutputDeviceDetached(@NonNull MidiOutputDevice midiOutputDevice) {
                _midiOutputDevice = null;
                _outputOpened = false;
                _service._manager.fireWriterClosed(_service, MidiNetDeviceInfoPeripheral.this);
            }
        });

        _provider.startAdvertising();;
    }

    public void terminate() {
        Log.e(TAG, "terminate");
        _midiInputDevice = null;
        _midiOutputDevice = null;
        _provider.terminate();
    }

    @Override
    public boolean prepareInput(MidiNetStream handler) {
        Log.e(TAG, "prepareInput");
        _onRead = handler;
        if (_midiInputDevice != null) {
            _midiInputDevice.start();
            return true;
        }else {
            startAdvertising();
            if (_midiInputDevice != null) {
                _midiInputDevice.start();
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean prepareOutput() {
        Log.e(TAG, "prepareOutput");
        if (_midiOutputDevice != null) {
            _midiOutputDevice.start();
            return true;
        }
        else {
            startAdvertising();
            if (_midiOutputDevice != null) {
                _midiOutputDevice.start();
                return true;
            }
            return false;
        }
    }

    @Override
    public void closeInput() {
        Log.e(TAG, "closeInput");
        super.closeInput();
        if (_midiInputDevice != null) {
            _midiInputDevice.stop();
        }
    }
    @Override
    public void closeOutput() {
        Log.e(TAG, "closeOutput");
        super.closeOutput();
        if (_midiOutputDevice != null) {
            _midiOutputDevice.stop();
        }
    }

    @Override
    public void closeDeviceConnection() {
        Log.e(TAG, "closeDeviceConnection");
        closeInput();
        closeOutput();
        _midiOutputDevice = null;
        _midiInputDevice = null;
        _provider.terminate();
        _provider = null;
    }

    public int getSortOrder() {
        return 0;
    }

    @Override
    public void processOutput(@NonNull byte[] data, int offset, int length) {
        if (length == 0 || _outputOpened == false) {
            return;
        }
        if (_midiOutputDevice != null) {
            try {
                _midiOutputDevice.sendMidiMessage(data, offset, length);
            }catch(Throwable ex) {
                recordIOException(ex);
            }
        }
    }
}
