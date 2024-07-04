package jp.synthtarou.midinet;

import android.util.Log;

import androidx.annotation.NonNull;

/**
 * 開く前のデバイスの情報、ここから開くこともできる
 * 開いたあとのオブジェクトをわけることで、切断からの再接続も簡単になる
 */
public abstract class MidiNetDeviceInfo implements MXDevice {
    public String TAG = "MidiNetDeviceInfo";

    public MidiNetDeviceInfo(@NonNull MidiNetService service, @NonNull String name, @NonNull String uuid) {
        _service = service;
        _name = name;
        _uuidInService = uuid;
        _launching = false;
    }

    public boolean _hasInput = true;
    public boolean _hasOutput = true;
    boolean _launching;
    String _name;
    public final MidiNetService _service;
    final String _uuidInService;
    Throwable _ioException;

    @Override
    public boolean hasOutput() {
        return _hasOutput;
    }

    @Override
    public boolean hasInput() {
        return _hasInput;
    }

    public String getName() {
        return _name;
    }

    @Override
    public abstract void processOutput(@NonNull byte[] data, int offset, int count);

    @Override
    public void processInput(@NonNull byte[] data, int offset, int count){
        if (_inputOpened) {
            if (_onRead != null) {
                _onRead.receivedData(data, offset, count);
            }
        }
    }

    @Override
    public void recordIOException(Throwable ex) {
        Log.e(TAG, "recordIOException " + this, ex);
        _ioException = ex;
        closeInput();
        closeOutput();
        closeDeviceConnection();
    }

    public MidiNetStream _onRead;

    public abstract boolean prepareInput(MidiNetStream handler);
    public abstract boolean prepareOutput();

    public void fireOpened(boolean isWriter) {
        if (isWriter) {
            _service._manager.fireWriterOpened(_service, this);
        }else {
            _service._manager.fireReaderOpened(_service, this);
        }
    }

    //disconnect or lost both
    public void fireClosed(boolean isWriter) {
        if (isWriter) {
            _service._manager.fireWriterClosed(_service, this);
        }else {
            _service._manager.fireReaderClosed(_service, this);
        }
    }

    boolean _inputOpened = false;
    boolean _outputOpened = false;

    public void closeInput() {
        _inputOpened = false;
        _service._manager.fireReaderClosed(_service, this);
    }

    public void closeOutput() {
        _outputOpened = false;
        _service._manager.fireWriterClosed(_service, this);
    }

    public abstract void closeDeviceConnection();

    public String toString() {
        return _name;
    }

    public abstract int getSortOrder();
}
