package jp.synthtarou.midinet.task;

import jp.synthtarou.midinet.libs.MXQueue;

/**
 * 非同期メソッドだけど、順番に処理する必要があるもの
 * （Bluetoothやデバイスなど）を、順次実行するQueue
 * AndroidでいうLooperのような
 */
public class SingleTaskQueue {
    static String TAG = "SingleTask";
    String _name;
    public SingleTaskQueue(String name) {
        _name = name;
    }

    public int getPendingCount() {
        return _queue.size();
    }

    static SingleTaskQueue _instance = new SingleTaskQueue("CallOS");
    public static SingleTaskQueue getMainLooper() {
        return _instance;
    }

    public static void remakeMainLooper() {
        _instance = new SingleTaskQueue("CallOS2");
    }

    public synchronized boolean isEmpty() {
        if (_queue.isEmpty() && _running == null) {
            return true;
        }
        return false;
    }
    Thread _thread = null;
    MXQueue<SingleTaskHandler> _queue = new MXQueue<>();

    public synchronized void push(SingleTaskHandler element) {
        if (_thread == null || _thread.isAlive() == false) {
            new Thread(() -> {
                _thread = Thread.currentThread();
                infinityLoop();
                notifyAll();
            }).start();;

            try {
                while (_thread == null) {
                    wait(10);
                }
            }catch(InterruptedException ex) {

            }
        }
        _queue.push(element);
    }

    public synchronized SingleTaskFlag push(Runnable run) {
        SingleTaskHandler handler = new SingleTaskHandler(run);
        push(handler);
        return handler._flag;
    }

    SingleTaskHandler _running = null;
    public void infinityLoop() {
        while(true) {
            SingleTaskHandler element = _queue.pop();
            if (element == null) {
                _thread = null;
                break;
            }
            _running = element;
            element.runCurrentThread();;

            _running = null;
        }
    }
}
