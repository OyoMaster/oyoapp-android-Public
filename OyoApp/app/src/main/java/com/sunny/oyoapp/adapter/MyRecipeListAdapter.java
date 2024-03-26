package com.sunny.oyoapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sunny.oyoapp.MyRecipeActivity;
import com.sunny.oyoapp.R;
import com.sunny.oyoapp.RecipeActivity;
import com.sunny.oyoapp.UpdateRecipeActivity;
import com.sunny.oyoapp.UserRecipeAllActivity;
import com.sunny.oyoapp.api.FavoritesApi;
import com.sunny.oyoapp.api.NetworkClient;
import com.sunny.oyoapp.config.Config;
import com.sunny.oyoapp.model.Posting;
import com.sunny.oyoapp.model.Res;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MyRecipeListAdapter extends RecyclerView.Adapter<MyRecipeListAdapter.ViewHolder> {

    Context context;
    ArrayList<Posting> postingArrayList;
    Posting posting;

    SimpleDateFormat sf;
    SimpleDateFormat df;





    public MyRecipeListAdapter(Context context, ArrayList<Posting> postingArrayList) {
        this.context = context;
        this.postingArrayList = postingArrayList;

        sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        df = new SimpleDateFormat("yyyy년MM월dd일 HH:mm");
        sf.setTimeZone(TimeZone.getTimeZone("UTC"));
        df.setTimeZone(TimeZone.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_recipe_row, parent, false);
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Posting posting = postingArrayList.get(position);

        // Glide를 사용하여 이미지 설정
        Glide.with(context).load(posting.imageURL).into(holder.thumbRecipe);

        // 닉네임 설정
        holder.txtNickname.setText(posting.nickname);

        // 레시피 내용 설정
        holder.txtRecipeTitle.setText(posting.title);

        holder.txtUploadDate.setText(posting.createdAt);

        holder.txtRating.setText(String.format("%.1f", posting.avgRating));

        // 서버의 시간(글로벌 표준시)을, 로컬의 시간으로 변환해야 한다.

        try {
            Date date = sf.parse( posting.createdAt );
            String localTime = df.format(date);
            holder.txtUploadDate.setText(localTime);

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public int getItemCount() {
        // 아이템 수 반환
        return postingArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtRecipeView;
        // ViewHolder 내부 뷰들 설정
        ImageView thumbRecipe;
        TextView txtRecipeTitle;
        TextView btnUpdate;
        TextView txtNickname;
        TextView txtUploadDate;
        TextView textView16;
        TextView txtRating;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // ViewHolder 내부 뷰들 초기화
            thumbRecipe = itemView.findViewById(R.id.thumbRecipe);
            txtRecipeTitle = itemView.findViewById(R.id.txtRecipeTitle);
            txtNickname = itemView.findViewById(R.id.txtNickname);
            txtUploadDate = itemView.findViewById(R.id.txtUploadDate);
            textView16 = itemView.findViewById(R.id.textView16);
            txtRating = itemView.findViewById(R.id.txtRating);
            btnUpdate = itemView.findViewById(R.id.btnUpdate);
            cardView = itemView.findViewById(R.id.cardView);


            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = getAdapterPosition();
                    Posting posting = postingArrayList.get(index);

                    Intent intent = new Intent(context, UpdateRecipeActivity.class);
                    intent.putExtra("postingId", posting.postingid);
                    ((MyRecipeActivity) context).startActivity(intent);
                }
            });
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = getAdapterPosition();
                    Posting posting = postingArrayList.get(index);

                    Intent intent = new Intent(context, RecipeActivity.class);
                    intent.putExtra("postingId", posting.postingid);
                    context.startActivity(intent);
                }
            });


        }
    }
}


