package com.example.mp3two.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.mp3two.R;
import com.example.mp3two.data.MusicData;


public class Mp3Player extends AppCompatActivity {
    public static Context mContext;
    ImageView imgvAlbum;
    TextView tvTitle, tvArtist, tvTimeNow, tvTimeEnd;
    SeekBar seekBar;

    private MusicData musicData;
    private boolean runThread;

    int dur, hrs, mns, scs;
    int curDur, curHrs, curMns, curScs;
    String songTime, curSongTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mp3_player);

        mContext = this;

        imgvAlbum = findViewById(R.id.imgv_album_player);
        tvTitle = findViewById(R.id.tv_title_player);
        tvArtist = findViewById(R.id.tv_artist_player);
        tvTimeNow = findViewById(R.id.tv_time_now);
        tvTimeEnd = findViewById(R.id.tv_time_end);
        seekBar = findViewById(R.id.seekBar_player);

        registerReceiver(receiver, new IntentFilter("MusicPlayService"));
        musicData = new MusicData();

        Intent rIntent = new Intent("PlayService");
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                rIntent.putExtra("curProgress", progress);
                sendBroadcast(rIntent);

                setCurTimeFormat();
                Message msg = handler.obtainMessage();
                handler.sendMessage(msg);

                ((MainActivity) MainActivity.mContext).seekBar.setProgress(seekBar.getProgress());
            }
        });

        ProgressThread thread = new ProgressThread();
        thread.start();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            musicData = (MusicData) intent.getSerializableExtra("musicData");
            runThread = intent.getBooleanExtra("runThread", false);
            //Set UI Contents

            Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri sAlbumArtUri = ContentUris.withAppendedId(sArtworkUri, musicData.getAlbumId());

            tvTitle.setText(musicData.getTitle());
            tvArtist.setText(musicData.getArtist());
            imgvAlbum.setImageURI(sAlbumArtUri);

            if (runThread && seekBar.getProgress() == seekBar.getMax()) {
                seekBar.setProgress(0);
            }

            seekBar.setMax((int) musicData.getDuration());

            dur = (int) musicData.getDuration();
            hrs = (dur / 3600000);
            mns = (dur / 60000) % 60000;
            scs = dur % 60000 / 1000;
            songTime = String.format("%02d:%02d:%02d", hrs, mns, scs);
            if (hrs == 0) {
                songTime = String.format("%02d:%02d", mns, scs);
            }
            tvTimeEnd.setText(songTime);

            System.out.println((int) musicData.getDuration());

            System.out.println("BroadCasted");
        }
    };

    class ProgressThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (runThread) {
                    seekBar.incrementProgressBy(1000);
                }
                if (runThread && seekBar.getProgress() == seekBar.getMax()) {
                    seekBar.setProgress(0);
                }
                setCurTimeFormat();
                Message msg = handler.obtainMessage();
                handler.sendMessage(msg);
            }
        }
    }

    private void setCurTimeFormat() {
        curDur = seekBar.getProgress();
        curHrs = (curDur / 3600000);
        curMns = (curDur / 60000) % 60000;
        curScs = curDur % 60000 / 1000;
        curSongTime = String.format("%02d:%02d:%02d", curHrs, curMns, curScs);
        if (hrs == 0) {
            curSongTime = String.format("%02d:%02d", curMns, curScs);
        }
    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            tvTimeNow.setText(curSongTime);
        }
    };
}