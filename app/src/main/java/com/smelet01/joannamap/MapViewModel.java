package com.smelet01.joannamap;

import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MapViewModel extends ViewModel {
    private final MutableLiveData<Bundle> mapState = new MutableLiveData<>();

    public void setMapState(Bundle bundle) {
        mapState.setValue(bundle);
    }

    public LiveData<Bundle> getMapState() {
        return mapState;
    }
}
