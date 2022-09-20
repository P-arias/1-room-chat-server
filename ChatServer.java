/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

/**
 *
 * @author paria
 */
import java.net.*;
import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;



class Client{
    
    private final PrintWriter wr;
    public Client( PrintWriter wr){
     
        this.wr = wr;
    }
    public PrintWriter getPrintWriter(){return wr;}
}

public class ChatServer {

    /**
     * @param args the command line arguments
     */
    
   static BlockingQueue<Client> clients = new LinkedBlockingQueue<>();
    
   static class ClientHandler implements Runnable {
        
        Socket s;
        
        public ClientHandler(Socket socket) {
            s = socket;
        }

        public void run() {
          
            try{
                PrintWriter writer = new PrintWriter(s.getOutputStream(), true);
                Client client = new Client(writer);
                clients.add(client);
                writer.println("You have joined this room chat.");
                writer.flush();
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String userName = reader.readLine();
                System.out.println( userName + " connected.");
                broadcastMsg(userName, " connected.", writer);
                
                String line;
                while(!s.isClosed()){
                    try{
                        
                        line = reader.readLine();
                        if (line != null) 
                        broadcastMsg(userName, line, writer);
                    
                    }
                    catch(SocketException ex){
                      s.close();
                      broadcastMsg(userName, " disconnected.", writer); 
                       clients.remove(client);
                      reader.close();
                      writer.close();
                    }
                 
                } 
                
                
                
                
                
                

                } catch (IOException ex) {
                ex.printStackTrace();

            }
            
          
         
       }
        
       private void broadcastMsg(String userName, String line, PrintWriter wr) {
        
           PrintWriter writer;
           for(Client listener: clients){
             synchronized(listener){  
             writer = listener.getPrintWriter();
             if(writer != wr)
                 writer.println(userName + ": " + line);
             writer.flush();
            }
           }
            
      }
        
    }
    
    
    
    
    
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        ServerSocket ss = new ServerSocket(8080);
        while(true){
            Socket s = ss.accept();
            ClientHandler handler = new ClientHandler(s);
            Thread worker = new Thread(handler);
            worker.start();
        }
    }
    
}
