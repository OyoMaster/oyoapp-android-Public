package com.sunny.oyoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sunny.oyoapp.api.NetworkClient;
import com.sunny.oyoapp.api.UserApi;
import com.sunny.oyoapp.config.Config;
import com.sunny.oyoapp.model.User;
import com.sunny.oyoapp.model.UserRes;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RegisterActivity extends AppCompatActivity {

    EditText editEmail;
    TextView btnDuplicateCheck;
    EditText editName;
    EditText editNickname;
    EditText editPassword;
    EditText editPasswordCheck;
    EditText editIdNum1;
    EditText editIdNum2;

    CheckBox ckbAllAgree;
    CheckBox checkBox1;
    CheckBox checkBox2;
    CheckBox checkBox3;
    CheckBox checkBox4;

    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setTitle("회원가입");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back_ios);

        editEmail = findViewById(R.id.editEmail);
        btnDuplicateCheck = findViewById(R.id.btnDuplicateCheck);
        editName = findViewById(R.id.editName);
        editNickname = findViewById(R.id.editNickname);
        editPassword = findViewById(R.id.editPassword);
        editPasswordCheck = findViewById(R.id.editPasswordCheck);
        editIdNum1 = findViewById(R.id.editIdNum1);
        editIdNum2 = findViewById(R.id.editIdNum2);
        ckbAllAgree = findViewById(R.id.ckbAllAgree);
        checkBox1 = findViewById(R.id.checkBox1);
        checkBox2 = findViewById(R.id.checkBox2);
        checkBox3 = findViewById(R.id.checkBox3);
        checkBox4 = findViewById(R.id.checkBox4);
        btnRegister = findViewById(R.id.btnRegister);

        ckbAllAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = ckbAllAgree.isChecked();
                checkBox1.setChecked(isChecked);
                checkBox2.setChecked(isChecked);
                checkBox3.setChecked(isChecked);
                checkBox4.setChecked(isChecked);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editEmail.getText().toString().trim();
                String username = editName.getText().toString().trim();
                String nickname = editNickname.getText().toString().trim();
                String password = editPassword.getText().toString().trim();
                String passwordCheck = editPasswordCheck.getText().toString().trim();
                String idNum1 = editIdNum1.getText().toString().trim();
                String idNum2 = editIdNum2.getText().toString().trim();
                String age= idNum1+"-"+idNum2;

                if(email.isEmpty()|| username.isEmpty()|| nickname.isEmpty() || password.isEmpty() || passwordCheck.isEmpty()){
                    Toast.makeText(RegisterActivity.this,"항목을 모두 입력하세요.",Toast.LENGTH_SHORT).show();

                }
                // 이메일 형식이 맞는 지 체크
                Pattern pattern = Patterns.EMAIL_ADDRESS;
                if(pattern.matcher(email).matches() == false){
                    Toast.makeText(RegisterActivity.this,"이메일을 정확히 입력하세요.",Toast.LENGTH_SHORT).show();
                    return;
                }
                // 비밀번호 길이 체크 4~12자까지만 허용.
                // 아닌 것을 먼저 기준으로
                if(password.length() < 4 || password.length() > 13 ){
                    Toast.makeText(RegisterActivity.this,"비밀번호 길이를 확인하세요.",Toast.LENGTH_SHORT).show();
                    return;
                }

                showProgress();

                Retrofit retrofit = NetworkClient.getRetrofitClient(RegisterActivity.this);

                UserApi api = retrofit.create(UserApi.class);

                User user = new User(email, password, nickname, age, username);

                Call<UserRes> call = api.register(user);

                call.enqueue(new Callback<UserRes>() {
                    @Override
                    public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                        // 0-1. 다이얼로그 없애기
                        dismissProgress();

                        // 200 OK
                        if(response.isSuccessful()){
                            UserRes userRes = response.body();

                            SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME,MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("token",userRes.access_token);
                            editor.apply();

                            // 이상 없으면 액티비티 실행.
                            Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<UserRes> call, Throwable t) {
                        // 0-1. 다이얼로그 없애기
                        dismissProgress();
                        // 유저한테 네트워크 통신 실패했다고 알려준다.
                        Toast.makeText(RegisterActivity.this, "네트워크 통신 실패.", Toast.LENGTH_SHORT).show();
                        Log.e("Network Failure", t.getMessage()); // Log the error message

                    }
                });



            }
        });
    }



    // 네트워크로 데이터 처리할 때 사용할 다이얼로그
    Dialog dialog;

    private void showProgress(){
        dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(new ProgressBar(this));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void dismissProgress(){
        dialog.dismiss();
    }
}