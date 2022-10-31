package com.giddie.wisedigits;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface WiseDigitsApiService {
    @Multipart
    @POST("/sandbox/index.php/")
    Call<ResponseModel> createUser(
            @Part MultipartBody.Part avatar,
            @Part("name") RequestBody name,
            @Part("email") RequestBody email,
            @Part("password") RequestBody password
            );
}
