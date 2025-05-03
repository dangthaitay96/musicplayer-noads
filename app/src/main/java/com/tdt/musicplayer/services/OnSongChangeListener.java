package com.tdt.musicplayer.services;

import com.tdt.musicplayer.models.Song;

public interface OnSongChangeListener {
  void onSongChanged(Song newSong);
}
