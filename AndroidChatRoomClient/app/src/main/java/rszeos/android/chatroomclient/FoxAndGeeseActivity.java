package rszeos.android.chatroomclient;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;

public class FoxAndGeeseActivity extends AppCompatActivity {
    private Socket socket;
    private BufferedReader br;
    private PrintWriter pw;

    int numRows = 8;
    int numColumns = 8;
    private TextView tvTurn;

    HashMap<String, ImageView> images;

    public BufferedReader getBr() {
        return this.br;
    }

    public void setTvTurn(String turn) {
        this.tvTurn.setText(turn);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foxandgeese);
        connectToServer();

        images = new HashMap<String, ImageView>();
        LinearLayout llmain = findViewById(R.id.lvmain);
        tvTurn = findViewById(R.id.tvTurn);
        tvTurn.setText("Fox turn");
        Random random = new Random();
        int[] evenColumns = {1, 3, 5, 7};
        int foxCol =  evenColumns[random.nextInt(evenColumns.length)];

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        for (int row = 1; row <= numRows; row++) {
            LinearLayout llrow = new LinearLayout(this);
            llrow.setOrientation(LinearLayout.HORIZONTAL);
            for (int col = 1; col <= numColumns; col++) {
                ImageView iv = new ImageView(this);
                iv.setTag(row + "," + col);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,140);
                layoutParams.height = displayMetrics.widthPixels / numColumns;
                layoutParams.weight = 1;
                iv.setLayoutParams(layoutParams);

                if( (row + col) % 2 == 0)
                    iv.setBackgroundColor(Color.rgb(0x96, 0x4b, 0x00));
                else
                    iv.setBackgroundColor(Color.rgb(0xf3, 0xdc, 0xa2));

                if(row == numRows && col == foxCol)
                    iv.setImageResource(R.drawable.fox);
                if(row == 1 && col % 2 == 0)
                    iv.setImageResource(R.drawable.goose);

                images.put(row + "," + col, iv);

                iv.setOnClickListener((v)-> sendMessage("Clicked:" + v.getTag().toString()));
                llrow.addView(iv);
            }
            llmain.addView(llrow);
        }
        sendMessage("Initial fox column:" + foxCol);
    }

    public void connectToServer(){
        new Thread(() -> {
            Singleton singleton = Singleton.getInstance();
            if(singleton != null) {
                FoxAndGeeseActivity.this.socket = singleton.socket;
                FoxAndGeeseActivity.this.br = singleton.br;
                FoxAndGeeseActivity.this.pw = singleton.pw;
                new Thread(new ReceiveMessageFromGameServer(FoxAndGeeseActivity.this)).start();
            }
            else {
                System.out.println("Problem with singleton");
            }
        }).start();
    }

    public void sendMessage(String message){
        new Thread(() -> {
            if (FoxAndGeeseActivity.this.pw != null){
                FoxAndGeeseActivity.this.pw.println(message);
                System.out.println("FoxAndGeeseActivity: Sending: " + message);
            }
        }).start();
    }
}