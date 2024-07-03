package jp.synthtarou.midinet.patchlib;

import android.util.Log;

import androidx.annotation.NonNull;

import jp.kshoji.blemidi.device.MidiInputDevice;
import jp.kshoji.blemidi.listener.OnMidiInputEventListener;

/**
 * kshojiさんのメッセージディスパッチャを、Streamのような単一メソッドにするラッパ
 * すでにStreamのように処理するライブラリをもつアプリで、kshojiさんのクラスを使うために必要でした
 * バイト配列のアドレスを使いまわすので、マルチスレッドなどでは、PacketDataへコピーする運用です
 */
public abstract class OnMidiInputEventStreamForBLE implements OnMidiInputEventListener {
    static String TAG = "OnMidiInputEventListener2";
    public abstract void onReceivedData(@NonNull MidiInputDevice sender, @NonNull byte[] data, int offset, int count);

    boolean _dump = false;
    byte[] _reuse = new byte[3];

    protected final void onReceivedData(@NonNull MidiInputDevice sender, int command) {
        byte[] data = _reuse;
        data[0] = (byte)command;
        onReceivedData(sender, data, 0, 1);
    }
    protected final void onReceivedData(@NonNull MidiInputDevice sender, int command, int data1) {
        byte[] data = _reuse;
        data[0] = (byte)command;
        data[1] = (byte)data1;
        onReceivedData(sender, data, 0, 3);
    }
    protected final void onReceivedData(@NonNull MidiInputDevice sender, int command, int data1, int data2) {
        byte[] data = _reuse;
        data[0] = (byte)command;
        data[1] = (byte)data1;
        data[2] = (byte)data2;
        onReceivedData(sender, data, 0, 3);
    }
    @Override
    public void onMidiSystemExclusive(@NonNull MidiInputDevice sender, @NonNull byte[] systemExclusive) {
        onReceivedData(sender, systemExclusive, 0, systemExclusive.length);
    }
    public static final int COMMAND_CH_NOTEOFF = 0x80;
    public static final int COMMAND_CH_NOTEON = 0x90;
    public static final int COMMAND_CH_POLYPRESSURE = 0xa0;
    public static final int COMMAND_CH_CONTROLCHANGE = 0xb0;
    public static final int COMMAND_CH_PROGRAMCHANGE = 0xc0;
    public static final int COMMAND_CH_CHANNELPRESSURE = 0xd0;
    public static final int COMMAND_CH_PITCH = 0xe0;

    public static final int COMMAND_SYSEX = 0xf0;
    public static final int COMMAND_MIDITIMECODE = 0xf1;
    public static final int COMMAND_SONGPOSITION = 0xf2;
    public static final int COMMAND_SONGSELECT = 0xf3;
    public static final int COMMAND_F4 = 0xf4;
    public static final int COMMAND_F5 = 0xf5;
    public static final int COMMAND_TUNEREQUEST = 0xf6;
    public static final int COMMAND_SYSEX_END = 0xf7;
    public static final int COMMAND_TRANSPORT_MIDICLOCK = 0xf8;
    public static final int COMMAND_F9 = 0xf9;
    public static final int COMMAND_TRANSPOORT_START = 0xfa;
    public static final int COMMAND_TRANSPORT_CONTINUE = 0xfb;
    public static final int COMMAND_TRNASPORT_STOP = 0xfc;
    public static final int COMMAND_FD = 0xfd;
    public static final int COMMAND_ACTIVESENSING = 0xfe;
    public static final int COMMAND_META_OR_RESET = 0xff;

    @Override
    public void onMidiNoteOff(@NonNull MidiInputDevice sender, int channel, int note, int velocity) {
        if (_dump) Log.e(TAG, "onMidiNoteOff");
        if (channel < 0 || channel >= 16) {
            return;
        }
        if (note < 0 || note >= 128) {
            return;
        }
        if (velocity < 0 || velocity >= 128) {
            return;
        }
        onReceivedData(sender, COMMAND_CH_NOTEOFF + channel, note, velocity);
    }

