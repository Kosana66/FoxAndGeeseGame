package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectedClient implements Runnable {
    
    private Socket socket;
    private PrintWriter pw;
    private BufferedReader br;
    private ArrayList<ConnectedClient> allClients;
    private String username;
    
    private boolean available;
    private boolean iAmFox; 
    private boolean iAmGeese;
    private boolean myTurn;
    
    private HashMap<String, Integer> matrix;
    
    private String foxPlayer;
    private String geesePlayer;
    
    private int currentGeeseRow;
    private int currentGeeseCol;
    private int currentFoxRow;
    private int currentFoxCol;
    
    private boolean playAgain;
    
    public ConnectedClient(Socket clientSocket, ArrayList<ConnectedClient> allClients, Server parent) {
        try {  
            this.socket = clientSocket;
            this.allClients = allClients;
            this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream(),"UTF-8"));
            this.pw = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()), true);
            this.username = "";
            this.available = true;
            this.iAmFox = false;
            this.iAmGeese = false;
            this.myTurn = false;
            this.matrix = new HashMap<String, Integer>();
            this.foxPlayer = "";
            this.geesePlayer = "";
            this.playAgain = false;
        } catch (IOException ex) {
            Logger.getLogger(ConnectedClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public void printMatrix()
    {
        for (int i = 1; i <= 8; i++)
        {
            for (int j = 1; j <= 8; j++)
            {
                System.out.print(matrix.get(i + "," + j) + " ");
            }
            System.out.println();
        }
    }
    
    public boolean areGeeseWin()
    {
        if (currentFoxRow == 1) {
            return true;
        }

        int[][] neighborhood = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        boolean isTrapped = true;
        for (int[] neighbor : neighborhood) {
            int newRow = currentFoxRow + neighbor[0];
            int newCol = currentFoxCol + neighbor[1];
            if (newRow > 0 && newRow <= 8 && newCol > 0 && newCol <= 8) {
                if (matrix.get(newRow + "," + newCol) != 1 || matrix.get(newRow + "," + newCol) != -1) {
                    isTrapped = false;
                    break;
                }
            }
        }
        if (isTrapped) 
            return true;
        else    
            return false;
    }
    
    private void updateStatus() {
        String connectedUsers = "Players:";
        for (ConnectedClient c : this.allClients) 
            connectedUsers += " " + c.getUsername();
        System.out.println(connectedUsers);
        for (ConnectedClient allUpdateCB : this.allClients) 
            allUpdateCB.pw.println(connectedUsers); 
    }
    
    
    
    @Override
    public void run() {
        while (true) {
            try {
                if (this.username.equals("")) {
                    this.username = this.br.readLine();
                    if (this.username != null) {
                        System.out.println("Client " + this.username + " is connected");
                        updateStatus();
                    } else {
                        System.out.println("Client " + this.username + " is disconnected");
                        for (ConnectedClient cl : this.allClients) {
                            if (cl.getUsername().equals(this.username)) {
                                this.allClients.remove(cl);
                                break;
                            }
                        }
                        updateStatus();
                        break;
                    }
                }
                else {
                    String line = this.br.readLine();
                    System.out.println(line);
                    if (line != null) {
                        //Challenge:challenger:userToChallenge
                        if(line.startsWith("Challenge:")) {
                            
                            String challenger = line.split(":")[1];
                            String userToChallenge = line.split(":")[2];
                            
                            for (ConnectedClient clnt : this.allClients) {
                                if (clnt.getUsername().equals(userToChallenge)) {
                                    if(clnt.available) 
                                        //send to userToChallenge
                                        clnt.pw.println("Received challenge:" + challenger + ":" + userToChallenge); 
                                    else
                                        //send to challenger
                                        this.pw.println("Unavailable user:" + userToChallenge); 
                                    break;
                                }
                            }
                            
                        }
                        //Yes:challenger:userToChallenge
                        if(line.startsWith("Yes:")) {
                            
                            String challenger = line.split(":")[1];
                            String userToChallenge = line.split(":")[2];
                            this.foxPlayer = challenger;
                            this.geesePlayer = userToChallenge;
                            
                            this.iAmGeese = true;
                            this.iAmFox = false;
                            this.myTurn = false;
                            this.available = false;
                            
                            this.pw.println("Start match");
                            
                            for (ConnectedClient clnt : this.allClients) {
                                if (clnt.getUsername().equals(challenger)) {
                                    clnt.foxPlayer = challenger;
                                    clnt.geesePlayer = userToChallenge;
                                    
                                    clnt.iAmFox = true;
                                    clnt.iAmGeese = false;
                                    clnt.myTurn = true;
                                    clnt.available = false;
                                    clnt.pw.println("Start match");
                                    
                                    break;
                                }
                            }
                            
                        }
                        //No:challenger
                        if(line.startsWith("No:")) {
                            for (ConnectedClient clnt : this.allClients) {
                                if (clnt.getUsername().equals(line.split(":")[1])) {
                                    clnt.pw.println("Declined challenge");
                                    break;
                                } 
                            }
                        }
                        //Initial fox column:column
                        if(line.startsWith("Initial fox column:"))
                        {
                            int initialFoxCol = Integer.parseInt(line.split(":")[1]);
                            for(int row = 1 ; row <= 8 ; row++) {
                                for(int col = 1; col <= 8 ; col++) {
                                    //// light background - 4 ; dark background - 3 ; geese - 2 ; fox - 1 //
                                    if( (row + col) % 2 == 0)
                                        this.matrix.put(row + "," + col, 3);
                                    else
                                        this.matrix.put(row + "," + col, 4);
                                    
                                    if(row == 1 && col % 2 == 0) 
                                        this.matrix.put(row + "," + col, 1);
                                    else if(row == 8 && col == initialFoxCol) {
                                        this.matrix.put(row + "," + col, 2);
                                        for(ConnectedClient clnt : this.allClients) {
                                            if(clnt.username.equals(this.foxPlayer) || clnt.username.equals(this.geesePlayer)) {
                                                clnt.currentFoxRow = row;
                                                clnt.currentFoxCol = col;
                                            }
                                        }
                                    }
                                }
                            }
                            this.printMatrix();
                        }
                    } else {
                        System.out.println("Client " + this.username + " is disconnected");
                        for (ConnectedClient cl : this.allClients) {
                            if (cl.getUsername().equals(this.username)) {
                                this.allClients.remove(cl);
                                updateStatus();
                                return;
                            }
                        }
                    }
                } 
            } catch (IOException ex) {
                System.out.println("Client " + this.username + " is disconnected");
                for (ConnectedClient cl : this.allClients) {
                    if (cl.getUsername().equals(this.username)) {
                        this.allClients.remove(cl);
                        updateStatus();
                        return;
                    }
                }
            }
        }
    }   
}
