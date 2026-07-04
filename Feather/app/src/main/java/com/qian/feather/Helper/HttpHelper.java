package com.qian.feather.Helper;

import android.util.Log;

import androidx.annotation.NonNull;

import com.qian.feather.item.Msg;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpHelper {
    private OkHttpClient client;

    public HttpHelper(OkHttpClient okHttpClient) {
       client = okHttpClient;
    }

    public void sendGetRequest() {
        String url = "http://www.example.com";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    // 处理响应数据
                }
            }
        });
    }


    /**
     *
     * @param msg
     * note that this method must be called after the Client#sendMsg(Msg msg)
     * or the AESKey,VectorIV,encryptedContent may not have been initialized
     */
    public void sendPostRequest(Msg msg) {
        JSONObject jsonObject = getJsonObjectForMsg(msg);
        String postBody = "key1=value1&key2=value2"; // 或者使用 FormBody, RequestBody 等构造更复杂的请求体
        RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("http://115.29.241.155:8081/featherapp/api/msg/send")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()) {
                    String responseData = response.body().string();
                    System.out.println("The Server responses succeed"+responseData);
                } else {
                    System.out.println("The Server responses failed"+response.code());
                }
            }
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("The Server responses failed"+e.toString());
            }
        });
    }

    private JSONObject getJsonObjectForMsg(Msg msg) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("from",msg.getFrom().trim());
            jsonObject.put("to",msg.getTo().trim());
            jsonObject.put("when",msg.getWhen());
            jsonObject.put("type",msg.getType());
            jsonObject.put("state",msg.getState());
            jsonObject.put("AESKey",msg.getAESKey());
            jsonObject.put("VectorIV",msg.getVectorIV());
            jsonObject.put("content",msg.getContent());
        } catch (JSONException e) {
            Log.e("HttpHelper",e.toString());
        } finally {
            return jsonObject;
        }
    }

}
