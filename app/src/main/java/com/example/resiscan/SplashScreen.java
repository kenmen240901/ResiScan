package com.example.resiscan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.VideoView;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        VideoView videoview = (VideoView) findViewById(R.id.logoVid);
        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.resi_anim);
        videoview.setVideoURI(uri);
        videoview.animate().alpha(1);
        videoview.seekTo(0);
        videoview.start();
        videoview.setAudioFocusRequest(AudioManager.AUDIOFOCUS_NONE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashScreen.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        }, 3000);

    }
}