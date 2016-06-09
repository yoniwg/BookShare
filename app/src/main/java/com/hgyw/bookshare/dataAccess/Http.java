package com.hgyw.bookshare.dataAccess;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Created by haim7 on 27/05/2016.
 */
public class Http {

    private static final String UTF_8 = StandardCharsets.UTF_8.name();

    /**
     * Get http
     */
    public static String get(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        return readAllOfHttpUrlConnection(con);
    }

    public static String get(String urlString, Map<String,?> params) throws IOException {
        String parametersString = Stream.of(params.entrySet())
                .map(kv -> encodeUrl(kv.getKey()) + '=' + encodeUrl(kv.getValue().toString()))
                .collect(Collectors.joining("&"));
        return get(urlString + "?" + parametersString);
    }

    /**
     * Post http
     */
    public static String post(String urlString, Map<String,?> params) throws IOException {
        String parametersString = Stream.of(params.entrySet())
                .map(kv -> encodeUrl(kv.getKey()) + '=' + encodeUrl(kv.getValue().toString()))
                .collect(Collectors.joining("&"));
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");

        // For POST only - START
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write(parametersString.getBytes(UTF_8));
        os.flush();
        os.close();
        // For POST only - END

        return readAllOfHttpUrlConnection(con);
    }

    /**
     * read all output of http connection
     * @param con an {@link HttpURLConnection} object
     * @return String of connection output
     * @throws HttpRetryException if response code is not equals to {@link HttpURLConnection#HTTP_OK}.
     * @throws IOException on I/O error
     */
    private static String readAllOfHttpUrlConnection(HttpURLConnection con) throws IOException {
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) response.append(inputLine);
            in.close();
            return response.toString();
        } else {
            throw new HttpRetryException("Error on post Http. (code " + responseCode + ")", responseCode);
        }
    }

    /**
     * Encode text to uri string for post http
     */
    private static String encodeUrl(String text) {
        try {
            return URLEncoder.encode(text, UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new InternalError("Should not occurs because the charset is legal");
        }
    }
}
