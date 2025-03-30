package com.tdt.musicplayer.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tdt.musicplayer.models.PlaybackMode;
import com.tdt.musicplayer.models.Song;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MusicPlayerManager {
  private final Context context;
  private final SeekBar seekBar;
  private final TextView tvCurrentTime;
  private final TextView tvTotalTime;
  private final Handler timeHandler = new Handler();

  private MediaPlayer mediaPlayer;
  private Runnable timeRunnable;
  private List<Song> songList;
  private int currentIndex = 0;
  private PlaybackMode playbackMode = PlaybackMode.NORMAL;

  public MusicPlayerManager(
      Context context, SeekBar seekBar, TextView tvCurrentTime, TextView tvTotalTime) {
    this.context = context;
    this.seekBar = seekBar;
    this.tvCurrentTime = tvCurrentTime;
    this.tvTotalTime = tvTotalTime;

    this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

  public void setPlaybackMode(PlaybackMode mode) {
    this.playbackMode = mode;
  }

  public PlaybackMode getPlaybackMode() {
    return playbackMode;
  }

  public void play(List<Song> songs, int index) {
    this.songList = songs;
    this.currentIndex = index;
    playSong(songList.get(currentIndex));
  }

  public void playNext() {
    if (songList == null || songList.isEmpty()) return;
    currentIndex = (currentIndex + 1) % songList.size();
    playSong(songList.get(currentIndex));
  }

  public void playPrev() {
    if (songList == null || songList.isEmpty()) return;
    currentIndex = (currentIndex - 1 + songList.size()) % songList.size();
    playSong(songList.get(currentIndex));
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

      mediaPlayer = new MediaPlayer();
      mediaPlayer.setDataSource(song.getFilePath());
      mediaPlayer.prepare();
      mediaPlayer.start();

      seekBar.setMax(mediaPlayer.getDuration());
      tvTotalTime.setText(formatTime(mediaPlayer.getDuration()));
      setupSeekBarUpdater();


      mediaPlayer.setOnCompletionListener(
          mp -> {
            switch (playbackMode) {
              case NORMAL:
                currentIndex++;
                if (currentIndex < songList.size()) {
                  playSong(songList.get(currentIndex));
                }
                break;
              case REPEAT_ALL:
                currentIndex = (currentIndex + 1) % songList.size();
                playSong(songList.get(currentIndex));
                break;
              case REPEAT_ONE:
                playSong(songList.get(currentIndex));
                break;
              case SHUFFLE:
                currentIndex = getRandomIndexExcept(songList.size(), currentIndex);
                playSong(songList.get(currentIndex));
                break;
            }
          });

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void setupSeekBarUpdater() {
    timeRunnable =
        new Runnable() {
          @Override
          public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
              int current = mediaPlayer.getCurrentPosition();
              seekBar.setProgress(current);
              tvCurrentTime.setText(formatTime(current));
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
    if (songList == null || songList.isEmpty()) return null;
    return songList.get(currentIndex);
  }
    public void seekBy(int millis) {
      if (mediaPlayer != null) {
        int current = mediaPlayer.getCurrentPosition();
        int newPosition = current + millis;
        newPosition = Math.max(0, Math.min(newPosition, mediaPlayer.getDuration()));

        mediaPlayer.seekTo(newPosition);
        seekBar.setProgress(newPosition);
        tvCurrentTime.setText(formatTime(newPosition));
      }
    }


}
