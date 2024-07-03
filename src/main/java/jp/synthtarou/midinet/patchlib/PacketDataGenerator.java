package jp.synthtarou.midinet.patchlib;

import java.util.ArrayList;

/**
 * パケットデータを管理するクラス、管理とは使いまわすことです
 * メモリの再生成を控え、再利用することで、GC（ガーベージコレクタ）を作動しにくくする目的
 * マルチスレッド用にライフサイクルで生成されるメモリにコピーする
 */
public class PacketDataGenerator {
    int _recylerRoomSize = 128;
    int _packetSize;
    ArrayList<PacketData> _listRecycler;

    public PacketDataGenerator(int packetSize) {
        _listRecycler = new ArrayList<>();
        _packetSize = packetSize;

    }

    private synchronized PacketData getCachedInternal() {
        PacketData packet = null;
        if (_listRecycler.isEmpty()) {
            packet = new PacketData(_packetSize);
        } else {
            packet = _listRecycler.remove(_listRecycler.size() - 1);
        }
        packet._count = _packetSize;
        packet._offset = 0;
        return packet;
    }

    public synchronized void markRecyled(PacketData packet) {
        if (packet._count == _packetSize && packet._offset == 0) {
            if (_listRecycler.size() < _recylerRoomSize) {
                _listRecycler.add(packet);
            } else {
                //toss to free memory
            }
        }
    }

    public PacketData fromData(byte[] from, int offset, int count) {
        if (count <= _packetSize) {
            PacketData packet = getCachedInternal();
            for (int i = 0; i < _packetSize; ++i) {
                packet._data[i] = from[i + offset];
            }
            packet._offset = 0;
            packet._count = count;
            packet._time = System.currentTimeMillis();
            return packet;
        }
        PacketData packet = new PacketData(count);
        for (int i = 0; i < count; ++i) {
            packet._data[i] = from[i + offset];
        }
        packet._time = System.currentTimeMillis();
        return packet;
    }

    public PacketData fromDataNoCopy(byte[] data, int offset, int count) {
        synchronized (this) {
            if (!_listRecycler.isEmpty()) {
                if (count <= _packetSize) {
                    return fromData(data, offset, count);
                }
            }
        }
        PacketData packet = PacketData.createNoCopy(data, offset, count);
        return packet;
    }

    public PacketData fromData(int data0) {
        if (_packetSize >= 1) {
            PacketData packet = getCachedInternal();
            packet.set(data0);
            return packet;
        } else {
            PacketData packet = new PacketData(1);
            packet.set(data0);
            return packet;
        }
    }

    public PacketData fromData(int data0, int data1) {
        if (_packetSize >= 2) {
            PacketData packet = getCachedInternal();
            packet.set(data0, data1);
            return packet;
        } else {
            PacketData packet = new PacketData(2);
            packet.set(data0, data1);
            return packet;
        }
    }

    public PacketData fromData(int data0, int data1, int data2) {
        if (_packetSize >= 3) {
            PacketData packet = getCachedInternal();
            packet.set(data0, data1, data2);
            return packet;
        } else {
            PacketData packet = new PacketData(3);
            packet.set(data0, data1, data2);
            return packet;
        }
    }

    public PacketData fromData(int data0, int data1, int data2, int data3) {
        if (_packetSize >= 4) {
            PacketData packet = getCachedInternal();
            packet.set(data0, data1, data2, data3);
            return packet;
        } else {
            PacketData packet = new PacketData(4);
            packet.set(data0, data1, data2, data3);
            return packet;
        }
    }
}
