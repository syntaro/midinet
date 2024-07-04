package jp.synthtarou.midinet;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.kshoji.driver.midi.device.MidiInputDevice;
import jp.kshoji.driver.midi.device.MidiOutputDevice;
import jp.kshoji.driver.midi.util.UsbMidiDriver;
import jp.synthtarou.midinet.patchlib.OnMidiInputEventStreamForUSB;

/**
 * KshojiさんのつかられてるUSB-MIDIを用いるMidiNetDriverの一種
 * RolandUM-ONE2で確認、公開用の名前をドライバが保持していないので、
 * でっちあげる
 */
public class MidiNetDeviceInfoUSB extends MidiNetDeviceInfo {
    static String TAG= "MidiNetDeviceInfoUSB";
    Context _context;

    public MidiNetDeviceInfoUSB(@NonNull MidiNetServiceUSB driver, Context context) {
        super(driver, "*List UP USB", "usb");
        _context = context;
    }

    MidiNetDeviceInfoUSB(@NonNull MidiNetServiceUSB driver, @NonNull Context context, @NonNull UsbDevice usb) {
        super(driver, "USB" + usb.getDeviceId() + ")" + usb.getProductName(), usb.getManufacturerName() + usb.getProductName() + usb.getDeviceId());
        _usbDevice = usb;
    }

    UsbDevice _usbDevice = null;
    List<MidiInputDevice> _listInput = Collections.synchronizedList(new ArrayList<>());
    List<MidiOutputDevice> _listOutput = Collections.synchronizedList(new ArrayList<>());

    @Override
    public boolean prepareInput(MidiNetStream handler) {
        _onRead = handler;
        if (_usbDevice == null) {
            _service.startDeepScan();
            return true;
        }
        if (_listInput.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public void processOutput(@NonNull  byte[] data, int offset, int length)
    {
        if (length == 0 || _outputOpened == false) {
            return;
        }
        try {
            synchronized (_listOutput) {
                for (int pos = 0; pos < _listOutput.size(); ++ pos) {
                    MidiOutputDevice output = _listOutput.get(pos);
                    int b1 = data[offset] & 0xff;
                    if (b1 >= 0x80 && b1 <= 0xef) {
                        if (length <= 3) {
                            int d1 = length >= 2 ? (data[offset + 1] & 0xff) : 0;
                            int d2 = length >= 3 ? (data[offset + 2] & 0xff) : 0;
                            output.sendMidiMessage(0, b1, d1, d2);
                            return;
                        }
                    }
                    if (offset > 0) {
                        byte[] data2 = new byte[length];
                        for (int i = 0; i < length; ++i) {
                            data2[i] = data[i + offset];
                        }
                        data = data2;
                    }
                    output.sendMidiSystemCommonMessage(0, data);
                }
            }
        }catch(Throwable ex) {
            recordIOException(ex);
        }
    }

    @Override
    public boolean prepareOutput() {
        if (_usbDevice == null) {
            _service.startDeepScan();
            return true;
        }
        if (_listOutput.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public void closeDeviceConnection() {
        closeInput();
        closeOutput();
        if (_usbDevice != null) {
            //how ?
        }
    }

    @Override
    public int getSortOrder() {
        if (_usbDevice == null) {
            return 10;
        }
        else {
            return 11;
        }
    }
}

