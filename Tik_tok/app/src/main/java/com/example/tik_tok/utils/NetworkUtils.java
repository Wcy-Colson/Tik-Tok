package com.example.tik_tok.utils;


import com.example.tik_tok.bean.FeedResponse;
import com.example.tik_tok.bean.PostVideoResponse;
import com.example.tik_tok.newtork.IMiniDouyinService;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * @author Xavier.S
 * @date 2019.01.15 13:27
 */
public class NetworkUtils {

    public static String getResponseWithHttpURLConnection(String url) {
        String result = null;
        InputStream in = null;
        HttpURLConnection urlConnection = null;
        try {
            URL netUrl = new URL(url);
            urlConnection = (HttpURLConnection) netUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            in = new BufferedInputStream(urlConnection.getInputStream());
            result = readStream(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

//    public static Call<List<Cat>> getResponseWithRetrofitAsync() {
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(ICatService.HOST)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        return retrofit.create(ICatService.class).getRandomCats();
//    }

    public static Call<PostVideoResponse> getResponseWithRetrofitAsyncPost(
            @Query("student_id") String studentId,
            @Query("user_name") String userName,
            @Part MultipartBody.Part image,
            @Part MultipartBody.Part video
    ) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(IMiniDouyinService.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(IMiniDouyinService.class).createVideo(studentId, userName, image, video);
    }

    public static Call<FeedResponse> getResponseWithRetrofitAsyncFeed(
    ) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(IMiniDouyinService.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(IMiniDouyinService.class).fetchFeed();
    }

    private static String readStream(final InputStream inputStream) {
        final Scanner scanner = new Scanner(inputStream);
        scanner.useDelimiter("\\A");
        final String data = scanner.next();
        return data;
    }

    private static String readStreamBuffer(InputStream in) {
        StringBuilder result = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String s;
            while ((s = reader.readLine()) != null) {
                result.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
