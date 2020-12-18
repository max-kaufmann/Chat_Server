package uk.ac.cam.mk2030.fjava.tick4;



public class SafeMessageQueue<T> implements MessageQueue<T> {
    private static class Link<L> {
        L val;
        Link<L> next;

        Link(L val) {
            this.val = val;
            this.next = null;
        }
    }

    private Link<T> first = null;
    private Link<T> last = null;

    synchronized public void put(T val) {
        if (last == null){
            last = (first = new Link(val));
        } else {
            Link<T> v= new Link<>(val);
            last.next = v;
            last = v;
        }

        this.notify();
    }

    synchronized public T take() {
        while (first == null) { // use a loop to block thread until data is available
            try {
                this.wait();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        T returnVal = first.val;

        if (last == first){
            first = last = null;
        } else {
            first = first.next;
        }
        return returnVal;
    }
}

