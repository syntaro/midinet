package jp.synthtarou.midinet.task;

/**
 * このパッケージのインスタンスを制御する
 * （まるでAndroidのHandlerのように）
 */
public class SingleTaskHandler {
    SingleTaskHandler(SingleTaskFlag flag, Runnable target) {
        _flag = flag;
        _target = target;
    }
    SingleTaskHandler(Runnable target) {

        _flag = new SingleTaskFlag();
        _target = target;
    }

    final SingleTaskFlag _flag;
    final Runnable _target;

    void runCurrentThread() {
        try {
            _flag._started = true;
            synchronized (this) {
                notifyAll();
            }
            _target.run();
            _flag.done(true);
        }catch(Throwable ex) {
            _flag.done(ex);
        }
    }
}
