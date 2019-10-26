package com.example.singularium;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface RetrofitInterface {
    @Multipart
    @POST("/getText")
    Call<ress> uploadImage(@Part MultipartBody.Part file, @Part("name") RequestBody name, @Part("filename") RequestBody filename);
}