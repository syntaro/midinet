package jp.synthtarou.midinet;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * このパッケージを司るマネージャー
 */

public class MidiNetManager {
    static String TAG = "MidiNetManager";
    public Activity _context;
    MidiNetDriverDefault driver1;
    MidiNetDriverBluetooth driver2;
    MidiNetDriverUSB driver3;
    MidiNetDriverPeripheral driver4;

    public MidiNetDriverDefault getDriverAndroid() {
        return driver1;
    }

    public MidiNetDriverBluetooth getDriverBluetooth() {
        return driver2;
    }
    public MidiNetDriverUSB getDriverUSB() {
        return driver3;
    }

    public MidiNetDriverPeripheral getDriverPeripheral() { return driver4; }

    public MidiNetManager(Activity context) {
        _context = context;
        driver1 = new MidiNetDriverDefault(this, context);
        driver2 = new MidiNetDriverBluetooth(this, context);
        driver3 = new MidiNetDriverUSB(this, context);
        driver4 = new MidiNetDriverPeripheral(this, context);
        addDriver(driver1);
        addDriver(driver2);
        addDriver(driver3);
        addDriver(driver4);
    }

    public void terminateAllDrivers() {
        LinkedList<MidiNetDriver> copy;
        synchronized (this) {
            copy = new LinkedList<>(_installedDriver);
            _installedDriver.clear();
        }
        for (MidiNetDriver seek : copy) {
            try {
                seek.terminate();
            }
            catch(Throwable ex) {

            }
        }
    }

    public ArrayList<MidiNetDeviceInfo> listAllDevices() {
        ArrayList<MidiNetDeviceInfo> result = new ArrayList<>();
        for (MidiNetDriver seek : _installedDriver) {
            for (MidiNetDeviceInfo info : seek.listDevice()) {
                result.add(info);
            }
        }
        return result;
    }

    ArrayList<MidiNetDriver> _installedDriver = new ArrayList<>();
    ArrayList<MidiNetListener> _listeners = new ArrayList<>();
    public synchronized void addDriver(MidiNetDriver driver) {
        if (_installedDriver.contains(driver)) {
            return;
        }
        _installedDriver.add(driver);
        fireDriverAddred(driver);
    }

    public synchronized void removeDriver(MidiNetDriver driver) {
        if (_installedDriver.contains(driver)) {
            _installedDriver.remove(driver);

            fireDriverRemoved(driver);
        }
    }

    public synchronized void addListener(MidiNetListener listener) {
        if (_listeners.contains(listener)) {
            return;
        }
        _listeners.add(listener);
    }

    public synchronized void removeListener(MidiNetListener listener) {
        _listeners.remove(listener);
    }
    static boolean _useInfo = true;

    public void stopAllScan() {
        ArrayList<MidiNetDeviceInfo> result = new ArrayList<>();
        for (MidiNetDriver driver : _installedDriver) {
            driver.stopScan();
        }
    }

    public void fireDriverAddred(MidiNetDriver driver) {
        if (_useInfo) Log.i(TAG, "global onDriverAddred " + driver);
        for (MidiNetListener listener : _listeners) {
            listener.onDriverAdded(driver);
        }
    }

    public void fireDriverRemoved(MidiNetDriver driver) {
        if (_useInfo) Log.i(TAG, "global onDriverRemoved " + driver);
        for (MidiNetListener listener : _listeners) {
            listener.onDriverRemoved(driver);
        }
    }

    public void fireDeviceDetected(MidiNetDriver driver, MidiNetDeviceInfo info) {
        if (_useInfo) Log.i(TAG, "global fireDeviceDetected " + info._name);
        for (MidiNetListener listener : _listeners) {
            listener.onDeviceDectected(driver, info);
        }
    }

    public void fireDeviceLostDectected(MidiNetDriver driver, MidiNetDeviceInfo info) {
        if (_useInfo) Log.i(TAG, "global fireDeviceLostDectected " + info._name);
        for (MidiNetListener listener : _listeners) {
            listener.onDeviceLostDectected(driver, info);
        }
    }

    public void fireWriterOpened(MidiNetDriver driver, MidiNetDeviceInfo info) {
        if (_useInfo) Log.i(TAG, "global onWriteOpened " + info._name);
        for (MidiNetListener listener : _listeners) {
            listener.onWriteOpened(driver, info);
        }
    }

    public void fireWriterClosed(MidiNetDriver driver, MidiNetDeviceInfo info) {
        if (_useInfo) Log.i(TAG, "global onWriteClosed " + info._name);
        for (MidiNetListener listener : _listeners) {
            listener.onWriteClosed(driver, info);
        }
    }

    public void fireReaderOpened(MidiNetDriver driver, MidiNetDeviceInfo info) {
        if (_useInfo) Log.i(TAG, "global onReaderOpened " + info._name);
        for (MidiNetListener listener : _listeners) {
            listener.onReaderOpened(driver, info);
        }
    }

    public void fireReaderClosed(MidiNetDriver driver, MidiNetDeviceInfo info) {
        if (_useInfo) Log.i(TAG, "global onReaderClosed " + info._name);
        for (MidiNetListener listener : _listeners) {
            listener.onReaderClosed(driver, info);
        }
    }

    public void postEnumerateDevicesForAll() {
        for (MidiNetDriver seek : _installedDriver) {
            seek.postEnumerateDevice();;
        }
    }

    MidiNetStream _globalReader;

    public void setGlobalReader(MidiNetStream stream) {
        _globalReader = stream;
    }

    public MidiNetStream getGlobalReader() {
        return _globalReader;
    }
}
