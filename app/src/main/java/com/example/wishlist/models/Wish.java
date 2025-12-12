package com.example.wishlist.models;

import com.example.wishlist.R;
import com.example.wishlist.utils.DateUtils;
import com.google.gson.annotations.SerializedName;

public class Wish {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("category_id")
    private Integer categoryId;

    @SerializedName("category_name")
    private String categoryName;

    @SerializedName("estimated_price")
    private Double estimatedPrice;

    @SerializedName("store_url")
    private String storeUrl;

    @SerializedName("priority")
    private String priority;

    @SerializedName("status")
    private String status;

    @SerializedName("image_path")
    private String imagePath;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Конструкторы
    public Wish() {}

    public Wish(String title, String description, Integer categoryId,
                Double estimatedPrice, String priority) {
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.estimatedPrice = estimatedPrice;
        this.priority = priority;
        this.status = "wish";
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Double getEstimatedPrice() { return estimatedPrice; }
    public void setEstimatedPrice(Double estimatedPrice) { this.estimatedPrice = estimatedPrice; }

    public String getStoreUrl() { return storeUrl; }
    public void setStoreUrl(String storeUrl) { this.storeUrl = storeUrl; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // Вспомогательные методы
    public String getFormattedPrice() {
        if (estimatedPrice == null) return "Цена не указана";
        return String.format("%.2f руб.", estimatedPrice);
    }

    public int getPriorityColorResId() {
        if (priority == null) return R.color.priority_medium;
        switch (priority) {
            case "high": return R.color.priority_high;
            case "low": return R.color.priority_low;
            default: return R.color.priority_medium;
        }
    }

    public int getStatusColorResId() {
        if (status == null) return R.color.status_wish;
        switch (status) {
            case "purchased": return R.color.status_purchased;
            case "cancelled": return R.color.status_cancelled;
            default: return R.color.status_wish;
        }
    }

    public String getStatusText() {
        if (status == null) return "Желаю";
        switch (status) {
            case "wish": return "Желаю";
            case "purchased": return "Куплено";
            case "cancelled": return "Отменено";
            default: return status;
        }
    }

    public String getPriorityText() {
        if (priority == null) return "Средний";
        switch (priority) {
            case "high": return "Высокий";
            case "low": return "Низкий";
            default: return "Средний";
        }
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null || createdAt.isEmpty()) {
            return "";
        }
        return DateUtils.formatDate(createdAt);
    }
}