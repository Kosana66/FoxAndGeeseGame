package rszeos.android.chatroomclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class Singleton {
    private static Singleton single_instance = null;

    public Socket socket;
    public BufferedReader br;
    public PrintWriter pw;

    public static String ip_address;
    public static int port;
    public static void setIP(String ip_addres){
        ip_address = ip_addres;
    }
    public static void setPort(int portt) {
        port = portt;
    }

    private Singleton() throws IOException {
        //10.0.2.2  //192.168.1.7//
        this.socket = new Socket(ip_address, port);
        this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.pw = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()), true);
        if (this.socket == null) {
            System.out.println("Singleton isn't created.");
            this.single_instance = null;
        }
        else {
            System.out.println("Singleton is created.");
        }
    }

    // Static method to create instance of Singleton class
    public static synchronized Singleton getInstance()
    {
        try{
            if (single_instance == null) {
                single_instance = new Singleton();
            }
        } catch (IOException e) {
            single_instance = null;
            System.out.println("Instance of singleton isn't create.");
        }

        return single_instance;
    }
}