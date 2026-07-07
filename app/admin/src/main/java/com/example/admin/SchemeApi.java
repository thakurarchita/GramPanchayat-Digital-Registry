package com.example.admin;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;
import okhttp3.ResponseBody;

public interface SchemeApi {

    @GET("get_all_schemes.php")
    Call<SchemeResponse> getSchemes();

    @GET("get_scheme.php")
    Call<SchemeDetailResponse> getScheme(@Query("scheme_id") String schemeId);

    @POST("add_scheme.php")
    @Headers({"Content-Type: application/json"})
    Call<ResponseBody> addScheme(@Body Scheme scheme);

    @POST("update_scheme.php")
    @Headers({"Content-Type: application/json"})
    Call<ResponseBody> updateScheme(@Body Scheme scheme);

    @POST("delete_scheme.php")
    @Headers({"Content-Type: application/json"})
    Call<ResponseBody> deleteScheme(@Body DeleteRequest request);
}

// Response wrapper classes
class SchemeResponse {
    public boolean success;
    public String message;
    public List<Scheme> data;
}

class SchemeDetailResponse {
    public boolean success;
    public String message;
    public Scheme data;
}

class DeleteRequest {
    public String scheme_id;

    public DeleteRequest(String scheme_id) {
        this.scheme_id = scheme_id;
    }
}