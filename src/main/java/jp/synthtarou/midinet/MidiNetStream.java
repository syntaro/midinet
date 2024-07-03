package jp.synthtarou.midinet;

import androidx.annotation.NonNull;

/**
 * パケット単位で、読み取り、書き込みをおこなう、読み書き両用
 */
public interface MidiNetStream {
    void receivedData(@NonNull byte[] data, int offset, int length);
}
