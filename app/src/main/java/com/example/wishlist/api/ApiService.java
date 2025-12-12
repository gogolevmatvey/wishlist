package com.example.wishlist.api;

import com.example.wishlist.models.Category;
import com.example.wishlist.models.Wish;
import com.example.wishlist.models.ApiResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

import java.util.List;
import java.util.Map;

public interface ApiService {

    // ========== КАТЕГОРИИ ==========
    @GET("api/api_categories.php")
    Call<List<Category>> getCategories();

    // ========== ЖЕЛАНИЯ ==========
    @GET("api/api_wishes.php")
    Call<List<Wish>> getWishes(
            @Query("category_id") Integer categoryId,
            @Query("status") String status
    );

    @GET("api/api_wishes.php")
    Call<Wish> getWishById(@Query("id") int id);

    @POST("api/api_wishes.php")
    Call<ApiResponse> createWish(@Body Wish wish);

    @PUT("api/api_wishes.php")
    Call<ApiResponse> updateWish(@Body Wish wish);

    @DELETE("api/api_wishes.php")
    Call<ApiResponse> deleteWish(@Query("id") int id);

    // ========== ИЗОБРАЖЕНИЯ ==========
    @Multipart
    @POST("api/upload_image.php")
    Call<Map<String, Object>> uploadImage(@Part MultipartBody.Part image);
}