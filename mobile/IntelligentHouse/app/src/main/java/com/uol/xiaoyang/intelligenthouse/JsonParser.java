package com.uol.xiaoyang.intelligenthouse;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;


public class JsonParser {
    final String TAG = "JsonParser.java";
    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";

    public JSONObject getJSONFromUrl(String url) {

        try {
            //create new HTTP client for a get post
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);

            HttpResponse httpResponse = httpClient.execute(httpGet);
            JSONObject jObj = processResponse(httpResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jObj;
    }

    public JSONObject getJSONFromPut(String url){
        try {
            //create new HTTP client for a get post
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPut httpPut = new HttpPut(url);

            HttpResponse httpResponse = httpClient.execute(httpPut);
            JSONObject jObj = processResponse(httpResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jObj;

    }

    public JSONObject processResponse(HttpResponse httpResponse){
        try{
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }catch (ClientProtocolException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

        try{
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = new String();

            while ((line = bufferedReader.readLine()) != null){
                sb.append(line);
            }
            is.close();
            json = sb.toString();
        }catch (Exception e){
            Log.e(TAG, "Error converting result " + e.toString());
        }

        try{
            jObj = new JSONObject(json);
        }catch (JSONException e){
            Log.e(TAG, "Error parsing data " + e.toString());
        }

        return jObj;
    }


}
