package com.tdt.musicplayer.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.tdt.musicplayer.R;
import com.tdt.musicplayer.models.PlaybackMode;
import com.tdt.musicplayer.models.Song;
import com.tdt.musicplayer.player.MusicPlayerManager;
import com.tdt.musicplayer.repository.SongRepository;
import com.tdt.musicplayer.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {
  private DrawerLayout drawerLayout;
  private ListView songListView;
  private TextView songTitle, tvCurrentTime, tvTotalTime;
  private SeekBar seekBar;
  private ImageButton btnPlayPause;
  private MusicPlayerManager musicPlayerManager;
  private List<Song> songList = new ArrayList<>();
  private SongRepository songRepository;
  private ArrayAdapter<String> songListAdapter;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.home_fragment, container, false);
    songRepository = new SongRepository();

    setupUI(view);
    setupButtonListeners(view);
    setupPlaybackModeButton(view);

    return view;
  }

  private void setupUI(View view) {
    drawerLayout = view.findViewById(R.id.drawer_layout);
    songListView = view.findViewById(R.id.song_list_view);
    btnPlayPause = view.findViewById(R.id.btn_play_pause);
    seekBar = view.findViewById(R.id.seek_bar);
    tvCurrentTime = view.findViewById(R.id.tv_current_time);
    tvTotalTime = view.findViewById(R.id.tv_total_time);
    songTitle = view.findViewById(R.id.song_title);

    // Header view with buttons
    View headerView = LayoutInflater.from(getContext()).inflate(R.layout.item_music_header, songListView, false);
    songListView.addHeaderView(headerView);

    Button btnScanAll = headerView.findViewById(R.id.btn_scan_all);
    Button btnClearList = headerView.findViewById(R.id.btn_clear_list);

    btnScanAll.setOnClickListener(v -> loadSongs());
    btnClearList.setOnClickListener(v -> {
      songList.clear();
      songListAdapter.clear();
      Toast.makeText(getContext(), "\u0110\u00e3 xo\u00e1 danh s\u00e1ch nh\u1ea1c", Toast.LENGTH_SHORT).show();
    });

    songListAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
    songListView.setAdapter(songListAdapter);

    musicPlayerManager = new MusicPlayerManager(requireContext(), seekBar, tvCurrentTime, tvTotalTime);

    view.findViewById(R.id.btn_menu).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
    view.findViewById(R.id.btn_next).setOnClickListener(v -> {
      musicPlayerManager.playNext();
      updateTitle();
    });
    view.findViewById(R.id.btn_prev).setOnClickListener(v -> {
      musicPlayerManager.playPrev();
      updateTitle();
    });
  }

  private void setupButtonListeners(View view) {
    btnPlayPause.setOnClickListener(v -> {
      if (musicPlayerManager.isPlaying()) {
        musicPlayerManager.pause();
        btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
      } else {
        musicPlayerManager.resume();
        btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
      }
    });

    view.findViewById(R.id.btn_forward_5s).setOnClickListener(v -> musicPlayerManager.seekBy(5000));
    view.findViewById(R.id.btn_back_5s).setOnClickListener(v -> musicPlayerManager.seekBy(-5000));
  }

  private void setupPlaybackModeButton(View view) {
    ImageButton btnPlaybackMode = view.findViewById(R.id.btn_playback_mode);
    TextView textFeedback = view.findViewById(R.id.text_feedback);

    updatePlaybackModeIcon(btnPlaybackMode);

    btnPlaybackMode.setOnClickListener(v -> {
      PlaybackMode newMode;
      String message;
      int iconRes;

      switch (musicPlayerManager.getPlaybackMode()) {
        case NORMAL:
          newMode = PlaybackMode.REPEAT_ALL;
          iconRes = R.drawable.ic_repeat_all;
          message = "\ud83d\udd01 L\u1eb7p danh s\u00e1ch";
          break;
        case REPEAT_ALL:
          newMode = PlaybackMode.REPEAT_ONE;
          iconRes = R.drawable.ic_repeat_one;
          message = "\ud83d\udd02 L\u1eb7p 1 b\u00e0i";
          break;
        case REPEAT_ONE:
          newMode = PlaybackMode.SHUFFLE;
          iconRes = R.drawable.ic_shuffle;
          message = "\ud83d\udd00 Ph\u00e1t ng\u1eabu nhi\u00ean";
          break;
        default:
          newMode = PlaybackMode.NORMAL;
          iconRes = R.drawable.ic_play_order;
          message = "\u25b6\ufe0f Ph\u00e1t tu\u1ea7n t\u1ef1";
          break;
      }

      musicPlayerManager.setPlaybackMode(newMode);
      btnPlaybackMode.setImageResource(iconRes);
      ViewUtils.showQuickFeedback(textFeedback, message);
    });
  }

  private void updatePlaybackModeIcon(ImageButton button) {
    int iconRes;
    switch (musicPlayerManager.getPlaybackMode()) {
      case REPEAT_ALL:
        iconRes = R.drawable.ic_repeat_all;
        break;
      case REPEAT_ONE:
        iconRes = R.drawable.ic_repeat_one;
        break;
      case SHUFFLE:
        iconRes = R.drawable.ic_shuffle;
        break;
      default:
        iconRes = R.drawable.ic_play_order;
        break;
    }
    button.setImageResource(iconRes);
  }

  @SuppressLint("SetTextI18n")
  private void loadSongs() {
    songList = songRepository.loadLocalSongs(requireContext());
    songListAdapter.clear();
    songListAdapter.addAll(songList.stream().map(Song::getTitle).collect(Collectors.toList()));
    songListAdapter.notifyDataSetChanged();

    songListView.setOnItemClickListener((parent, view, position, id) -> {
      int realPosition = position - songListView.getHeaderViewsCount();
      if (realPosition >= 0 && realPosition < songList.size()) {
        musicPlayerManager.play(songList, realPosition);
        updateTitle();
        btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        drawerLayout.closeDrawer(GravityCompat.START);
      }
    });
  }

  @SuppressLint("SetTextI18n")
  private void updateTitle() {
    Song song = musicPlayerManager.getCurrentSong();
    if (song != null) {
      songTitle.setText("\u0110ang ph\u00e1t: " + song.getTitle());
    }
  }
}
