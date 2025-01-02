// EmergencyViewModel.java
package com.septi.resq.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.septi.resq.model.Emergency;
import com.septi.resq.database.EmergencyDBHelper;
import java.util.List;

public class EmergencyViewModel extends ViewModel {
    private final MutableLiveData<List<Emergency>> emergencies = new MutableLiveData<>();
    private final MutableLiveData<Emergency> newEmergency = new MutableLiveData<>();
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

    public void loadEmergencies() {
        if (dbHelper != null) {
            List<Emergency> emergencyList = dbHelper.getAllEmergencies();
            emergencies.setValue(emergencyList);
        }
    }

    public void addEmergency(Emergency emergency) {
        // Simpan ke database
        long id = dbHelper.insertEmergency(emergency);
        if (id > 0) {
            // Update LiveData dengan data baru
            List<Emergency> currentList = emergencies.getValue();
            if (currentList != null) {
                currentList.add(emergency);
                emergencies.setValue(currentList);
            }
        }
    }
}
