package com.hgyw.bookshare.dataAccess;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by Yoni on 5/27/2016.
 */
public class HttpAsync extends AsyncTask<Void,Void,String> {

    public final static String POST = "POST";
    public final static String GET = "GET";

    private final String type;
    private final URL url;
    private final Map<String,String> request;

    byte[] postDataBytes;
    private HttpURLConnection connection;
    private Exception exception;

    public HttpAsync(URL url, Map<String,String> request, String type) {
        this.url = url;
        this.request = request;
        this.type = type;
    }

    public void buildPostRequest() throws UnsupportedEncodingException {
        StringBuilder postData = new StringBuilder("&");
        for (Map.Entry<String,String> param : request.entrySet()) {
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(param.getValue(), "UTF-8"));
        }
        postDataBytes = postData.toString().getBytes("UTF-8");
    }

    public void sendRequest() throws UnsupportedEncodingException {
        buildPostRequest();
        execute();
    }

    public String getReply() throws ExecutionException, InterruptedException, IOException {
        String toReturn = get();
        if (exception != null){
            throw new RuntimeException(exception);
        }
        return toReturn;
    }


    @Override
    protected String doInBackground(Void[] params) {
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(type);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            os.write(postDataBytes);
            os.flush();
            os.close();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                StringBuilder toReturn = new StringBuilder();
                String lineBuffer;
                while ((lineBuffer = br.readLine()) != null){
                    toReturn.append(lineBuffer);
                }
                br.close();
                return toReturn.toString();
            } else {
                throw new HttpRetryException("Error on post Http. (code " + responseCode + ")", responseCode);
            }
        } catch (IOException e) {
            this.exception = e;
            return null;
        }
    }

    public Exception getException() {
        return exception;
    }
}
