package com.example.wishlist.models;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("id")
    private Integer id;

    @SerializedName("error")
    private String error;

    // Геттеры и сеттеры
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}