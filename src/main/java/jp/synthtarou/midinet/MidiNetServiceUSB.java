package jp.synthtarou.midinet;

import android.content.Context;
import android.hardware.usb.UsbDevice;

import androidx.annotation.NonNull;

//import org.star_advance.midimixer.libs.midi.MXMessageBus;

import java.util.HashSet;

import jp.kshoji.driver.midi.device.MidiInputDevice;
import jp.kshoji.driver.midi.device.MidiOutputDevice;
import jp.kshoji.driver.midi.util.UsbMidiDriver;
import jp.synthtarou.midinet.libs.MXMidiStatic;
import jp.synthtarou.midinet.patchlib.OnMidiInputEventStreamForUSB;

/**
 * USB機器をおあつかい、MidiNEtService
 */
public class MidiNetServiceUSB extends MidiNetService {
    static String TAG = "MidiNetServiceUSB";
    Context _context;
    MidiNetDeviceInfoUSB _index;
    UsbMidiDriver _usb = null;

    public MidiNetServiceUSB(MidiNetManager manager, Context context) {
        super("USB", manager);
        _context = context;
        _usb = createUSB();
    }

    @Override
    public void startEnumerate() {
        if (_index == null) {
            _index = new MidiNetDeviceInfoUSB(this, _context);
            recordDeviceDetected(_index);
        }
    }

    @Override
    public void startDeepScan() {
        _usb.open();
    }

    protected void terminateAllDevices() {
        //TODO
    }

    @Override
    public void stopDeepScan() {
    }

    void onAttached(UsbDevice device, MidiInputDevice input, MidiOutputDevice output) {
        /*
        String msg = device != null ? device.toString() : "";
        String msg2 = input != null ? input.toString() : "";
        String msg3 = output != null ? output.toString() : "";
        Log.e(TAG, "onAttached device="  + msg + " input=" + msg2 + " output=" + msg3);
        */
        if (device == null && input != null) {
            device = input.getUsbDevice();
        }
        if (device == null && output != null) {
            device = output.getUsbDevice();
        }
        MidiNetDeviceInfoUSB found = null;
        for (MidiNetDeviceInfo seek : _listDevices) {
            MidiNetDeviceInfoUSB seekingUsb = (MidiNetDeviceInfoUSB) seek;
            if (seekingUsb._usbDevice == device) {
                found = seekingUsb;
            }
        }
        boolean deviceFlag = false;
        boolean inputFlag = false;
        boolean outputFlag = false;
        if (found == null) {
            if (device == null) {
                throw new NullPointerException("device not seeked");
            }
            MidiNetDeviceInfoUSB elem = new MidiNetDeviceInfoUSB(this, _context, device);
            _listDevices.add(elem);
            deviceFlag = true;
            found = elem;
        }
        if (input != null) {
            synchronized (found._listInput) {
                found._listInput.add(input);
            }
            found._inputOpened = true;
            input.setMidiEventListener(new UsbListener(found));
            inputFlag = true;
        }
        if (output != null) {
            synchronized (found._listOutput) {
                found._listOutput.add(output);
            }
            found._outputOpened = true;
            outputFlag = true;
        }
        if (deviceFlag) {
            _manager.fireDeviceDetected(this, found);
        }
        if (inputFlag) {
            _manager.fireReaderOpened(this, found);
        }
        if (outputFlag) {
            _manager.fireWriterOpened(this, found);
        }
    }

    class UsbListener extends OnMidiInputEventStreamForUSB {

        @NonNull MidiNetDeviceInfoUSB _info;

        public UsbListener(@NonNull MidiNetDeviceInfoUSB info) {
            _info = info;
        }

        @Override
        public void onReceivedData(@NonNull MidiInputDevice sender, @NonNull byte[] data, int offset, int count) {
            _info.processInput(data, offset, count);
        }

        @Override
        public void onMidiMiscellaneousFunctionCodes(@NonNull MidiInputDevice sender, int cable, int byte1, int byte2, int byte3) {
            byte[] data = new byte[] { (byte)byte1, (byte)byte2, (byte)byte3 };
            _info.processInput(data, 0, data.length);
        }

        @Override
        public void onMidiCableEvents(@NonNull MidiInputDevice sender, int cable, int byte1, int byte2, int byte3) {

        }

        @Override
        public void onMidiSystemCommonMessage(@NonNull MidiInputDevice sender, int cable, byte[] bytes) {
            _info.processInput(bytes, 0, bytes.length);
        }

