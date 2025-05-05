package com.tdt.musicplayer.models;

import android.os.CountDownTimer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SleepTimerViewModel extends ViewModel {
  private final MutableLiveData<Long> remainingTime = new MutableLiveData<>(0L);
  private final MutableLiveData<Boolean> isRunning = new MutableLiveData<>(false);
  private CountDownTimer countDownTimer;

  public LiveData<Long> getRemainingTime() {
    return remainingTime;
  }

  public LiveData<Boolean> isRunning() {
    return isRunning;
  }

  public void start(long millis, Runnable onFinish) {
    cancel(); // cancel old timer
    isRunning.setValue(true);

    countDownTimer =
        new CountDownTimer(millis, 1000) {
          @Override
          public void onTick(long millisUntilFinished) {
            remainingTime.setValue(millisUntilFinished);
          }

          @Override
          public void onFinish() {
            remainingTime.setValue(0L);
            isRunning.setValue(false);
            if (onFinish != null) onFinish.run();
          }
        };

    countDownTimer.start();
  }

  public void cancel() {
    if (countDownTimer != null) {
      countDownTimer.cancel();
      countDownTimer = null;
    }
    remainingTime.setValue(0L);
    isRunning.setValue(false);
  }
}
