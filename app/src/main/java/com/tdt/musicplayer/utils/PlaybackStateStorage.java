package com.tdt.musicplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tdt.musicplayer.models.Song;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
public class PlaybackStateStorage {
    private static final String PREF_NAME = "player_state";
    private static final String KEY_SONG_LIST = "song_list";
    private static final String KEY_CURRENT_INDEX = "current_index";

    public static void saveState(Context context, List<Song> songList, int currentIndex) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String songListJson = gson.toJson(songList);

        editor.putString(KEY_SONG_LIST, songListJson);
        editor.putInt(KEY_CURRENT_INDEX, currentIndex);

        editor.apply();
    }

    public static List<Song> getSavedSongList(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_SONG_LIST, null);

        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<List<Song>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    public static int getSavedCurrentIndex(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_CURRENT_INDEX, -1);
    }

    public static void clear(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
