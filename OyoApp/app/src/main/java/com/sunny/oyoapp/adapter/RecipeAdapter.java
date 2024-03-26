package com.sunny.oyoapp.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.sunny.oyoapp.MainActivity;
import com.sunny.oyoapp.R;
import com.sunny.oyoapp.RecipeActivity;
import com.sunny.oyoapp.model.Posting;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    Context context;
    ArrayList<Posting> postingArrayList;

    SimpleDateFormat sf;
    SimpleDateFormat df;





    public RecipeAdapter(Context context, ArrayList<Posting> postingArrayList) {
        this.context = context;
        this.postingArrayList = postingArrayList;

        sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        df = new SimpleDateFormat("yyyy년MM월dd일 HH:mm");
        sf.setTimeZone(TimeZone.getTimeZone("UTC"));
        df.setTimeZone(TimeZone.getDefault());
    }

    @NonNull
    @Override
    public RecipeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recipe_main_row, parent, false);
        return new RecipeAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Posting posting = postingArrayList.get(position);

        if(posting.imageURL == null && posting.imageURL == ""){

            Glide.with(context).load(R.drawable.no_image).into(holder.thumbRecipe);
        }else {

            // Glide를 사용하여 이미지 설정
            Glide.with(context).load(posting.imageURL).into(holder.thumbRecipe);
        }

        // 닉네임 설정
        holder.txtNickname.setText(posting.nickname);

        // 레시피 내용 설정
        holder.txtRecipeView.setText(posting.title);

    }

    @Override
    public int getItemCount() {
        // 아이템 수 반환
        return postingArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // ViewHolder 내부 뷰들 설정
        ImageView thumbRecipe;
        ImageView imageView6;
        TextView txtNickname;
        ImageView btnScrap;
        TextView txtRecipeView;
        Posting posting;

        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // ViewHolder 내부 뷰들 초기화
            thumbRecipe = itemView.findViewById(R.id.thumbRecipe);
            imageView6 = itemView.findViewById(R.id.imageView6);
            txtNickname = itemView.findViewById(R.id.txtNickname);
            btnScrap = itemView.findViewById(R.id.btnScrap);
            txtRecipeView = itemView.findViewById(R.id.txtRecipeView);
            cardView = itemView.findViewById(R.id.cardView);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = getAdapterPosition();
                    Posting posting = postingArrayList.get(index);

                    Intent intent = new Intent(context, RecipeActivity.class);
                    intent.putExtra("postingId", posting.postingid);
                    intent.putExtra("userId", posting.userId);
                    Log.i("AAA", "포스팅과 유저 ID " + posting.postingid + "  " +posting.userId);
                    ((MainActivity) context).startActivity(intent);
                }
            });
        }
    }


}