    @Override
    public void onMidiNoteOn(@NonNull MidiInputDevice sender, int channel, int note, int velocity) {
        if (_dump) Log.e(TAG, "onMidiNoteOn");
        if (channel < 0 || channel >= 16) {
            return;
        }
        if (note < 0 || note >= 128) {
            return;
        }
        if (velocity < 0 || velocity >= 128) {
            return;
        }
        onReceivedData(sender, COMMAND_CH_NOTEON + channel, note, velocity);
    }

    @Override
    public void onMidiPolyphonicAftertouch(@NonNull MidiInputDevice sender, int channel, int note, int pressure) {
        if (_dump) Log.e(TAG, "onMidiPolyphonicAftertouch");
        if (channel < 0 || channel >= 16) {
            return;
        }
        if (note < 0 || note >= 128) {
            return;
        }
        if (pressure < 0 || pressure >= 128) {
            return;
        }
        onReceivedData(sender, COMMAND_CH_POLYPRESSURE + channel, note, pressure);
    }

    @Override
    public void onMidiControlChange(@NonNull MidiInputDevice sender, int channel, int function, int value) {
        if (_dump) Log.e(TAG, "onMidiControlChange");
        if (channel < 0 || channel >= 16) {
            return;
        }
        if (function < 0 || function >= 128) {
            return;
        }
        if (value < 0 || value >= 128) {
            return;
        }
        onReceivedData(sender, COMMAND_CH_CONTROLCHANGE + channel, function, value);
    }

    @Override
    public void onMidiProgramChange(@NonNull MidiInputDevice sender, int channel, int program) {
        if (_dump) Log.e(TAG, "onMidiProgramChange");
        if (channel < 0 || channel >= 16) {
            return;
        }
        if (program < 0 || program >= 128) {
            return;
        }
        onReceivedData(sender, COMMAND_CH_CONTROLCHANGE + channel, program, program);
    }

    @Override
    public void onMidiChannelAftertouch(@NonNull MidiInputDevice sender, int channel, int pressure) {
        if (_dump) Log.e(TAG, "onMidiChannelAftertouch");
        if (channel < 0 || channel >= 16) {
            return;
        }
        if (pressure < 0 || pressure >= 128) {
            return;
        }
        onReceivedData(sender, COMMAND_CH_CHANNELPRESSURE + channel, pressure);
    }

    @Override
    public void onMidiPitchWheel(@NonNull MidiInputDevice sender, int channel, int amount) {
        if (_dump) Log.e(TAG, "onMidiPitchWheel");
        if (channel < 0 || channel >= 16) {
            return;
        }
        if (amount < 0 || amount >= 161384) {
            return;
        }
        int hi = (amount >> 7) & 0x7f;
        int lo = (amount) & 0x7f;
        onReceivedData(sender, COMMAND_CH_PITCH + channel, lo, hi);
    }

    @Override
    public void onMidiTimeCodeQuarterFrame(@NonNull MidiInputDevice sender, int timing) {
        if (_dump) Log.e(TAG, "onMidiTimeCodeQuarterFrame");
        if (timing < 0 || timing >= 128) {
            return;
        }
        onReceivedData(sender, COMMAND_MIDITIMECODE,timing);
    }

    @Override
    public void onMidiSongSelect(@NonNull MidiInputDevice sender, int song) {
        if (_dump) Log.e(TAG, "onMidiSongSelect");
        if (song < 0 || song >= 128) {
            return;
        }
        onReceivedData(sender, COMMAND_SONGSELECT,song);
    }

    @Override
    public void onMidiSongPositionPointer(@NonNull MidiInputDevice sender, int position) {
        if (_dump) Log.e(TAG, "onMidiSongPositionPointer");
        if (position < 0 || position >= 16364) {
            return;
        }
        int hi = (position >> 7) & 0x7f;
        int lo = position & 0x7f;
        onReceivedData(sender, COMMAND_SONGSELECT, lo, hi);
    }

