package com.tdt.musicplayer.utils;

import android.content.Context;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import com.tdt.musicplayer.R;

public class DiscSwitcher {

  private final Context context;
  private final ImageView imageView;
  private final int[] imageResources;
  private final Animation rotateAnimation;
  private final Handler handler = new Handler();

  private int currentIndex = 0;
  private boolean isRunning = false;

  private final Runnable switchTask =
      new Runnable() {
        @Override
        public void run() {
          currentIndex = (currentIndex + 1) % imageResources.length;
          imageView.setImageResource(imageResources[currentIndex]);
          handler.postDelayed(this, 60_000); // đổi mỗi 1 phút
        }
      };

  public DiscSwitcher(Context context, ImageView imageView, int[] imageResources) {
    this.context = context;
    this.imageView = imageView;
    this.imageResources = imageResources;
    this.rotateAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate_forever);
  }

  public void start() {
    if (isRunning) return;

    isRunning = true;
    imageView.setImageResource(imageResources[currentIndex]);
    imageView.startAnimation(rotateAnimation);
    handler.postDelayed(switchTask, 60_000); // lần đầu sau 1 phút
  }

  public void stop() {
    isRunning = false;
    imageView.clearAnimation();
    handler.removeCallbacks(switchTask);
  }

  public void reset() {
    currentIndex = 0;
    imageView.setImageResource(imageResources[currentIndex]);
  }
}
