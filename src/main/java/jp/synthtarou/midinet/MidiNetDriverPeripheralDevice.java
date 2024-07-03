package jp.synthtarou.midinet;

import android.content.Context;

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
public class MidiNetDriverPeripheralDevice {
    Context _context;
    MidiNetDeviceInfo _alias;
    MidiNetDriverPeripheral _device;

    public MidiNetDriverPeripheralDevice(MidiNetDriverPeripheral device, Context context, MidiNetDeviceInfo alias) {
        _context = context;
        _alias = alias;
        _device = device;
    }

    BleMidiPeripheralProvider _provider;

    jp.kshoji.blemidi.device.MidiInputDevice _midiInputDevice;
    jp.kshoji.blemidi.device.MidiOutputDevice _midiOutputDevice;

    public void start() {
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

                _midiInputDevice.setOnMidiInputEventListener(new OnMidiInputEventStreamForBLE() {
                    @Override
                    public void onReceivedData(@NonNull MidiInputDevice sender, @NonNull byte[] data, int offset, int count) {
                        if (sender == _midiInputDevice) {
                            if (_alias._onRead != null){
                                _alias._onRead.receivedData(data, offset, count);
                            }
                        }
                    }
                });
                _device._manager.fireReaderOpened(_device, _alias);
            }

            @Override
            public void onMidiOutputDeviceAttached(@NonNull MidiOutputDevice midiOutputDevice) {
                _midiOutputDevice = midiOutputDevice;
                _alias._onWrite = new MidiNetStream() {
                    @Override
                    public void receivedData(byte[] data, int offset, int length) {
                        if (_midiOutputDevice != null) {
                            try {
                                _midiOutputDevice.sendMidiMessage(data, offset, length);
                            }catch(Throwable ex) {
                                _alias.markBreak();
                            }
                        }
                    }
                };
                _device._manager.fireWriterOpened(_device, _alias);
            }
        });
        _provider.setOnMidiDeviceDetachedListener(new OnMidiDeviceDetachedListener() {
            @Override
            public void onMidiInputDeviceDetached(@NonNull MidiInputDevice midiInputDevice) {
                _midiInputDevice = null;
                _device._manager.fireReaderClosed(_device, _alias);
            }

            @Override
            public void onMidiOutputDeviceDetached(@NonNull MidiOutputDevice midiOutputDevice) {
                _midiOutputDevice = null;
                _device._manager.fireWriterClosed(_device, _alias);
            }
        });

        _provider.startAdvertising();;
    }

    static String TAG = "MidiNetDeviceInfoUSB";

    public void terminate() {
        _midiInputDevice = null;
        _midiOutputDevice = null;
        _provider.terminate();
    }
}
