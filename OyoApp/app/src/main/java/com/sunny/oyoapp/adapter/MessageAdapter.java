package com.sunny.oyoapp.adapter;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sunny.oyoapp.ChatbotActivity;
import com.sunny.oyoapp.R;
import com.sunny.oyoapp.YoutubeActivity;
import com.sunny.oyoapp.api.NetworkClient;
import com.sunny.oyoapp.api.UserApi;
import com.sunny.oyoapp.config.Config;
import com.sunny.oyoapp.model.Message;
import com.sunny.oyoapp.model.UserInfoRes;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    Context context;
    List<Message> messageList;
    private UserInfoRes userInfoRes;




    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatbot_row, null);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messageList.get(position);

        if (message.getSentBy().equals(Message.SENT_BY_ME)) {
            holder.left_chat_view.setVisibility(View.GONE);
            holder.right_chat_view.setVisibility(View.VISIBLE);
            holder.right_chat_tv.setText(message.getMessage());


            Retrofit retrofit = NetworkClient.getRetrofitClient(context);

            UserApi api = retrofit.create(UserApi.class);

            SharedPreferences sp = context.getSharedPreferences(Config.PREFERENCE_NAME,MODE_PRIVATE);
            String token = sp.getString("token", "");

            token = "Bearer " + token;

            Call<UserInfoRes> call  = api.getUserInfo(token);

            call.enqueue(new Callback<UserInfoRes>(){

                @Override
                public void onResponse(Call<UserInfoRes> call, Response<UserInfoRes> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        userInfoRes = response.body(); // 사용자 정보를 변수에 할당
                        if (userInfoRes.items != null) {
                            UserInfoRes.UserItem userItem = userInfoRes.items;
                            if (userItem.profileUrl != null) {
                                Glide.with(context)
                                        .load(userItem.profileUrl)
                                        .into(holder.userProfile);
                            }
                            Log.d("Glide", "Profile URL: " + userInfoRes.items.profileUrl);
                            holder.userNickname.setText(userInfoRes.items.nickname);
                        }
                    } else {
                        // 실패 응답 처리
                    }
                }

                @Override
                public void onFailure(Call<UserInfoRes> call, Throwable t) {
                    // 실패 처리
                }
            });

        } else {
            holder.right_chat_view.setVisibility(View.GONE);
            holder.left_chat_view.setVisibility(View.VISIBLE);
            holder.left_chat_tv.setText(message.getMessage());

            if (message.getMessage().contains("음식:")) {
                holder.left_chat_tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String foodMessage = message.getMessage();
                        int startIdx = foodMessage.indexOf("음식:") + 4; // "음식:" 다음 인덱스부터
                        int endIdx = foodMessage.indexOf("소개:"); // "소개:" 전까지
                        if (startIdx != -1 && endIdx != -1) { // "음식:"와 "소개:"가 둘 다 발견되면
                            String foodName = foodMessage.substring(startIdx, endIdx).trim();
                            Intent intent = new Intent((ChatbotActivity) context, YoutubeActivity.class);
                            intent.putExtra("foodName", foodName); // 음식 값을 Intent에 추가
                            context.startActivity(intent); // YoutubeActivity 시작
                        }
                    }
                });
            }
        }

    }




    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView left_chat_view, right_chat_view;


        CircleImageView userProfile;
        TextView userNickname;
        TextView left_chat_tv, right_chat_tv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            left_chat_view = itemView.findViewById(R.id.left_chat_view);
            right_chat_view = itemView.findViewById(R.id.right_chat_view);

            left_chat_tv = itemView.findViewById(R.id.left_chat_tv);
            right_chat_tv = itemView.findViewById(R.id.right_chat_tv);
            userProfile = itemView.findViewById(R.id.userProfile);
            userNickname = itemView.findViewById(R.id.userNickname);






        }
    }
}
