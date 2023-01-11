package com.example.pregnancyfitness;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.pregnancyfitness.databinding.ActivityAboutUsBinding;


import java.util.Objects;

public class AboutUs extends AppCompatActivity {
    private ActivityAboutUsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        binding = ActivityAboutUsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.webViewMap.loadUrl("https://goo.gl/maps/VPJDbohG2E17Zx4x8");
            }
        });

        binding.fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.webViewFb.loadUrl("https://www.facebook.com/MarbieFuertesLaxamanaOBGyneClinic");
            }
        });

        binding.gmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.webViewGmail.loadUrl("https://mail.google.com/mail/");
            }
        });

    }
}