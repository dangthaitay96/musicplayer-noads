package com.tdt.musicplayer.repository;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import com.tdt.musicplayer.models.Song;
import java.util.ArrayList;
import java.util.List;

public class SongRepository {
  public List<Song> loadLocalSongs(Context context) {
    List<Song> songList = new ArrayList<>();

    Uri collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    String[] projection = {
      MediaStore.Audio.Media.TITLE,
      MediaStore.Audio.Media.ARTIST,
      MediaStore.Audio.Media.DATA,
      MediaStore.Audio.Media.DURATION
    };

    try (Cursor cursor =
        context
            .getContentResolver()
            .query(
                collection,
                projection,
                MediaStore.Audio.Media.IS_MUSIC + " != 0",
                null,
                MediaStore.Audio.Media.TITLE + " ASC")) {
      if (cursor != null) {
        while (cursor.moveToNext()) {
          String title = cursor.getString(0);
          String artist = cursor.getString(1);
          String path = cursor.getString(2);
          int duration = cursor.getInt(3);

          songList.add(new Song(title, artist, path, duration));
        }
      }
    }

    return songList;
  }
}
