package com.sunny.oyoapp.adapter;


import static com.sunny.oyoapp.api.NetworkClient.retrofit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sunny.oyoapp.R;
import com.sunny.oyoapp.RecipeActivity;
import com.sunny.oyoapp.ScarpActivity;
import com.sunny.oyoapp.api.FavoritesApi;
import com.sunny.oyoapp.api.NetworkClient;
import com.sunny.oyoapp.config.Config;
import com.sunny.oyoapp.model.Posting;
import com.sunny.oyoapp.model.RecipeDetail;
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

public class ScarpAdapter extends RecyclerView.Adapter<ScarpAdapter.ViewHolder> {

    Context context;
    ArrayList<Posting> postingArrayList;

    SimpleDateFormat sf;
    SimpleDateFormat df;








    public ScarpAdapter(Context context, ArrayList<Posting> postingArrayList) {
        this.context = context;
        this.postingArrayList = postingArrayList;


        sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        df = new SimpleDateFormat("yyyy년MM월dd일 HH:mm");
        sf.setTimeZone(TimeZone.getTimeZone("UTC"));
        df.setTimeZone(TimeZone.getDefault());
    }

    @NonNull
    @Override
    public ScarpAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_recipe_row, parent, false);
        return new ScarpAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Posting posting = postingArrayList.get(position);

        // Glide를 사용하여 이미지 설정
        Glide.with(context).load(posting.imageURL).into(holder.thumbRecipe);

        holder.txtRecipeTitle.setText(posting.title);
        // 닉네임 설정
        holder.txtNickname.setText(posting.nickname);

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

        // 좋아요 처리
        if( posting.isFavorite == 1){
            holder.btnScrap.setImageResource(R.drawable.bookmark_24px_color);
        }else {
            holder.btnScrap.setImageResource(R.drawable.bookmark_24px);
        }

    }

    @Override
    public int getItemCount() {
        // 아이템 수 반환
        return postingArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // ViewHolder 내부 뷰들 설정
        ImageView thumbRecipe;
        TextView txtRecipeTitle;
        TextView txtNickname;
        TextView txtUploadDate;

        ImageView btnScrap;
        TextView txtRating;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // ViewHolder 내부 뷰들 초기화
            thumbRecipe = itemView.findViewById(R.id.thumbRecipe);
            txtRecipeTitle = itemView.findViewById(R.id.txtRecipeTitle);
            txtNickname = itemView.findViewById(R.id.txtNickname);
            txtUploadDate = itemView.findViewById(R.id.txtUploadDate);
            btnScrap = itemView.findViewById(R.id.btnScrap);
            txtRating = itemView.findViewById(R.id.txtRating);
            cardView = itemView.findViewById(R.id.cardView);

            btnScrap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int index = getAdapterPosition();
//                    Log.i("AAAAAAAAAAAAA", index+"");
                    for (Posting posting : postingArrayList){
                        Log.i("AAAAAAAAAAA", posting.postingId+"");
                        Log.i("AAAAAAAAAAA", posting.nickname+"");
                        Log.i("AAAAAAAAAAA", posting.title+"");
                    }
                    Posting posting = postingArrayList.get(index);
//                    Log.i("AAAAAAAAAAAAAAAAAAA", posting.postingid+"");
                    Retrofit retrofit = NetworkClient.getRetrofitClient(context);

                    FavoritesApi api = retrofit.create(FavoritesApi.class);

                    SharedPreferences sp = context.getSharedPreferences(Config.PREFERENCE_NAME, Context.MODE_PRIVATE);
                    String token = sp.getString("token", "");
                    token = "Bearer " + token;

                    if (posting.isFavorite == 1) {
                        // 좋아요 해지 API

                        Call<Res> call = api.deleteFavorite(posting.postingId, token);

                        call.enqueue(new Callback<Res>() {
                            @Override
                            public void onResponse(Call<Res> call, Response<Res> response) {
                                if(response.isSuccessful()){
                                    // 클릭된 아이템을 목록에서 삭제
                                    posting.isFavorite = 0;
                                    postingArrayList.remove(index);
                                    notifyItemRemoved(index);



//                                     로컬 저장소에 삭제된 아이템의 ID 저장

//                                    sp.edit().putBoolean("deleted_" + posting.postingid, true).apply();

                                }else {}
                            }

                            @Override
                            public void onFailure(Call<Res> call, Throwable t) {


                            }
                        });

                    }

                    // onClick 분계지점

                }
            });
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = getAdapterPosition();
                    Posting posting = postingArrayList.get(index);

                    Intent intent = new Intent(context, RecipeActivity.class);
                    intent.putExtra("postingId", posting.postingId);
                    intent.putExtra("userId", posting.userId);
                    Log.i("AAA", "포스팅과 유저 ID " + posting.postingId +"   " +  posting.userId);
                    ((ScarpActivity) context).startActivity(intent);
                }
            });


        }

    }
}


