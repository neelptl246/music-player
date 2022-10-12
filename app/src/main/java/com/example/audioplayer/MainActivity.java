package com.example.audioplayer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity {

    ImageButton btop, btpt, btpa, btst;
    TextView tv;
    SeekBar sb;
    int duration = 0;
    boolean finish = false;
    boolean pauseFinish = false;
    int current = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btop = findViewById(R.id.btop);
        btpt = findViewById(R.id.btpt);
        btpa = findViewById(R.id.btpa);
        btst = findViewById(R.id.btst);

        tv = findViewById(R.id.tv);
        sb = findViewById(R.id.sb);

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (GlobalMedia.mp != null) {
                    current = sb.getProgress();
                    current = current * 1000;
                    GlobalMedia.mp.seekTo(current);
                    tv.setText("" + (current / 1000) + "/" + duration);
                }
            }
        });

        btst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GlobalMedia.mp != null) {
                    tv.setText("0/0");
                    pauseFinish = true;
                    finish = true;
                    duration = 0;
                    current = 0;
                    sb.setProgress(0);
                    GlobalMedia.mp.stop();
                    GlobalMedia.mp = null;
                }
            }
        });

        btpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (GlobalMedia.mp != null) {
                    GlobalMedia.mp.start();
                    pauseFinish = false;
                }
            }
        });

        btpa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (GlobalMedia.mp != null) {
                    GlobalMedia.mp.pause();
                    pauseFinish = true;
                }
            }
        });

        btop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent ii = new Intent(Intent.ACTION_GET_CONTENT);   // to get content of client divces
                ii.setType("audio/");     // ii.start("video/);  ii.start("images/*);
                startActivityForResult(Intent.createChooser(ii, "SELECT YOUR SONG"), 151);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int reqCode, int result, Intent data) {
        if (reqCode == 151 && result == RESULT_OK)    // here 151 for adio Result_Ok means, User has Selected something
        {
            Uri uri = data.getData();  // to get exact location of selected song
            GlobalMedia.mp = MediaPlayer.create(getApplicationContext(), uri);
            GlobalMedia.mp.start();

            duration = GlobalMedia.mp.getDuration();
            duration = duration / 1000;
            tv.setText("0/" + duration);

            sb.setMax(duration); //to set length of seekbar as per length of selected song

            notifyMe(uri);

            finish = false;
            pauseFinish = false;


            new Thread(new Runnable() {
                @Override
                public void run() {

                    while (!finish) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                        }

                        if (!pauseFinish) {
                            current = GlobalMedia.mp.getCurrentPosition();
                            current = current / 1000;
                            sb.setProgress(current);

                           /* tv.post(new Runnable() {
                                @Override
                                public void run() {

                                    tv.setText(""+current+"/"+duration);

                                }
                            });*/

                            tv.setText("" + current + "/" + duration);

                            if (current >= duration) {
                                pauseFinish = true;
                                finish = true;
                                duration = 0;
                                current = 0;
                                sb.setProgress(0);
                                GlobalMedia.mp.stop();
                                GlobalMedia.mp = null;
                                tv.setText("0/0");
                            }
                        }
                    }
                    sb.setProgress(0);
                }
            }).start();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void notifyMe(Uri uri) {
        String path = uri.getPath();
        int p = path.lastIndexOf("/");
        String song = path.substring(p + 1);

        NotificationChannel channel = new NotificationChannel("1001", "My Audio Player", NotificationManager.IMPORTANCE_HIGH);

        Intent i1 = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pi1 = PendingIntent.getActivity(this, 0, i1, 0);

        Intent i2 = new Intent(getApplicationContext(), PauseService.class);
        PendingIntent pi2 = PendingIntent.getService(this, 0, i2, 0);

        Intent i3 = new Intent(getApplicationContext(), PlayService.class);
        PendingIntent pi3 = PendingIntent.getService(this, 0, i3, 0);

        Intent i4 = new Intent(getApplicationContext(), StopService.class);
        PendingIntent pi4 = PendingIntent.getService(this, 0, i4, 0);

        NotificationCompat.Builder not = new NotificationCompat.Builder(getApplicationContext(), "1001")
                .setContentTitle(song)
                .setContentText("Favorite Song")
                .setSmallIcon(R.drawable.play)
                .setContentIntent(pi1)
                .addAction(android.R.drawable.ic_media_pause, "PAUSE", pi2)
                .addAction(android.R.drawable.ic_media_play, "PLAY", pi3)
                .addAction(R.drawable.stop, "STOP", pi4);

        NotificationManager manage = getSystemService(NotificationManager.class);

        manage.createNotificationChannel(channel);

        manage.notify(123, not.build());


    }

}