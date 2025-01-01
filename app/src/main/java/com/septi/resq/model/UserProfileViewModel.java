package com.septi.resq.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserProfileViewModel extends ViewModel {
    private final MutableLiveData<UserProfile> userProfile = new MutableLiveData<>();

    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }

    public void updateUserProfile(UserProfile profile) {
        userProfile.setValue(profile);
    }
}