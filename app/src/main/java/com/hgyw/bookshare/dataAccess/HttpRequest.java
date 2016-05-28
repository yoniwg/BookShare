package com.hgyw.bookshare.dataAccess;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
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
public class HttpRequest extends AsyncTask{

    public final static String POST = "POST";
    public final static String GET = "GET";

    private final String type;
    private final URL url;
    private final Map<String,String> request;

    byte[] postDataBytes;
    private HttpURLConnection connection;
    private Exception exception;

    public HttpRequest(URL url, HashMap request, String type) {
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

    public String getStringReply() throws ExecutionException, InterruptedException, IOException {
        StringBuilder toReturn = new StringBuilder();
        BufferedReader br = (BufferedReader) get();
        String lineBuffer = null;
        while ((lineBuffer = br.readLine()) != null){
            toReturn.append(lineBuffer);
        }
        return toReturn.toString();
    }

    public BufferedReader getBufferedReply() throws ExecutionException, InterruptedException {
        return (BufferedReader) get();
    }

    @Override
    protected Object doInBackground(Object[] params) {
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(type);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            connection.setDoOutput(true);
            connection.getOutputStream().write(postDataBytes);
            return new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8"));
        } catch (IOException e) {
            this.exception = e;
        }
        return null;
    }

    public Exception getException() {
        return exception;
    }
}
