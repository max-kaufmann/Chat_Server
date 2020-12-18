package uk.ac.cam.mk2030.fjava.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream.*;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;


import uk.ac.cam.cl.fjava.messages.*;



public class ChatClient {

        static String formatDate(Date d ){
            SimpleDateFormat sdf = new SimpleDateFormat("HH':'mm':'ss");
            return sdf.format(d);
        }

        private static String clientMessage(String text){

            String time = formatDate(new Date());
            return time + " [Client] "  + text;

        }

        public static void main(String[] args) throws IOException,ClassNotFoundException{


            try {

                if (args.length != 2) {
                    System.err.println("This application requires two arguments: <machine> <port>");
                    return;
                }

                String server = args[0];

                int port;
                try {
                    port = Integer.parseInt(args[1]);
                } catch (NumberFormatException n) {
                    System.err.println("This application requires two arguments: <machine> <port>");
                    return;
                }

                final Socket s = new Socket(server, port);
                System.out.println(clientMessage("Connected to " + server + " on port " + Integer.toString(port) + "."));

                Thread output =
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    DynamicObjectInputStream is = new DynamicObjectInputStream(s.getInputStream());
                                    while(true){
                                        Object msg = is.readObject();
                                        if (msg instanceof RelayMessage){
                                            RelayMessage rmsg = (RelayMessage) msg;
                                            String text = rmsg.getMessage();
                                            String name = rmsg.getFrom();

                                            String time = formatDate(rmsg.getCreationTime());

                                            System.out.println(time + " [" + name + "] "  + text);

                                        } else if (msg instanceof StatusMessage) {
                                            StatusMessage smsg = (StatusMessage) msg;
                                            String text = smsg.getMessage();
                                            String time = formatDate(smsg.getCreationTime());
                                            System.out.println(time + " [Server] " + text);

                                        } else if (msg instanceof NewMessageType) {
                                            NewMessageType nmsg = (NewMessageType) msg;
                                            is.addClass(nmsg.getName(),nmsg.getClassData());
                                            System.out.println(clientMessage("New class " + nmsg.getName() + " loaded." ));
                                        } else {
                                            Class<?> nmsgClass = msg.getClass();
                                            Field[] fields = nmsgClass.getDeclaredFields();
                                            String outputText = nmsgClass.getSimpleName() + ":";
                                            if (fields.length != 0) {

                                                int i = 0;
                                                while (i < fields.length - 1) {
                                                    fields[i].setAccessible(true);
                                                    outputText = outputText + " " + fields[i].getName() + "(" + fields[i].get(msg) + "),";
                                                    i++;
                                                }


                                                fields[i].setAccessible(true);
                                                outputText = outputText + " " + fields[i].getName() + "(" + fields[i].get(msg) + ")";
                                            }


                                            System.out.println(clientMessage(outputText));

                                            Method[] ms = nmsgClass.getMethods();

                                            for (Method m: ms) {
                                                int argnum = m.getParameterCount();
                                                if (m.isAnnotationPresent(Execute.class) && argnum == 0){
                                                    m.invoke(msg);
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception io) {
                                    System.err.println("Cannot connect to " + args[0] + " on port " + args[1] + ".");
                                    throw new RuntimeException("",io);
                                }

                            }
                        };
                output.setDaemon(true);
                output.start();

                BufferedReader r = new BufferedReader(new InputStreamReader(System.in));

                ObjectOutputStream o = new ObjectOutputStream(s.getOutputStream());


                while (true) {
                    String userIn = r.readLine();

                    if (userIn.startsWith("\\")){
                        String command = userIn.split(" ")[0].substring(1);

                        switch (command){

                            case "quit":
                                System.out.println(clientMessage("Connection terminated"));
                                return;
                            case "nick":
                                System.out.println(userIn.split(" ")[1]);
                                o.writeObject(new ChangeNickMessage(userIn.substring(userIn.split(" ")[0].length() + 1)));
                                break;
                            default:
                                System.out.println(clientMessage( "Unknown command \"" + userIn.split(" ")[1].substring(1) + "\""));
                        }
                    } else {
                        o.writeObject(new ChatMessage(userIn));
                    }

                }
            } catch (Exception e) {
                System.err.println("Cannot connect to " + args[0] + " on port " + args[1]);
                throw e;
            }
        }
}

