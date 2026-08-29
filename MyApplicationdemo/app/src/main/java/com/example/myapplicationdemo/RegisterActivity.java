package com.example.myapplicationdemo;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplicationdemo.databinding.ActivityRegisterBinding;
import com.example.myapplicationdemo.utils.LogUtils;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvGoToLogin.setOnClickListener(v -> finish());
        binding.btnRegister.setOnClickListener(v -> handleRegister());
    }

    private void handleRegister() {
        String username = binding.etUsername.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        if (!validate(username, email, password, confirmPassword)) return;

        LogUtils.info("註冊嘗試：" + email);
        // TODO: 串接後端 API
        Toast.makeText(this, "註冊功能開發中", Toast.LENGTH_SHORT).show();
    }

    private boolean validate(String username, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(username)) {
            binding.tilUsername.setError("請輸入用戶名稱");
            return false;
        }
        binding.tilUsername.setError(null);

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

        if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("兩次密碼不一致");
            return false;
        }
        binding.tilConfirmPassword.setError(null);

        return true;
    }
}
