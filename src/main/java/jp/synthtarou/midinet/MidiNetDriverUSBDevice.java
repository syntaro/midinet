package jp.synthtarou.midinet;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.Log;

import androidx.annotation.NonNull;

import jp.synthtarou.midinet.libs.MXUtil;
import jp.synthtarou.midinet.libs.MXMidiStatic;

import java.util.HashSet;

import jp.kshoji.driver.midi.device.MidiInputDevice;
import jp.kshoji.driver.midi.device.MidiOutputDevice;
import jp.synthtarou.midinet.patchlib.OnMidiInputEventStreamForUSB;
import jp.kshoji.driver.midi.util.UsbMidiDriver;

/**
 * KshojiさんのつかられてるUSB-MIDIを用いるMidiNetDriverの一種
 * RolandUM-ONE2で確認、公開用の名前をドライバが保持していないので、
 * でっちあげる
 */
public class MidiNetDriverUSBDevice {
    Context _context;
    MidiNetDeviceInfo _alias;

    public MidiNetDriverUSBDevice(MidiNetDriver driver, Context context, MidiNetDeviceInfo alias) {
        _context = context;
        _alias = alias;
    }
    UsbMidiDriver _driver = null;

    public void start() {
        if (_driver != null) {
            return;
        }

        _driver = new UsbMidiDriver(_context) {
            @Override
            public void onDeviceAttached(@NonNull UsbDevice usbDevice) {
                mapUsb.add(usbDevice);
            }

            @Override
            public void onMidiInputDeviceAttached(@NonNull MidiInputDevice midiInputDevice) {
                mapInput.add(midiInputDevice);
            }

            @Override
            public void onMidiOutputDeviceAttached(@NonNull MidiOutputDevice midiOutputDevice) {
                mapOutput.add(midiOutputDevice);
            }

            @Override
            public void onDeviceDetached(@NonNull UsbDevice usbDevice) {
                mapUsb.remove(usbDevice);
            }

            @Override
            public void onMidiInputDeviceDetached(@NonNull MidiInputDevice midiInputDevice) {
                mapInput.remove(midiInputDevice);
                midiInputDevice.setMidiEventListener(new OnMidiInputEventStreamForUSB() {
                    @Override
                    public void onReceivedData(@NonNull MidiInputDevice sender, @NonNull byte[] data, int offset, int count) {
                        onReceive(midiInputDevice, data, offset, count);
                    }

                    @Override
                    public void onMidiMiscellaneousFunctionCodes(@NonNull MidiInputDevice sender, int cable, int byte1, int byte2, int byte3) {

                    }

                    @Override
                    public void onMidiCableEvents(@NonNull MidiInputDevice sender, int cable, int byte1, int byte2, int byte3) {

                    }

                    @Override
                    public void onMidiSystemCommonMessage(@NonNull MidiInputDevice sender, int cable, byte[] bytes) {

                    }

                    @Override
                    public void onMidiSingleByte(@NonNull MidiInputDevice sender, int cable, int byte1) {

                    }

                });
            }

            @Override
            public void onMidiOutputDeviceDetached(@NonNull MidiOutputDevice midiOutputDevice) {
                mapOutput.remove(midiOutputDevice);
            }

            @Override
            public void onMidiMiscellaneousFunctionCodes(@NonNull MidiInputDevice sender, int cable, int byte1, int byte2, int byte3) {
                Log.e(TAG, "onMidiMiscellaneousFunctionCodes " + cable +"," + byte1 +"," +byte2+ "," + byte3);
            }

            @Override
            public void onMidiCableEvents(@NonNull MidiInputDevice sender, int cable, int byte1, int byte2, int byte3) {
                Log.e(TAG, "onMidiCableEvents " + cable +"," + byte1 +"," +byte2+ "," + byte3);
            }

            @Override
            public void onMidiSystemCommonMessage(@NonNull MidiInputDevice sender, int cable, byte[] bytes) {
                Log.e(TAG, "onMidiSystemCommonMessage " + cable +"," + cable +"," +MXUtil.dumpHex(bytes));
                onReceive(sender, bytes);
            }

            @Override
            public void onMidiSystemExclusive(@NonNull MidiInputDevice sender, int cable, byte[] systemExclusive) {
                onReceive(sender, systemExclusive);
            }

            @Override
            public void onMidiNoteOff(@NonNull MidiInputDevice sender, int cable, int channel, int note, int velocity) {
                onReceive(sender, MXMidiStatic.COMMAND_CH_NOTEOFF + channel, note, velocity);
            }

            @Override
            public void onMidiNoteOn(@NonNull MidiInputDevice sender, int cable, int channel, int note, int velocity) {
                onReceive(sender, MXMidiStatic.COMMAND_CH_NOTEON + channel, note, velocity);
            }

            @Override
            public void onMidiPolyphonicAftertouch(@NonNull MidiInputDevice sender, int cable, int channel, int note, int pressure) {
                onReceive(sender, MXMidiStatic.COMMAND_CH_POLYPRESSURE + channel, note, pressure);
            }

            @Override
            public void onMidiControlChange(@NonNull MidiInputDevice sender, int cable, int channel, int function, int value) {
                onReceive(sender, MXMidiStatic.COMMAND_CH_CONTROLCHANGE + channel, function, value);
            }

            @Override
            public void onMidiProgramChange(@NonNull MidiInputDevice sender, int cable, int channel, int program) {
                onReceive(sender, MXMidiStatic.COMMAND_CH_PROGRAMCHANGE + channel, program);
            }

            @Override
            public void onMidiChannelAftertouch(@NonNull MidiInputDevice sender, int cable, int channel, int pressure) {
                onReceive(sender, MXMidiStatic.COMMAND_CH_CHANNELPRESSURE + channel, pressure);
            }

            @Override
            public void onMidiPitchWheel(@NonNull MidiInputDevice sender, int cable, int channel, int amount) {
                int hi = (amount >> 7)& 0x7f;
                int lo = amount & 0x7f;
                onReceive(sender, MXMidiStatic.COMMAND_CH_PITCH + channel, lo, hi);
            }

            @Override
            public void onMidiSingleByte(@NonNull MidiInputDevice sender, int cable, int byte1) {
                onReceive(sender, byte1);
            }

            @Override
            public void onMidiTimeCodeQuarterFrame(@NonNull MidiInputDevice sender, int cable, int timing) {
                onReceive(sender, MXMidiStatic.COMMAND_MIDITIMECODE, timing);
            }

            @Override
            public void onMidiSongSelect(@NonNull MidiInputDevice sender, int cable, int song) {
                onReceive(sender, MXMidiStatic.COMMAND_SONGSELECT, song);
            }

            @Override
            public void onMidiSongPositionPointer(@NonNull MidiInputDevice sender, int cable, int position) {
                int hi = (position >> 7)& 0x7f;
                int lo = position & 0x7f;
                onReceive(sender, MXMidiStatic.COMMAND_SONGPOSITION, lo, hi);
            }

            @Override
            public void onMidiTuneRequest(@NonNull MidiInputDevice sender, int cable) {
                onReceive(sender, MXMidiStatic.COMMAND_TUNEREQUEST);
            }

            @Override
            public void onMidiTimingClock(@NonNull MidiInputDevice sender, int cable) {
                onReceive(sender, MXMidiStatic.COMMAND_TRANSPORT_MIDICLOCK);
            }

            @Override
            public void onMidiStart(@NonNull MidiInputDevice sender, int cable) {
                onReceive(sender, MXMidiStatic.COMMAND_TRANSPOORT_START);
            }

            @Override
            public void onMidiContinue(@NonNull MidiInputDevice sender, int cable) {
                onReceive(sender, MXMidiStatic.COMMAND_TRANSPORT_CONTINUE);
            }

            @Override
            public void onMidiStop(@NonNull MidiInputDevice sender, int cable) {
                onReceive(sender, MXMidiStatic.COMMAND_TRNASPORT_STOP);
            }

            @Override
            public void onMidiActiveSensing(@NonNull MidiInputDevice sender, int cable) {
                onReceive(sender, MXMidiStatic.COMMAND_ACTIVESENSING);
            }

            @Override
            public void onMidiReset(@NonNull MidiInputDevice sender, int cable) {
                onReceive(sender, MXMidiStatic.COMMAND_META_OR_RESET);
            }

            @Override
            public boolean needProcessDataEntry() {
                return false;
            }
        };
        _driver.open();
    }

    void onReceive(MidiInputDevice device, byte[] data, int offset, int count) {
        _alias._driver._manager._globalReader.receivedData(data, offset, count);
    }

    static String TAG = "MidiNetDeviceInfoUSB";

    void onReceive(MidiInputDevice device, byte[] data) {
        onReceive(device, data , 0, data.length);
    }

    void onReceive(MidiInputDevice device, int status) {
        byte[] data = new byte[] { (byte)status };
        onReceive(device, data, 0, 1);
    }
    void onReceive(MidiInputDevice device, int status, int data1) {
        byte[] data = new byte[] { (byte)status, (byte)data1};
        onReceive(device, data, 0, 2);
    }
    void onReceive(MidiInputDevice device, int status, int data1, int data2) {
        byte[] data = new byte[] { (byte)status, (byte)data1, (byte)data2 };
        onReceive(device, data, 0, 3);
    }

    public void terminate() {
        _driver.close();
    }

    HashSet<UsbDevice> mapUsb = new HashSet();
    HashSet<MidiInputDevice> mapInput = new HashSet();
    HashSet<MidiOutputDevice> mapOutput = new HashSet();

}
