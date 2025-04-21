package com.tdt.musicplayer.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import com.tdt.musicplayer.models.PlaybackMode;
import com.tdt.musicplayer.models.Song;
import com.tdt.musicplayer.services.OnSongChangeListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MusicPlayerManager {
  private static MusicPlayerManager instance;

  private OnSongChangeListener songChangeListener;

  public void setOnSongChangeListener(OnSongChangeListener listener) {
    this.songChangeListener = listener;
  }

  private void notifySongChanged(Song song) {
    if (songChangeListener != null) {
      songChangeListener.onSongChanged(song);
    }
  }


  public static MusicPlayerManager getInstance(
      Context context, SeekBar seekBar, TextView tvCurrentTime, TextView tvTotalTime) {
    if (instance == null) {
      instance =
          new MusicPlayerManager(
              context.getApplicationContext(), seekBar, tvCurrentTime, tvTotalTime);
    } else {
      instance.updateUI(seekBar, tvCurrentTime, tvTotalTime);
    }
    return instance;
  }

  private final Context context;
  private SeekBar seekBar;
  private TextView tvCurrentTime;
  private TextView tvTotalTime;
  private final Handler timeHandler = new Handler();

  private MediaPlayer mediaPlayer;
  private Runnable timeRunnable;

  private List<Song> songList;
  private int currentIndex = 0;
  private Song currentSong = null;
  private PlaybackMode playbackMode = PlaybackMode.NORMAL;

  private MusicPlayerManager(
      Context context, SeekBar seekBar, TextView tvCurrentTime, TextView tvTotalTime) {
    this.context = context;
    this.seekBar = seekBar;
    this.tvCurrentTime = tvCurrentTime;
    this.tvTotalTime = tvTotalTime;
    setupSeekBarListener();
  }

  public void updateUI(SeekBar seekBar, TextView tvCurrentTime, TextView tvTotalTime) {
    this.seekBar = seekBar;
    this.tvCurrentTime = tvCurrentTime;
    this.tvTotalTime = tvTotalTime;
    setupSeekBarListener();
    setupSeekBarUpdater();
  }

  private void setupSeekBarListener() {
    if (this.seekBar != null) {
      this.seekBar.setOnSeekBarChangeListener(
          new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
              if (fromUser && mediaPlayer != null) {
                mediaPlayer.seekTo(progress);
                tvCurrentTime.setText(formatTime(progress));
              }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
          });
    }
  }

  public void setPlaybackMode(PlaybackMode mode) {
    this.playbackMode = mode;
  }

  public PlaybackMode getPlaybackMode() {
    return playbackMode;
  }

  public void play(List<Song> songs, int index) {
    this.songList = songs;
    this.currentIndex = index;
    this.currentSong = songs.get(index);
    playSong(currentSong);
  }

  public void playNext() {
    if (songList == null || songList.isEmpty()) return;
    currentIndex = (currentIndex + 1) % songList.size();
    currentSong = songList.get(currentIndex);
    playSong(currentSong);
  }

  public void playPrev() {
    if (songList == null || songList.isEmpty()) return;
    currentIndex = (currentIndex - 1 + songList.size()) % songList.size();
    currentSong = songList.get(currentIndex);
    playSong(currentSong);
  }

  public void pause() {
    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
      mediaPlayer.pause();
      timeHandler.removeCallbacks(timeRunnable);
    }
  }

  public void resume() {
    if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
      mediaPlayer.start();
      timeHandler.post(timeRunnable);
    }
  }

  public boolean isPlaying() {
    return mediaPlayer != null && mediaPlayer.isPlaying();
  }

  private void playSong(Song song) {
    try {
      if (mediaPlayer != null) {
        mediaPlayer.stop();
        mediaPlayer.release();
      }
      currentSong = song;
      mediaPlayer = new MediaPlayer();
      mediaPlayer.setDataSource(song.getFilePath());
      mediaPlayer.prepare();
      mediaPlayer.start();

      songChangeListener.onSongChanged(song);
      if (seekBar != null) seekBar.setMax(mediaPlayer.getDuration());
      if (tvTotalTime != null) tvTotalTime.setText(formatTime(mediaPlayer.getDuration()));
      setupSeekBarUpdater();

      mediaPlayer.setOnCompletionListener(
          mp -> {
            switch (playbackMode) {
              case NORMAL:
                currentIndex++;
                if (currentIndex < songList.size()) {
                  currentSong = songList.get(currentIndex);
                  playSong(currentSong);
                }
                break;
              case REPEAT_ALL:
                currentIndex = (currentIndex + 1) % songList.size();
                currentSong = songList.get(currentIndex);
                playSong(currentSong);
                break;
              case REPEAT_ONE:
                playSong(currentSong);
                break;
              case SHUFFLE:
                currentIndex = getRandomIndexExcept(songList.size(), currentIndex);
                currentSong = songList.get(currentIndex);
                playSong(currentSong);
                break;
            }
          });

    } catch (IOException e) {
      Log.e("MusicPlayerManager", "💥 ERROR: " + e.getMessage(), e);
      e.printStackTrace();
    }
  }

  private void setupSeekBarUpdater() {
    if (timeRunnable != null) timeHandler.removeCallbacks(timeRunnable);

    timeRunnable =
        new Runnable() {
          @Override
          public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
              int current = mediaPlayer.getCurrentPosition();
              if (seekBar != null) seekBar.setProgress(current);
              if (tvCurrentTime != null) tvCurrentTime.setText(formatTime(current));
              timeHandler.postDelayed(this, 500);
            }
          }
        };
    timeHandler.post(timeRunnable);
  }

  private String formatTime(int millis) {
    int minutes = (millis / 1000) / 60;
    int seconds = (millis / 1000) % 60;
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
  }

  private int getRandomIndexExcept(int size, int exclude) {
    Random random = new Random();
    int index;
    do {
      index = random.nextInt(size);
    } while (index == exclude && size > 1);
    return index;
  }

  public int getCurrentIndex() {
    return currentIndex;
  }

  public Song getCurrentSong() {
    return currentSong;
  }

  public void seekBy(int millis) {
    if (mediaPlayer != null) {
      int current = mediaPlayer.getCurrentPosition();
      int newPosition = current + millis;
      newPosition = Math.max(0, Math.min(newPosition, mediaPlayer.getDuration()));
      mediaPlayer.seekTo(newPosition);
      if (seekBar != null) seekBar.setProgress(newPosition);
      if (tvCurrentTime != null) tvCurrentTime.setText(formatTime(newPosition));
    }
  }

  public void syncUI() {
    if (currentSong != null) {
      seekBar.setMax(mediaPlayer != null ? mediaPlayer.getDuration() : currentSong.getDuration());
      tvTotalTime.setText(formatTime(seekBar.getMax()));

      int current = mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
      seekBar.setProgress(current);
      tvCurrentTime.setText(formatTime(current));
    }
  }
}
