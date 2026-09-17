package com.example.myapplicationdemo.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplicationdemo.model.Group;
import com.example.myapplicationdemo.repository.GroupRepository;
import com.example.myapplicationdemo.utils.LogUtils;
import com.example.myapplicationdemo.utils.SessionManager;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Group>> groups = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final GroupRepository repository = new GroupRepository();
    private final SessionManager session;
    private ListenerRegistration groupListener;

    public MainViewModel(@NonNull Application application) {
        super(application);
        session = new SessionManager(application);
        loadGroups();
    }

    public LiveData<List<Group>> getGroups() { return groups; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadGroups() {
        String deviceId = session.getDeviceId();
        loading.postValue(true);
        if (groupListener != null) groupListener.remove();
        groupListener = repository.addGroupsListener(deviceId, list -> {
            groups.postValue(list);
            loading.postValue(false);
        });
    }

    public void addGroup(String name) {
        String deviceId = session.getDeviceId();
        String nickname = session.getNickname();
        new Thread(() -> {
            try {
                repository.addGroup(name, nickname, deviceId);
            } catch (Exception e) {
                LogUtils.error("新增群組失敗", e);
                errorMessage.postValue("新增群組失敗");
            }
        }).start();
    }

    public void joinGroup(String inviteCode, Runnable onSuccess, Runnable onNotFound) {
        String deviceId = session.getDeviceId();
        String nickname = session.getNickname();
        new Thread(() -> {
            try {
                boolean found = repository.joinGroup(inviteCode, nickname, deviceId);
                if (found) onSuccess.run();
                else onNotFound.run();
            } catch (Exception e) {
                LogUtils.error("加入群組失敗", e);
                errorMessage.postValue("加入群組失敗");
            }
        }).start();
    }

    public void deleteGroup(String groupId) {
        new Thread(() -> {
            try {
                repository.deleteGroup(groupId);
            } catch (Exception e) {
                LogUtils.error("刪除群組失敗", e);
            }
        }).start();
    }

    public void renameGroup(String groupId, String newName) {
        new Thread(() -> {
            try {
                repository.renameGroup(groupId, newName);
            } catch (Exception e) {
                LogUtils.error("重新命名群組失敗", e);
            }
        }).start();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (groupListener != null) groupListener.remove();
    }
}
