package jp.synthtarou.midinet.libs;

/**
 * ロックを管理するクラス、作ったけどつかわずにすんでいる
 */
public class TheLock {
    int count;
    public void waiting(long timeout) {
        if (timeout == 0) {
            try {
                synchronized (this) {
                    while (x != 0) {
                        wait(500);
                    }
                }
            }catch(InterruptedException ex) {

            }
        }
        else {
            try {
                long started = System.currentTimeMillis();
                synchronized (this) {
                    long willend = started + timeout;
                    while(x > 0){
                        long left = willend - System.currentTimeMillis();
                        if (left >= 1) {
                            wait(left);
                        }
                        else {
                            break;
                        }
                    }
                }
            }catch(InterruptedException ex) {

            }
        }
    }

    public synchronized void increment() {
        ++ x;
        notifyAll();
    }
    public synchronized void decriment() {
        -- x;
        notifyAll();
    }
    public synchronized void zero() {
        x = 0;
        notifyAll();
    }
    int x;
}
