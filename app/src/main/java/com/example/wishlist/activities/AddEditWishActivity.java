package com.example.wishlist.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.wishlist.R;
import com.example.wishlist.api.ApiService;
import com.example.wishlist.api.RetrofitClient;
import com.example.wishlist.models.Category;
import com.example.wishlist.models.Wish;
import com.example.wishlist.models.ApiResponse;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditWishActivity extends AppCompatActivity {

    public static final int REQUEST_ADD_WISH = 1;
    public static final int REQUEST_EDIT_WISH = 2;
    private static final int REQUEST_IMAGE_PICK = 100;
    private static final int REQUEST_CAMERA = 101;

    private EditText etTitle, etDescription, etPrice, etStoreUrl;
    private Spinner spinnerCategory, spinnerPriority;
    private MaterialButtonToggleGroup toggleStatus;
    private ImageView ivImagePreview;
    private Button btnSelectImage, btnSave, btnCancel;

    private ApiService apiService;
    private List<Category> categories = new ArrayList<>();
    private Uri selectedImageUri;
    private String uploadedImageName;

    private Wish currentWish;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_wish);

        initViews();
        setupListeners();

        apiService = RetrofitClient.getApiService();

        // Проверяем режим (добавление или редактирование)
        if (getIntent().hasExtra("wish")) {
            isEditMode = true;
            String wishJson = getIntent().getStringExtra("wish");
            currentWish = new Gson().fromJson(wishJson, Wish.class);
            setTitle("Редактировать желание");
            populateForm(currentWish);
        } else {
            setTitle("Добавить желание");
        }

        // Загружаем категории
        loadCategories();
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etStoreUrl = findViewById(R.id.etStoreUrl);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        toggleStatus = findViewById(R.id.toggleStatus);
        ivImagePreview = findViewById(R.id.ivImagePreview);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Настройка спиннера приоритета
        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(
                this, R.array.priority_array, android.R.layout.simple_spinner_item);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> showImagePickerDialog());
        btnSave.setOnClickListener(v -> saveWish());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadCategories() {
        apiService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();
                    setupCategorySpinner();

                    // Если редактируем, выбираем категорию
                    if (isEditMode && currentWish.getCategoryId() != null) {
                        selectCategoryInSpinner(currentWish.getCategoryId());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(AddEditWishActivity.this,
                        "Ошибка загрузки категорий", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCategorySpinner() {
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("Без категории");

        for (Category cat : categories) {
            categoryNames.add(cat.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void selectCategoryInSpinner(int categoryId) {
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId() == categoryId) {
                spinnerCategory.setSelection(i + 1); // +1 из-за "Без категории"
                break;
            }
        }
    }

    private void populateForm(Wish wish) {
        etTitle.setText(wish.getTitle());
        etDescription.setText(wish.getDescription());

        if (wish.getEstimatedPrice() != null) {
            etPrice.setText(String.valueOf(wish.getEstimatedPrice()));
        }

        if (wish.getStoreUrl() != null) {
            etStoreUrl.setText(wish.getStoreUrl());
        }

        // Установка приоритета
        String priority = wish.getPriority();
        if (priority != null) {
            switch (priority) {
                case "high":
                    spinnerPriority.setSelection(2);
                    break;
                case "low":
                    spinnerPriority.setSelection(0);
                    break;
                default:
                    spinnerPriority.setSelection(1);
                    break;
            }
        }

        // Установка статуса
        String status = wish.getStatus();
        if (status != null) {
            switch (status) {
                case "wish":
                    toggleStatus.check(R.id.btnStatusWish);
                    break;
                case "purchased":
                    toggleStatus.check(R.id.btnStatusPurchased);
                    break;
                case "cancelled":
                    toggleStatus.check(R.id.btnStatusCancelled);
                    break;
            }
        }

        // Загрузка изображения если есть
        if (wish.getImageUrl() != null && !wish.getImageUrl().isEmpty()) {
            uploadedImageName = wish.getImagePath();
            Glide.with(this)
                    .load(wish.getImageUrl())
                    .placeholder(R.drawable.ic_photo_placeholder)
                    .into(ivImagePreview);
        }
    }

    private void showImagePickerDialog() {
        String[] options = {"Сделать фото", "Выбрать из галереи", "Отмена"};

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Выберите изображение");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    // Сделать фото
                    checkCameraPermission();
                    break;
                case 1:
                    // Выбрать из галереи
                    checkStoragePermission();
                    break;
            }
        });
        builder.show();
    }

    private void checkCameraPermission() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        openCamera();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(AddEditWishActivity.this,
                                "Разрешение на камеру отклонено", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .check();
    }

    private void checkStoragePermission() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        openGallery();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(AddEditWishActivity.this,
                                "Разрешение на доступ к хранилищу отклонено", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .check();
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CAMERA);
        } else {
            Toast.makeText(this, "Камера не доступна", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK && data != null && data.getData() != null) {
                selectedImageUri = data.getData();
                ivImagePreview.setImageURI(selectedImageUri);
                uploadImageToServer();
            } else if (requestCode == REQUEST_CAMERA && data != null && data.getExtras() != null) {
                // Для фото с камеры используем временное решение
                // В реальном приложении нужно сохранять файл
                Toast.makeText(this, "Фото сделано. Сохраните желание.", Toast.LENGTH_SHORT).show();
                // Можно установить флаг, что фото есть
                uploadedImageName = "camera_photo.jpg";
            }
        }
    }

    private void uploadImageToServer() {
        if (selectedImageUri == null) {
            return;
        }

        String path = getPathFromUri(selectedImageUri);
        if (path == null) {
            Toast.makeText(this, "Не удалось получить путь к файлу", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(path);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        apiService.uploadImage(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> result = response.body();
                    if (result.containsKey("success") && (Boolean) result.get("success")) {
                        uploadedImageName = (String) result.get("image_path");
                        Toast.makeText(AddEditWishActivity.this, "Изображение загружено", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(AddEditWishActivity.this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return uri.getPath();
    }

    private void saveWish() {
        // Валидация
        String title = etTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Введите название");
            etTitle.requestFocus();
            return;
        }

        // Создание объекта Wish
        Wish wish = new Wish();
        wish.setTitle(title);
        wish.setDescription(etDescription.getText().toString().trim());

        // Цена
        String priceStr = etPrice.getText().toString().trim();
        if (!TextUtils.isEmpty(priceStr)) {
            try {
                wish.setEstimatedPrice(Double.parseDouble(priceStr));
            } catch (NumberFormatException e) {
                etPrice.setError("Некорректная цена");
                etPrice.requestFocus();
                return;
            }
        }

        // Ссылка на магазин
        wish.setStoreUrl(etStoreUrl.getText().toString().trim());

        // Категория
        int selectedCategoryPosition = spinnerCategory.getSelectedItemPosition();
        if (selectedCategoryPosition > 0 && selectedCategoryPosition - 1 < categories.size()) {
            wish.setCategoryId(categories.get(selectedCategoryPosition - 1).getId());
        }

        // Приоритет
        int priorityIndex = spinnerPriority.getSelectedItemPosition();
        String priority = "medium";
        if (priorityIndex == 0) priority = "low";
        else if (priorityIndex == 2) priority = "high";
        wish.setPriority(priority);

        // Статус
        int checkedButtonId = toggleStatus.getCheckedButtonId();
        String status = "wish";
        if (checkedButtonId == R.id.btnStatusPurchased) {
            status = "purchased";
        } else if (checkedButtonId == R.id.btnStatusCancelled) {
            status = "cancelled";
        }
        wish.setStatus(status);

        // Изображение
        if (uploadedImageName != null) {
            wish.setImagePath(uploadedImageName);
        }

        // Если редактируем, устанавливаем ID
        if (isEditMode) {
            wish.setId(currentWish.getId());
        }

        // Сохранение
        saveWishToServer(wish);
    }

    private void saveWishToServer(Wish wish) {
        btnSave.setEnabled(false);

        Call<ApiResponse> call;
        if (isEditMode) {
            call = apiService.updateWish(wish);
        } else {
            call = apiService.createWish(wish);
        }

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                btnSave.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        Toast.makeText(AddEditWishActivity.this, "Сохранено успешно", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AddEditWishActivity.this,
                                "Ошибка: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddEditWishActivity.this, "Ошибка сервера", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                btnSave.setEnabled(true);
                Toast.makeText(AddEditWishActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}