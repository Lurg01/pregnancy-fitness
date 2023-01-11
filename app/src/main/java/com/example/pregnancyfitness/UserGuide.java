package com.example.pregnancyfitness;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.pregnancyfitness.databinding.ActivityHomeBinding;
import com.example.pregnancyfitness.databinding.ActivityUserGuideBinding;

import java.util.Objects;

public class UserGuide extends AppCompatActivity {
    private ActivityUserGuideBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        binding = ActivityUserGuideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.webView.loadUrl("https://www.facebook.com/MarbieFuertesLaxamanaOBGyneClinic");
            }
        });
    }
}