package com.pizzk.album;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv).setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int result = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
                if (PackageManager.PERMISSION_GRANTED != result) {
                    Toast.makeText(getBaseContext(), "please grant READ_EXTERNAL_STORAGE permission", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            startActivity(new Intent(MainActivity.this, AlbumActivity.class));
        });
    }
}
