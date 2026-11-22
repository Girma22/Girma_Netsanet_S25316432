package com.example.girma_netsanet_s2531643.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.girma_netsanet_s2531643.model.Situation;
import com.example.girma_netsanet_s2531643.repository.TrafficRepository;

import java.util.ArrayList;
import java.util.List;

public class SituationViewModel extends AndroidViewModel {

    private final TrafficRepository repository;

    private final MutableLiveData<List<Situation>> situations =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<String> status =
            new MutableLiveData<>("Ready");

    public SituationViewModel(@NonNull Application application) {

        super(application);

        repository = new TrafficRepository(application);
    }

    // Get traffic situations
    public LiveData<List<Situation>> getSituations() {

        return situations;
    }

    // Get application status
    public LiveData<String> getStatus() {

        return status;
    }

    // Refresh online or offline traffic information
    public void refresh() {

        status.setValue("Downloading traffic information...");

        repository.refresh(new TrafficRepository.DataCallback() {

            @Override
            public void onSuccess(
                    List<Situation> data,
                    String message) {

                situations.postValue(data);

                status.postValue(message);
            }

            @Override
            public void onError(String message) {

                situations.postValue(new ArrayList<>());

                status.postValue(message);
            }
        });
    }

    // Clear traffic information when required
    public void clearSituations() {

        situations.setValue(new ArrayList<>());

        status.setValue("Traffic information cleared");
    }

    @Override
    protected void onCleared() {

        super.onCleared();
    }
}
