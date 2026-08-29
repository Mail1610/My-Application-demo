package com.example.myapplicationdemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplicationdemo.databinding.ActivityLoginBinding;
import com.example.myapplicationdemo.utils.LogUtils;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLogin.setOnClickListener(v -> handleLogin());
        binding.tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void handleLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (!validate(email, password)) return;

        LogUtils.info("登入嘗試：" + email);
        // TODO: 串接後端 API
        Toast.makeText(this, "登入功能開發中", Toast.LENGTH_SHORT).show();
    }

    private boolean validate(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("請輸入電子郵件");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("電子郵件格式不正確");
            return false;
        }
        binding.tilEmail.setError(null);

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("請輸入密碼");
            return false;
        }
        if (password.length() < 6) {
            binding.tilPassword.setError("密碼至少需要 6 個字元");
            return false;
        }
        binding.tilPassword.setError(null);

        return true;
    }
}
