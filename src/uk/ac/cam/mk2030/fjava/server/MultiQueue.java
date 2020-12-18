
package uk.ac.cam.mk2030.fjava.server;

import java.util.HashSet;
import java.util.Set;

public class MultiQueue<T> {

  private Set<MessageQueue<T>> outputs = new HashSet<>();

  public void register(MessageQueue<T> q) {
    synchronized (this) {
      outputs.add(q);
    }
  }

  public void deregister(MessageQueue<T> q) {

    synchronized (this) {
      outputs.remove(q);
    }

  }

  public void put(T message) {

    synchronized (this) {
      for(MessageQueue<T> q : outputs){
        q.put(message);
      }
    }

  }

  }

