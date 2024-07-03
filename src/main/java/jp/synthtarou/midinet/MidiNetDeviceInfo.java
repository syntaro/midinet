package jp.synthtarou.midinet;

import androidx.annotation.NonNull;

/**
 * 開く前のデバイスの情報、ここから開くこともできる
 * 開いたあとのオブジェクトをわけることで、切断からの再接続も簡単になる
 */
public class MidiNetDeviceInfo implements MXDevice {
    public MidiNetDeviceInfo(@NonNull MidiNetDriver driver, @NonNull String name, @NonNull String uuid, Object info) {
        _driver = driver;
        _name = name;
        _uuidInDriver = uuid;
        _infoObject = info;
    }

    public boolean _hasInput = true;
    public boolean _hasOutput = true;
    boolean _disconnected;
    String _name;
    public final MidiNetDriver _driver;
    final String _uuidInDriver;

    int _portCountWriter;
    int _portCountReder;

    public void terminate() {
    }

    public Object _infoObject;
    public Object _connectedObject;

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
    public boolean prepareInput(MidiNetStream stream) {
        if (_onRead == null) {
            _onRead = _driver.getOrCreateReader(this, (data, offset, count) -> {
                stream.receivedData(data, offset, count);
            });
        }
        return _onRead != null;
    }

    @Override
    public void closeInput() {
        //_reader.terminate();;
    }

    @Override
    public boolean isDisconnected() {
        return _disconnected;
    }

    @Override
    public boolean prepareOutput() {
        if (_onWrite == null) {
            _onWrite = _driver.getOrCreateWriter(this);
        }
        return _onWrite != null;
    }

    @Override
    public void processDataBytes(byte[] data, int offset, int count) {
        try {
            _onWrite.receivedData(data, offset, count);
        }catch(Throwable ex) {
            markBreak();
        }
    }

    @Override
    public void markBreak() {
        _disconnected = true;
    }

    @Override
    public void closeOutput() {
    //
    }

    public Object _connectedPort;

    public MidiNetStream _onRead;
    public MidiNetStream _onWrite;

}
