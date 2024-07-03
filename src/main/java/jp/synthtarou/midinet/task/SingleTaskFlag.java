package jp.synthtarou.midinet.task;

/**
 * 非同期メソッドの結果を管理する
 * 結果がくる前であれば、その状態を取得したり、それまで待機したり
 */
public class SingleTaskFlag {
    public void done() {
        done(true, null);
    }

    public void done(boolean success) {
        done(success, null);
    }
    public void done(Throwable caught) {
        done(false, caught);
    }

    public void started() {
        _started = true;
    }
    boolean _started;
    boolean _finished;
    boolean _success;
    Throwable _ex;
    public synchronized void done(boolean success, Throwable ex) {
        _finished  = true;
        _success = success;
        _ex = ex;
        notifyAll();
    }

    public Throwable awaitThrowable(long timeout) {
        awaitResult(timeout, false);
        if (_finished == false) {
            return new IllegalStateException("Timeout");
        }
        return _ex;
    }
    public boolean awaitResult(long timeout) {
        return awaitResult(timeout, false);
    }
    public boolean awaitResult(long timeout, boolean throwTimeout) {
        if (timeout == 0) {
            timeout = 600000;
        }

        long start = System.currentTimeMillis();
        long limit = start + timeout;

        while (System.currentTimeMillis() < limit) {
            synchronized (this) {
                if (_finished) {
                    return _success;
                }
                else {
                    try {
                        wait(limit - System.currentTimeMillis());
                    }catch(InterruptedException ex) {
                        return false;
                    }
                }
            }
        }
        _ex = new IllegalStateException("Timeout");
        if (throwTimeout) {
            throw (IllegalStateException)_ex;
        }
        return false;
    }
}
