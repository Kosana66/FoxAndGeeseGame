package rszeos.android.chatroomclient;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;


public class MainActivity extends AppCompatActivity {
    EditText etUsername;
    EditText etIP;
    EditText etPort;
    Button btnOK;
    Button btnPlay;
    Spinner spnPlayers;

    private Socket socket;
    private BufferedReader br;
    private PrintWriter pw;

    private ReceiveMessageFromServer rmfs;

    public String getUsername() {
        return this.etUsername.getText().toString();
    }
    public BufferedReader getBr(){
        return MainActivity.this.br;
    }
    public Spinner getPlayers(){
        return this.spnPlayers;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etIP = (EditText) findViewById(R.id.etIP);
        etPort = (EditText) findViewById(R.id.etPort);
        btnOK = (Button) findViewById(R.id.btnOK);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnPlay.setEnabled(false);
        spnPlayers = (Spinner) findViewById(R.id.spinner);
        spnPlayers.setEnabled(false);

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!MainActivity.this.etUsername.getText().toString().equals("") &&
                        !MainActivity.this.etIP.getText().toString().equals("") &&
                        !MainActivity.this.etPort.getText().toString().equals("")) {
                    connectToServer();
                } else {
                    Toast.makeText(MainActivity.this, "All 3 fields must be filled in", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!etUsername.getText().toString().equals(spnPlayers.getSelectedItem().toString()))
                {
                    String stringForSend = "Challenge:" + etUsername.getText().toString() + ":" + spnPlayers.getSelectedItem().toString();
                    sendMessage(stringForSend);
                }
                else
                    Toast.makeText(MainActivity.this, "You can't play with yourself!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void connectToServer(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Singleton.setIP(MainActivity.this.etIP.getText().toString());
                Singleton.setPort(Integer.parseInt(MainActivity.this.etPort.getText().toString()));
                Singleton singleton = Singleton.getInstance();
                if(singleton != null) {
                    MainActivity.this.socket = singleton.socket;
                    MainActivity.this.br = singleton.br;
                    MainActivity.this.pw = singleton.pw;

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.btnOK.setEnabled(false);
                            MainActivity.this.etUsername.setEnabled(false);
                            MainActivity.this.etIP.setEnabled(false);
                            MainActivity.this.etPort.setEnabled(false);
                            MainActivity.this.btnPlay.setEnabled(true);
                            MainActivity.this.spnPlayers.setEnabled(true);
                            sendMessage(MainActivity.this.etUsername.getText().toString());
                            ReceiveMessageFromServer rmfs = new ReceiveMessageFromServer(MainActivity.this);
                            MainActivity.this.rmfs = rmfs;
                            new Thread(rmfs).start();
                        }
                    });
                }
                else {
                    System.out.println("Failed connection with server");
                }
            }
        }).start();
    }

    public void sendMessage(String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (MainActivity.this.pw != null){
                    MainActivity.this.pw.println(message);
                    System.out.println("MainActivity: Sending: " + message);
                }
            }
        }).start();
    }

    ActivityResultLauncher<Intent> activity2Launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == 6){
                        if(rmfs != null)
                        {
                            rmfs.setRunning(true);
                            new Thread(rmfs).start();
                        }
                    }
                }
            }
    );
}