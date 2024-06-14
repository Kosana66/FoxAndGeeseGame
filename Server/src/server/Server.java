package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Server {

    private ServerSocket socket;
    private int port;
    private ArrayList<ConnectedClient> allClients;
  
    public Server(int port){
        this.allClients = new ArrayList<>();
        try {
            this.port = port;
            this.socket = new ServerSocket(port);
        } catch (Exception ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void acceptClients() {
        Socket clientSocket = null;
        while(true) {
            try {
                clientSocket = this.socket.accept();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex); 
            }
            if(clientSocket != null) {
                ConnectedClient clnt = new ConnectedClient(clientSocket, allClients, this);
                allClients.add(clnt);
                Thread thr = new Thread(clnt);
                thr.start();
            }
            else {
                break;
            }
        }
    }
    
    public static void main(String[] args) {
        Server server = new Server(6001);
        System.out.println("The server is listening on port 6001");
        server.acceptClients();
    }
    
}
