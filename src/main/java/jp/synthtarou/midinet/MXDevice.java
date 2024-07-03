package jp.synthtarou.midinet;

/**
 * 人間の目からみたデバイスの情報
 * ドライバーのしたのデバイスがぶらさがるイメージ
 * そのしたのポートはマネージしていない
 */
public interface MXDevice {
    boolean hasOutput();
    boolean hasInput();

    String getName();
    boolean prepareInput(MidiNetStream stream);
    void closeInput();

    boolean isDisconnected();
    boolean prepareOutput();
    void processDataBytes(byte[] data, int offset, int count);

    public void markBreak();
    void closeOutput();
}
