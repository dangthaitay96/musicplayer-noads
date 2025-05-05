package com.tdt.musicplayer.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {
  private final MutableLiveData<List<Song>> songListLiveData;

  public HomeViewModel() {
    songListLiveData = new MutableLiveData<>(new ArrayList<>());
  }

  public LiveData<List<Song>> getSongList() {
    return songListLiveData;
  }

  public void setSongList(List<Song> songs) {
    songListLiveData.setValue(songs);
  }

  public void clearSongs() {
    songListLiveData.setValue(new ArrayList<>());
  }
}
