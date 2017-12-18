package com.thkoeln.paulo.hearme;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class NewRecordActivityActivity extends AppCompatActivity {

    ImageButton recordPlay;
    AudioRecordTest audioRecordTest;
    CountDownTimer testTimer;

    int test3, progress, currentLength = 0;

    boolean mStartRecording = true; // Für Record
    boolean mStartPlaying = true; // Für Play

    long millisUntilFinished;

    /// Recordtest
    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mFileName = null;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    private Button abschicken;
    private ImageButton recordButton;
    private BroadcastReceiver broadcastReceiver;
    EditText kommentar;

//    @Override
//    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int [] grantResults){
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if(requestCode == 100){
//            if ( grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
//                //enable_button();
//            } else {
//                runtime_permissions();
//            }
//        }
//    }


    @Override
    protected void onResume(){
        super.onResume();
        if(broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    kommentar.append(intent.getExtras().get("coordinates").toString());
                   System.out.println("Koordinaten"+"\n" +intent.getExtras().get("coordinates"));
                   System.out.println("Test");
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            case 100:
                if ( grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    //enable_button();
                    System.out.println("alles cool");
                } else {
                    runtime_permissions();
                }

        }
        if (!permissionToRecordAccepted) finish();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_record_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //--------------------AudioRecordTest-Klasse----------------------------------------
        mFileName = getExternalCacheDir().getAbsolutePath(); //Original
        mFileName += "/audiorecordtest.3gp";
        audioRecordTest = new AudioRecordTest(mFileName);
        //------------------------------------------------------------


        //GPS
        runtime_permissions();

        if (!runtime_permissions()){
            System.out.println("Es wird kein Permission Chck fü GPS benötigt");
        }
        Intent i = new Intent(getApplicationContext(),GPS_Service.class);
        startService(i);


        //GPS_Ende


        // Progress Bar
        // initiate progress bar and start button

        kommentar = (EditText)findViewById(R.id.kommentarN);
        final ProgressBar simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);
        final LinearLayout record_player = (LinearLayout) findViewById(R.id.record_player);
        recordButton = (ImageButton) findViewById(R.id.record);

        final Intent test = new Intent(this, AudioRecordTest.class);

        // perform click event on button
        recordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    simpleProgressBar.setVisibility(View.VISIBLE);


                    audioRecordTest.onRecord(mStartRecording);

                    mStartRecording = false;


                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {

                    simpleProgressBar.setVisibility(View.INVISIBLE);
                    record_player.setVisibility(View.VISIBLE);
                    audioRecordTest.onRecord(mStartRecording);
                    mStartRecording = true;

                    return true;
                }
                return false;
            }
        });


        abschicken = (Button) findViewById(R.id.abschicken);
        final Intent abschickenIntent = new Intent(this, MapsActivity.class);


        abschicken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(getApplicationContext(),GPS_Service.class);
                stopService(i);

                // Write a message to the database

                EditText titel_edit = (EditText) findViewById(R.id.titel_edit);


                DatabaseReference mDatabase;
                mDatabase = FirebaseDatabase.getInstance().getReference();
                String PostId = mDatabase.push().getKey();
                Post post = new Post(1, titel_edit.getText().toString(), "admin", "gps");

                mDatabase.child("posts").child(PostId).setValue(post);

                startActivity(abschickenIntent);
            }
        });


        final ProgressBar recordProgressbar = (ProgressBar) findViewById(R.id.progressBar_2_record);
        recordProgressbar.setProgress(0);
        // Ende


        final Intent recordPlaybackServiceIntent = new Intent(this, AudioPlayer.class);
        recordPlay = (ImageButton) findViewById(R.id.play_record);


        testTimer = new CountDownTimer(30000, 100) {


            public void onTick(long millisUntilFinished) {

                currentLength = audioRecordTest.getmPlayer().getCurrentPosition();
                int maxLength = audioRecordTest.getmPlayer().getDuration();
                progress = ((currentLength * 100) / maxLength);

                System.out.println("Länge in Timer " + maxLength);

                recordProgressbar.setProgress(progress);
                System.out.println(currentLength);
//              System.out.println(currentLength);
                System.out.println(progress);
                System.out.println("tick");

                test3 = test3 + 100;

                if (test3 >= maxLength) {
                    currentLength = 0;
                    test3 = 0;
                    onFinish();
                }

            }

            public void onFinish() {
                recordProgressbar.setProgress(0);
                recordPlay.setImageResource(R.drawable.play);
                audioRecordTest.onPlay(mStartPlaying);
                mStartPlaying = true;
                this.cancel();
                System.out.println("Nachricht fertig abgespiel!");
            }
        };

        recordPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                audioRecordTest.onPlay(mStartPlaying);
                if (mStartPlaying) {
                    recordPlay.setImageResource(R.drawable.pause);
                    testTimer.start();

                } else {
                    recordPlay.setImageResource(R.drawable.play);
                    recordProgressbar.setProgress(0);
                    testTimer.cancel();
                    progress = 0;
                    currentLength = 0;
                }
                mStartPlaying = !mStartPlaying;

            }

        });

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

    }

    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
        requestPermissions(new String []{ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);

        return true;
        }
        return false;
    }



}


