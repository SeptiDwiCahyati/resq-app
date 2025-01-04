// EmergencyViewModel.java
package com.septi.resq.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.septi.resq.model.Emergency;
import com.septi.resq.database.EmergencyDBHelper;

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
        if (dbHelper != null && dbHelper.updateEmergency(emergency)) {
            List<Emergency> currentList = emergencies.getValue();
            if (currentList != null) {
                int index = -1;
                for (int i = 0; i < currentList.size(); i++) {
                    if (currentList.get(i).getId() == emergency.getId()) {
                        index = i;
                        break;
                    }
                }
                if (index != -1) {
                    currentList.set(index, emergency);
                    emergencies.setValue(currentList);
                    updatedEmergency.setValue(emergency);
                }
            }
        }
    }

    public void deleteEmergency(long emergencyId) {
        if (dbHelper != null && dbHelper.deleteEmergency(emergencyId)) {
            List<Emergency> currentList = emergencies.getValue();
            if (currentList != null) {
                List<Emergency> newList = new ArrayList<>();
                for (Emergency e : currentList) {
                    if (e.getId() != emergencyId) {
                        newList.add(e);
                    }
                }
                emergencies.setValue(newList);
                deletedEmergencyId.setValue(emergencyId);
            }
        }
    }


}

