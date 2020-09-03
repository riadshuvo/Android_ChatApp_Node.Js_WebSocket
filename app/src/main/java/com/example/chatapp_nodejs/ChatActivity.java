package com.example.chatapp_nodejs;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatActivity extends AppCompatActivity implements TextWatcher {

    private String name;
    private WebSocket webSocket;
    private String SERVER_PATH = "ws://192.168.0.106:3000";
    private EditText messageEdit;
    private View sendButton, pickImageBtn;
    private RecyclerView recyclerView;
    private int IMAGE_REQUEST_ID = 1;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        name = getIntent().getStringExtra("name");
        initiateSocketConnection();
    }

    private void initiateSocketConnection() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(SERVER_PATH).build();
        webSocket = client.newWebSocket(request, new SocketListener());
    }


    private class SocketListener extends WebSocketListener{

        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            super.onOpen(webSocket, response);
            
            runOnUiThread(()->{
                Toast.makeText(ChatActivity.this,"Socket Connetion Successful!", Toast.LENGTH_SHORT).show();
                initializeView();
            });
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            super.onMessage(webSocket, text);
            /**
             * Here We Will Accept The Meggase and Will be Shown Into RecyclerView
             */

            runOnUiThread(() -> {

                try {
                    JSONObject jsonObject = new JSONObject(text);
                    jsonObject.put("isSent", false);

                    messageAdapter.addItem(jsonObject);

                    /**
                     * Make RecyclerView Scrollable Autometically
                     */

                    recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            });

        }


    }

    private void initializeView() {

        messageEdit = findViewById(R.id.messageEdit);
        sendButton = findViewById(R.id.sendBtn);
        pickImageBtn = findViewById(R.id.pickImgBtn);
        recyclerView = findViewById(R.id.recyclerView);

        messageAdapter = new MessageAdapter(getLayoutInflater());
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        /**
         * When EditText Field is Empty then the Image Icon Will be Shown
         * Otherwise SendButton icon will be shown
         */
        messageEdit.addTextChangedListener(this);

        sendButton.setOnClickListener(v ->{

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("name",name);
                jsonObject.put("message",messageEdit.getText().toString());

                webSocket.send(jsonObject.toString());
                jsonObject.put("isSent", true);
                messageAdapter.addItem(jsonObject);

                /**
                 * Make RecyclerView Scrollable Autometically
                 */

                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);

                resetMessageEdit();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        pickImageBtn.setOnClickListener(v->{

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                startActivityForResult(Intent.createChooser(intent,"Pick Image"),IMAGE_REQUEST_ID);
            }

        });

    }

    /**
     * This onActivityResult() Method Is For getting permission from phone to Pick Images
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_REQUEST_ID && resultCode == RESULT_OK){

            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                Bitmap image = BitmapFactory.decodeStream(inputStream);
                sendImage(image);

            } catch (FileNotFoundException e) {

            }

        }

    }

    private void sendImage(Bitmap image) {


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);

        String base64String = Base64.encodeToString(outputStream.toByteArray(),
                Base64.DEFAULT);

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("name", name);
            jsonObject.put("image", base64String);

            webSocket.send(jsonObject.toString());

            jsonObject.put("isSent", true);
            messageAdapter.addItem(jsonObject);

            /**
             * Make RecyclerView Scrollable Autometically
             */

            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    /**
     * When EditText Field is Empty then the Image Icon Will be Shown
     * Otherwise SendButton icon will be shown
     */
    @Override
    public void afterTextChanged(Editable editable) {
        String string  = editable.toString().trim();

        if(string.isEmpty()){

            resetMessageEdit();

        }else{
            sendButton.setVisibility(View.VISIBLE);
            pickImageBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void resetMessageEdit() {

        messageEdit.removeTextChangedListener(this);
        messageEdit.setText("");

        sendButton.setVisibility(View.INVISIBLE);
        pickImageBtn.setVisibility(View.VISIBLE);

        messageEdit.addTextChangedListener(this);

    }

}