package com.tdt.musicplayer.repository;

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

public class SongRepository {
  private static final String TAG = "SongRepository";

  public List<Song> loadLocalSongs(Context context) {
    List<Song> songList = new ArrayList<>();

    // URI của MediaStore chứa dữ liệu file âm thanh
    Uri collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    String[] projection = {
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION
    };

    // Lấy đường dẫn thư mục Music một cách động
    File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);


    String targetFolder = musicDir.getAbsolutePath();
    if (!targetFolder.endsWith("/")) {
      targetFolder = targetFolder + "/";
    }

    Log.d(TAG, "Target folder: " + targetFolder);

    // Điều kiện: chỉ lấy các file là nhạc (IS_MUSIC != 0) và có đường dẫn bắt đầu bằng targetFolder
//    String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND "
//            + MediaStore.Audio.Media.DATA + " LIKE ?";
//    String[] selectionArgs = new String[] { targetFolder + "%" };
    String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
    String[] selectionArgs = null;


    try (Cursor cursor = context.getContentResolver().query(
            collection,
            projection,
            selection,
            selectionArgs,
            MediaStore.Audio.Media.TITLE + " ASC")) {
      if (cursor != null) {
        Log.d(TAG, "Total songs found: " + cursor.getCount());
        while (cursor.moveToNext()) {
          String title = cursor.getString(0);
          String artist = cursor.getString(1);
          String path = cursor.getString(2);
          int duration = cursor.getInt(3);

          Log.d(TAG, "Song: " + title + " | Path: " + path);
          songList.add(new Song(title, artist, path, duration));
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
