package com.tdt.musicplayer.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.tdt.musicplayer.R;
import com.tdt.musicplayer.models.PlaybackMode;
import com.tdt.musicplayer.models.Song;
import com.tdt.musicplayer.player.MusicPlayerManager;
import com.tdt.musicplayer.repository.SongRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {
  private static final int PERMISSION_REQUEST_CODE = 123;

  private DrawerLayout drawerLayout;
  private ListView songListView;
  private TextView songTitle, tvCurrentTime, tvTotalTime;
  private SeekBar seekBar;
  private ImageButton btnPlayPause;
  private MusicPlayerManager musicPlayerManager;
  private List<Song> songList = new ArrayList<>();
  private SongRepository songRepository;



  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.home_fragment, container, false);
    songRepository = new SongRepository();

    setupUI(view);
    setupButtonListeners(view);
    setupPlaybackModeButton(view);
    checkPermissionAndLoadSongs();

    return view;
  }

  private void setupUI(View view) {
    drawerLayout = view.findViewById(R.id.drawer_layout);
    btnPlayPause = view.findViewById(R.id.btn_play_pause);
    songListView = view.findViewById(R.id.song_list_view);
    songTitle = view.findViewById(R.id.song_title);
    seekBar = view.findViewById(R.id.seek_bar);
    tvCurrentTime = view.findViewById(R.id.tv_current_time);
    tvTotalTime = view.findViewById(R.id.tv_total_time);
    // Gắn header có 2 nút chức năng
    View headerView = LayoutInflater.from(getContext()).inflate(R.layout.item_music_header, songListView, false);
    songListView.addHeaderView(headerView);

    Button btnScanAll = headerView.findViewById(R.id.btn_scan_all);
    Button btnClearList = headerView.findViewById(R.id.btn_clear_list);

    btnScanAll.setOnClickListener(v -> checkPermissionAndLoadSongs());

    btnClearList.setOnClickListener(v -> {
      songList.clear();
      songListView.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, new ArrayList<>()));
      Toast.makeText(getContext(), "Đã xoá danh sách nhạc", Toast.LENGTH_SHORT).show();
    });

    musicPlayerManager =
        new MusicPlayerManager(requireContext(), seekBar, tvCurrentTime, tvTotalTime);

    ImageButton btnMenu = view.findViewById(R.id.btn_menu);
    btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

    view.findViewById(R.id.btn_next)
        .setOnClickListener(
            v -> {
              musicPlayerManager.playNext();
              updateTitle();
            });

    view.findViewById(R.id.btn_prev)
        .setOnClickListener(
            v -> {
              musicPlayerManager.playPrev();
              updateTitle();
            });
  }

  private void setupButtonListeners(View view) {
    btnPlayPause.setOnClickListener(
        v -> {
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

    btnPlaybackMode.setOnClickListener(
        v -> {
          PlaybackMode newMode;
          String message;
          int iconRes;

          switch (musicPlayerManager.getPlaybackMode()) {
            case NORMAL:
              newMode = PlaybackMode.REPEAT_ALL;
              iconRes = R.drawable.ic_repeat_all;
              message = "🔁 Lặp danh sách";
              break;
            case REPEAT_ALL:
              newMode = PlaybackMode.REPEAT_ONE;
              iconRes = R.drawable.ic_repeat_one;
              message = "🔂 Lặp 1 bài";
              break;
            case REPEAT_ONE:
              newMode = PlaybackMode.SHUFFLE;
              iconRes = R.drawable.ic_shuffle;
              message = "🔀 Phát ngẫu nhiên";
              break;
            default:
              newMode = PlaybackMode.NORMAL;
              iconRes = R.drawable.ic_play_order;
              message = "▶️ Phát tuần tự";
              break;
          }

          musicPlayerManager.setPlaybackMode(newMode);
          btnPlaybackMode.setImageResource(iconRes);
          showQuickFeedback(textFeedback, message);
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

  private void checkPermissionAndLoadSongs() {
    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_AUDIO)
        != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(
          new String[] {Manifest.permission.READ_MEDIA_AUDIO}, PERMISSION_REQUEST_CODE);
    } else {
      loadSongs();
    }
  }

  @SuppressLint("SetTextI18n")
  private void loadSongs() {
    songList = songRepository.loadLocalSongs(requireContext());

    ArrayAdapter<String> adapter =
            new ArrayAdapter<>(
                    getContext(),
                    android.R.layout.simple_list_item_1,
                    songList.stream().map(Song::getTitle).collect(Collectors.toList()));
    songListView.setAdapter(adapter);

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

  private void updateTitle() {
    Song song = musicPlayerManager.getCurrentSong();
    if (song != null) {
      songTitle.setText("Đang phát: " + song.getTitle());
    }
  }

  private void showQuickFeedback(TextView textView, String message) {
    textView.setText(message);
    textView.setVisibility(View.VISIBLE);
    textView.setAlpha(1f);
    textView.animate().cancel();
    textView
        .animate()
        .alpha(0f)
        .setDuration(1000)
        .setStartDelay(1500)
        .withEndAction(() -> textView.setVisibility(View.INVISIBLE))
        .start();
  }
}
