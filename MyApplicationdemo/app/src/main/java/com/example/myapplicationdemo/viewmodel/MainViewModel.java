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
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Group>> groups = new MutableLiveData<>();
    private final GroupRepository repository = new GroupRepository();

    public MainViewModel(@NonNull Application application) {
        super(application);
        loadGroups();
    }

    public LiveData<List<Group>> getGroups() {
        return groups;
    }

    public void loadGroups() {
        String username = new SessionManager(getApplication()).getUsername();
        new Thread(() -> {
            try {
                groups.postValue(repository.getAll(username));
            } catch (Exception e) {
                LogUtils.error("載入群組失敗", e);
            }
        }).start();
    }

    public void deleteGroup(int groupId) {
        String username = new SessionManager(getApplication()).getUsername();
        new Thread(() -> {
            try {
                repository.deleteGroup(groupId);
                groups.postValue(repository.getAll(username));
            } catch (Exception e) {
                LogUtils.error("刪除群組失敗", e);
            }
        }).start();
    }

    public void addGroup(String name) {
        String username = new SessionManager(getApplication()).getUsername();
        new Thread(() -> {
            try {
                repository.addGroup(name);
                groups.postValue(repository.getAll(username));
            } catch (Exception e) {
                LogUtils.error("新增群組失敗", e);
            }
        }).start();
    }
}
