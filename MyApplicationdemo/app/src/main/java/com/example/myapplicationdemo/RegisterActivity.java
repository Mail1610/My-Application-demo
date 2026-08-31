package com.example.myapplicationdemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplicationdemo.databinding.ActivityRegisterBinding;
import com.example.myapplicationdemo.viewmodel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
        viewModel.getState().observe(this, state -> {
            switch (state.status) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    goToMain();
                    break;
                case ERROR:
                    setLoading(false);
                    binding.tilPassword.setError(state.message);
                    break;
            }
        });

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
        viewModel.register(username, email, password);
    }

    private void setLoading(boolean loading) {
        binding.btnRegister.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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
