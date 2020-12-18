/*
 * Copyright 2020 Andrew Rice <acr31@cam.ac.uk>, Alastair Beresford <arb33@cam.ac.uk>, M. Kaufmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.cam.mk2030.fjava.tick4;

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

