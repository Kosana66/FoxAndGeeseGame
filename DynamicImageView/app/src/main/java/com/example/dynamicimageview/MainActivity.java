package com.example.dynamicimageview;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    int numRows = 8;
    int numColumns = 8;
    boolean foxTurn = true;
    int selRow = 0;
    int selCol = 0;
    int foxRow;
    int foxCol;
    int selItem = 0;
    HashMap<String, Integer> matrix;
    HashMap<String, ImageView> images;

    private boolean canGooseMove (int row, int col) {
        int newRow = row + 1;       // guska moze ici samo na dole
        int newCol = 0;
        int[] lr = {-1, 1};
        for(int i : lr) {
            newCol = col + i;
            if (newRow > 0 && newRow <= numRows && newCol > 0 && newCol <= numColumns) {
                if (matrix.get(newRow + "," + newCol) != 1 && matrix.get(newRow + "," + newCol) != -1) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean AllowedFieldGoose (int cRow, int cCol, int sRow, int sCol) {
        if(cRow == sRow && cCol == sCol) {
            Toast.makeText(MainActivity.this, "Vec je selektovana guska", Toast.LENGTH_SHORT).show();
            return false;
        }                                                                                                               
        if(matrix.get(cRow + "," + cCol) == -1) {
            Toast.makeText(MainActivity.this, "Nedozvoljeno polje jer postoji guska" , Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(matrix.get(cRow + "," + cCol) == 1) {
            Toast.makeText(MainActivity.this, "Nedozvoljeno polje jer postoji lisica" , Toast.LENGTH_SHORT).show();
            return false;
        }
        if((cRow + cCol) % 2 == 0)  {
            Toast.makeText(MainActivity.this, "Zabranjeno kretanje tamnim poljima" , Toast.LENGTH_SHORT).show();
            return false;
        }
        return (cRow + cCol) % 2 != 0 &&  // da li je polje adekvatne boje
                cRow == sRow + 1 &&    // da li je prvi donji red
                Math.abs(cCol - sCol) == 1;      // da li je susedno polje po koloni
    }

    private boolean AllowedFieldFox (int cRow, int cCol, int sRow, int sCol) {
        if(cRow == sRow && cCol == sCol) {
            Toast.makeText(MainActivity.this, "Vec je selektovana lisica", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(matrix.get(cRow + "," + cCol) == -1) {
            Toast.makeText(MainActivity.this, "Nedozvoljeno polje jer postoji guska" , Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(matrix.get(cRow + "," + cCol) == 1) {
            Toast.makeText(MainActivity.this, "Nedozvoljeno polje jer postoji lisica" , Toast.LENGTH_SHORT).show();
            return false;
        }
        if((cRow + cCol) % 2 == 0)  {
            Toast.makeText(MainActivity.this, "Zabranjeno kretanje tamnim poljima" , Toast.LENGTH_SHORT).show();
            return false;
        }
        return (cRow + cCol) % 2 != 0 &&  // da li je polje adekvatne boje
                Math.abs(cRow - sRow) == 1 &&    // da li je prvi donji red
                Math.abs(cCol - sCol) == 1;      // da li je susedno polje po koloni
    }


    private void EndGame() {
        if (foxRow == 1) {
            Toast.makeText(this, "LISICA JE POBEDNIK!", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(this::recreate, 2000);
            return;
        }

        int[][] neighborhood = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        boolean isTrapped = true;
        for (int[] neighbor : neighborhood) {
            int newRow = foxRow + neighbor[0];
            int newCol = foxCol + neighbor[1];
            if (newRow > 0 && newRow <= numRows && newCol > 0 && newCol <= numColumns) {
                if (matrix.get(newRow + "," + newCol) != 1 || matrix.get(newRow + "," + newCol) != -1) {
                    isTrapped = false;
                    break;
                }
            }
        }
        if (isTrapped) {
            Toast.makeText(this, "GUSKE SU POBEDNICI!", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(this::recreate, 2000);
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        matrix = new HashMap<String, Integer>();
        images = new HashMap<String, ImageView>();
        LinearLayout llmain = findViewById(R.id.lvmain);

        Random random = new Random();
        int[] evenColumns = {1, 3, 5, 7};
        foxRow = 8;
        foxCol =  evenColumns[random.nextInt(evenColumns.length)];

        for (int row = 1; row <= numRows; row++){
            LinearLayout llrow = new LinearLayout(this);
            llrow.setOrientation(LinearLayout.HORIZONTAL);
            for (int col = 1; col <= numColumns; col++){
                ImageView iv = new ImageView(this);
                iv.setTag(row + "," + col);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,140);
                layoutParams.weight = 1;
                iv.setLayoutParams(layoutParams);

                if( (row + col) % 2 == 0)
                    iv.setBackgroundColor(Color.rgb(0x96, 0x4b, 0x00));
                else
                    iv.setBackgroundColor(Color.rgb(0xf3, 0xdc, 0xa2));

                if(row == numRows && col == foxCol) {
                    matrix.put(row + "," + col, 1);
                    iv.setImageResource(R.drawable.fox);
                }
                else if(row == 1 && col % 2 == 0) {
                    matrix.put(row + "," + col, -1);
                    iv.setImageResource(R.drawable.goose);
                }
                else
                    matrix.put(row + "," + col, 0);

                images.put(row + "," + col, iv);

                final int finalRow = row;
                final int finalCol = col;

                iv.setOnClickListener((v)-> {
                    int clickedRow = Integer.parseInt(v.getTag().toString().split(",")[0]);
                    int clickedCol = Integer.parseInt(v.getTag().toString().split(",")[1]);
                    if (selItem == 0) {
                        // da li je red na lisicu i da li je na ovoj poziciji lisica
                        // lisica ima oznaku 1
                        if (foxTurn && foxRow == finalRow && foxCol == finalCol) {
                            // selektovana je lisica
                            selRow = clickedRow;
                            selCol = clickedCol;
                            selItem = 1;
                            iv.setImageResource(R.drawable.clicked_fox);
                            images.get(clickedRow + "," + clickedCol).setImageResource(R.drawable.clicked_fox);
                        // da li je red na gusku i da li je na ovoj poziciji guska
                        // guska ima oznaku -1
                        } else if (!foxTurn && matrix.get(finalRow + "," + finalCol) != null && matrix.get(finalRow + "," + finalCol) == -1) {
                            if (canGooseMove(clickedRow, clickedCol)) {
                                selRow = clickedRow;
                                selCol = clickedCol;
                                selItem = -1;
                                iv.setImageResource(R.drawable.clicked_goose);
                                images.get(clickedRow + "," + clickedCol).setImageResource(R.drawable.clicked_goose);
                            } else {
                                Toast.makeText(MainActivity.this, "Biraj drugu gusku!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    else if(selItem == 1) {
                        if(AllowedFieldFox(clickedRow, clickedCol, selRow, selCol)) {
                           if( (selRow + selCol) % 2 == 0)
                               images.get(selRow + "," + selCol).setBackgroundColor(Color.rgb(0x96, 0x4b, 0x00));
                           else
                               images.get(selRow + "," + selCol).setBackgroundColor(Color.rgb(0xf3, 0xdc, 0xa2));
                           foxRow = clickedRow;
                           foxCol = clickedCol;
                           images.get(clickedRow + "," + clickedCol).setImageResource(R.drawable.fox);
                           matrix.put(clickedRow + "," + clickedCol, 1);
                           foxTurn = false;
                           // ponisti izabrano
                           images.get(selRow + "," + selCol).setImageDrawable(null);
                           matrix.put(selRow + "," + selCol, 0);
                           selItem = 0;
                           EndGame();
                        }
                    }
                    else if(selItem == -1) {
                        if(AllowedFieldGoose(clickedRow, clickedCol, selRow, selCol)) {
                            if( (selRow + selCol) % 2 == 0)
                                images.get(selRow + "," + selCol).setBackgroundColor(Color.rgb(0x96, 0x4b, 0x00));
                            else
                                images.get(selRow + "," + selCol).setBackgroundColor(Color.rgb(0xf3, 0xdc, 0xa2));
                            images.get(clickedRow + "," + clickedCol).setImageResource(R.drawable.goose);
                            matrix.put(clickedRow + "," + clickedCol, -1);
                            foxTurn = true;
                            // ponisti izabrano
                            images.get(selRow + "," + selCol).setImageDrawable(null);
                            matrix.put(selRow + "," + selCol, 0);
                            selItem = 0;
                            EndGame();
                        }
                    }

                });
                llrow.addView(iv);
            }
            llmain.addView(llrow);
        }
    }


}