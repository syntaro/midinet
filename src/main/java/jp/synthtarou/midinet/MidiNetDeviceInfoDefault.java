package jp.synthtarou.midinet;

import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.media.midi.MidiOutputPort;
import android.media.midi.MidiReceiver;

import androidx.annotation.NonNull;

import java.io.IOException;

import jp.synthtarou.midinet.task.SingleTaskFlag;
import jp.synthtarou.midinet.task.SingleTaskQueue;

public class MidiNetDeviceInfoDefault extends MidiNetDeviceInfo {
    MidiDeviceInfo _info;
    MidiDevice _connected;

    public MidiNetDeviceInfoDefault(@NonNull MidiNetServiceDefault service, @NonNull String name, @NonNull String uuid, MidiDeviceInfo info) {
        super(service, name, uuid);
        _info = info;
        _connected = null;
    }

    @Override
    public boolean prepareInput(MidiNetStream handler) {
        _onRead = handler;
        if (_connected == null) {
            devicePrepare();
            return _connected != null;
        }
        return true;
    }

    /*
    @Override
    public synchronized void safeClose() {
        MidiDevice device = _connected;

        if (device != null) {
            _connected = null;
            _lostConnection = false;
            _onRead = null;
            _onWrite = null;
            _input = null;
            _output = null;
            try {
                device.close();
            } catch (IOException ex) {

            }
            fireClosed(true);
            fireClosed(false);
        }
    }
     */

    @Override
    public boolean prepareOutput() {
        if (_connected == null) {
            devicePrepare();
            if (_connected != null) {
                return true;
            }
            return false;
        }
        return true;
    }

    long _lastPrepared = 0;

    MidiInputPort _output;
    MidiOutputPort _input;

    public MidiDevice devicePrepare() {
        MidiDeviceInfo deviceInfo = _info;
        MidiDevice device = _connected;
        if (device != null) {
            return device;
        }
        _lastPrepared = System.currentTimeMillis();

        SingleTaskFlag flag = new SingleTaskFlag();
        SingleTaskFlag f2 = SingleTaskQueue.getMainLooper().push(() -> {
            try {
                MidiManager manager = ((MidiNetServiceDefault) _service)._manager;
                manager.openDevice(deviceInfo, new MidiManager.OnDeviceOpenedListener() {
                    @Override
                    public void onDeviceOpened(MidiDevice device) {
                        try {
                            _connected = device;
                            _info = device.getInfo();
                            _input = null;
                            _output = null;
                            for (int i = 0; i < _info.getInputPortCount(); ++ i)
                            {
                                _output = device.openInputPort(i);
                                _outputOpened = true;
                                if (_output != null) {
                                    break;
                                }
                            }
                            fireOpened(true);
                            for (int i = 0; i < _info.getInputPortCount(); ++ i)
                            {
                                _input = device.openOutputPort(i);
                                if (_input != null) {
                                    _input.connect(new MidiReceiver() {
                                        @Override
                                        public void onSend(byte[] msg, int offset, int count, long timestamp) throws IOException {
                                        processInput(msg, offset, count);
                                        }
                                    });
                                    _inputOpened = true;
                                    break;
                                }
                            }
                            fireOpened(false);
                            flag.done();
                        } catch (Throwable ex) {
                            flag.done(ex);
                        }
                    }
                }, null);
            } catch (Throwable ex) {
                flag.done(ex);
            }
        });
        return _connected;
    }

    @Override
    public void processOutput(@NonNull byte[] data, int offset, int length) {
        if (length == 0 || _outputOpened == false) {
            return;
        }
        try {
            _output.send(data, offset, length);
        } catch (Throwable ex) {
            recordIOException(ex);
        }
    }

    @Override
    public  void closeDeviceConnection() {
        closeInput();
        closeOutput();
        _service._listDevices.remove(this);
    }

    public int getSortOrder() {
        return 0;
    }
}

