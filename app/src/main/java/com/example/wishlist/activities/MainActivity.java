package com.example.wishlist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.wishlist.R;
import com.example.wishlist.adapters.WishAdapter;
import com.example.wishlist.api.ApiService;
import com.example.wishlist.api.RetrofitClient;
import com.example.wishlist.models.Wish;
import com.example.wishlist.models.ApiResponse;
import com.example.wishlist.utils.NetworkUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements WishAdapter.OnWishClickListener, SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView recyclerView;
    private WishAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private FloatingActionButton fabAdd;

    private List<Wish> wishList = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();

        apiService = RetrofitClient.getApiService();

        // Проверка сети и загрузка данных
        if (NetworkUtils.isNetworkAvailable(this)) {
            loadWishes();
        } else {
            showNoInternetError();
        }
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        fabAdd = findViewById(R.id.fabAdd);
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Мой Wishlist");
        }
    }

    private void setupRecyclerView() {
        adapter = new WishAdapter(wishList, this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(this);

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditWishActivity.class);
            startActivityForResult(intent, AddEditWishActivity.REQUEST_ADD_WISH);
        });

        // Повторная попытка при клике на пустое состояние
        tvEmpty.setOnClickListener(v -> {
            if (NetworkUtils.isNetworkAvailable(this)) {
                loadWishes();
            }
        });
    }

    private void loadWishes() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        apiService.getWishes(null, null).enqueue(new Callback<List<Wish>>() {
            @Override
            public void onResponse(Call<List<Wish>> call, Response<List<Wish>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    wishList.clear();
                    wishList.addAll(response.body());
                    adapter.updateWishes(wishList);

                    if (wishList.isEmpty()) {
                        showEmptyState();
                    }
                } else {
                    showError("Ошибка загрузки данных");
                }
            }

            @Override
            public void onFailure(Call<List<Wish>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                showError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void showEmptyState() {
        tvEmpty.setText("Список желаний пуст\nДобавьте первое желание!");
        tvEmpty.setVisibility(View.VISIBLE);
    }

    private void showNoInternetError() {
        tvEmpty.setText("Нет подключения к интернету\nНажмите для повтора");
        tvEmpty.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        if (wishList.isEmpty()) {
            tvEmpty.setText("Ошибка загрузки\nНажмите для повтора");
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }

    // ========== Обработчики кликов по элементам ==========

    @Override
    public void onWishClick(Wish wish) {
        Intent intent = new Intent(this, WishDetailActivity.class);
        intent.putExtra("wish", new Gson().toJson(wish));
        startActivity(intent);
    }

    @Override
    public void onWishLongClick(Wish wish) {
        Toast.makeText(this, "Долгое нажатие: " + wish.getTitle(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditClick(Wish wish) {
        Intent intent = new Intent(this, AddEditWishActivity.class);
        intent.putExtra("wish", new Gson().toJson(wish));
        startActivityForResult(intent, AddEditWishActivity.REQUEST_EDIT_WISH);
    }

    @Override
    public void onDeleteClick(Wish wish) {
        // Подтверждение удаления
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Удаление")
                .setMessage("Удалить \"" + wish.getTitle() + "\"?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteWish(wish))
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public void onStatusChangeClick(Wish wish) {
        // Изменение статуса
        String[] statuses = {"wish", "purchased", "cancelled"};
        String[] statusNames = {"Желаю", "Куплено", "Отменено"};

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Изменить статус")
                .setItems(statusNames, (dialog, which) -> {
                    wish.setStatus(statuses[which]);
                    updateWishStatus(wish);
                })
                .show();
    }

    private void deleteWish(Wish wish) {
        int position = wishList.indexOf(wish);
        if (position == -1) return;

        apiService.deleteWish(wish.getId()).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().isSuccess()) {

                    adapter.removeWish(position);
                    Toast.makeText(MainActivity.this,
                            "Удалено успешно", Toast.LENGTH_SHORT).show();

                    if (wishList.isEmpty()) {
                        showEmptyState();
                    }
                } else {
                    Toast.makeText(MainActivity.this,
                            "Ошибка удаления", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWishStatus(Wish wish) {
        apiService.updateWish(wish).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().isSuccess()) {

                    int position = wishList.indexOf(wish);
                    adapter.updateWish(position, wish);
                    Toast.makeText(MainActivity.this,
                            "Статус обновлен", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "Ошибка обновления", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRefresh() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            loadWishes();
        } else {
            swipeRefreshLayout.setRefreshing(false);
            showNoInternetError();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_filter) {
            showFilterDialog();
            return true;
        } else if (id == R.id.action_stats) {
            showStatistics();
            return true;
        } else if (id == R.id.action_settings) {
            showSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showFilterDialog() {
        String[] filterOptions = getResources().getStringArray(R.array.filter_options);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Фильтр")
                .setItems(filterOptions, (dialog, which) -> {
                    String status = null;
                    switch (which) {
                        case 1: status = "wish"; break;
                        case 2: status = "purchased"; break;
                        case 3: status = "cancelled"; break;
                    }
                    filterByStatus(status);
                })
                .show();
    }

    private void filterByStatus(String status) {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getWishes(null, status).enqueue(new Callback<List<Wish>>() {
            @Override
            public void onResponse(Call<List<Wish>> call, Response<List<Wish>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    wishList.clear();
                    wishList.addAll(response.body());
                    adapter.updateWishes(wishList);

                    if (wishList.isEmpty()) {
                        showEmptyState();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Wish>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Ошибка фильтрации",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showStatistics() {
        int total = wishList.size();
        int purchased = 0;
        int wished = 0;
        int cancelled = 0;

        for (Wish wish : wishList) {
            if ("purchased".equals(wish.getStatus())) purchased++;
            else if ("wish".equals(wish.getStatus())) wished++;
            else if ("cancelled".equals(wish.getStatus())) cancelled++;
        }

        String message = String.format(
                "Статистика:\n\n" +
                        "Всего: %d\n" +
                        "Желаю: %d\n" +
                        "Куплено: %d\n" +
                        "Отменено: %d",
                total, wished, purchased, cancelled
        );

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Статистика")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showSettings() {
        Toast.makeText(this, "Настройки", Toast.LENGTH_SHORT).show();
        // В будущем можно добавить настройки приложения
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // Обновить список после добавления/редактирования
            loadWishes();
        }
    }
}