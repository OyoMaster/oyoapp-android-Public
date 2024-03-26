package com.sunny.oyoapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.common.util.IOUtils;
import com.bumptech.glide.Glide;
import com.sunny.oyoapp.api.NetworkClient;
import com.sunny.oyoapp.api.UserApi;
import com.sunny.oyoapp.config.Config;

import com.sunny.oyoapp.model.User;
import com.sunny.oyoapp.model.UserInfoRes;
import com.sunny.oyoapp.model.UserRes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class UpdateUserActivity extends AppCompatActivity {

    CircleImageView ivProfile;
    TextView txtNickname;
    TextView txtId;
    EditText editNickname;
    TextView btnDuplicateCheck;
    TextView btnIdUpdate;
    EditText editPassword;
    EditText editPasswordCheck;
    Button btnUpdateProfile;
    TextView txtDeleteUser;

    String token;


    // 사진 파일!!
    private File photoFile;

    String imgurl;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);

        getSupportActionBar().setTitle("회원정보 관리");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back_ios);

        ivProfile = findViewById(R.id.ivProfile);
        txtNickname = findViewById(R.id.txtNickname);
        txtId = findViewById(R.id.txtId);
        editNickname = findViewById(R.id.editNickname);
        btnDuplicateCheck = findViewById(R.id.btnDuplicateCheck);
        btnIdUpdate = findViewById(R.id.btnIdUpdate);
        editPassword = findViewById(R.id.editPassword);
        editPasswordCheck = findViewById(R.id.editPasswordCheck);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        txtDeleteUser = findViewById(R.id.txtDeleteUser);

        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDialog();

            }
        });

        Retrofit retrofit = NetworkClient.getRetrofitClient(UpdateUserActivity.this);

        UserApi api = retrofit.create(UserApi.class);

        SharedPreferences sp = UpdateUserActivity.this.getSharedPreferences(Config.PREFERENCE_NAME,MODE_PRIVATE);
        token = sp.getString("token", "");

        token = "Bearer " + token;

        Call<UserInfoRes> call  = api.getUserInfo(token);

        call.enqueue(new Callback<UserInfoRes>(){

            @Override
            public void onResponse(Call<UserInfoRes> call, Response<UserInfoRes> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // API 응답이 성공적으로 수행되었을 때

                    UserInfoRes userInfoRes = response.body();

                    if (userInfoRes.items != null) {
                        // 사용자 정보가 존재하면 사용자 정보 가져오기
                        UserInfoRes.UserItem userItem = userInfoRes.items;

                        // Log를 통해 사용자 정보 확인
                        Log.d("UpdateUserActivity", "User Nickname: " + userItem.nickname);

                        // 사용자 닉네임 설정

                        Glide.with(UpdateUserActivity.this).load(userItem.profileUrl).into(ivProfile);
                        Glide.with(UpdateUserActivity.this)
                                .asFile()
                                .load(userItem.profileUrl)
                                .into(new CustomTarget<File>() {
                                    @Override
                                    public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                                        // 이미지 다운로드가 완료되면 photoFile을 설정
                                        photoFile = resource;
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {
                                        // 아무런 동작이 필요하지 않음
                                    }
                                });

                        txtNickname.setText(userItem.nickname);
                        txtId.setText(userItem.email);
                        editNickname.setText(userItem.nickname);



                        // Log를 통해 txtNickname의 값 확인
                        Log.d("UpdateUserActivity", "txtNickname value: " + txtNickname.getText().toString());
                    } else {
                        // 사용자 정보가 비어있을 경우에 대한 처리
                        txtNickname.setText("사용자 정보가 없습니다.");
                    }
                } else {
                    // API 응답이 실패했을 때
                    txtNickname.setText("외안되");

                    // Log를 통해 실패한 응답 확인
                    Log.e("UpdateUserActivity", "API 응답 실패: " + response.message());
                }

            }

            @Override
            public void onFailure(Call<UserInfoRes> call, Throwable t) {

            }
        });

        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nickname = editNickname.getText().toString().trim();
                String password = editPassword.getText().toString().trim();


                // 비밀번호 길이 체크 4~12자까지만 허용.
                // 아닌 것을 먼저 기준으로
                if(password.length() < 4 || password.length() > 13 ){
                    Toast.makeText(UpdateUserActivity.this,"비밀번호 길이를 확인하세요.",Toast.LENGTH_SHORT).show();
                    return;
                }

                showProgress();

                Retrofit retrofit = NetworkClient.getRetrofitClient(UpdateUserActivity.this);

                UserApi api = retrofit.create(UserApi.class);

                SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME,MODE_PRIVATE);
                String token = sp.getString("token","");
                token = "Bearer " + token;

                // 폼데이터로 보낼 파일

                if (photoFile != null) {
                    RequestBody fileBody = RequestBody.create(photoFile, MediaType.parse("image/jpg"));
                    MultipartBody.Part photo = MultipartBody.Part.createFormData("photo", photoFile.getName(), fileBody);

                    // 나머지 코드는 그대로 유지
                    RequestBody textBody1 = RequestBody.create(nickname, MediaType.parse("text/plain"));
                    RequestBody textBody2 = RequestBody.create(password, MediaType.parse("text/plain"));

                    Call<UserRes> call = api.updateUserInfo(token, photo, textBody1, textBody2);

                    call.enqueue(new Callback<UserRes>() {
                        @Override
                        public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                            dismissProgress();
                            if (response.isSuccessful()) {
                                finish();
                            } else {
                                // 처리 실패 시 동작
                            }
                        }

                        @Override
                        public void onFailure(Call<UserRes> call, Throwable t) {
                            dismissProgress();
                            // 에러 처리 동작
                        }
                    });
                } else {
                    // photoFile이 null일 때의 처리
                    Log.e("UpdateUserActivity", "photoFile is null");
                    // 또는 특별한 처리를 원하는 경우 예외를 던질 수 있음
                    throw new IllegalStateException("photoFile is null");
                }
            }

        });
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UpdateUserActivity.this);
        builder.setTitle(R.string.alert_title);
        builder.setItems(R.array.alert_photo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (i == 0) {
                    // 첫번째 항목 : 카메라 실행
                    camera();
                } else if (i == 1) {
                    // 두번째 항목 : 앨범에서 선택
                    album();
                }

            }
        });
        builder.show();
    }

    private void camera() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                UpdateUserActivity.this, android.Manifest.permission.CAMERA);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UpdateUserActivity.this,
                    new String[]{android.Manifest.permission.CAMERA},
                    1000);
            Toast.makeText(UpdateUserActivity.this, "카메라 권한 필요합니다.",
                    Toast.LENGTH_SHORT).show();
            return;
        } else {
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (i.resolveActivity(UpdateUserActivity.this.getPackageManager()) != null) {

                // 사진의 파일명을 만들기
                String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                photoFile = getPhotoFile(fileName);

                // todo : 패키지명을, 현재의 프로젝트에 맞게 수정해야 함.
                Uri fileProvider = FileProvider.getUriForFile(UpdateUserActivity.this,
                        "com.sunny.oyoapp.fileprovider", photoFile);
                i.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
                startActivityForResult(i, 100);



            } else {
                Toast.makeText(UpdateUserActivity.this, "이폰에는 카메라 앱이 없습니다.",
                        Toast.LENGTH_SHORT).show();
            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(UpdateUserActivity.this, "카메라 권한 허가 되었음",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UpdateUserActivity.this, "카메라 권한이 필요합니다.",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case 500: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(UpdateUserActivity.this, "앨범 권한 허가 되었음",
                            Toast.LENGTH_SHORT).show();
                    displayFileChoose();
                } else {
                    Toast.makeText(UpdateUserActivity.this, "앨범 권한이 필요합니다.",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private File getPhotoFile(String fileName) {
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            return File.createTempFile(fileName, ".jpg", storageDirectory);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Bitmap photo = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(photoFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            photo = rotateBitmap(photo, orientation);

            // 압축시킨다. 해상도 낮춰서
            OutputStream os;
            try {
                os = new FileOutputStream(photoFile);
                photo.compress(Bitmap.CompressFormat.JPEG, 50, os);
                os.flush();
                os.close();
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "비트맵 쓰기 오류", e);
            }

            saveBitmapToFile(photo);


            imgurl = photoFile.getAbsolutePath();
            Glide.with(UpdateUserActivity.this).load(imgurl).into(ivProfile);



            // 네트워크로 데이터 보낸다.
        } else if (requestCode == 300 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri albumUri = data.getData();
            String fileName = getFileName(albumUri);
            try {
                ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(albumUri, "r");
                if (parcelFileDescriptor == null) return;
                FileInputStream inputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
                photoFile = new File(this.getCacheDir(), fileName);
                FileOutputStream outputStream = new FileOutputStream(photoFile);
                IOUtils.copyStream(inputStream, outputStream);

                // 압축시킨다. 해상도 낮춰서
                Bitmap photo = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                OutputStream os;
                try {
                    os = new FileOutputStream(photoFile);
                    photo.compress(Bitmap.CompressFormat.JPEG, 60, os);
                    os.flush();
                    os.close();
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "비트맵 쓰기 오류", e);
                }
                saveBitmapToFile(photo);
                ivProfile.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imgurl = photoFile.getAbsolutePath();

                Glide.with(UpdateUserActivity.this).load(imgurl).into(ivProfile);


                // 네트워크로 보낸다.
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void saveBitmapToFile(Bitmap bitmap) {
        try {
            OutputStream os = new FileOutputStream(photoFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "비트맵 쓰기 오류", e);
        }
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    private void album() {
        if (checkPermission()) {
            displayFileChoose();
        } else {
            requestPermission();
        }
    }


    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(UpdateUserActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.i("DEBUGGING5", "true");
            Toast.makeText(UpdateUserActivity.this, "권한 수락이 필요합니다.",
                    Toast.LENGTH_SHORT).show();
        } else {
            Log.i("DEBUGGING6", "false");
            ActivityCompat.requestPermissions(UpdateUserActivity.this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 500);
        }
    }

    private void displayFileChoose() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "SELECT IMAGE"), 300);
    }

    //앨범에서 선택한 사진이름 가져오기
    public String getFileName(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        try {
            if (cursor == null) return null;
            cursor.moveToFirst();
            @SuppressLint("Range") String fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            cursor.close();
            return fileName;

        } catch (Exception e) {
            e.printStackTrace();
            cursor.close();
            return null;
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(UpdateUserActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    Dialog dialog;

    void showProgress(){
        dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(new ProgressBar(this));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    void dismissProgress(){
        dialog.dismiss();
    }

}