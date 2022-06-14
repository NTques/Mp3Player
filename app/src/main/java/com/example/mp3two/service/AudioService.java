package com.example.mp3two.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import com.example.mp3two.activity.MainActivity;
import com.example.mp3two.data.MusicData;

import java.util.ArrayList;

public class AudioService extends Service {
    private ArrayList<MusicData> playList = new ArrayList<>();
    private MediaPlayer mediaPlayer;
    private MusicData musicData;

    int curPosition;

    IBinder mBinder = new AudioServiceBinder();

    public class AudioServiceBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        registerReceiver(receiver, new IntentFilter("PlayService"));
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        playList = (ArrayList<MusicData>) intent.getSerializableExtra("playList");
        curPosition = intent.getIntExtra("position", 0);
        mediaPlayerManager(curPosition);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("Mp3 BroadCasted");
            String mode;
            mode = intent.getStringExtra("mode");
            if (mode != null) {
                if (mode.equals("start")) {
                    mediaPlayer.start();
                    System.out.println(mode);
                } else if (mode.equals("stop")) {
                    mediaPlayer.pause();
                    System.out.println(mode);
                }
            }
            int curProgress;
            curProgress = intent.getIntExtra("curProgress", -1);
            if (curProgress != -1) {
                mediaPlayer.seekTo(curProgress);
                //System.out.println(mediaPlayer.getCurrentPosition());
            }
        }
    };

    private void mediaPlayerManager(int position) {
        musicData = playList.get(position);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                curPosition++;
                if (curPosition > playList.size() - 1) {
                    curPosition = 0;
                } else if (curPosition < 0) {
                    curPosition = playList.size() - 1;
                }
                musicData = playList.get(curPosition);
                playMusic();
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
                //브로드캐스트로 musicdata 전송
                if (mediaPlayer.isPlaying()) {
                    Intent mIntent = new Intent("MusicPlayService");
                    mIntent.putExtra("musicData", musicData);
                    mIntent.putExtra("runThread", mediaPlayer.isPlaying());
                    sendBroadcast(mIntent);
                }
            }
        });
        playMusic();
    }

    private void playMusic() {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(musicData.getDataPath());
            mediaPlayer.prepareAsync();
            System.out.println(musicData.getTitle());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}