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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.sunny.oyoapp.api.NetworkClient;
import com.sunny.oyoapp.api.UserApi;
import com.sunny.oyoapp.config.Config;
import com.sunny.oyoapp.model.User;
import com.sunny.oyoapp.model.UserRes;

import java.util.regex.Pattern;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {

    TextView txtLogin;

    EditText editEmail;
    EditText editPassword;
    Button btnLogin;
    Button btnNaverLogin;
    Button btnKakaoLogin;
    Button btnGoogleLogin;
    Button btnRegister;

    private static final String TAG = "LoginActivity";

    private static final int RC_SIGN_IN = 123;
    private GoogleSignInClient mGoogleSignInClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        txtLogin = findViewById(R.id.txtLogin);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnNaverLogin = findViewById(R.id.btnNaverLogin);
        btnKakaoLogin = findViewById(R.id.btnKakaoLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnRegister = findViewById(R.id.btnRegister);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editEmail.getText().toString().trim();
                String password = editPassword.getText().toString().trim();

                if(email.isEmpty() || password.isEmpty()){
                    Toast.makeText(LoginActivity.this,
                            "항목을 모두 입력하세요.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Pattern pattern = Patterns.EMAIL_ADDRESS;
                if(pattern.matcher(email).matches() == false){
                    Toast.makeText(LoginActivity.this,
                            "이메일 형식을 확인하세요.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if(password.length() < 4 || password.length() > 12){
                    Toast.makeText(LoginActivity.this,
                            "비번 길이를 확인하세요.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                showProgress();

                Retrofit retrofit = NetworkClient.getRetrofitClient(LoginActivity.this);

                UserApi api = retrofit.create(UserApi.class);

                User user = new User(email, password);

                Call<UserRes> call = api.login(user);

                call.enqueue(new Callback<UserRes>() {
                    @Override
                    public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                        dismissProgress();

                        if(response.isSuccessful()){
                            UserRes userRes = response.body();

                            SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("token", userRes.access_token);
                            editor.apply();

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);

                            finish();

                        } else if(response.code() == 400){

                        } else if(response.code() == 401){

                        } else if(response.code() == 500){

                        }

                    }

                    @Override
                    public void onFailure(Call<UserRes> call, Throwable t) {

                        dismissProgress();
                    }
                });

            }
        });


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        btnGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();
                signIn();
            }
        });

        btnKakaoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showProgress();

                Function2<OAuthToken, Throwable, Unit> callback = new  Function2<OAuthToken, Throwable, Unit>() {
                    @Override
                    public Unit invoke(OAuthToken oAuthToken, Throwable throwable) {

                        // 이때 토큰이 전달이 되면 로그인이 성공한 것이고 토큰이 전달되지 않았다면 로그인 실패
                        if(oAuthToken != null) {
                            SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("token", oAuthToken.getAccessToken());
                            editor.apply();

                            // 로그인 성공 시 MainActivity로 화면 전환
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish(); // 현재 LoginActivity 종료

                        }
                        if (throwable != null) {
                            // 로그인 실패 처리
                            Log.e(TAG, "Kakao login failed", throwable);
                            // 실패에 대한 처리 로직 추가

                        }

                        updateKakaoLoginUi();
                        return null;
                    }
                };

                if(UserApiClient.getInstance().isKakaoTalkLoginAvailable(LoginActivity.this)) {
                    UserApiClient.getInstance().loginWithKakaoTalk(LoginActivity.this, callback);
                }else {
                    UserApiClient.getInstance().loginWithKakaoAccount(LoginActivity.this, callback);
                }




            }
        });
    }
    // 구글 로그인
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            String email = account.getEmail();
            String username = account.getDisplayName();
            String nickname = "google user"+username;
            String profileUrl = account.getPhotoUrl() != null ?
                    account.getPhotoUrl().toString() : "";
            Log.d(TAG, "Email: " + email);
            Log.d(TAG, "Display Name: " + username);
            Log.d(TAG, "Photo URL: " + profileUrl);

            Retrofit retrofit = NetworkClient.getRetrofitClient(LoginActivity.this);

            UserApi api = retrofit.create(UserApi.class);

            User user = new User(email,nickname,profileUrl,username);

            Call<UserRes> call = api.googlelogin(user);

            call.enqueue(new Callback<UserRes>() {
                @Override
                public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                    dismissProgress();

                    if(response.isSuccessful()){
                        UserRes userRes = response.body();

                        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("token", userRes.access_token);
                        editor.apply();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);

                        finish();

                    } else if(response.code() == 400){

                    } else if(response.code() == 401){

                    } else if(response.code() == 500){

                    }

                }

                @Override
                public void onFailure(Call<UserRes> call, Throwable t) {

                    dismissProgress();
                }
            });

        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            // 로그인 실패 처리
        }
    }

    private void startMainActivity(UserRes userRes) {
        SharedPreferences sp =
                getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("token",userRes.access_token);
        editor.apply();

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("",userRes.access_token);
        startActivity(intent);
        finish();
    }
    // 구글 로그인

    //카카오 로그인
    private  void updateKakaoLoginUi(){
        UserApiClient.getInstance().me(new Function2<com.kakao.sdk.user.model.User, Throwable, Unit>() {
            @Override
            public Unit invoke(com.kakao.sdk.user.model.User user, Throwable throwable) {
                // 로그인이 되어있으면
                if (user!=null){

                    String username = user.getKakaoAccount().getProfile().getNickname();
                    String nickname = "kakao user"+username;
                    String email = username + "@kakao.com";
                    String profileUrl = user.getKakaoAccount().getProfile().getProfileImageUrl()!= null ?
                            user.getKakaoAccount().getProfile().getProfileImageUrl() : "";


                    // 유저의 어카운트정보에 이메일
                    Log.d(TAG,"invoke: nickname " + nickname);
                    // 유저의 어카운트 정보의 프로파일에 닉네임
                    Log.d(TAG,"invoke: email " + email);
                    Log.d(TAG,"invoke: profileUrl " + profileUrl);

                    Retrofit retrofit = NetworkClient.getRetrofitClient(LoginActivity.this);

                    UserApi api = retrofit.create(UserApi.class);

                    User kakaouser = new User(email,nickname,profileUrl,username);

                    Call<UserRes> call = api.kakaologin(kakaouser);

                    call.enqueue(new Callback<UserRes>() {
                        @Override
                        public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                            dismissProgress();

                            if(response.isSuccessful()){
                                UserRes userRes = response.body();

                                SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("token", userRes.access_token);
                                editor.apply();

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();

                            } else if(response.code() == 400){

                            } else if(response.code() == 401){

                            } else if(response.code() == 500){

                            }

                        }

                        @Override
                        public void onFailure(Call<UserRes> call, Throwable t) {
                            dismissProgress();
                        }
                    });

                }else {

                }
                return null;
            }
        });
    }

    // 네트워크로 데이터 처리할 때 사용할 다이얼로그(복붙)
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