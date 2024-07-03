package jp.synthtarou.midinet.patchlib;

/**
 * MIDIやBluetoothのパケットデータを扱うクラス
 * メモリの再生成を控え、再利用することで、GC（ガーベージコレクタ）を作動しにくくする目的
 */
public class PacketData {
    public long _time;
    public byte[] _data;
    public int _offset;
    public int _count;

    public PacketData(int capacity) {
        if (capacity < 0) {
            //dont crate buffer
        } else if (capacity < 3) {
            _data = new byte[3];
        } else {
            _data = new byte[capacity];
        }
        _count = capacity;
        _offset = 0;
    }

    public void setLength(int capacity) {
        if (_data.length < capacity) {
            _data = new byte[capacity];
        }
        _count = capacity;
    }

    public void set(int x0) {
        setLength(1);
        _time = System.currentTimeMillis();
        _data[0] = (byte) x0;
    }

    public void set(int x0, int x1) {
        setLength(2);
        _time = System.currentTimeMillis();
        _data[0] = (byte) x0;
        _data[1] = (byte) x1;
    }

    public void set(int x0, int x1, int x2) {
        setLength(3);
        _time = System.currentTimeMillis();
        _data[0] = (byte) x0;
        _data[1] = (byte) x1;
        _data[2] = (byte) x2;
    }

    public void set(int x0, int x1, int x2, int x3) {
        setLength(4);
        _time = System.currentTimeMillis();
        _data[0] = (byte) x0;
        _data[1] = (byte) x1;
        _data[2] = (byte) x2;
        _data[3] = (byte) x3;
    }

    public void set(int x0, int x1, int x2, int x3, int x4) {
        setLength(5);
        _time = System.currentTimeMillis();
        _data[0] = (byte) x0;
        _data[1] = (byte) x1;
        _data[2] = (byte) x2;
        _data[3] = (byte) x3;
        _data[4] = (byte) x4;
    }

    public void set(byte[] data, int offset, int count) {
        setLength(count);
        _time = System.currentTimeMillis();
        for (int i = 0; i < count; ++i) {
            _data[i] = data[i + offset];
        }
    }

    public static PacketData createNoCopy(byte[] data, int offset, int count) {
        PacketData packet = new PacketData(-1);
        packet._data = data;
        packet._count = count;
        packet._offset = offset;
        return packet;
    }
}

