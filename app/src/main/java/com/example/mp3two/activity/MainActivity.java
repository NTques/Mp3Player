package com.example.mp3two.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.mp3two.R;
import com.example.mp3two.adapter.MusicListAdapter;
import com.example.mp3two.data.MusicData;
import com.example.mp3two.service.AudioService;
import com.example.mp3two.service.AudioService.AudioServiceBinder;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    RecyclerView musicListView;
    ImageView imgvBtnPreSmall, imgvBtnPlaySmall, imgvBtnNextSmall, imgvAlbumSmall;
    SeekBar seekBar;
    TextView tvTitleSmall, tvArtistSmall;
    LinearLayout smallLayout;

    private ArrayList<MusicData> playList = new ArrayList<>();

    MusicData musicData;
    Context context;
    public static Context mContext;
    MusicListAdapter adapter;

    private boolean runThread = false;
    private int curPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set SeekBar Thread
        PogressThread thread = new PogressThread();
        thread.start();

        //Request Permissions, external storage
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MODE_PRIVATE);

        //Find View by Id...
        musicListView = findViewById(R.id.music_list);//RecyclerView
        imgvBtnPreSmall = findViewById(R.id.imgv_btn_pre_small);//Image Buttons
        imgvBtnPlaySmall = findViewById(R.id.imgv_btn_play_small);
        imgvBtnNextSmall = findViewById(R.id.imgv_btn_next_small);
        imgvAlbumSmall = findViewById(R.id.imgv_album_small);
        seekBar = findViewById(R.id.seekBar);//SeekBar
        tvTitleSmall = findViewById(R.id.tv_title_small);
        tvArtistSmall = findViewById(R.id.tv_artist_small);
        smallLayout = findViewById(R.id.small_layout);

        context = getBaseContext();
        mContext = this;

        //Set Recycler View LayoutManager
        musicListView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        musicListView.setLayoutManager(new LinearLayoutManager(this));

        //set adapter
        adapter = new MusicListAdapter();
        musicListView.setAdapter(adapter);

        //get music file data
        readAudio();
        registerReceiver(receiver, new IntentFilter("MusicPlayService"));

        Intent sIntent = new Intent(MainActivity.this, AudioService.class);
        Intent rIntent = new Intent("PlayService");
        //Click Event
        adapter.setOnItemClickListener(new MusicListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                playList = adapter.getPlaylist();
                musicData = playList.get(position);
                curPosition = position;

                seekBar.setProgress(0);
                sIntent.putExtra("playList", playList);
                sIntent.putExtra("position", curPosition);
                startService(sIntent);

                Intent intent = new Intent(MainActivity.this, Mp3Player.class);
                startActivity(intent);
            }
        });
        imgvBtnPreSmall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent("MusicPlayService");
                seekBar.setProgress(0);
                curPosition--;
                if (curPosition < 0) {
                    curPosition = playList.size() - 1;
                }
                sIntent.putExtra("position", curPosition);
                startService(sIntent);
            }
        });
        imgvBtnPlaySmall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mode;
                if (runThread) {
                    mode = "stop";
                    runThread = false;
                } else {
                    mode = "start";
                    runThread = true;
                }
                rIntent.putExtra("mode", mode);
                sendBroadcast(rIntent);
            }
        });
        imgvBtnNextSmall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setProgress(0);
                curPosition++;
                if (curPosition > playList.size() - 1) {
                    curPosition = 0;
                }
                sIntent.putExtra("position", curPosition);
                startService(sIntent);
            }
        });
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

                ((Mp3Player) Mp3Player.mContext).seekBar.setProgress(seekBar.getProgress());
            }
        });
        smallLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Mp3Player.class);
                startActivity(intent);
            }
        });
    }

    @SuppressLint("Range")

    private void readAudio() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);
        cursor.moveToFirst();
        if (cursor != null && cursor.getCount() > 0) {
            do {
                long trackId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                long mDuration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String datapath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                adapter.addItem(trackId, albumId, title, artist, album, mDuration, datapath);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Get Intent Put Extras
            musicData = (MusicData) intent.getSerializableExtra("musicData");
            runThread = intent.getBooleanExtra("runThread", false);
            //Set UI Contents
            try {
                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                Uri sAlbumArtUri = ContentUris.withAppendedId(sArtworkUri, musicData.getAlbumId());

                tvTitleSmall.setText(musicData.getTitle());
                tvArtistSmall.setText(musicData.getArtist());
                imgvAlbumSmall.setImageURI(sAlbumArtUri);

                if (runThread && seekBar.getProgress() == seekBar.getMax()) {
                    seekBar.setProgress(0);
                }

                seekBar.setMax((int) musicData.getDuration());
                System.out.println((int) musicData.getDuration());
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("BroadCasted");
            smallLayout.setVisibility(View.VISIBLE);
        }
    };

    class PogressThread extends Thread {
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
            }
        }
    }

}