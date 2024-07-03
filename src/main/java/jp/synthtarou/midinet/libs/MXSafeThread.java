package jp.synthtarou.midinet.libs;

import android.util.Log;

import androidx.annotation.NonNull;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXSafeThread extends Thread {
    static final String TAG = "MXSafeThread";

    static ThreadGroup _group = new ThreadGroup("SynthTAROU") {
        @Override
        public void uncaughtException(Thread t, @NonNull Throwable e) {
            Log.e(TAG, e.toString(), e);
        }        
    };
    Runnable _run;
    
    public MXSafeThread(String name, Runnable run) {
        super(_group, run, name);
    }

    public static void listThread() {
        _group.list();
    }

    public static void exitAll() {
        int count = _group.activeCount();
        Thread[] list = new Thread[count + 5];
        _group.enumerate(list);
        for (int i = 0; i < list.length; ++ i) {
            Thread t = list[i];
            if (t == null) {
                continue;
            }
            if (t.isAlive()) {
                Log.i(TAG, "Checked as Alive [" + i + "] " + t.getName());
            }
        }
        for (int i = 0; i < list.length; ++ i) {
            Thread t = list[i];
            if (t == null) {
                continue;
            }
            if(t.isAlive()) {
                if (t.getName().startsWith("*")) {
                    continue;
                }
                Log.i(TAG, "Exit thread [" + i + "] " + t.getName());
                t.interrupt();
                try {
                    t.join();
                } catch (InterruptedException ex) {
                    Log.e(TAG, ex.getMessage(), ex);
                    break;
                }
            }
        }
        Log.i(TAG, "Finally ... ");
    }
}
