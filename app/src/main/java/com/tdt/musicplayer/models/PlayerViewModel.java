package com.tdt.musicplayer.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PlayerViewModel extends ViewModel {
  private final MutableLiveData<Song> currentSong = new MutableLiveData<>();
  private final MutableLiveData<Integer> currentIndex = new MutableLiveData<>(0);
  private final MutableLiveData<PlaybackMode> playbackMode =
      new MutableLiveData<>(PlaybackMode.NORMAL);
  private final MutableLiveData<Boolean> isDiscSpinning = new MutableLiveData<>(false);
  private final MutableLiveData<Integer> currentDiscIndex = new MutableLiveData<>(0);
  private final MutableLiveData<Long> sleepTimerRemaining = new MutableLiveData<>(0L);

  public LiveData<Song> getCurrentSong() {
    return currentSong;
  }

  public void setCurrentSong(Song song) {
    currentSong.setValue(song);
  }

  public LiveData<Integer> getCurrentIndex() {
    return currentIndex;
  }

  public void setCurrentIndex(int index) {
    currentIndex.setValue(index);
  }

  public LiveData<PlaybackMode> getPlaybackMode() {
    return playbackMode;
  }

  public void setPlaybackMode(PlaybackMode mode) {
    playbackMode.setValue(mode);
  }

  public LiveData<Boolean> isDiscSpinning() {
    return isDiscSpinning;
  }

  public void setDiscSpinning(boolean spinning) {
    isDiscSpinning.setValue(spinning);
  }

  public LiveData<Integer> getCurrentDiscIndex() {
    return currentDiscIndex;
  }

  public void setCurrentDiscIndex(int index) {
    currentDiscIndex.setValue(index);
  }

  public LiveData<Long> getSleepTimerRemaining() {
    return sleepTimerRemaining;
  }

  public void setSleepTimerRemaining(long millis) {
    sleepTimerRemaining.setValue(millis);
  }
}
