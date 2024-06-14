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
    
    private int currentFoxRow;
    private int currentFoxCol;
    private int selItem;
    private int selRow = 0;
    private int selCol = 0;
    private int numOfAgainGame;
    
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
            this.selItem = 0;
            this.numOfAgainGame = 0;
        } catch (IOException ex) {
            Logger.getLogger(ConnectedClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public void printMatrix()
    {
        System.out.println("***************************************");
        for (int i = 1; i <= 8; i++)
        {
            for (int j = 1; j <= 8; j++)
            {
                System.out.print(matrix.get(i + "," + j) + " ");
            }
            System.out.println();
        }
        System.out.println("***************************************");
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
                                        this.matrix.put(row + "," + col, 2);
                                    else if(row == 8 && col == initialFoxCol) {
                                        this.matrix.put(row + "," + col, 1);
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
                        //Clicked:row,col
                        if(line.startsWith("Clicked:")) {
                            int row = Integer.parseInt(line.split(":")[1].split(",")[0]);
                            int col = Integer.parseInt(line.split(":")[1].split(",")[1]);
                            
                            if(this.myTurn) {
                                switch(selItem) {
                                    case 0:
                                        if(this.iAmFox && row == this.currentFoxRow && col == this.currentFoxCol) {
                                            this.selRow = row;
                                            this.selCol = col;
                                            this.selItem = 1;
                                            this.pw.println("Selected:fox:" + row + "," + col);
                                        } 
                                        else if(this.iAmGeese && matrix.get(row + "," + col) != null && matrix.get(row + "," + col) == 2) {
                                            if (canGooseMove(row, col)) {
                                                selRow = row;
                                                selCol = col;
                                                selItem = 2;
                                                this.pw.println("Selected:geese:" + row + "," + col);
                                            } 
                                            else {
                                                this.pw.println("Select other goose");
                                            }
                                        }
                                        break;
                                    case 1:
                                        if(AllowedFieldFox(row, col, selRow, selCol)) {
                                            for(ConnectedClient clnt : this.allClients) {
                                                if(clnt.username.equals(this.foxPlayer) || clnt.username.equals(this.geesePlayer)) {
                                                    clnt.currentFoxRow = row;
                                                    clnt.currentFoxCol = col;
                                                    clnt.matrix.put(row + "," + col, 1);
                                                    clnt.pw.println("Clicked:fox:" + selRow + "," + selCol + "," + row + "," + col);
                                                    if( (selRow + selCol) % 2 == 0)
                                                        clnt.matrix.put(selRow + "," + selCol, 3);
                                                    else
                                                        clnt.matrix.put(selRow + "," + selCol, 4);
                                                }
                                                if(clnt.username.equals(this.geesePlayer)) {
                                                    clnt.myTurn = true;
                                                    clnt.pw.println("geese");
                                                }
                                            }
                                            this.myTurn = false;
                                            this.selRow = 0;
                                            this.selCol = 0;
                                            selItem = 0;
                                            if(isFoxWinner()) {
                                                for(ConnectedClient clnt : this.allClients) {
                                                    if(clnt.username.equals(this.foxPlayer) || clnt.username.equals(this.geesePlayer))
                                                        clnt.pw.println("Fox is winner");
                                                }
                                            }
                                        }
                                        break;
                                    case 2:
                                        if(AllowedFieldGoose(row, col, this.selRow, selCol)) {
                                            for(ConnectedClient clnt : this.allClients) {
                                                if(clnt.username.equals(this.foxPlayer) || clnt.username.equals(this.geesePlayer)) {
                                                    clnt.matrix.put(row + "," + col, 2);
                                                    clnt.pw.println("Clicked:geese:" + selRow + "," + selCol + "," + row + "," + col);
                                                    if( (selRow + selCol) % 2 == 0)
                                                        clnt.matrix.put(selRow + "," + selCol, 3);
                                                    else
                                                        clnt.matrix.put(selRow + "," + selCol, 4);
                                                }
                                                if(clnt.username.equals(this.foxPlayer)) {
                                                    clnt.myTurn = true;
                                                    clnt.pw.println("fox");
                                                }
                                            }
                                            this.myTurn = false;
                                            this.selRow = 0;
                                            this.selCol = 0;
                                            selItem = 0;
                                            if(areGeeseWinner()) {
                                                for(ConnectedClient clnt : this.allClients) {
                                                if(clnt.username.equals(this.foxPlayer) || clnt.username.equals(this.geesePlayer))
                                                    clnt.pw.println("Geese are winners");
                                                }
                                            }
                                        }
                                        break;
                                }
                            } 
                            else {
                                this.pw.println("Not your turn");
                            }
                        }
                        if(line.startsWith("Declined restart")) {
                            for(ConnectedClient clnt : this.allClients) {
                                if(clnt.username.equals(this.foxPlayer) || clnt.username.equals(this.geesePlayer)) {
                                    clnt.available = true;
                                    clnt.numOfAgainGame = 0;
                                    clnt.pw.println("Terminate");
                                }
                            }
                        }
                        if(line.startsWith("Accepted restart")) {
                            int i = 0;
                            this.playAgain = true;
                            for(ConnectedClient clnt : this.allClients) {
                                if((clnt.username.equals(this.foxPlayer) || clnt.username.equals(this.geesePlayer)) && clnt.playAgain)
                                        i++; 
                            }

                            if(i == 2) {
                                for(ConnectedClient clnt : this.allClients) {
                                    if(clnt.username.equals(this.foxPlayer) && clnt.numOfAgainGame % 2 == 0) {
                                        clnt.selItem = 0;
                                        clnt.selRow = 0;
                                        clnt.selCol = 0;
                                        clnt.available = false;
                                        clnt.iAmFox = false;
                                        clnt.iAmGeese = true;
                                        clnt.myTurn = false;
                                        clnt.playAgain = false;
                                        clnt.numOfAgainGame++;
                                        clnt.pw.println("Restart");
                                    }
                                    else if(clnt.username.equals(this.geesePlayer) && clnt.numOfAgainGame % 2 == 1) {
                                        clnt.selItem = 0;
                                        clnt.selRow = 0;
                                        clnt.selCol = 0;
                                        clnt.available = false;
                                        clnt.iAmFox = true;
                                        clnt.iAmGeese = false;
                                        clnt.myTurn = true;
                                        clnt.playAgain = false;
                                        clnt.numOfAgainGame++;
                                        clnt.pw.println("Restart");
                                    }
                                    if(clnt.username.equals(this.geesePlayer) && clnt.numOfAgainGame % 2 == 0) {
                                        clnt.selItem = 0;
                                        clnt.selRow = 0;
                                        clnt.selCol = 0;
                                        clnt.available = false;
                                        clnt.iAmFox = false;
                                        clnt.iAmGeese = true;
                                        clnt.myTurn = false;
                                        clnt.playAgain = false;
                                        clnt.numOfAgainGame++;
                                        clnt.pw.println("Restart");
                                    }
                                    else if(clnt.username.equals(this.foxPlayer) && clnt.numOfAgainGame % 2 == 1) {
                                        clnt.selItem = 0;
                                        clnt.selRow = 0;
                                        clnt.selCol = 0;
                                        clnt.available = false;
                                        clnt.iAmFox = true;
                                        clnt.iAmGeese = false;
                                        clnt.myTurn = true;
                                        clnt.playAgain = false;
                                        clnt.numOfAgainGame++;
                                        clnt.pw.println("Restart");
                                    }
                                }
                            }
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
    
    private boolean canGooseMove (int row, int col) {
        int newRow = row + 1;       // guska moze ici samo na dole
        int newCol = 0;
        int[] lr = {-1, 1};
        for(int i : lr) {
            newCol = col + i;
            if (newRow > 0 && newRow <= 8 && newCol > 0 && newCol <= 8) {
                if (matrix.get(newRow + "," + newCol) != 1 && matrix.get(newRow + "," + newCol) != 2) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean AllowedFieldFox (int cRow, int cCol, int sRow, int sCol) {
        if(cRow == sRow && cCol == sCol) {
            this.pw.println("Fox is selected");
            return false;
        }
        if(matrix.get(cRow + "," + cCol) == -1) {
            this.pw.println("Forbidden field because a goose exists");
            return false;
        }
        else if(matrix.get(cRow + "," + cCol) == 1) {
            this.pw.println("Forbidden field because a goose fox"); 
            return false;
        }
        if((cRow + cCol) % 2 == 0)  {
            this.pw.println("Movement on dark fields is forbidden");
            return false;
        }
        return (cRow + cCol) % 2 != 0 &&  // da li je polje adekvatne boje
                Math.abs(cRow - sRow) == 1 &&    
                Math.abs(cCol - sCol) == 1;      // da li je susedno polje po koloni
    }
    
    private boolean AllowedFieldGoose (int cRow, int cCol, int sRow, int sCol) {
        if(cRow == sRow && cCol == sCol) {
            this.pw.println("Goose is selected");
            return false;
        }                                                                                                               
        if(matrix.get(cRow + "," + cCol) == -1) {
            this.pw.println("Forbidden field because a goose exists");
            return false;
        }
        else if(matrix.get(cRow + "," + cCol) == 1) {
            this.pw.println("Forbidden field because a goose fox"); 
            return false;
        }
        if((cRow + cCol) % 2 == 0)  {
            this.pw.println("Movement on dark fields is forbidden");
            return false;
        }
        return (cRow + cCol) % 2 != 0 &&  // da li je polje adekvatne boje
                cRow == sRow + 1 &&    // da li je prvi donji red
                Math.abs(cCol - sCol) == 1;      // da li je susedno polje po koloni
    }
    
    public boolean isFoxWinner() {
        return currentFoxRow == 1;
    }
    
    public boolean areGeeseWinner() {
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
        return isTrapped;
    }
    
}
