package jp.synthtarou.midinet.patchlib;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * デバッグのメッセージを表示させる、BluetoothGattCallback。その他の用途はありません。
 */

public class BluetoothGatCallbackForDebug extends BluetoothGattCallback {
    static String TAG = BluetoothGatCallbackForDebug.class.getName();

    public BluetoothGatCallbackForDebug() {
        if (_forDebug) {
            Log.i(TAG, TAG);
        }
    }

    static boolean _forDebug = false;

    @Override
    public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
        if (_forDebug) {
            Log.i(TAG, "onPhyUpdate");
        }
        super.onPhyUpdate(gatt, txPhy, rxPhy, status);
    }

    @Override
    public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
        if (_forDebug) {
            Log.i(TAG, "onPhyRead");
        }
        super.onPhyRead(gatt, txPhy, rxPhy, status);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (_forDebug) {
            Log.i(TAG, "onConnectionStateChange " + status + " -> " + newState);
        }
        super.onConnectionStateChange(gatt, status, newState);
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (_forDebug) {
            Log.i(TAG, "onServicesDiscovered");
        }
        super.onServicesDiscovered(gatt, status);
    }

    @Override
    @Deprecated
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                     int status) {
        if (_forDebug) {
            Log.i(TAG, "onCharacteristicRead " + characteristic.getStringValue(0));
        }
        super.onCharacteristicRead(gatt, characteristic, status);
    }

    @Override
    public void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull
    BluetoothGattCharacteristic characteristic, @NonNull byte[] value, int status) {
        if (_forDebug) {
            Log.i(TAG, "onCharacteristicRead");
        }
        super.onCharacteristicRead(gatt, characteristic, value, status);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic characteristic, int status) {
        if (_forDebug) {
            Log.i(TAG, "onCharacteristicWrite");
        }
        super.onCharacteristicWrite(gatt, characteristic, status);
    }

    @Override
    @Deprecated
    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic) {
        if (_forDebug) {
            Log.i(TAG, "onCharacteristicChanged");
        }
        super.onCharacteristicChanged(gatt, characteristic);
    }

    @Override
    public void onCharacteristicChanged(@NonNull BluetoothGatt gatt,
                                        @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value) {
        if (_forDebug) {
            Log.i(TAG, "onCharacteristicChanged");
        }
        super.onCharacteristicChanged(gatt, characteristic, value);
    }

    @Override
    @Deprecated
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                 int status) {
        if (_forDebug) {
            Log.i(TAG, "onDescriptorRead");
        }
        super.onDescriptorRead(gatt, descriptor, status);
    }

    @Override
    public void onDescriptorRead(@NonNull BluetoothGatt gatt,
                                 @NonNull BluetoothGattDescriptor descriptor, int status, @NonNull byte[] value) {
        if (_forDebug) {
            Log.i(TAG, "onDescriptorRead");
        }
        super.onDescriptorRead(gatt, descriptor, status, value);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                  int status) {
        if (_forDebug) {
            Log.i(TAG, "onDescriptorWrite");
        }
        super.onDescriptorWrite(gatt, descriptor, status);
    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        if (_forDebug) {
            Log.i(TAG, "onReliableWriteCompleted");
        }
        super.onReliableWriteCompleted(gatt, status);
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        if (_forDebug) {
            Log.i(TAG, "onReadRemoteRssi");
        }
        super.onReadRemoteRssi(gatt, rssi, status);
    }

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        if (_forDebug) {
            Log.i(TAG, "onMtuChanged");
        }
        super.onMtuChanged(gatt, mtu, status);
    }


    @Override
    public void onServiceChanged(@NonNull BluetoothGatt gatt) {
        if (_forDebug) {
            Log.i(TAG, "onServiceChanged");
        }
        super.onServiceChanged(gatt);
    }

}
