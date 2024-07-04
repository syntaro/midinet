package jp.synthtarou.midinet;

import android.util.Log;

import java.io.Serializable;

/**
 * このパッケージで用いているリスナ
 * 実験的にすべてのレイヤーを1つにまとめたら、正解だった
 */
public class MidiNetListenerForDebug implements MidiNetListener {
    boolean _debug = false;
    static String TAG = "MidiNetListenerForDebug";

    public MidiNetListenerForDebug(boolean debug) {
        _debug = debug;
        if (_debug) {
            Log.i(TAG, "MidiNetListenerForDebug");
        }
    }

    @Override
    public void onServiceActivated(MidiNetService service) {
        if (_debug) {
            Log.i(TAG, "onServiceActivated " + service);
        }
    }
    @Override
    public void onServiceDeactivated(MidiNetService service){
        if (_debug) {
            Log.i(TAG, "onServiceDeactivated " + service);
        }
    }
    @Override
    public void onDeviceDectected(MidiNetService service, MidiNetDeviceInfo info) {
        if (_debug) {
            Log.i(TAG, "onDeviceDectected " + info);
        }
    }
    @Override
    public void onDeviceLostDectected(MidiNetService service, MidiNetDeviceInfo info){
        if (_debug) {
            Log.i(TAG, "onDeviceLostDectected " + info);
        }
    }
    @Override
    public void onWriteOpened(MidiNetService service, MidiNetDeviceInfo info){
        if (_debug) {
            Log.i(TAG, "onWriteOpened " + info);
        }
    }
    @Override
    public void onWriteClosed(MidiNetService service, MidiNetDeviceInfo info){
        if (_debug) {
            Log.i(TAG, "onWriteClosed " + info);
        }
    }
    @Override
    public void onReaderOpened(MidiNetService service, MidiNetDeviceInfo info){
        if (_debug) {
            Log.i(TAG, "onReaderOpened " + info);
        }
    }
    @Override
    public void onReaderClosed(MidiNetService service, MidiNetDeviceInfo info){
        if (_debug) {
            Log.i(TAG, "onReaderClosed " + info);
        }
    }
}
