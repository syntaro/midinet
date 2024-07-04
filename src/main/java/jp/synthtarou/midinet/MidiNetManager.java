package jp.synthtarou.midinet;

import android.app.Activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * このパッケージを司るマネージャー
 */

public class MidiNetManager {
    static String TAG = "MidiNetManager";
    public Activity _context;
    MidiNetServiceDefault service1;
    MidiNetServiceBluetooth service2;
    MidiNetServiceUSB service3;
    MidiNetServicePeripheral service4;

    public MidiNetServiceDefault getServiceAndroid() {
        return service1;
    }
    public MidiNetServiceBluetooth getServiceBluetooth() {
        return service2;
    }
    public MidiNetServiceUSB getServiceUSB() {
        return service3;
    }

    public MidiNetServicePeripheral getServicePeripheral() { return service4; }

    public MidiNetManager(Activity context) {
        _context = context;
        //addListener(new MidiNetListenerForDebug(true));
        service1 = new MidiNetServiceDefault(this, context);
        service2 = new MidiNetServiceBluetooth(this, context);
        service3 = new MidiNetServiceUSB(this, context);
        service4 = new MidiNetServicePeripheral(this, context);
        addService(service1);
        addService(service2);
        addService(service3);
        addService(service4);
    }

    public void terminateAllServices() {
        LinkedList<MidiNetService> copy;
        synchronized (this) {
            copy = new LinkedList<>(_installedServices);
            _installedServices.clear();
        }
        for (MidiNetService seek : copy) {
            try {
                seek.terminateAllDevices();
            }
            catch(Throwable ex) {

            }
        }
    }

    public ArrayList<MidiNetDeviceInfo> listAllDevices() {
        ArrayList<MidiNetDeviceInfo> result = new ArrayList<>();

        for (MidiNetService seek : _installedServices) {
            ArrayList<MidiNetDeviceInfo> seg = new ArrayList<>();
            for (MidiNetDeviceInfo info : seek._listDevices) {
                seg.add(info);
            }
            Collections.sort(seg, new Comparator<MidiNetDeviceInfo>() {
                @Override
                public int compare(MidiNetDeviceInfo o1, MidiNetDeviceInfo o2) {
                    int a1 = o1.getSortOrder();
                    int a2 = o2.getSortOrder();
                    if (a1 == a2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                    return a1 < a2 ? -1 : 1;
                }
            });
            result.addAll(seg);
        }
        return result;
    }

    List<MidiNetService> _installedServices = Collections.synchronizedList(new ArrayList<>());
    List<MidiNetListener> _listeners = Collections.synchronizedList(new ArrayList<>());

    public void addService(MidiNetService service) {
        synchronized (_installedServices) {
            if (_installedServices.contains(service)) {
                return;
            }
            _installedServices.add(service);
        }
        fireServiceAddred(service);
    }

    public synchronized void removeService(MidiNetService service) {
        synchronized (_installedServices) {
            if (_installedServices.contains(service)) {
                _installedServices.remove(service);
            }
        }
        fireDriverRemoved(service);
    }

    public void addListener(MidiNetListener listener) {
        synchronized (_listeners) {
            if (_listeners.contains(listener)) {
                return;
            }
            _listeners.add(listener);
        }
    }

    public void removeListener(MidiNetListener listener) {
        _listeners.remove(listener);
    }
    static boolean _useInfo = true;

    public void stopAllScan() {
        ArrayList<MidiNetDeviceInfo> result = new ArrayList<>();
        synchronized (_installedServices) {
            for (MidiNetService driver : _installedServices) {
                driver.stopDeepScan();
            }
        }
    }

    public void fireServiceAddred(MidiNetService esrvice) {
        ArrayList<MidiNetListener> copy;
        synchronized (_listeners) {
            copy = new ArrayList<>(_listeners);
        }
        for (MidiNetListener listener : copy) {
            listener.onServiceActivated(esrvice);
        }
    }

    public void fireDriverRemoved(MidiNetService service) {
        ArrayList<MidiNetListener> copy;
        synchronized (_listeners) {
            copy = new ArrayList<>(_listeners);
        }
        for (MidiNetListener listener : copy) {
            listener.onServiceDeactivated(service);
        }
    }

    public void fireDeviceDetected(MidiNetService service, MidiNetDeviceInfo info) {
        ArrayList<MidiNetListener> copy;
        synchronized (_listeners) {
            copy = new ArrayList<>(_listeners);
        }
        for (MidiNetListener listener : copy) {
            listener.onDeviceDectected(service, info);
        }
    }

    public void fireDeviceLostDectected(MidiNetService service, MidiNetDeviceInfo info) {
        ArrayList<MidiNetListener> copy;
        synchronized (_listeners) {
            copy = new ArrayList<>(_listeners);
        }
        for (MidiNetListener listener : copy) {
            listener.onDeviceLostDectected(service, info);
        }
    }

    public void fireWriterOpened(MidiNetService service, MidiNetDeviceInfo info) {
        ArrayList<MidiNetListener> copy;
        synchronized (_listeners) {
            copy = new ArrayList<>(_listeners);
        }
        for (MidiNetListener listener : copy) {
            listener.onWriteOpened(service, info);
        }
    }

    public void fireWriterClosed(MidiNetService service, MidiNetDeviceInfo info) {
        ArrayList<MidiNetListener> copy;
        synchronized (_listeners) {
            copy = new ArrayList<>(_listeners);
        }
        for (MidiNetListener listener : copy) {
            listener.onWriteClosed(service, info);
        }
    }

    public void fireReaderOpened(MidiNetService service, MidiNetDeviceInfo info) {
        ArrayList<MidiNetListener> copy;
        synchronized (_listeners) {
            copy = new ArrayList<>(_listeners);
        }
        for (MidiNetListener listener : copy) {
            listener.onReaderOpened(service, info);
        }
    }

    public void fireReaderClosed(MidiNetService service, MidiNetDeviceInfo info) {
        ArrayList<MidiNetListener> copy;
        synchronized (_listeners) {
            copy = new ArrayList<>(_listeners);
        }
        for (MidiNetListener listener : copy) {
            listener.onReaderClosed(service, info);
        }
    }

    public void postEnumerateDevicesForAll() {
        ArrayList<MidiNetService> copy;
        synchronized (_installedServices) {
            copy = new ArrayList<>(_installedServices);
        }
        synchronized (copy) {
            for (MidiNetService seek : copy) {
                seek.startEnumerate();;
            }
        }
    }

    MidiNetStream _baseStream;

    public void setBaseStream(MidiNetStream stream) {
        _baseStream = stream;
    }

    /*
    MidiNetStream _globalReader;
    public void setGlobalReader(MidiNetStream stream) {
        _globalReader = stream;
    }
     */
}

