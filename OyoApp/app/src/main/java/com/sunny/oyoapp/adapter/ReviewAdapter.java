package com.sunny.oyoapp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sunny.oyoapp.R;
import com.sunny.oyoapp.model.Review;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    Context context;
    ArrayList<Review> reviewArrayList;
    Review review;

    SimpleDateFormat sf;
    SimpleDateFormat df;





    public ReviewAdapter(Context context, ArrayList<Review> reviewArrayList) {
        this.context = context;
        this.reviewArrayList = reviewArrayList;

        sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        df = new SimpleDateFormat("yyyy년MM월dd일 HH:mm");
        sf.setTimeZone(TimeZone.getTimeZone("UTC"));
        df.setTimeZone(TimeZone.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_row, parent, false);
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviewArrayList.get(position);
        // 프로필 이미지 가져오기
        // Glide를 사용하여 이미지 설정

        Log.i("XXXXXXX","프로필 이미지 : "+review.profileUrl);
        Log.i("XXXXXXX","프로필 닉네임 : "+review.nickname);
        if(review.profileUrl == null){
            Glide.with(context).load(R.drawable.no_profile).into(holder.ivProfile);

        }else {
            Glide.with(context).load(review.profileUrl).into(holder.ivProfile);
        }

        // 닉네임 설정
        holder.txtNick.setText(review.nickname);

        // 별점 가져오기
        holder.txtRating.setText("" + review.rating);

        // 레시피 내용 설정
        holder.txtComment.setText(review.content);

    }

    @Override
    public int getItemCount() {
        // 아이템 수 반환
        return reviewArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView ivProfile;
        TextView txtNick;
        TextView txtRating;
        TextView txtComment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // ViewHolder 내부 뷰들 초기화
            ivProfile= itemView.findViewById(R.id.ivProfile);
            txtNick = itemView.findViewById(R.id.txtNick);
            txtRating = itemView.findViewById(R.id.txtRating);
            txtComment = itemView.findViewById(R.id.txtComment);


        }
    }
}



