package com.tdt.musicplayer.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.tdt.musicplayer.R;
import com.tdt.musicplayer.models.HomeViewModel;
import com.tdt.musicplayer.models.PlaybackMode;
import com.tdt.musicplayer.models.Song;
import com.tdt.musicplayer.player.MusicPlayerManager;
import com.tdt.musicplayer.repository.SongRepository;
import com.tdt.musicplayer.utils.ViewUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {
  private DrawerLayout drawerLayout;
  private ListView songListView;
  private TextView songTitle, tvCurrentTime, tvTotalTime;
  private SeekBar seekBar;
  private ImageButton btnPlayPause;
  private MusicPlayerManager musicPlayerManager;
  private List<Song> songList = new ArrayList<>();
  private SongRepository songRepository;
  private ArrayAdapter<Song> songListAdapter;
  private HomeViewModel homeViewModel;
  private ImageView rotatingImage;
  private Animation rotateAnimation;


  @Nullable
  @Override
  public View onCreateView(
          @NonNull LayoutInflater inflater,
          @Nullable ViewGroup container,
          @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.home_fragment, container, false);
    songRepository = new SongRepository();
    homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
    homeViewModel.getSongList().observe(getViewLifecycleOwner(), songs -> {
      songListAdapter.clear();
      songListAdapter.addAll(songs);
      songListAdapter.notifyDataSetChanged();
    });

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

    musicPlayerManager =
            MusicPlayerManager.getInstance(requireContext(), seekBar, tvCurrentTime, tvTotalTime);

    musicPlayerManager.syncUI();
    updateTitle();
    btnPlayPause.setImageResource(
            musicPlayerManager.isPlaying()
                    ? android.R.drawable.ic_media_pause
                    : android.R.drawable.ic_media_play);

    songListAdapter =
            new ArrayAdapter<Song>(
                    requireContext(), R.layout.item_song_drawer, R.id.tvSongTitle, new ArrayList<>()) {
              @NonNull
              @Override
              public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                @SuppressLint("ViewHolder")
                View view =
                        LayoutInflater.from(getContext()).inflate(R.layout.item_song_drawer, parent, false);

                TextView tv = view.findViewById(R.id.tvSongTitle);
                ImageView ivPlay = view.findViewById(R.id.ivPlayIcon);

                Song currentSong = musicPlayerManager.getCurrentSong();
                Song thisSong = getItem(position);

                if (thisSong != null && tv != null) {
                  tv.setText(thisSong.getTitle());
                  boolean isCurrent = currentSong != null && thisSong.getId() == currentSong.getId();
                  tv.setSelected(isCurrent);
                  view.setBackgroundColor(isCurrent ? Color.parseColor("#FFF3E0") : Color.TRANSPARENT);
                }

                if (ivPlay != null) {
                  boolean isCurrent =
                          currentSong != null
                                  && thisSong != null
                                  && thisSong.getId() == currentSong.getId();
                  ivPlay.setVisibility(isCurrent ? View.VISIBLE : View.GONE);
                }

                return view;
              }
            };

    View headerView =
            LayoutInflater.from(getContext()).inflate(R.layout.item_music_header, songListView, false);
    songListView.addHeaderView(headerView);
    songListView.setAdapter(songListAdapter);

    Button btnScanAll = headerView.findViewById(R.id.btn_scan_all);
    Button btnClearList = headerView.findViewById(R.id.btn_clear_list);

    btnScanAll.setOnClickListener(v -> loadSongs());
    btnClearList.setOnClickListener(
            v -> {
              songList.clear();
              songListAdapter.clear();
              Toast.makeText(getContext(), "\uD83D\uDDD1️ Đã xoá danh sách nhạc", Toast.LENGTH_SHORT)
                      .show();
            });

    view.findViewById(R.id.btn_menu)
            .setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
    view.findViewById(R.id.btn_next)
            .setOnClickListener(
                    v -> {
                      musicPlayerManager.playNext();
                      updateTitle();
                      songListAdapter.notifyDataSetChanged();
                    });
    view.findViewById(R.id.btn_prev)
            .setOnClickListener(
                    v -> {
                      musicPlayerManager.playPrev();
                      updateTitle();
                      songListAdapter.notifyDataSetChanged();
                    });

    songListView.setOnItemClickListener((parent, view1, position, id) -> {
      // Lưu ý: nếu có header, ta cần trừ số header
      int realPosition = position - songListView.getHeaderViewsCount();
      if (realPosition >= 0 && realPosition < songListAdapter.getCount()) {
        // Lấy bài hát từ adapter (hoặc từ ViewModel)
        Song selectedSong = songListAdapter.getItem(realPosition);
        if (selectedSong != null) {
          // Giả sử HomeViewModel đã có danh sách bài hát từ loadSongs()
          musicPlayerManager.play(Objects.requireNonNull(homeViewModel.getSongList().getValue()), realPosition);
          updateTitle();
          btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
          songListAdapter.notifyDataSetChanged();
          drawerLayout.closeDrawer(GravityCompat.START);
        }
      }
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
                  message = "\uD83D\uDD01 Lặp danh sách";
                  break;
                case REPEAT_ALL:
                  newMode = PlaybackMode.REPEAT_ONE;
                  iconRes = R.drawable.ic_repeat_one;
                  message = "\uD83D\uDD02 Lặp 1 bài";
                  break;
                case REPEAT_ONE:
                  newMode = PlaybackMode.SHUFFLE;
                  iconRes = R.drawable.ic_shuffle;
                  message = "\uD83D\uDD00 Phát ngẫu nhiên";
                  break;
                default:
                  newMode = PlaybackMode.NORMAL;
                  iconRes = R.drawable.ic_play_order;
                  message = "▶️ Phát tuần tự";
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
//    songListAdapter.clear();

    if (songList.isEmpty()) {
      Toast.makeText(
                      getContext(), "⚠️ Không tìm thấy bài hát nào trong thiết bị!", Toast.LENGTH_SHORT)
              .show();
    } else {
//      songListAdapter.addAll(songList);
      homeViewModel.setSongList(songList);
      Toast.makeText(
                      getContext(), "✅ Quét xong: " + songList.size() + " bài hát", Toast.LENGTH_SHORT)
              .show();
    }
    songListAdapter.notifyDataSetChanged();

    songListView.setOnItemClickListener(
            (parent, view1, position, id) -> {
              int realPosition = position - songListView.getHeaderViewsCount();
              if (realPosition >= 0 && realPosition < songList.size()) {
                musicPlayerManager.play(songList, realPosition);
                updateTitle();
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                songListAdapter.notifyDataSetChanged();
                drawerLayout.closeDrawer(GravityCompat.START);
              }
            });
  }

  @SuppressLint("SetTextI18n")
  private void updateTitle() {
    Song song = musicPlayerManager.getCurrentSong();
    if (song != null) {
      songTitle.setText("Đang phát: " + song.getTitle());
      songTitle.setSelected(true);
    }
  }
}
