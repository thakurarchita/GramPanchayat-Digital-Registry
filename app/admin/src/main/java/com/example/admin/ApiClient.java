package com.example.admin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;

public class ApiClient {
    private static final String BASE_URL = "http://192.168.147.188/grampanchayat_api/";
    private static Retrofit retrofit = null;

    public static SchemeApi getClient() {
        if (retrofit == null) {
            // Add interceptor to clean response
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = chain.request();
                            Response response = chain.proceed(request);

                            // Get response body as string
                            String body = response.body().string();

                            // Clean the response - remove trailing vvv and any non-JSON characters
                            body = body.trim();
                            body = body.replaceAll("vvv$", "");
                            body = body.replaceAll("[\\x00-\\x1F]+$", "");

                            // Create new response with cleaned body
                            ResponseBody newBody = ResponseBody.create(
                                    response.body().contentType(),
                                    body
                            );

                            return response.newBuilder()
                                    .body(newBody)
                                    .build();
                        }
                    })
                    .build();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }
        return retrofit.create(SchemeApi.class);
    }
}