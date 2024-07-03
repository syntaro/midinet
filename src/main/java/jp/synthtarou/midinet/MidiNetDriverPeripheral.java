package jp.synthtarou.midinet;

import android.content.Context;

/**
 * KshojiさんのつかられているBLEペリフェラルを用いるMidiNetDriverの一種
 * Windows上でMixRecipeとの接続を確認
 */
public class MidiNetDriverPeripheral extends MidiNetDriver {
    Context _context;
    MidiNetDeviceInfo _alias;
    MidiNetDriverPeripheralDevice _target;

    public MidiNetDriverPeripheral(MidiNetManager manager, Context context) {
        super("BLE+", manager);
        _context = context;
    }

    @Override
    protected void mainEnumerateDevices() {
        if (_target == null) {
            _alias = recordDeviceDetected("Be BLE Device", "Peripherral", null);
            _alias._hasInput = true;
            _alias._hasOutput = true;
            _target = new MidiNetDriverPeripheralDevice(this, _context, _alias);
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
        //retry till read
        return info._onWrite;
    }

    protected void terminate() {
        if (_target != null) {
            _target.terminate();
            _target = null;
        }
    }

    @Override
    public void stopScan() {
    }
}
