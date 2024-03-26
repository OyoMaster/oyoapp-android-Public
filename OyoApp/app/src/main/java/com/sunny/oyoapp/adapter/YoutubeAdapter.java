package com.sunny.oyoapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.WindowDecorActionBar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.sunny.oyoapp.R;
import com.sunny.oyoapp.model.Video;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class YoutubeAdapter extends RecyclerView.Adapter<YoutubeAdapter.ViewHolder>{

    Context context;
    ArrayList<Video> videoArrayList;

    public YoutubeAdapter(Context context, ArrayList<Video> videoArrayList) {
        this.context = context;
        this.videoArrayList = videoArrayList;
    }


    @NonNull
    @Override
    public YoutubeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.youtube_row,parent,false);
        return new YoutubeAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull YoutubeAdapter.ViewHolder holder, int position) {

        // 현재 위치의 Video 객체 가져오기
        Video video = videoArrayList.get(position);

        holder.txtTitle.setText(video.title);
        holder.txtChTitle.setText(video.channelTitle);

        String date = video.publishTime.replace("T"," ").substring(0,15+1);

        holder.txtUploadDate.setText(date);

        // Glide를 사용하여 이미지 로딩
        Glide.with(context).load(video.highUrl).into(holder.imgPhoto);
    }

    @Override
    public int getItemCount() {
        return videoArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

//        public WindowDecorActionBar.TabImpl txtDescription;
        TextView txtTitle;
        TextView txtChTitle;
        TextView txtUploadDate;
        ImageView imgPhoto;
        CardView cardView;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtChTitle = itemView.findViewById(R.id.txtChTitle);
            txtUploadDate = itemView.findViewById(R.id.txtUploadDate);
            imgPhoto = itemView.findViewById(R.id.imgPhoto);
            cardView = itemView.findViewById(R.id.cardView);

            cardView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    int index = getAdapterPosition();
                    Video video = videoArrayList.get(index);

                    String url = "https://www.youtube.com/watch?v="+video.videoId;

                    openWebPage(url);
                }
            });
            


        }

        // 2.카드뷰 누르면 웹페이지로 해당 유튜브 영상 재생 함수
        void openWebPage(String url){
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        }
    }
}
