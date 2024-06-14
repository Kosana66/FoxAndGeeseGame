package rszeos.android.chatroomclient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.Image;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class ReceiveMessageFromGameServer implements Runnable {
    FoxAndGeeseActivity parent;
    BufferedReader br;
    int numRows = 8;
    int numColumns = 8;
    int foxRow;
    int foxCol;
    String foxPlayer, geesePlayer;

    public ReceiveMessageFromGameServer(FoxAndGeeseActivity parent) {
        this.parent = parent;
        this.br = parent.getBr();
    }

    @Override
    public void run() {
        while (true) {
            String line;
            try {
                line = this.br.readLine();


            } catch (IOException ex) {
                System.out.println("I can't receive message!");
            }
        }
    }
}
