package jp.synthtarou.midinet;

/**
 * このパッケージで用いているリスナ
 * 実験的にすべてのレイヤーを1つにまとめたら、正解だった
 */
public interface MidiNetListener {
    void onServiceActivated(MidiNetService service);
    void onServiceDeactivated(MidiNetService service);
    void onDeviceDectected(MidiNetService service, MidiNetDeviceInfo info);
    void onDeviceLostDectected(MidiNetService service, MidiNetDeviceInfo info);

    void onWriteOpened(MidiNetService service, MidiNetDeviceInfo info);
    void onWriteClosed(MidiNetService service, MidiNetDeviceInfo info);
    void onReaderOpened(MidiNetService service, MidiNetDeviceInfo info);
    void onReaderClosed(MidiNetService service, MidiNetDeviceInfo info);
}
