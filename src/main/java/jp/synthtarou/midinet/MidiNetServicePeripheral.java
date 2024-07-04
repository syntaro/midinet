package jp.synthtarou.midinet;

import android.content.Context;

/**
 * KshojiさんのつかられているBLEペリフェラルを用いるMidiNetServiceの一種
 * Windows上でMixRecipeとの接続を確認
 */
public class MidiNetServicePeripheral extends MidiNetService {
    static String TAG = "MidiNetServicePeripheral";

    Context _context;

    public MidiNetServicePeripheral(MidiNetManager manager, Context context) {
        super("BLE+", manager);
        MidiNetDeviceInfoPeripheral info = new MidiNetDeviceInfoPeripheral(this, context);
        recordDeviceDetected(info);
        _context = context;
    }

    @Override
    public void startEnumerate() {
    }

    @Override
    public void startDeepScan() {
    }

    protected void terminateAllDevices() {
    }

    @Override
    public void stopDeepScan() {
    }
}
