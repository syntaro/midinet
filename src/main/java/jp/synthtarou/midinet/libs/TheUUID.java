package jp.synthtarou.midinet.libs;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;

/**
 * 頻繁につかうUUIDに、BluetoothAdapterのインスタンスをグローバル変数化する
 */
public class TheUUID {
    static BluetoothAdapter _adapter;
    public static void setupAdapter(Context applicatoinContext) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            //APIレベル22以前の機種の場合の処理
            BluetoothManager bluetoothManager = applicatoinContext.getSystemService(BluetoothManager.class);
            _adapter = bluetoothManager.getAdapter();
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //APIレベル23以降の機種の場合の処理
            BluetoothManager bluetoothManager = (BluetoothManager) applicatoinContext.getSystemService(Context.BLUETOOTH_SERVICE);
            _adapter = bluetoothManager.getAdapter();
        }
    }
    public static BluetoothAdapter getAdapter() {
        return _adapter;
    }

    public static ParcelUuid MIDI_SERVICE = ParcelUuid.fromString("03B80E5A-EDE8-4B33-A751-6CE34EC4C700");

}
