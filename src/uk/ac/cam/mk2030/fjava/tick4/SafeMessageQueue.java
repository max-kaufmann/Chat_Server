package uk.ac.cam.mk2030.fjava.tick4;

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

