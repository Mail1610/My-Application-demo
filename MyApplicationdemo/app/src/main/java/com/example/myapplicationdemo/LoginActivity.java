package com.example.myapplicationdemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplicationdemo.databinding.ActivityLoginBinding;
import com.example.myapplicationdemo.utils.SessionManager;
import com.example.myapplicationdemo.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (new SessionManager(this).isLoggedIn()) {
            goToMain();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
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

        binding.btnLogin.setOnClickListener(v -> handleLogin());
        binding.tvGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void handleLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        if (!validate(email, password)) return;
        viewModel.login(email, password);
    }

    private void setLoading(boolean loading) {
        binding.btnLogin.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
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
