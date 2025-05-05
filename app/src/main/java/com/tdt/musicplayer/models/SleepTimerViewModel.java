package com.tdt.musicplayer.models;

import android.os.CountDownTimer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SleepTimerViewModel extends ViewModel {
  private final MutableLiveData<Long> remainingTime = new MutableLiveData<>(0L);
  private CountDownTimer countDownTimer;

  public LiveData<Long> getRemainingTime() {
    return remainingTime;
  }

  public void startTimer(long millis, Runnable onFinish) {
    cancelTimer();
    countDownTimer =
        new CountDownTimer(millis, 1000) {
          public void onTick(long millisUntilFinished) {
            remainingTime.setValue(millisUntilFinished);
          }

          public void onFinish() {
            remainingTime.setValue(0L);
            if (onFinish != null) onFinish.run();
          }
        };
    countDownTimer.start();
  }

  public void cancelTimer() {
    if (countDownTimer != null) {
      countDownTimer.cancel();
      countDownTimer = null;
    }
    remainingTime.setValue(0L);
  }
}
