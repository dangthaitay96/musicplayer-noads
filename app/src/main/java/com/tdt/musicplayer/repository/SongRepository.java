package com.tdt.musicplayer.repository;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import com.tdt.musicplayer.models.Song;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.os.Bundle;


public class SongRepository {
  private static final String TAG = "SongRepository";

  public List<Song> loadLocalSongs(Context context) {
    List<Song> songList = new ArrayList<>();

    Uri collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    String[] projection = {
      MediaStore.Audio.Media._ID,
      MediaStore.Audio.Media.TITLE,
      MediaStore.Audio.Media.ARTIST,
      MediaStore.Audio.Media.DATA,
      MediaStore.Audio.Media.DURATION
    };

    File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
    String targetPath = musicDir.getAbsolutePath();
    if (!targetPath.endsWith("/")) targetPath += "/";

    Log.d(TAG, "Scanning Music folder at: " + targetPath);

    String selection =
        MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.DATA + " LIKE ?";
    String[] selectionArgs = new String[] {targetPath + "%"};

    Bundle queryArgs = new Bundle();
    queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection);
    queryArgs.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs);

    try (Cursor cursor =
        context
            .getContentResolver()
            .query(
                collection,
                projection,
                selection,
                selectionArgs,
                MediaStore.Audio.Media.TITLE + " ASC")) {

      if (cursor != null) {
        Log.d(TAG, "Total songs found: " + cursor.getCount());
        while (cursor.moveToNext()) {
          long id = cursor.getLong(0);
          String title = cursor.getString(1);
          String artist = cursor.getString(2);
          String path = cursor.getString(3);
          int duration = cursor.getInt(4);

          Log.d(TAG, "Song: " + title + " | Path: " + path);
          Log.d(TAG, "Loaded: " + id + " | " + title);
          songList.add(new Song(id, title, artist, path, duration));
        }
      } else {
        Log.d(TAG, "Cursor is null");
      }
    } catch (Exception e) {
      Log.e(TAG, "Error loading songs: " + e.getMessage(), e);
    }

    return songList;
  }
}
