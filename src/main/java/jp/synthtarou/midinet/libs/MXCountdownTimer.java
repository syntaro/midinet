package jp.synthtarou.midinet.libs;

import android.util.Log;

import java.util.ArrayList;

/**
 * Androidではつかっていない
 * Handler / Looperがあるので不要
 * @author Syntarou YOSHIDA
 */
public class MXCountdownTimer {

    static class Item {

        long tick;
        Runnable action;
    }

    ArrayList<Item> _pending;

    private static final MXCountdownTimer _timer = new MXCountdownTimer();

    static {
        Thread t = new Thread(() -> {
            _timer.mysticLoop();
        });
        t.setDaemon(true);
        t.start();
    }

    protected MXCountdownTimer() {
        _pending = new ArrayList<Item>();
    }

    static public void letsCountdown(long time, Runnable action) {
       _timer.startCountdown(time, action);
    }
    
    private void startCountdown(long time, Runnable action) {
        Item i = new Item();
        i.tick = System.currentTimeMillis() + time;
        i.action = action;
        synchronized (this) {
            _pending.add(i);
            _timer.notifyAll();            
        };
    }

    public void mysticLoop() {
        while (true) {
            Item pop = null;
            long current = System.currentTimeMillis();
            long nextTick = 60 * 1000 + current;
            synchronized (this) {
                for (Item i : _pending) {
                    if (i.tick <= current) {
                        pop = i;
                        _pending.remove(i);
                        break;
                    }
                }
                for (Item i : _pending) {
                    if (i.tick <= nextTick) {
                        nextTick = i.tick;
                    }
                }
                if (pop == null) {
                    try {
                        wait(nextTick - current);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
            if (pop != null) {
                Runnable start = pop.action;
                //ここで別スレッド起動したほうがいいよ～
                new Thread(() -> {
                    try {
                        start.run();
                    } catch (Throwable ex) {
                        Log.w("-", ex.getMessage(), ex);
                    }
                }).start();
            }
        }
    }
}
