// EmergencyViewModel.java
package com.septi.resq.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.septi.resq.database.EmergencyDBHelper;
import com.septi.resq.model.Emergency;

import java.util.ArrayList;
import java.util.List;

public class EmergencyViewModel extends ViewModel {
    private final MutableLiveData<List<Emergency>> emergencies = new MutableLiveData<>();
    private final MutableLiveData<Emergency> newEmergency = new MutableLiveData<>();
    private final MutableLiveData<Emergency> updatedEmergency = new MutableLiveData<>();
    private final MutableLiveData<Long> deletedEmergencyId = new MutableLiveData<>();
    private EmergencyDBHelper dbHelper;

    public void init(EmergencyDBHelper dbHelper) {
        this.dbHelper = dbHelper;
        loadEmergencies();
    }


    public LiveData<List<Emergency>> getEmergencies() {
        return emergencies;
    }

    public LiveData<Emergency> getNewEmergency() {
        return newEmergency;
    }

    public LiveData<Emergency> getUpdatedEmergency() {
        return updatedEmergency;
    }

    public LiveData<Long> getDeletedEmergencyId() {
        return deletedEmergencyId;
    }


    public void loadEmergencies() {
        if (dbHelper != null) {
            List<Emergency> emergencyList = dbHelper.getAllEmergencies();
            emergencies.setValue(emergencyList);
        }
    }

    public void addEmergency(Emergency emergency) {
        if (dbHelper != null) {
            long id = dbHelper.insertEmergency(emergency);
            if (id > 0) {
                emergency.setId(id);
                List<Emergency> currentList = emergencies.getValue();
                if (currentList == null) {
                    currentList = new ArrayList<>();
                }
                currentList.add(0, emergency);
                emergencies.setValue(currentList);
                newEmergency.setValue(emergency);
            }
        }
    }

    public void updateEmergency(Emergency emergency) {
        if (dbHelper != null) {
            boolean updated = dbHelper.updateEmergency(emergency);
            if (updated) {
                List<Emergency> currentList = emergencies.getValue();
                if (currentList != null) {
                    for (int i = 0; i < currentList.size(); i++) {
                        if (currentList.get(i).getId() == emergency.getId()) {
                            currentList.set(i, emergency);
                            break;
                        }
                    }
                    emergencies.setValue(currentList);
                    updatedEmergency.setValue(emergency);
                }
            }
        }
    }

    public void deleteEmergency(long id) {
        if (dbHelper != null) {
            boolean deleted = dbHelper.deleteEmergency(id);
            if (deleted) {
                List<Emergency> currentList = emergencies.getValue();
                if (currentList != null) {
                    currentList.removeIf(e -> e.getId() == id);
                    emergencies.setValue(currentList);
                    deletedEmergencyId.setValue(id);
                }
            }
        }
    }


}

