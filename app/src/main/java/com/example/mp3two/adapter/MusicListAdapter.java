package com.example.mp3two.adapter;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mp3two.R;
import com.example.mp3two.activity.MainActivity;
import com.example.mp3two.data.MusicData;
import com.example.mp3two.service.AudioService;

import java.util.ArrayList;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.ViewHolder> {

    Context context;
    private ArrayList<MusicData> musicList = new ArrayList<>();
    MusicData musicData;

    //Set Click EventListener
    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    private OnItemClickListener mListener = null;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public void addItem(long id, long albumId, String title, String artist, String album, long duration, String datapath) {
        MusicData data = new MusicData();
        data.setId(id);
        data.setAlbumId(albumId);
        data.setTitle(title);
        data.setArtist(artist);
        data.setAlbum(album);
        data.setDuration(duration);
        data.setDataPath(datapath);
        musicList.add(data);
    }

    @NonNull
    @Override
    public MusicListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.music_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MusicListAdapter.ViewHolder holder, int position) {
        musicData = musicList.get(position);

        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri sAlbumArtUri = ContentUris.withAppendedId(sArtworkUri, musicData.getAlbumId());
        holder.imgvAlbum.setImageURI(sAlbumArtUri);
        holder.tvTitle.setText(musicData.getTitle());
        holder.tvArtist.setText(musicData.getArtist());

        //Change Duration Format
        int dur = (int) musicData.getDuration();
        int hrs = (dur / 3600000);
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;
        String songTime = String.format("%02d:%02d:%02d", hrs, mns, scs);
        if (hrs == 0) {
            songTime = String.format("%02d:%02d", mns, scs);
        }
        holder.tvDuration.setText(songTime);
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public ArrayList<MusicData> getPlaylist() {
        return musicList;
    }

    //Create ViewHolder
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle, tvArtist, tvDuration;
        public ImageView imgvAlbum;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tv_title);
            tvArtist = itemView.findViewById(R.id.tv_artist);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            imgvAlbum = itemView.findViewById(R.id.imgv_album);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();

                    if (pos != RecyclerView.NO_POSITION) {
                        if (mListener != null) {
                            mListener.onItemClick(view, pos);
                        }
                        //TO DO
                    }
                }
            });
        }
    }//뷰홀더
}
