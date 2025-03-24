package com.example.bai4;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_CODE = 101;
    private ImageView imageView;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnCapture = findViewById(R.id.btnCapture);
        imageView = findViewById(R.id.imageView);

        btnCapture.setOnClickListener(v -> {
            if (checkPermissions()) {
                openCamera();
            } else {
                requestPermissions();
            }
        });
    }

    // Kiểm tra quyền CAMERA & WRITE_EXTERNAL_STORAGE
    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    // Yêu cầu quyền từ người dùng
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                CAMERA_REQUEST_CODE);
    }

    // Xử lý kết quả khi người dùng cấp quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Cần cấp quyền Camera để chụp ảnh!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Mở Camera
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.cameracapture.fileprovider",
                        photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, CAMERA_CAPTURE_CODE);
            }
        }
    }

    // Tạo file ảnh với tên file dựa trên ngày giờ
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Nhận kết quả sau khi chụp ảnh
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_CAPTURE_CODE && resultCode == RESULT_OK) {
            imageView.setImageURI(Uri.parse(currentPhotoPath));
            Toast.makeText(this, "Ảnh đã lưu: " + currentPhotoPath, Toast.LENGTH_SHORT).show();
        }
    }
}