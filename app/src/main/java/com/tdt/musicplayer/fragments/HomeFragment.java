package com.tdt.musicplayer.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.tdt.musicplayer.R;
import com.tdt.musicplayer.models.Song;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {

  private static final int PERMISSION_REQUEST_CODE = 123;
  private DrawerLayout drawerLayout;
  private ListView songListView;
  private TextView songTitle, tvCurrentTime, tvTotalTime;
  private MediaPlayer mediaPlayer;
  private List<Song> songList = new ArrayList<>();
  private ImageButton btnPlayPause;
  private boolean isPlaying = false;
  private int currentSongIndex = -1;
  private SeekBar seekBar;
  private Handler timeHandler = new Handler();
  private Runnable timeRunnable;


  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.home_fragment, container, false);

    setupUI(view);
    setupButtonListeners();
    setupRotatingImages(view);
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

    setupSeekBarAndTimeUpdater();

    ImageButton btnMenu = view.findViewById(R.id.btn_menu);
    btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

    ImageButton btnNext = view.findViewById(R.id.btn_next);
    ImageButton btnPrev = view.findViewById(R.id.btn_prev);
    btnNext.setOnClickListener(v -> playNextSong());
    btnPrev.setOnClickListener(v -> playPrevSong());

    // Header với các nút trong list
    View headerView = LayoutInflater.from(getContext())
            .inflate(R.layout.item_music_header, songListView, false);
    songListView.addHeaderView(headerView);

    Button btnScanAll = headerView.findViewById(R.id.btn_scan_all);
    Button btnClearList = headerView.findViewById(R.id.btn_clear_list);

    btnScanAll.setOnClickListener(v -> checkPermissionAndLoadSongs());
    btnClearList.setOnClickListener(v -> {
      songList.clear();
      ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(
              requireContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
      songListView.setAdapter(emptyAdapter);
      Toast.makeText(getContext(), "Đã xoá danh sách nhạc", Toast.LENGTH_SHORT).show();
    });
  }

  private void setupSeekBarAndTimeUpdater() {
    // Lắng nghe sự kiện tua
    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser && mediaPlayer != null) {
          mediaPlayer.seekTo(progress);
          tvCurrentTime.setText(formatTime(progress));
        }
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {}
      @Override public void onStopTrackingTouch(SeekBar seekBar) {}
    });
  }

  private String formatTime(int millis) {
    int minutes = (millis / 1000) / 60;
    int seconds = (millis / 1000) % 60;
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
  }


  private void setupButtonListeners() {
    btnPlayPause.setOnClickListener(v -> {
      if (mediaPlayer != null) {
        if (mediaPlayer.isPlaying()) {
          mediaPlayer.pause();
          isPlaying = false;
          btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
        } else {
          mediaPlayer.start();
          isPlaying = true;
          btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        }
      }
    });
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
    songList.clear(); // reset danh sách

    Uri collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    String[] projection = {
      MediaStore.Audio.Media._ID,
      MediaStore.Audio.Media.TITLE,
      MediaStore.Audio.Media.ARTIST,
      MediaStore.Audio.Media.DATA, // Đường dẫn file nhạc
      MediaStore.Audio.Media.DURATION // Thời lượng
    };
    String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

    try (Cursor cursor =
        requireContext()
            .getContentResolver()
            .query(
                collection, projection, selection, null, MediaStore.Audio.Media.TITLE + " ASC")) {
      if (cursor != null) {
        int titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
        int artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
        int pathCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        int durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);

        while (cursor.moveToNext()) {
          String title = cursor.getString(titleCol);
          String artist = cursor.getString(artistCol);
          String path = cursor.getString(pathCol);
          int duration = cursor.getInt(durationCol);

          Song song = new Song(title, artist, path, duration);
          songList.add(song);
        }
      }
    }

    // Hiển thị danh sách tiêu đề trong ListView
    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(
            getContext(),
            android.R.layout.simple_list_item_1,
            songList.stream().map(Song::getTitle).collect(Collectors.toList()));
    songListView.setAdapter(adapter);

    songListView.setOnItemClickListener(
        (parent, view1, position, id) -> {
          int realPosition = position - songListView.getHeaderViewsCount();
          if (realPosition >= 0 && realPosition < songList.size()) {
            currentSongIndex = realPosition;
            Song selectedSong = songList.get(realPosition);
            if (songTitle != null) {
              songTitle.setText("Đang phát: " + selectedSong.getTitle());
            }

            playSong(selectedSong.getFilePath());

            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
            isPlaying = true;
            drawerLayout.closeDrawer(GravityCompat.START);
          }
        });
  }

  private void playSong(String filePath) {
    try {
      if (mediaPlayer != null) {
        mediaPlayer.stop();
        mediaPlayer.release();
      }

      mediaPlayer = new MediaPlayer();
      mediaPlayer.setDataSource(filePath);
      mediaPlayer.prepare();
      mediaPlayer.start();

      int duration = mediaPlayer.getDuration();
      seekBar.setMax(duration);
      tvTotalTime.setText(formatTime(duration));

// Cập nhật thời gian phát
      timeRunnable = new Runnable() {
        @Override
        public void run() {
          if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            int current = mediaPlayer.getCurrentPosition();
            seekBar.setProgress(current);
            tvCurrentTime.setText(formatTime(current));
            timeHandler.postDelayed(this, 500);
          }
        }
      };
      timeHandler.post(timeRunnable);


    } catch (IOException e) {
      Toast.makeText(getContext(), "Không thể phát nhạc", Toast.LENGTH_SHORT).show();
      e.printStackTrace();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == 999 && resultCode == Activity.RESULT_OK && data != null) {
      Uri uri = data.getData();
      if (uri != null) {
        // Truy cập lâu dài
        requireContext()
            .getContentResolver()
            .takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Lấy thông tin file (tên, duration nếu có thể)
        String title = getFileNameFromUri(uri);

        Song newSong = new Song(title, "Unknown", uri.toString(), 0);
        songList.add(newSong);

        // Cập nhật ListView
        ArrayAdapter<String> adapter =
            new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_list_item_1,
                songList.stream().map(Song::getTitle).collect(Collectors.toList()));
        songListView.setAdapter(adapter);

        Toast.makeText(getContext(), "Đã thêm: " + title, Toast.LENGTH_SHORT).show();
      }
    }
  }

  private String getFileNameFromUri(Uri uri) {
    String result = "File nhạc";
    Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
    if (cursor != null && cursor.moveToFirst()) {
      int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
      if (nameIndex != -1) {
        result = cursor.getString(nameIndex);
      }
      cursor.close();
    }
    return result;
  }

  private void playNextSong() {
    if (songList.isEmpty()) return;
    currentSongIndex = (currentSongIndex + 1) % songList.size(); // quay vòng nếu vượt quá
    playSelectedSong(currentSongIndex);
  }

  private void playPrevSong() {
    if (songList.isEmpty()) return;
    currentSongIndex = (currentSongIndex - 1 + songList.size()) % songList.size(); // tránh < 0
    playSelectedSong(currentSongIndex);
  }

  private void playSelectedSong(int index) {
    Song selectedSong = songList.get(index);
    songTitle.setText("Đang phát: " + selectedSong.getTitle());
    playSong(selectedSong.getFilePath());
    btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
    isPlaying = true;
  }

  private void setupRotatingImages(View view) {
    ImageView rotatingImage = view.findViewById(R.id.rotating_image);
    Handler imageSwitchHandler = new Handler();

    int[] images = {
            R.drawable.baru,
            R.drawable.fugaku,
            R.drawable.indra
    };
    final int[] currentImageIndex = {0};
    Animation rotate = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_forever);
    rotatingImage.setImageResource(images[currentImageIndex[0]]);
    rotatingImage.startAnimation(rotate);

    Runnable imageSwitcher = new Runnable() {
      @Override
      public void run() {
        currentImageIndex[0] = (currentImageIndex[0] + 1) % images.length;
        rotatingImage.setImageResource(images[currentImageIndex[0]]);

        Animation rotateAgain = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_forever);
        rotatingImage.startAnimation(rotateAgain);

        imageSwitchHandler.postDelayed(this, 60000);
      }
    };

    imageSwitchHandler.postDelayed(imageSwitcher, 60000);
  }
}
