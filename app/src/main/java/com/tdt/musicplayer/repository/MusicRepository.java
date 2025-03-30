package com.tdt.musicplayer.repository;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.tdt.musicplayer.models.Song;

import java.util.ArrayList;
import java.util.List;

public class MusicRepository {
    private final Context context;

    public MusicRepository(Context context) {
        this.context = context;
    }

    public List<Song> getAllSongs(Context context) {
        List<Song> songs = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };

        Cursor cursor = this.context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(0);
                String artist = cursor.getString(1);
                String path = cursor.getString(2);
                int duration = cursor.getInt(3);

                if (path != null && !path.isEmpty()) {
                    songs.add(new Song(title, artist, path, duration));
                    Log.d("MusicRepository", "Loaded: " + title + " | " + artist + " | " + path);
                } else {
                    Log.e("MusicRepository", "Lỗi: Bài hát không có đường dẫn hợp lệ.");
                }
            }
            cursor.close();
        } else {
            Log.e("MusicRepository", "Failed to load music");
        }
        return songs;
    }
}
