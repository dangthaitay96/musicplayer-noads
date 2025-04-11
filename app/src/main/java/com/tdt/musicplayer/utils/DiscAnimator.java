package com.tdt.musicplayer.utils;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.tdt.musicplayer.R;

public class DiscAnimator {
    private final ImageView rotatingImage;
    private final Animation rotateAnimation;

    public DiscAnimator(Context context, ImageView imageView) {
        this.rotatingImage = imageView;
        this.rotateAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate_forever);
    }

    public void start() {
        if (rotatingImage != null && rotateAnimation != null) {
            rotatingImage.startAnimation(rotateAnimation);
        }
    }

    public void stop() {
        if (rotatingImage != null) {
            rotatingImage.clearAnimation();
        }
    }
}