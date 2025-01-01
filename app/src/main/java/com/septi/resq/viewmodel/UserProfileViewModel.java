package com.septi.resq.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.septi.resq.model.UserProfile;

public class UserProfileViewModel extends ViewModel {
    private final MutableLiveData<UserProfile> userProfile = new MutableLiveData<>();

    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }

    public void updateUserProfile(UserProfile profile) {
        userProfile.setValue(profile);
    }
}