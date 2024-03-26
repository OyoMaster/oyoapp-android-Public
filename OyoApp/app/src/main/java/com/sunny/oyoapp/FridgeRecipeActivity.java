package com.sunny.oyoapp;

import static com.sunny.oyoapp.config.Config.MY_SECRET_KEY;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.sunny.oyoapp.adapter.MessageAdapter;

import com.sunny.oyoapp.model.Message;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FridgeRecipeActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText editMsg;
    ImageView btnSend;



    List<Message> messageList;
    MessageAdapter messageAdapter;

    private String extractedTitle;
    private String extractedIngredients;
    private String extractedRecipe;

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(300, TimeUnit.SECONDS) // 연결 Timeout 30초로 설정
            .readTimeout(300, TimeUnit.SECONDS) // 읽기 Timeout 30초로 설정
            .writeTimeout(300, TimeUnit.SECONDS) // 쓰기 Timeout 30초로 설정
            .build();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fridge_recipe);

        getSupportActionBar().setTitle("음식 레시피봇");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back_ios);

        recyclerView = findViewById(R.id.recyclerView); // RecyclerView 초기화
        editMsg = findViewById(R.id.editMsg); // EditText 초기화
        btnSend = findViewById(R.id.btnSend); // ImageView 초기화

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(manager);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        recyclerView.setAdapter(messageAdapter);


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // JSON 형식의 문자열을 생성하고 이스케이프 처리해야 합니다.
                String jsonPrompt = "{\n" +
                        "  \"title\": \"\",\n" +
                        "  \"ingredients\": [\n" +
                        "  ],\n" +
                        "  \"recipe\": [\n" +
                        "  ]\n" +
                        "}";

                String question = editMsg.getText().toString().trim();
                addToChat(question, Message.SENT_BY_ME);
                editMsg.setText("");
                // 프롬프트에만 jsonPrompt 추가하여 프롬프트를 줄입니다.
                callAPIWithRecipeQuestion(question/*+"제목,재료,레시피로된 json형식으로 대답 해줘"*/);
            }
        });

    }

    void callAPIWithRecipeQuestion(String question) {
        // "레시피 알려줘" 형식인지 확인
        if (question.contains(" 레시피 알려줘")) {
            // "레시피 알려줘" 부분 추출
            String foodName = question.replace("레시피 알려줘", "").trim();

            // OpenAI API 호출
            String apiQuestion = question + "제목,재료,레시피로된 json형식으로만 대답해줘 꼭 json형식을 지켜서 대답 해줘";

            // OpenAI API 호출
            callAPI(apiQuestion);
        } else {
            // 다른 형식의 질문에 대한 처리 로직 추가
            addResponse("죄송합니다. '~~ 레시피 알려줘' 형식으로만 레시피를 제공할 수 있습니다.");
        }
    }

    void addToChat(String message, String sentBy) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message, sentBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
                // 추가된 메시지를 로그로 출력
                Log.d("ChatMessage", "Message: " + message + ", Sent By: " + sentBy);
            }
        });
    }

    void addResponse(String response) {
        messageList.remove(messageList.size() - 1);
        addToChat(response, Message.SENT_BY_BOT);
        // 응답 메시지를 로그로 출력
        Log.d("ChatResponse", "Response: " + response);
    }


    void callAPI(String question) {
        // okhttp
        messageList.add(new Message("...", Message.SENT_BY_BOT));

        JSONObject object = new JSONObject();
        try {
            object.put("model", "gpt-3.5-turbo-instruct");
            object.put("prompt", question);
            object.put("max_tokens", 2000);
            object.put("temperature", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(object.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/completions")
                .header("Authorization", "Bearer " + MY_SECRET_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                String errorMessage = "응답을 불러오지 못했습니다. 오류: " + e.getMessage();
                Log.e("API Failure", errorMessage);
                addResponse(errorMessage);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getString("text");
                        Log.i("AAA", result);
                        processRecipeResponse(result.trim());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("JSON Parsing", "JSON 파싱 오류: " + e.getMessage());
                    }
                } else {
                    String errorMessage = "응답을 불러오지 못했습니다. 오류: " + response.body().string();
                    Log.e("API Response", errorMessage);
                    addResponse(errorMessage);
                }
            }
        });
    }

    void processRecipeResponse(String response) {
        try {
            // 응답이 JSON 객체인지 확인
            if (response.startsWith("{")) {
                // JSON 객체인 경우
                JSONObject recipeJson = new JSONObject(response);

                // 기존 코드는 그대로 유지
                JSONObject recipeInfo = new JSONObject();
                recipeInfo.put("title", recipeJson.optString("title"));
                recipeInfo.put("ingredients", extractIngredients(recipeJson.optJSONArray("ingredients")));
                recipeInfo.put("recipe", extractRecipeSteps(recipeJson.optJSONArray("recipe")));

                Log.i("Recipe JSON", recipeInfo.toString());

                // 추출된 레시피 정보를 전역 변수에 저장
                extractedTitle = recipeInfo.optString("title");
                extractedIngredients = recipeInfo.optString("ingredients");
                extractedRecipe = recipeInfo.optString("recipe");

                // 채팅창에 응답을 추가
                addResponse("레시피를 찾았습니다!");
                addToChat("레시피 이름: " + extractedTitle, Message.SENT_BY_BOT);
                addToChat("재료: " + extractedIngredients, Message.SENT_BY_BOT);

                StringBuilder recipeStepsBuilder = new StringBuilder();
                JSONArray recipeStepsArray = recipeJson.optJSONArray("recipe");
                for (int i = 0; i < recipeStepsArray.length(); i++) {
                    String step = recipeStepsArray.getString(i);
                    step = step.replaceAll("\\[|\\]|\\d+\\.\\s+", "").trim(); // 대괄호, 숫자, 마침표 제거
                    step = (i + 1) + ". " + step; // 순서와 마침표 추가
                    recipeStepsBuilder.append(step); // 단계 추가
                    if (i < recipeStepsArray.length() - 1) {
                        recipeStepsBuilder.append("\n"); // 줄 바꾸기
                    }
                }
                addToChat("레시피:\n" + recipeStepsBuilder.toString(), Message.SENT_BY_BOT);
            } else {
                // JSON 형식이 아닌 경우 무시
                addResponse("OpenAI API로부터 유효한 응답을 받지 못했습니다.");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON Parsing", "JSON 파싱 오류: " + e.getMessage());
        }
    }

    private String extractIngredients(JSONArray jsonArray) {
        StringBuilder ingredientsBuilder = new StringBuilder();

        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                ingredientsBuilder.append(jsonArray.optString(i));
                if (i < jsonArray.length() - 1) {
                    ingredientsBuilder.append(", ");
                }
            }
        }

        return ingredientsBuilder.toString();
    }

    private List<String> extractRecipeSteps(JSONArray jsonArray) {
        List<String> recipeSteps = new ArrayList<>();

        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                String step = jsonArray.optString(i);
                // '['와 ']'를 제거하고 문자열을 추가
                step = step.replaceAll("\\[|\\]", "").trim();
                recipeSteps.add(step);
            }
        }

        return recipeSteps;
    }

    void handleSaveButtonClick() {
        // messageList에서 레시피 정보 추출
        if (!messageList.isEmpty()) {
            Message lastMessage = messageList.get(messageList.size() - 1);
            String botResponse = lastMessage.getMessage();

            // botResponse를 처리하여 레시피 정보 추출
            processRecipeResponse(botResponse);

            // MainActivity2 시작 및 추출된 레시피 정보 전달
            Intent intent = new Intent(FridgeRecipeActivity.this, AddRecipeActivity.class);
            intent.putExtra("title", extractedTitle);
            intent.putExtra("ingredients", extractedIngredients);
            intent.putExtra("recipe", extractedRecipe);
            startActivity(intent);
        } else {
            addResponse("저장할 레시피 정보가 없습니다.");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



    // 눌렀을 때 새로운 액티비티
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();

        if(itemId == R.id.btnScrapAt){

            handleSaveButtonClick();
        }

        return super.onOptionsItemSelected(item);
    }
}