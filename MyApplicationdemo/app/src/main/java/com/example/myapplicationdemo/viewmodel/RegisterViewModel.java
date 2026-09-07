package com.example.myapplicationdemo.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplicationdemo.network.dto.AuthResponse;
import com.example.myapplicationdemo.repository.AuthRepository;
import com.example.myapplicationdemo.utils.LogUtils;
import com.example.myapplicationdemo.utils.SessionManager;

public class RegisterViewModel extends AndroidViewModel {

    private final MutableLiveData<AuthState> state = new MutableLiveData<>();
    private final AuthRepository repository = new AuthRepository();

    public RegisterViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<AuthState> getState() {
        return state;
    }

    public void register(String username, String email, String password) {
        state.setValue(AuthState.loading());
        new Thread(() -> {
            try {
                AuthResponse response = repository.register(username, email, password);
                if (response != null && response.getToken() != null) {
                    saveSession(response);
                    LogUtils.info("註冊成功：" + email);
                    state.postValue(AuthState.success());
                } else {
                    state.postValue(AuthState.error("註冊失敗，請稍後再試"));
                }
            } catch (Exception e) {
                LogUtils.error("註冊失敗", e);
                state.postValue(AuthState.error("網路錯誤，請稍後再試"));
            }
        }).start();
    }

    private void saveSession(AuthResponse response) {
        new SessionManager(getApplication()).saveSession(
                response.getToken(), response.getUserId(), response.getUsername());
    }
}
