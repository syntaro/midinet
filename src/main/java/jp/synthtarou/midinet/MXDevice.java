package jp.synthtarou.midinet;

import androidx.annotation.NonNull;

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

    boolean prepareOutput();
    void processOutput(@NonNull byte[] data, int offset, int count);
    void processInput(@NonNull  byte[] data, int offset, int count);

    void recordIOException(Throwable ex);
    void closeOutput();
}
