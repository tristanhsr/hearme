package com.example.paulo.mapstest;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

public class PlayActivity extends AppCompatActivity {

    ImageButton play;
    Boolean isPlay = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        final Intent playbackServiceIntent = new Intent(this, AudioPlayer.class);
        play = (ImageButton) findViewById(R.id.play);
        ImageButton stop = (ImageButton) findViewById(R.id.stop);
        
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPlay){
                    stopService(playbackServiceIntent);
                    isPlay = false;
                    play.setImageResource(R.drawable.play);                }
                else {
                    startService(playbackServiceIntent);
                    isPlay = true;
                    play.setImageResource(R.drawable.pause);
                }




                //finish();
            }

        });

        stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                stopService(playbackServiceIntent);

                //finish();
            }
        });
    }


}

