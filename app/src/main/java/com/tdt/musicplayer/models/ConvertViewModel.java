package com.tdt.musicplayer.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ConvertViewModel extends ViewModel {
  private final MutableLiveData<String> linkY = new MutableLiveData<>();

  public LiveData<String> getLink() {
    return linkY;
  }

  public void setYoutubeLink(String link) {
    linkY.setValue(link);
  }
}
