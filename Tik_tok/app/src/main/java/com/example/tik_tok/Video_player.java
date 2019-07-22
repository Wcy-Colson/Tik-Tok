package com.example.tik_tok;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class Video_player extends AppCompatActivity {

    private static VideoView video;
    private Button button;
    private SeekBar seekBar;
    private TextView tv;
    private static Bundle outState;
    private String path;

    static Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            refresh();
            handler.postDelayed(runnable,1000);
        }
    };

    private void refresh(){
        int current = video.getCurrentPosition();
        int total = video.getDuration();
        seekBar.setProgress((int)((float)current*100.0f/(float)total));
        int total_minute = total / 60000;
        int total_second = (total % 60000) / 1000;
        int current_minute = current / 60000;
        int current_second = (current % 60000) / 1000;
        tv.setText(current_minute+":"+current_second+"/"+total_minute+":"+total_second);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        path = intent.getStringExtra("value");
        int current = 0;
        if (outState != null) {
            current = outState.getInt("key");
        }
        setContentView(R.layout.videoplayer);
        video = findViewById(R.id.video);
        button = findViewById(R.id.state);
        seekBar = findViewById(R.id.bar);
        tv = findViewById(R.id.current);
        video.setVideoPath(path);
        video.seekTo(current);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (button.getText().equals("Start")){
                    video.start();
                    button.setText("Pause");
                }
                else{
                    video.pause();
                    button.setText("Start");
                }
            }
        });
        handler.postDelayed(runnable,0);
        seekBar.setOnSeekBarChangeListener(listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        int current = video.getCurrentPosition();
        if (outState != null){
            outState.clear();
            outState = null;
        }
        outState = new Bundle();
        outState.putInt("key",current);
    }

    private SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int current = seekBar.getProgress();
            int total = video.getDuration();
            int video_current = (int)((float)current/100.0f*(float)total);
            video.seekTo(video_current);
        }
    };
}
