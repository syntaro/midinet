package jp.synthtarou.midinet.patchlib;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

/**
 * デバッグのメッセージを表示させる、BluetoothGattServerCallback。その他の用途はありません。
 */

public class BluetoothGattServerCallbackForDebug extends BluetoothGattServerCallback {
    static String TAG = BluetoothGattServerCallbackForDebug.class.getName();

    static boolean _forDebug = false;
    public BluetoothGattServerCallbackForDebug() {
        if (_forDebug) {
            Log.i(TAG, TAG);
        }
    }

    @Override
    public void onConnectionStateChange(BluetoothDevice device, int status,
                                        int newState) {
        if (_forDebug) {
            Log.i(TAG, "onConnectionStateChange");
        }
        super.onConnectionStateChange(device, status, newState);
    }

    @Override
    public void onServiceAdded(int status, BluetoothGattService service) {
        if (_forDebug) {
            Log.i(TAG, "onServiceAdded");
        }
        super.onServiceAdded(status, service);
    }

    @Override
    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId,
                                            int offset, BluetoothGattCharacteristic characteristic) {
        if (_forDebug) {
            Log.i(TAG, "onCharacteristicReadRequest");
        }
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
    }

    @Override
    public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattCharacteristic characteristic,
                                             boolean preparedWrite, boolean responseNeeded,
                                             int offset, byte[] value) {
        if (_forDebug) {
            Log.i(TAG, "onCharacteristicWriteRequest");
        }
        super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
    }

    @Override
    public void onDescriptorReadRequest(BluetoothDevice device, int requestId,
                                        int offset, BluetoothGattDescriptor descriptor) {
        if (_forDebug) {
            Log.i(TAG, "onDescriptorReadRequest");
        }
        super.onDescriptorReadRequest(device, requestId, offset, descriptor);

    }

    @Override
    public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
                                         BluetoothGattDescriptor descriptor,
                                         boolean preparedWrite, boolean responseNeeded,
                                         int offset, byte[] value) {
        if (_forDebug) {
            Log.i(TAG, "onDescriptorWriteRequest");
        }
        super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
    }

    @Override
    public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
        if (_forDebug) {
            Log.i(TAG, "onExecuteWrite");
        }
        super.onExecuteWrite(device, requestId, execute);
    }

    @Override
    public void onNotificationSent(BluetoothDevice device, int status) {
        if (_forDebug) {
            Log.i(TAG, "onNotificationSent");
        }
        super.onNotificationSent(device, status);
    }

    @Override
    public void onMtuChanged(BluetoothDevice device, int mtu) {
        if (_forDebug) {
            Log.i(TAG, "onMtuChanged");
        }
        super.onMtuChanged(device, mtu);
    }

    @Override
    public void onPhyUpdate(BluetoothDevice device, int txPhy, int rxPhy, int status) {
        if (_forDebug) {
            Log.i(TAG, "onPhyUpdate");
        }
        super.onPhyUpdate(device, txPhy, rxPhy, status);
    }

    @Override
    public void onPhyRead(BluetoothDevice device, int txPhy, int rxPhy, int status) {
        if (_forDebug) {
            Log.i(TAG, "onPhyRead");
        }
        super.onPhyUpdate(device, txPhy, rxPhy, status);
    }
}
