
package uk.ac.cam.mk2030.fjava.server;

import uk.ac.cam.cl.fjava.messages.Message;
import uk.ac.cam.cl.fjava.messages.StatusMessage;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
  public static void main(String args[]) {
    try {
      if (args.length != 1) {
        System.err.println("Usage: java ChatServer <port>");
        return;
      }

      int port;
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException n) {
        System.err.println("Usage: java ChatServer <port>");
        return;
      }
      ServerSocket ss;
      try {
        ss = new ServerSocket(port);
      } catch (Exception e) {
        System.out.println("Cannot use port number " + args[0]);
        return;
      }

      MultiQueue<Message> multiQueue = new MultiQueue<>();

      while (true) {
          Socket client = ss.accept();
          ClientHandler ch = new ClientHandler(client,multiQueue);
      }

    } catch (Exception e) {
        throw new RuntimeException(e);
    }
  }
}