    @Override
    public void onMidiTuneRequest(@NonNull MidiInputDevice sender) {
        if (_dump) Log.e(TAG, "onMidiTuneRequest");
        onReceivedData(sender, COMMAND_TUNEREQUEST);
    }

    @Override
    public void onMidiTimingClock(@NonNull MidiInputDevice sender) {
        if (_dump) Log.e(TAG, "onMidiTimingClock");
        onReceivedData(sender, COMMAND_TRANSPORT_MIDICLOCK);
    }

    @Override
    public void onMidiStart(@NonNull MidiInputDevice sender) {
        if (_dump) Log.e(TAG, "onMidiStart");
        onReceivedData(sender, COMMAND_TRANSPOORT_START);
    }

    @Override
    public void onMidiContinue(@NonNull MidiInputDevice sender) {
        if (_dump) Log.e(TAG, "onMidiContinue");
        onReceivedData(sender, COMMAND_TRANSPORT_CONTINUE);
    }

    @Override
    public void onMidiStop(@NonNull MidiInputDevice sender) {
        if (_dump) Log.e(TAG, "onMidiStop");
        onReceivedData(sender, COMMAND_TRNASPORT_STOP);
    }

    @Override
    public void onMidiActiveSensing(@NonNull MidiInputDevice sender) {
        if (_dump) Log.e(TAG, "onMidiActiveSensing");
        onReceivedData(sender, COMMAND_ACTIVESENSING);
    }

    @Override
    public void onMidiReset(@NonNull MidiInputDevice sender) {
        if (_dump) Log.e(TAG, "onMidiReset");
        onReceivedData(sender, COMMAND_META_OR_RESET);
    }

    @Override
    public void onRPNMessage(@NonNull MidiInputDevice sender, int channel, int function, int value) {
        if (_dump) Log.e(TAG, "onRPNMessage");
        int rlsb = 100; //lo
        int rmsb = 101; //hi

        int rlsb_value = function & 0x7f;
        int rmsb_value = (function >> 7) & 0x7f;

        int msb = 6; //hi
        int lsb = 38; //lo

        int msb_value = (value >> 7) & 0x7f;
        int lsb_value = value & 0x7f;

        onReceivedData(sender, COMMAND_CH_CONTROLCHANGE+ channel, rmsb, rmsb_value);
        onReceivedData(sender, COMMAND_CH_CONTROLCHANGE+ channel, rlsb, rlsb_value);

        onReceivedData(sender, COMMAND_CH_CONTROLCHANGE + channel, msb, msb_value);
        onReceivedData(sender, COMMAND_CH_CONTROLCHANGE+ channel, lsb, lsb_value);
    }

    @Override
    public void onNRPNMessage(@NonNull MidiInputDevice sender, int channel, int function, int value) {
        if (_dump) Log.e(TAG, "onNRPNMessage");
        int nlsb = 98; //lo
        int nmsb = 99; //hi

        int rlsb_value = function & 0x7f;
        int rmsb_value = (function >> 7) & 0x7f;

        int msb = 6; //hi
        int lsb = 38; //lo

        int msb_value = (value >> 7) & 0x7f;
        int lsb_value = value & 0x7f;

        onReceivedData(sender, COMMAND_CH_CONTROLCHANGE+ channel, nmsb, rmsb_value);
        onReceivedData(sender, COMMAND_CH_CONTROLCHANGE+ channel, nlsb, rlsb_value);

        onReceivedData(sender, COMMAND_CH_CONTROLCHANGE + channel, msb, msb_value);
        onReceivedData(sender, COMMAND_CH_CONTROLCHANGE+ channel, lsb, lsb_value);
    }

    @Override
    public boolean needProcessDataEntry() {
        return false;
    }
}
