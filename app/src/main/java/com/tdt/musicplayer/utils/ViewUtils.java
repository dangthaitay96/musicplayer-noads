package com.tdt.musicplayer.utils;

import android.view.View;
import android.widget.TextView;

public class ViewUtils {
  public static void showQuickFeedback(TextView textView, String message) {
    textView.setText(message);
    textView.setVisibility(View.VISIBLE);
    textView.setAlpha(1f);
    textView.animate().cancel();
    textView
        .animate()
        .alpha(0f)
        .setDuration(1000)
        .setStartDelay(6000)
        .withEndAction(() -> textView.setVisibility(View.INVISIBLE))
        .start();
  }
}
