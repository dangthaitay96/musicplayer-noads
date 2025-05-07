package com.tdt.musicplayer.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
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
import androidx.lifecycle.ViewModelProvider;
import com.tdt.musicplayer.R;
import com.tdt.musicplayer.models.HomeViewModel;
import com.tdt.musicplayer.models.PlaybackMode;
import com.tdt.musicplayer.models.PlayerViewModel;
import com.tdt.musicplayer.models.SleepTimerViewModel;
import com.tdt.musicplayer.models.Song;
import com.tdt.musicplayer.player.MusicPlayerManager;
import com.tdt.musicplayer.repository.SongRepository;
import com.tdt.musicplayer.utils.DiscImageProvider;
import com.tdt.musicplayer.utils.DiscSwitcher;
import com.tdt.musicplayer.utils.PlaybackStateStorage;
import com.tdt.musicplayer.utils.ViewUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {
  private DrawerLayout drawerLayout;
  private ListView songListView;
  private TextView songTitle;
  private ImageButton btnPlayPause;
  private MusicPlayerManager musicPlayerManager;
  private List<Song> songList = new ArrayList<>();
  private SongRepository songRepository;
  private ArrayAdapter<Song> songListAdapter;
  private HomeViewModel homeViewModel;
  private PlayerViewModel playerViewModel;
  private DiscSwitcher discSwitcher;
  private boolean isSleepTimerRunning = false;
  private SleepTimerViewModel sleepTimerViewModel;
  private SeekBar seekBar;
  private boolean isSeekBarInteractive = false;
  private TextView tvCurrentTime;
  private TextView tvTotalTime;

  @Nullable
  @Override
  @SuppressLint("SetTextI18n")
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.home_fragment, container, false);
    songRepository = new SongRepository();

    homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
    playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);

    setupUI(view);
    setupButtonListeners(view);
    setupPlaybackModeButton(view);

    // 🔁 PHỤC HỒI TRẠNG THÁI LƯU TRƯỚC ĐÓ
    List<Song> restoredList = PlaybackStateStorage.getSavedSongList(requireContext());
    int savedIndex = PlaybackStateStorage.getSavedCurrentIndex(requireContext());

    if (!restoredList.isEmpty()) {
      songList = restoredList;
      homeViewModel.setSongList(songList);
      musicPlayerManager.setSongList(songList);

      if (savedIndex >= 0 && savedIndex < songList.size()) {
        Song song = songList.get(savedIndex);
        musicPlayerManager.setCurrentIndex(savedIndex);
        musicPlayerManager.setCurrentSong(song);
        musicPlayerManager.seekTo(0);
        playerViewModel.setCurrentSong(song);
        playerViewModel.setCurrentIndex(savedIndex);
        playerViewModel.setDiscSpinning(false);
        setSeekBarInteractive(true);
        btnPlayPause.setEnabled(true);
        btnPlayPause.setImageResource(android.R.drawable.ic_media_play);

        songListAdapter.notifyDataSetChanged();
        int scrollIndex = savedIndex + songListView.getHeaderViewsCount();
        songListView.smoothScrollToPosition(scrollIndex);
      }
    }

    homeViewModel
        .getSongList()
        .observe(
            getViewLifecycleOwner(),
            songs -> {
              songListAdapter.clear();
              songListAdapter.addAll(songs);
              songListAdapter.notifyDataSetChanged();

              boolean hasSongs = !songs.isEmpty();

              btnPlayPause.setEnabled(hasSongs);
              view.findViewById(R.id.btn_prev).setEnabled(hasSongs);
              view.findViewById(R.id.btn_next).setEnabled(hasSongs);
              view.findViewById(R.id.btn_forward_5s).setEnabled(hasSongs);
              view.findViewById(R.id.btn_back_5s).setEnabled(hasSongs);
            });

    playerViewModel
        .getCurrentSong()
        .observe(
            getViewLifecycleOwner(),
            song -> {
              if (song != null) {
                songTitle.setText("Đang phát: " + song.getTitle());
                songTitle.setSelected(true);
              }
            });

    playerViewModel
        .isDiscSpinning()
        .observe(
            getViewLifecycleOwner(),
            spinning -> {
              if (discSwitcher != null) {
                if (spinning != null && spinning) {
                  discSwitcher.start();
                } else {
                  discSwitcher.stop();
                }
              }
            });

    playerViewModel
        .getCurrentDiscIndex()
        .observe(
            getViewLifecycleOwner(),
            index -> {
              if (discSwitcher != null && index != null) {
                discSwitcher.setCurrentIndex(index);
              }
            });

    return view;
  }

  @SuppressLint({"DefaultLocale", "SetTextI18n", "ClickableViewAccessibility"})
  private void setupUI(View view) {
    ImageButton btnPrev = view.findViewById(R.id.btn_prev);
    ImageButton btnNext = view.findViewById(R.id.btn_next);
    ImageButton btnForward = view.findViewById(R.id.btn_forward_5s);
    ImageButton btnBackward = view.findViewById(R.id.btn_back_5s);

    drawerLayout = view.findViewById(R.id.drawer_layout);
    songListView = view.findViewById(R.id.song_list_view);
    btnPlayPause = view.findViewById(R.id.btn_play_pause);
    seekBar = view.findViewById(R.id.seek_bar);
    tvCurrentTime = view.findViewById(R.id.tv_current_time);
    tvTotalTime = view.findViewById(R.id.tv_total_time);
    songTitle = view.findViewById(R.id.song_title);
    ImageView rotatingImage = view.findViewById(R.id.rotating_image);
    discSwitcher = new DiscSwitcher(getContext(), rotatingImage, DiscImageProvider.getDiscImages());
    discSwitcher.setOnDiscIndexChangeListener(index -> playerViewModel.setCurrentDiscIndex(index));
    sleepTimerViewModel = new ViewModelProvider(requireActivity()).get(SleepTimerViewModel.class);

    btnPlayPause.setEnabled(false);
    btnPrev.setEnabled(false);
    btnNext.setEnabled(false);
    btnForward.setEnabled(false);
    btnBackward.setEnabled(false);
    seekBar.setOnTouchListener((v, event) -> !isSeekBarInteractive);

    musicPlayerManager = MusicPlayerManager.getInstance(seekBar, tvCurrentTime, tvTotalTime);
    musicPlayerManager.syncUI();
    btnPlayPause.setImageResource(
        musicPlayerManager.isPlaying()
            ? android.R.drawable.ic_media_pause
            : android.R.drawable.ic_media_play);

    playerViewModel
        .getSleepTimerRemaining()
        .observe(
            getViewLifecycleOwner(),
            millis -> {
              TextView countdownText = view.findViewById(R.id.tv_sleep_timer);
              if (millis != null && millis > 0) {
                long mins = millis / 60000;
                long secs = (millis % 60000) / 1000;
                countdownText.setText(String.format("⏰ %02d:%02d", mins, secs));
                countdownText.setVisibility(View.VISIBLE);
              } else {
                countdownText.setVisibility(View.GONE);
              }
            });

    sleepTimerViewModel
        .getRemainingTime()
        .observe(
            getViewLifecycleOwner(),
            millis -> {
              TextView countdownText = view.findViewById(R.id.tv_sleep_timer);
              if (millis != null && millis > 0) {
                long hours = millis / (1000 * 60 * 60);
                long minutes = (millis / (1000 * 60)) % 60;
                long seconds = (millis / 1000) % 60;
                countdownText.setText(String.format("⏰ %02d:%02d:%02d", hours, minutes, seconds));

                countdownText.setVisibility(View.VISIBLE);
              } else {
                countdownText.setVisibility(View.GONE);
              }
            });

    sleepTimerViewModel
        .isRunning()
        .observe(
            getViewLifecycleOwner(), running -> isSleepTimerRunning = running != null && running);

    songListAdapter =
        new ArrayAdapter<Song>(
            requireContext(), R.layout.item_song_drawer, R.id.tvSongTitle, new ArrayList<>()) {
          @NonNull
          @Override
          public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            @SuppressLint("ViewHolder")
            View itemView =
                LayoutInflater.from(getContext()).inflate(R.layout.item_song_drawer, parent, false);
            TextView tv = itemView.findViewById(R.id.tvSongTitle);
            ImageView ivPlay = itemView.findViewById(R.id.ivPlayIcon);

            Song currentSong = musicPlayerManager.getCurrentSong();
            Song thisSong = getItem(position);

            if (thisSong != null && tv != null) {
              tv.setText(thisSong.getTitle());
              boolean isCurrent = currentSong != null && thisSong.getId() == currentSong.getId();
              tv.setSelected(isCurrent);
              itemView.setBackgroundColor(
                  isCurrent ? Color.parseColor("#FFF3E0") : Color.TRANSPARENT);
              tv.setTextColor(isCurrent ? Color.parseColor("#B71C1C") : Color.WHITE);
            }

            if (ivPlay != null) {
              boolean isCurrent =
                  currentSong != null
                      && thisSong != null
                      && thisSong.getId() == currentSong.getId();
              ivPlay.setVisibility(isCurrent ? View.VISIBLE : View.GONE);
            }

            return itemView;
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
          homeViewModel.setSongList(new ArrayList<>());
          musicPlayerManager.setSongList(new ArrayList<>());
          musicPlayerManager.pause();
          playerViewModel.setCurrentSong(null);
          playerViewModel.setDiscSpinning(false);
          playerViewModel.setCurrentIndex(-1);
          songTitle.setText("Đang phát: ");
          songTitle.setSelected(false);
          btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
          btnPlayPause.setEnabled(false);
          tvCurrentTime.setText("00:00");
          tvTotalTime.setText("00:00");
          seekBar.setProgress(0);
          setSeekBarInteractive(false);

          Toast.makeText(getContext(), "🗑️ Đã xoá danh sách nhạc", Toast.LENGTH_SHORT).show();
        });

    view.findViewById(R.id.btn_menu)
        .setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
    view.findViewById(R.id.btn_next)
        .setOnClickListener(
            v -> {
              musicPlayerManager.playNext();
              playerViewModel.setCurrentSong(musicPlayerManager.getCurrentSong());
              playerViewModel.setCurrentIndex(musicPlayerManager.getCurrentIndex());
              playerViewModel.setDiscSpinning(true);
              btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
              songListAdapter.notifyDataSetChanged();
              setSeekBarInteractive(true);
              PlaybackStateStorage.saveState(
                  requireContext(), songList, musicPlayerManager.getCurrentIndex());
            });
    view.findViewById(R.id.btn_prev)
        .setOnClickListener(
            v -> {
              musicPlayerManager.playPrev();
              playerViewModel.setCurrentSong(musicPlayerManager.getCurrentSong());
              playerViewModel.setCurrentIndex(musicPlayerManager.getCurrentIndex());
              playerViewModel.setDiscSpinning(true);
              btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
              songListAdapter.notifyDataSetChanged();
              setSeekBarInteractive(true);
              PlaybackStateStorage.saveState(
                  requireContext(), songList, musicPlayerManager.getCurrentIndex());
            });

    musicPlayerManager.setOnSongChangeListener(
        newSong -> {
          playerViewModel.setCurrentSong(newSong);
          playerViewModel.setCurrentIndex(musicPlayerManager.getCurrentIndex());
          songListAdapter.notifyDataSetChanged();
          int index = songList.indexOf(newSong);
          if (index >= 0) {
            songListView.smoothScrollToPosition(index + songListView.getHeaderViewsCount());
          }
        });

    songListView.setOnItemClickListener(
        (parent, view1, position, id) -> {
          int realPosition = position - songListView.getHeaderViewsCount();
          setSeekBarInteractive(true);

          if (realPosition >= 0 && realPosition < songListAdapter.getCount()) {
            Song selectedSong = songListAdapter.getItem(realPosition);
            if (selectedSong != null) {
              musicPlayerManager.play(
                  Objects.requireNonNull(homeViewModel.getSongList().getValue()), realPosition);
              playerViewModel.setCurrentSong(selectedSong);
              playerViewModel.setCurrentIndex(realPosition);
              playerViewModel.setDiscSpinning(true);
              btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
              songListAdapter.notifyDataSetChanged();
              drawerLayout.closeDrawer(GravityCompat.START);
              PlaybackStateStorage.saveState(requireContext(), songList, realPosition);
            }
          }
        });
  }

  private void setupButtonListeners(View view) {
    ImageButton btnTimer = view.findViewById(R.id.btn_timer);
    btnTimer.setOnClickListener(v -> showCustomTimerDialog());

    btnPlayPause.setOnClickListener(
        v -> {
          if (musicPlayerManager.isPlaying()) {
            musicPlayerManager.pause();
            playerViewModel.setDiscSpinning(false);
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
          } else {
            Song currentSong = playerViewModel.getCurrentSong().getValue();
            if (currentSong != null) {
              if (musicPlayerManager.isPrepared()) {
                musicPlayerManager.resume();
              } else {
                musicPlayerManager.play(
                    Objects.requireNonNull(homeViewModel.getSongList().getValue()),
                    musicPlayerManager.getCurrentIndex());
              }
              playerViewModel.setDiscSpinning(true);
              btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
            } else {
              Toast.makeText(getContext(), "⚠️ Chưa chọn bài hát!", Toast.LENGTH_SHORT).show();
            }
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
          playerViewModel.setPlaybackMode(newMode);
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

    resetPlaybackState();
    if (songList.isEmpty()) {
      Toast.makeText(
              getContext(), "⚠️ Không tìm thấy bài hát nào trong thiết bị!", Toast.LENGTH_SHORT)
          .show();
    } else {
      homeViewModel.setSongList(songList);
      musicPlayerManager.setSongList(songList);
      Toast.makeText(
              getContext(), "✅ Quét xong: " + songList.size() + " bài hát", Toast.LENGTH_SHORT)
          .show();
    }
    songListAdapter.notifyDataSetChanged();
  }

  private void startSleepTimer(long millis) {
    sleepTimerViewModel.start(
        millis,
        () -> {
          musicPlayerManager.pause();
          if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), "⏰ Đã tắt nhạc sau hẹn giờ", Toast.LENGTH_SHORT).show();
          }
        });
    Toast.makeText(requireContext(), "⏳ Đã đặt hẹn giờ", Toast.LENGTH_SHORT).show();
  }

  private void cancelSleepTimer() {
    sleepTimerViewModel.cancel();
    Toast.makeText(requireContext(), "❌ Đã huỷ hẹn giờ", Toast.LENGTH_SHORT).show();
  }

  private void showCustomTimerDialog() {
    EditText input = new EditText(requireContext());
    input.setHint("Nhập số phút");
    input.setInputType(InputType.TYPE_CLASS_NUMBER);
    input.setBackgroundResource(R.drawable.bg_dialog_input);
    input.setTextColor(Color.BLACK);
    input.setPadding(24, 16, 24, 16);
    input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(3)});
    input.setGravity(Gravity.CENTER);

    LinearLayout container = new LinearLayout(requireContext());
    container.setOrientation(LinearLayout.VERTICAL);
    container.setPadding(24, 24, 24, 0);

    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(
            (int) (getResources().getDisplayMetrics().density * 240),
            ViewGroup.LayoutParams.WRAP_CONTENT);
    input.setLayoutParams(params);

    container.addView(input);

    AlertDialog.Builder builder =
        new AlertDialog.Builder(requireContext())
            .setTitle("⏰ Hẹn giờ tắt nhạc")
            .setView(container)
            .setNegativeButton("Huỷ", null)
            .setPositiveButton(
                "Đặt",
                (d, which) -> {
                  String text = input.getText().toString().trim();
                  if (!text.isEmpty()) {
                    long minutes = Long.parseLong(text);
                    startSleepTimer(minutes * 60_000);
                  }
                });

    if (isSleepTimerRunning) {
      builder.setNeutralButton("Tắt hẹn giờ", (d, which) -> cancelSleepTimer());
    }

    AlertDialog dialog = builder.create();
    dialog.setOnShowListener(
        d ->
            Objects.requireNonNull(dialog.getWindow())
                .setBackgroundDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog_input)));
    dialog.show();
  }

  @SuppressLint("ClickableViewAccessibility")
  private void setSeekBarInteractive(boolean enabled) {
    isSeekBarInteractive = enabled;
  }

  @SuppressLint("SetTextI18n")
  private void resetPlaybackState() {
    musicPlayerManager.resetPlayback();
    playerViewModel.setCurrentSong(null);
    playerViewModel.setCurrentIndex(-1);
    playerViewModel.setDiscSpinning(false);
    btnPlayPause.setEnabled(false);
    btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
    songTitle.setText("Đang phát: ");
    songTitle.setSelected(false);
    tvCurrentTime.setText("00:00");
    tvTotalTime.setText("00:00");
    seekBar.setProgress(0);
    setSeekBarInteractive(false);
  }

  @Override
  public void onResume() {
    super.onResume();
  }
}
