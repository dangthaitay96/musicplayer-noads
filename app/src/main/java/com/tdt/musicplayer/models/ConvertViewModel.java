package com.tdt.musicplayer.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ConvertViewModel extends ViewModel {
  private final MutableLiveData<String> linkY = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isConverting = new MutableLiveData<>(false);
  private final MutableLiveData<String> songTitle = new MutableLiveData<>("");
  private final MutableLiveData<Integer> progress = new MutableLiveData<>(0);

  public LiveData<String> getLink() {
    return linkY;
  }

  public void setLink(String link) {
    linkY.setValue(link);
  }

  public LiveData<Boolean> getIsConverting() {
    return isConverting;
  }

  public void setIsConverting(boolean value) {
    isConverting.setValue(value);
  }

  public LiveData<String> getSongTitle() {
    return songTitle;
  }

  public void setSongTitle(String title) {
    songTitle.setValue(title);
  }

  public LiveData<Integer> getProgress() {
    return progress;
  }

  public void setProgress(int value) {
    progress.setValue(value);
  }
}
