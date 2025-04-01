package com.tdt.musicplayer.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import java.io.IOException;

public class MusicService extends Service {
  private MediaPlayer mediaPlayer;

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    String songPath = intent.getStringExtra("songPath");
    if (songPath != null) {
      playMusic(songPath);
    }
    return START_STICKY;
  }

  private void playMusic(String path) {
    if (mediaPlayer != null) {
      mediaPlayer.release();
    }
    mediaPlayer = new MediaPlayer();
    try {
      mediaPlayer.setDataSource(path);
      mediaPlayer.prepare();
      mediaPlayer.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
