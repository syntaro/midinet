package jp.synthtarou.midinet.libs;

import java.util.LinkedList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXQueue<T> {

    LinkedList<T> _queue;
    boolean _quit;

    public MXQueue() {
        _queue = new LinkedList<T>();
        _quit = false;
    }

    public synchronized void push(T obj) {
        _queue.add(obj);
        notifyAll();
    }

    public synchronized boolean isEmpty() {
        return _queue.isEmpty();
    }
    
    public synchronized  int size() {
        return _queue.size();
    }

    public synchronized T popAndNoRemove() {
        while (true) {
            while (_queue.isEmpty() && !_quit) {
                try {
                    wait(100);
                } catch (InterruptedException ex) {
                    return null;
                }
            }
            if (!_queue.isEmpty()) {
                return _queue.peekFirst();
            }
            notifyAll();
            if (_quit) {
                return null;
            }
        }
    }

    public synchronized T pop() {
        while (true) {
            while (_queue.isEmpty() && !_quit) {
                try {
                    wait(100);
                } catch (InterruptedException ex) {
                    return null;
                }
            }
            if (!_queue.isEmpty()) {
                return _queue.removeFirst();
            }
            notifyAll();
            if (_quit) {
                return null;
            }
        }
    }

    public synchronized void quit() {
        _quit = true;
        notifyAll();
    }
}
