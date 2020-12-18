
package uk.ac.cam.mk2030.fjava.server;

// A FIFO queue of items of type T
public interface MessageQueue<T> {

  // place msg on back of queue
  public abstract void put(T msg);
//
  // block until queue length > 0; return head of queue
  public abstract T take();
}
