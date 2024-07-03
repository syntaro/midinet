package jp.synthtarou.midinet;

/**
 * このパッケージで用いているリスナ
 * 実験的にすべてのレイヤーを1つにまとめたら、正解だった
 */
public interface MidiNetListener {
    void onDriverAdded(MidiNetDriver driver);
    void onDriverRemoved(MidiNetDriver driver);
    void onDeviceDectected(MidiNetDriver driver, MidiNetDeviceInfo info);
    void onDeviceLostDectected(MidiNetDriver driver, MidiNetDeviceInfo info);

    void onWriteOpened(MidiNetDriver driver, MidiNetDeviceInfo info);
    void onWriteClosed(MidiNetDriver driver, MidiNetDeviceInfo info);
    void onReaderOpened(MidiNetDriver driver, MidiNetDeviceInfo info);
    void onReaderClosed(MidiNetDriver driver, MidiNetDeviceInfo info);
}
