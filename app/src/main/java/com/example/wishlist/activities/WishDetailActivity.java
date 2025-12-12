package com.example.wishlist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.example.wishlist.R;
import com.example.wishlist.api.ApiService;
import com.example.wishlist.api.RetrofitClient;
import com.example.wishlist.models.Wish;
import com.example.wishlist.models.ApiResponse;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WishDetailActivity extends AppCompatActivity {

    private ImageView ivImage;
    private TextView tvTitle, tvDescription, tvCategory, tvPrice, tvStoreUrl, tvPriority, tvStatus, tvCreatedAt;
    private Wish currentWish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish_detail);

        initViews();
        setupToolbar();

        // Получаем данные из Intent
        if (getIntent().hasExtra("wish")) {
            String wishJson = getIntent().getStringExtra("wish");
            currentWish = new Gson().fromJson(wishJson, Wish.class);
            displayWishDetails();
        } else {
            finish();
        }
    }

    private void initViews() {
        ivImage = findViewById(R.id.ivImage);
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvCategory = findViewById(R.id.tvCategory);
        tvPrice = findViewById(R.id.tvPrice);
        tvStoreUrl = findViewById(R.id.tvStoreUrl);
        tvPriority = findViewById(R.id.tvPriority);
        tvStatus = findViewById(R.id.tvStatus);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Детали желания");
        }
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void displayWishDetails() {
        if (currentWish == null) return;

        tvTitle.setText(currentWish.getTitle());

        if (currentWish.getDescription() != null && !currentWish.getDescription().isEmpty()) {
            tvDescription.setText(currentWish.getDescription());
            tvDescription.setVisibility(View.VISIBLE);
        } else {
            tvDescription.setVisibility(View.GONE);
        }

        if (currentWish.getCategoryName() != null && !currentWish.getCategoryName().isEmpty()) {
            tvCategory.setText(currentWish.getCategoryName());
            tvCategory.setVisibility(View.VISIBLE);
        } else {
            tvCategory.setVisibility(View.GONE);
        }

        tvPrice.setText(currentWish.getFormattedPrice());
        tvPriority.setText(currentWish.getPriorityText());
        tvStatus.setText(currentWish.getStatusText());
        tvStatus.setBackgroundResource(getStatusBackground(currentWish.getStatus()));

        if (currentWish.getStoreUrl() != null && !currentWish.getStoreUrl().isEmpty()) {
            tvStoreUrl.setText(currentWish.getStoreUrl());
            tvStoreUrl.setVisibility(View.VISIBLE);
        } else {
            tvStoreUrl.setVisibility(View.GONE);
        }

        if (currentWish.getCreatedAt() != null) {
            tvCreatedAt.setText("Добавлено: " + formatDate(currentWish.getCreatedAt()));
        }

        // Загрузка изображения
        if (currentWish.getImageUrl() != null && !currentWish.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentWish.getImageUrl())
                    .placeholder(R.drawable.ic_photo_placeholder)
                    .error(R.drawable.ic_broken_image)
                    .into(ivImage);
        } else {
            ivImage.setImageResource(R.drawable.ic_photo_placeholder);
        }

        // Обработчик клика по ссылке
        tvStoreUrl.setOnClickListener(v -> {
            if (currentWish.getStoreUrl() != null && !currentWish.getStoreUrl().isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        android.net.Uri.parse(currentWish.getStoreUrl()));
                startActivity(browserIntent);
            }
        });
    }

    private int getStatusBackground(String status) {
        if (status == null) return R.drawable.bg_status_wish;
        switch (status) {
            case "purchased": return R.drawable.bg_status_purchased;
            case "cancelled": return R.drawable.bg_status_cancelled;
            default: return R.drawable.bg_status_wish;
        }
    }

    private String formatDate(String dateString) {
        try {
            // Простое форматирование даты
            return dateString.replace("T", " ").substring(0, 16);
        } catch (Exception e) {
            return dateString;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_wish_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_edit) {
            editWish();
            return true;
        } else if (id == R.id.action_delete) {
            deleteWish();
            return true;
        } else if (id == R.id.action_share) {
            shareWish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void editWish() {
        Intent intent = new Intent(this, AddEditWishActivity.class);
        intent.putExtra("wish", new Gson().toJson(currentWish));
        startActivityForResult(intent, AddEditWishActivity.REQUEST_EDIT_WISH);
    }

    private void deleteWish() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Удаление")
                .setMessage("Удалить \"" + currentWish.getTitle() + "\"?")
                .setPositiveButton("Удалить", (dialog, which) -> confirmDelete())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void confirmDelete() {
        ApiService apiService = RetrofitClient.getApiService();
        apiService.deleteWish(currentWish.getId()).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().isSuccess()) {

                    setResult(RESULT_OK);
                    finish();
                } else {
                    new MaterialAlertDialogBuilder(WishDetailActivity.this)
                            .setTitle("Ошибка")
                            .setMessage("Не удалось удалить желание")
                            .setPositiveButton("OK", null)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                new MaterialAlertDialogBuilder(WishDetailActivity.this)
                        .setTitle("Ошибка сети")
                        .setMessage("Проверьте подключение к интернету")
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private void shareWish() {
        String shareText = currentWish.getTitle() + "\n" +
                "Цена: " + currentWish.getFormattedPrice() + "\n" +
                (currentWish.getStoreUrl() != null ?
                        "Ссылка: " + currentWish.getStoreUrl() : "");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Мое желание: " + currentWish.getTitle());

        startActivity(Intent.createChooser(shareIntent, "Поделиться желанием"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // Обновить данные если нужно
            setResult(RESULT_OK);
            finish();
        }
    }
}