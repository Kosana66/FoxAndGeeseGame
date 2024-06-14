package rszeos.android.chatroomclient;

import android.app.AlertDialog;

import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;

public class ReceiveMessageFromServer implements Runnable {
    MainActivity parent;
    BufferedReader br;
    private boolean running;

    public void setRunning(boolean running) {
        this.running = running;
    }

    public ReceiveMessageFromServer(MainActivity parent) {
        this.parent = parent;
        this.br = parent.getBr();
        this.running = true;
    }

    @Override
    public void run() {
        while (running) {
            String line;
            try {
                line = this.br.readLine();
                System.out.println(line);
                if (line.startsWith("Players:")) {
                    String[] currentPlayers = line.split(":")[1].trim().split(" ");
                    parent.runOnUiThread(() -> {
                        Spinner spinner = parent.getPlayers();
                        spinner.setAdapter(null);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(parent, android.R.layout.simple_spinner_item, currentPlayers);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(adapter);
                    });
                }
                if(line.startsWith("Received challenge:")) {
                    String challenger = line.split(":")[1];
                    String userToChallenge = line.split(":")[2];
                    parent.runOnUiThread(() -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(parent);
                        builder.setTitle("GAME REQUEST")
                                .setMessage("Do you accept the game with " + challenger + "?")
                                .setCancelable(false)
                                .setPositiveButton("YES", (dialog, which) -> {
                                    parent.sendMessage("Yes:" + challenger + ":" + userToChallenge);
                                    dialog.cancel();
                                })
                                .setNegativeButton("NO", (dialog, which) -> {
                                    dialog.cancel();
                                    parent.sendMessage("No:" + challenger);
                                })
                                .show();
                    });
                }
                if(line.startsWith("Unavailable user:")) {
                    parent.runOnUiThread(() -> Toast.makeText(parent, "Challenged user " + line.split(":")[1] + " is not available right now!", Toast.LENGTH_SHORT).show());
                }
                if(line.startsWith("Declined challenge")) {
                    parent.runOnUiThread(() -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(parent);
                        builder.setTitle("GAME RESPONSE")
                                .setMessage("The game is denied.")
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialog, which) -> dialog.cancel())
                                .show();
                    });
                }
                if(line.startsWith("Start match")) {
                    this.running = false;
                    Intent intent = new Intent(parent,FoxAndGeeseActivity.class);
                    parent.runOnUiThread(() -> {
                        parent.activity2Launcher.launch(intent);
                    });
                }
            } catch (IOException ex) {
                Toast.makeText(parent, "I can't to receive messageko!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
