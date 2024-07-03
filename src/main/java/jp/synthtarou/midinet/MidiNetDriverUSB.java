package jp.synthtarou.midinet;

import android.content.Context;

import jp.kshoji.driver.midi.device.MidiOutputDevice;

public class MidiNetDriverUSB extends MidiNetDriver {
    Context _context;
    MidiNetDriverUSBDevice _target;
    MidiNetDeviceInfo _alias;


    public MidiNetDriverUSB(MidiNetManager manager, Context context) {
        super("USB", manager);
        _context = context;
    }

    @Override
    protected void mainEnumerateDevices() {
        if (_target == null) {
            _alias = recordDeviceDetected("UM-ONE mk2 etc", "USB", null);
            _target = new MidiNetDriverUSBDevice(this, _context, _alias);
            _alias._infoObject = _target;
        }
    }

    @Override
    public void disconnectImpl(MidiNetDeviceInfo info) {
        recordDeviceLostDetected(info);
    }

    @Override
    public MidiNetStream getOrCreateReader(MidiNetDeviceInfo info, MidiNetStream handler) {
        _target.start();
        if (info._onRead != null) {
            return info._onRead;
        }

        info._onRead = handler;
        return handler;
    }

    @Override
    public MidiNetStream getOrCreateWriter(MidiNetDeviceInfo info) {
        _target.start();
        if (info._onWrite != null) {
            return info._onWrite;
        }
        info._onWrite = new MidiNetStream() {

            @Override
            public void receivedData(byte[] data, int offset, int length) {
                for (MidiOutputDevice out : _target.mapOutput) {
                    if (data.length <= 3) {
                        int status = data[0];
                        int data1 = data.length >= 2 ? data[1] : 0;
                        int data2 = data.length >= 3 ? data[2] : 0;
                        out.sendMidiMessage(0, status & 0xff, data1 & 0x7f, data2 & 0x7f);
                    }
                    else {
                        if (offset != 0 || length != data.length) {
                            byte[] copy = new byte[length];
                            for (int i = 0; i < copy.length; ++ i) {
                                copy[i] = data[i + offset];
                            }
                            data = copy;
                        }
                        out.sendMidiSystemCommonMessage(0, data);
                    }
                }
                /*
                Log.e(TAG, "devices " + _target.mapUsb);
                Log.e(TAG, "mapInput " +_target.mapInput);
                Log.e(TAG, "mapOutput " + _target.mapOutput);
                */
            }
        };
        return info._onWrite;
    }

    protected void terminate() {
        if (_target != null) {
            _target.terminate();
        }
    }

    @Override
    public void stopScan() {
    }
}
