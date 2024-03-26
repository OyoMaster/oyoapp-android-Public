    package com.sunny.oyoapp;

    import androidx.annotation.NonNull;
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

    import android.media.ExifInterface;
    import android.net.Uri;
    import android.os.Bundle;
    import android.os.Environment;
    import android.os.ParcelFileDescriptor;
    import android.provider.MediaStore;
    import android.provider.OpenableColumns;
    import android.util.Log;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.view.View;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.ProgressBar;
    import android.widget.TextView;
    import android.widget.Toast;

    import com.google.android.gms.common.util.IOUtils;
    import com.sunny.oyoapp.api.NetworkClient;
    import com.sunny.oyoapp.api.RecipeApi;
    import com.sunny.oyoapp.config.Config;
    import com.sunny.oyoapp.model.Recipe;
    import com.sunny.oyoapp.model.Res;


    import java.io.File;
    import java.io.FileInputStream;
    import java.io.FileOutputStream;
    import java.io.IOException;
    import java.io.OutputStream;
    import java.text.SimpleDateFormat;
    import java.util.Date;

    import okhttp3.MediaType;
    import okhttp3.MultipartBody;
    import okhttp3.RequestBody;
    import retrofit2.Call;
    import retrofit2.Callback;
    import retrofit2.Response;
    import retrofit2.Retrofit;

    public class AddRecipeActivity extends AppCompatActivity {

        TextView txtTitle;
        EditText editTitle;

        TextView btnUploadImg;
        TextView txtImageFile;

        TextView txtIngredients;
        EditText editIngredients;

        TextView txtRecipe;
        EditText editRecipe;

        private File photoFile;



        String imgurl;




        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_recipe);

            getSupportActionBar().setTitle("레시피 작성");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back_ios);

            txtTitle = findViewById(R.id.txtTitle);
            editTitle = findViewById(R.id.editTitle);
            btnUploadImg = findViewById(R.id.btnUploadImg);
            txtImageFile = findViewById(R.id.txtImageFile);
            txtIngredients = findViewById(R.id.txtIngredients);
            editIngredients = findViewById(R.id.editIngredients);
            txtRecipe = findViewById(R.id.txtRecipe);
            editRecipe = findViewById(R.id.editRecipe);

            Intent intent = getIntent();
            if (intent != null) {
                String title = intent.getStringExtra("title");
                String ingredients = intent.getStringExtra("ingredients");
                String recipe = intent.getStringExtra("recipe");

                // Set values in EditText fields
                editTitle.setText(title);
                editIngredients.setText(ingredients);
                editRecipe.setText(recipe);
            }

            // photoFile을 생성하는 부분을 여기로 이동
            String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            photoFile = getPhotoFile(fileName);

            btnUploadImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialog();
                }
            });

            btnUploadImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialog();
                }
            });
        }

        private void showDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(AddRecipeActivity.this);
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
                    AddRecipeActivity.this, android.Manifest.permission.CAMERA);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AddRecipeActivity.this,
                        new String[]{android.Manifest.permission.CAMERA},
                        1000);
                Toast.makeText(AddRecipeActivity.this, "카메라 권한 필요합니다.",
                        Toast.LENGTH_SHORT).show();
                return;
            } else {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (i.resolveActivity(AddRecipeActivity.this.getPackageManager()) != null) {

                    // 사진의 파일명을 만들기
                    String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    photoFile = getPhotoFile(fileName);

                    // todo : 패키지명을, 현재의 프로젝트에 맞게 수정해야 함.
                    Uri fileProvider = FileProvider.getUriForFile(AddRecipeActivity.this,
                            "com.sunny.oyoapp.fileprovider", photoFile);
                    i.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
                    startActivityForResult(i, 100);



                } else {
                    Toast.makeText(AddRecipeActivity.this, "이폰에는 카메라 앱이 없습니다.",
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
                        Toast.makeText(AddRecipeActivity.this, "권한 허가 되었음",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AddRecipeActivity.this, "아직 승인하지 않았음",
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case 500: {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(AddRecipeActivity.this, "권한 허가 되었음",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AddRecipeActivity.this, "아직 승인하지 않았음",
                                Toast.LENGTH_SHORT).show();
                    }

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

                // txtImageFile에 파일 이름 설정
                String fileName = photoFile.getName();
                txtImageFile.setText(fileName);

                imgurl = photoFile.getAbsolutePath();

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
                    imgurl = photoFile.getAbsolutePath();

                    // txtImageFile에 파일 이름 설정
                    txtImageFile.setText(fileName);

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
            if (ActivityCompat.shouldShowRequestPermissionRationale(AddRecipeActivity.this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Log.i("DEBUGGING5", "true");
                Toast.makeText(AddRecipeActivity.this, "권한 수락이 필요합니다.",
                        Toast.LENGTH_SHORT).show();
            } else {
                Log.i("DEBUGGING6", "false");
                ActivityCompat.requestPermissions(AddRecipeActivity.this,
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
            int result = ContextCompat.checkSelfPermission(AddRecipeActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (result == PackageManager.PERMISSION_DENIED) {
                return false;
            } else {
                return true;
            }
        }


        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.save, menu);
            return true;
        }

        // 눌렀을 때 새로운 액티비티
        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
            int itemId = item.getItemId();

            if (itemId == R.id.btnSave) {
                showAlertDialog();
            }else if (itemId == android.R.id.home) {
                showExitConfirmationDialog();
                return true;  // 이벤트를 소비했음을 나타내기 위해 true 반환
            }

            return super.onOptionsItemSelected(item);
        }

        private void showExitConfirmationDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(AddRecipeActivity.this);
            builder.setTitle("작성취소");
            builder.setMessage("작성 중인 내용이 저장되지 않을 수 있습니다. 정말로 나가시겠습니까?");
            builder.setNegativeButton("아니오", null);
            builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();  // 액티비티 종료
                }
            });

            builder.show();
        }
        private void showAlertDialog(){
            AlertDialog.Builder builder = new AlertDialog.Builder(AddRecipeActivity.this);
            builder.setTitle("레시피 저장");
            builder.setMessage("레시피를 저장하시겠습니까?");
            builder.setNegativeButton("아니오", null);
            builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String title = editTitle.getText().toString().trim();
                    String recipe = editRecipe.getText().toString().trim();
                    String ingredients=editIngredients.getText().toString().trim();
                    String imageURL = imgurl;



                    if(title.isEmpty() || recipe.isEmpty() ){
                        Toast.makeText(AddRecipeActivity.this,
                                "항목을 모두 입력하세요.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    showProgress();

                    Retrofit retrofit = NetworkClient.getRetrofitClient(AddRecipeActivity.this);

                    RecipeApi api = retrofit.create(RecipeApi.class);

                    // 토큰 가져온다.
                    SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                    String token = sp.getString("token", "");
                    token = "Bearer " + token;

                    // body 에 보낼 json 을, 자바의 객체로 생성
                    Recipe recipelist = new Recipe(title,imageURL,ingredients,recipe);


                    // 이미지 파일을 RequestBody로 변환
                    RequestBody photoBody = RequestBody.create(MediaType.parse("image/*"), photoFile);
                    MultipartBody.Part photoPart = MultipartBody.Part.createFormData("photo", photoFile.getName(), photoBody);

                    // 나머지 텍스트 데이터를 RequestBody로 변환
                    RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), title);
                    RequestBody ingredientsBody = RequestBody.create(MediaType.parse("text/plain"), ingredients);
                    RequestBody recipeBody = RequestBody.create(MediaType.parse("text/plain"), recipe);

                    // API 호출
                    Call<Res> call = api.add(token, photoPart, titleBody, ingredientsBody, recipeBody);



                    call.enqueue(new Callback<Res>() {
                        @Override
                        public void onResponse(Call<Res> call, Response<Res> response) {
                            dismissProgress();

                            if(response.isSuccessful()){

                                Log.i("AAAA","EE");
                                finish();
                                return;


                            }else{
                                // 유저한테 알리고
                                return;
                            }

                        }

                        @Override
                        public void onFailure(Call<Res> call, Throwable t) {
                            dismissProgress();
                        }
                    });


                }
            });


            builder.show();
        }
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



