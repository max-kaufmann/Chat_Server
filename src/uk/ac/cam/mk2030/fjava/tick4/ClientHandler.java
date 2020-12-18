

package uk.ac.cam.mk2030.fjava.tick4;

import java.io.*;
import java.net.Socket;
import java.util.*;

import uk.ac.cam.cl.fjava.messages.*;

public class ClientHandler {
  private Socket socket;
  private MultiQueue<Message> multiQueue;
  private String nickname;
  private MessageQueue<Message> clientMessages;

  public ClientHandler(Socket s, MultiQueue<Message> q) {
    socket = s;
    multiQueue = q;
    clientMessages = new SafeMessageQueue<>();
    nickname = "Anonymous";

    PrimitiveIterator.OfInt randomInts = (new Random()).ints().iterator();
    for(int i = 0; i < 5;i++){
      nickname = nickname + Integer.toString(Math.abs(randomInts.next() % 10));
    }

    multiQueue.register(clientMessages);
    multiQueue.put( new StatusMessage(nickname + " connected from " + s.getInetAddress().getHostName()));

    Thread incomingHandler = new Thread() {
      @Override
      public void run() {
        try {
            ObjectInputStream in;
            in = new ObjectInputStream(s.getInputStream());

          while (true) {
              Object clientMessage;
              clientMessage = in.readObject();

            if (clientMessage instanceof ChangeNickMessage) {
              ChangeNickMessage nickMessage = (ChangeNickMessage) clientMessage;
              String newName = ((ChangeNickMessage) clientMessage).name;
              multiQueue.put(new StatusMessage(nickname + " is now known as " + newName));
              nickname = newName;
            } else if (clientMessage instanceof ChatMessage) {
              ChatMessage chatMessage = (ChatMessage) clientMessage;
              multiQueue.put(new RelayMessage(nickname,chatMessage));
            }
          }

        } catch (IOException e) {
          multiQueue.deregister(clientMessages);
          multiQueue.put(new StatusMessage(nickname + " has disconnected."));

        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          try {
            socket.close();
          }catch (Exception e){}
        }

      }

    };

    Thread outgoingHandler = new Thread(){
      @Override
      public void run() {
        try {
          ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
          while (true) {
              Message m = clientMessages.take();
              out.writeObject(m);
          }

        } catch (IOException e) {
              //empty as we handle client disconnecting in the incomingHandler
        } catch (Exception e){
            e.printStackTrace();
        }
      }
    };

    incomingHandler.start();
    outgoingHandler.start();
  }
}
