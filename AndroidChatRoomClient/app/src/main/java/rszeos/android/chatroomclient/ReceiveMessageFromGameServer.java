package rszeos.android.chatroomclient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import android.widget.Toast;

public class ReceiveMessageFromGameServer implements Runnable {
    FoxAndGeeseActivity parent;
    BufferedReader br;
    HashMap<String, ImageView> images;
    int numRows = 8;
    int numColumns = 8;
    int foxRow;
    int foxCol;
    String foxPlayer, geesePlayer;

    public ReceiveMessageFromGameServer(FoxAndGeeseActivity parent) {
        this.parent = parent;
        this.br = parent.getBr();
        this.images = parent.images;
    }

    @Override
    public void run() {
        while (true) {
            String line;
            try {
                line = this.br.readLine();
                System.out.println(line);
                if(line.startsWith("Not your turn")) {
                    parent.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(parent, "Not your turn!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                if(line.startsWith("Select other goose")) {
                    parent.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(parent, "Select other goose!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                if(line.startsWith("Fox is selected") || line.startsWith("Forbidden") ||
                        line.startsWith("Movement") || line.startsWith("Goose is selected")) {
                    parent.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(parent, line, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                if(line.startsWith("Selected:fox:")) {
                    String row = line.split(":")[2].split(",")[0];
                    String col = line.split(":")[2].split(",")[1];

                    parent.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            images.get(row + "," + col).setImageDrawable(null);
                            images.get(row + "," + col).setImageResource(R.drawable.clicked_fox);
                        }
                    });
                }
                if(line.startsWith("Clicked:fox:")) {
                    String selRow = line.split(":")[2].split(",")[0];
                    String selCol = line.split(":")[2].split(",")[1];
                    String row = line.split(":")[2].split(",")[2];
                    String col = line.split(":")[2].split(",")[3];

                    parent.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            images.get(selRow + "," + selCol).setImageDrawable(null);
                            images.get(row + "," + col).setImageResource(R.drawable.fox);
                            parent.setTvTurn("Geese turn");
                        }
                    });
                }
                if(line.startsWith("fox")) {
                    parent.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            parent.setTvTurn("Fox turn");
                        }
                    });
                }
                if(line.startsWith("Selected:geese:")) {
                    String row = line.split(":")[2].split(",")[0];
                    String col = line.split(":")[2].split(",")[1];
                    parent.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            images.get(row + "," + col).setImageDrawable(null);
                            images.get(row + "," + col).setImageResource(R.drawable.clicked_goose);
                        }
                    });
                }
                if(line.startsWith("Clicked:geese:")) {
                    String selRow = line.split(":")[2].split(",")[0];
                    String selCol = line.split(":")[2].split(",")[1];
                    String row = line.split(":")[2].split(",")[2];
                    String col = line.split(":")[2].split(",")[3];
                    parent.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            images.get(selRow + "," + selCol).setImageDrawable(null);
                            images.get(row + "," + col).setImageResource(R.drawable.goose);
                            parent.setTvTurn("Fox turn");
                        }
                    });
                }
                if(line.startsWith("geese")) {
                    parent.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            parent.setTvTurn("Geese turn");
                        }
                    });
                }
                if(line.startsWith("Fox is winner")) {
                    parent.runOnUiThread(() -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(parent);
                        builder.setTitle("RESTART REQUEST")
                                .setMessage("FOX IS WINNER! Would you like to play again with the same player?")
                                .setCancelable(false)
                                .setPositiveButton("YES", (dialog, which) -> {
                                    parent.sendMessage("Accepted restart");
                                    dialog.cancel();
                                })
                                .setNegativeButton("NO", (dialog, which) -> {
                                    dialog.cancel();
                                    parent.sendMessage("Declined restart");
                                })
                                .show();
                    });
                }
                if(line.startsWith("Geese are winners")) {
                    parent.runOnUiThread(() -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(parent);
                        builder.setTitle("RESTART REQUEST")
                                .setMessage("GEESE ARE WINNER! Would you like to play again with the same player?")
                                .setCancelable(false)
                                .setPositiveButton("YES", (dialog, which) -> {
                                    parent.sendMessage("Accepted restart");
                                    dialog.cancel();
                                })
                                .setNegativeButton("NO", (dialog, which) -> {
                                    dialog.cancel();
                                    parent.sendMessage("Declined restart");
                                })
                                .show();
                    });
                }
                if(line.startsWith("Restart")) {
                    parent.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            parent.restartGame();
                        }
                    });
                }
                if(line.startsWith("Terminate")) {
                    parent.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent resultIntent = new Intent();
                            parent.setResult(6,resultIntent);
                            parent.finish();
                        }
                    });
                    break;
                }




            } catch (IOException ex) {
                System.out.println("I can't receive message!");
            }
        }
    }
}