        @Override
        public void onMidiSingleByte(@NonNull MidiInputDevice sender, int cable, int byte1) {
            byte[] data = new byte[] { (byte)byte1 };
            _info.processInput(data, 0, data.length);
        }
    }

    public void onDetached(UsbDevice device, MidiInputDevice input, MidiOutputDevice output) {
        /*
        String msg = device != null ? device.toString() : "";
        String msg2 = input != null ? input.toString() : "";
        String msg3 = output != null ? output.toString() : "";
        Log.e(TAG, "onDetached device="  + msg + " input=" + msg2 + " output=" + msg3);
        */
        MidiNetDeviceInfoUSB found = null;
        UsbDevice owner = device;
        if (owner == null && input != null) {
            owner = input.getUsbDevice();
        }
        if (owner == null && output != null) {
            owner = output.getUsbDevice();
        }
        synchronized (_listDevices) {
            for (MidiNetDeviceInfo seek : _listDevices) {
                MidiNetDeviceInfoUSB seekingUsb = (MidiNetDeviceInfoUSB) seek;
                if (seekingUsb._usbDevice == owner) {
                    found = seekingUsb;
                }
            }
        }
        boolean deviceFlag = false;
        boolean inputFlag = false;
        boolean outputFlag = false;
        if (found != null) {
            if (input != null) {
                found._listInput.remove(input);
                _manager.fireReaderClosed(this, found);
            }
            if (output != null) {
                found._listOutput.remove(output);
                _manager.fireWriterClosed(this, found);
            }
            if (device != null) {
                _manager.fireDeviceLostDectected(this, found);
            }
        }
    }

    public UsbMidiDriver createUSB() {
        return new UsbMidiDriver(_context) {

            @Override
            public void onMidiInputDeviceAttached(@NonNull MidiInputDevice midiInputDevice) {
                onAttached(null, midiInputDevice, null);

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
            public void onDeviceAttached(@NonNull UsbDevice usbDevice) {
                onAttached(usbDevice, null, null);
            }
            @Override
            public void onMidiOutputDeviceAttached(@NonNull MidiOutputDevice midiOutputDevice) {
                onAttached(null, null,midiOutputDevice);
            }

            @Override
            public void onDeviceDetached(@NonNull UsbDevice usbDevice) {
                onDetached(usbDevice, null, null);
            }

            @Override
            public void onMidiInputDeviceDetached(@NonNull MidiInputDevice midiInputDevice) {
                onDetached(null, midiInputDevice, null);
            }

            @Override
            public void onMidiOutputDeviceDetached(@NonNull MidiOutputDevice midiOutputDevice) {
                onDetached(null, null, midiOutputDevice);
            }

            @Override
            public void onMidiMiscellaneousFunctionCodes(@NonNull MidiInputDevice sender, int cable, int byte1, int byte2, int byte3) {
                //Log.e(TAG, "onMidiMiscellaneousFunctionCodes " + cable +"," + byte1 +"," +byte2+ "," + byte3);
            }

            @Override
            public void onMidiCableEvents(@NonNull MidiInputDevice sender, int cable, int byte1, int byte2, int byte3) {
                //Log.e(TAG, "onMidiCableEvents " + cable +"," + byte1 +"," +byte2+ "," + byte3);
            }

            @Override
            public void onMidiSystemCommonMessage(@NonNull MidiInputDevice sender, int cable, byte[] bytes) {
                //Log.e(TAG, "onMidiSystemCommonMessage " + cable +"," + cable +"," + MXUtil.dumpHex(bytes));
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
    }

    void onReceive(MidiInputDevice device, byte[] data, int offset, int count) {
        for (MidiNetDeviceInfo seek : _listDevices) {
            MidiNetDeviceInfoUSB seekingUsb = (MidiNetDeviceInfoUSB) seek;
            for (MidiInputDevice input : seekingUsb._listInput) {
                if (input == device) {
                    //if (MXMessageBus.getMain().getPrimalIn() == seekingUsb) {
                        _manager._baseStream.receivedData(data, offset, count);
                    //}
                }
            }
        }
    }

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

    HashSet<UsbDevice> mapUsb = new HashSet();
    HashSet<MidiInputDevice> _candidateInput = new HashSet();
    HashSet<MidiOutputDevice> _candidateOutput = new HashSet();
}
