package com.tdt.musicplayer.services;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.File;
import java.text.Normalizer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.StreamInfo;

public class AudioConverterManager {
  private final DownloadService downloadService;
  private final ConvertService convertService;

  public AudioConverterManager() {
    this.downloadService = new DownloadService();
    this.convertService = new ConvertService();
  }

  public void startDownloadAndConvert(
      String youtubeUrl,
      Runnable onStart,
      Consumer<Integer> onProgress,
      Runnable onSuccess,
      Runnable onFail,
      Consumer<String> onTitleReady) {

    onStart.run();

    new Thread(
            () -> {
              AtomicReference<File> mp3FileRef = new AtomicReference<>(null);
              AtomicBoolean convertDone = new AtomicBoolean(false);
              AtomicBoolean progressDone = new AtomicBoolean(false);

              try {
                NewPipe.init(DownloaderImpl.getInstance());
                StreamInfo streamInfo = StreamInfo.getInfo(NewPipe.getService(0), youtubeUrl);

                if (onTitleReady != null) {
                  new Handler(Looper.getMainLooper())
                      .post(() -> onTitleReady.accept(streamInfo.getName()));
                }

                List<AudioStream> audioStreams = streamInfo.getAudioStreams();
                if (audioStreams == null || audioStreams.isEmpty()) {
                  throw new Exception("Không tìm thấy audio stream.");
                }

                AudioStream audioStream = audioStreams.get(0);
                String audioUrl = audioStream.getUrl();

                File downloadDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                if (!downloadDir.exists()) downloadDir.mkdirs();

                String songName = sanitizeFileName(streamInfo.getName());

                File m4aFile;
                try {
                  m4aFile =
                      downloadService.downloadAudio(audioUrl, downloadDir, songName, onProgress);
                } catch (Exception e) {
                  Log.e("AudioConverterManager", "Lỗi khi tải file: " + e.getMessage(), e);
                  new Handler(Looper.getMainLooper()).post(onFail);
                  return;
                }

                if (!m4aFile.exists() || m4aFile.length() < 200 * 1024) {
                  Log.e("AudioConverterManager", "File m4a không hợp lệ hoặc quá nhỏ.");
                  if (m4aFile.exists()) m4aFile.delete();
                  new Handler(Looper.getMainLooper()).post(onFail);
                  return;
                }

                new Thread(
                        () -> {
                          for (int p = 71; p <= 100; p++) {
                            int finalP = p;
                            new Handler(Looper.getMainLooper())
                                .post(
                                    () -> {
                                      if (onProgress != null) onProgress.accept(finalP);
                                    });
                            try {
                              Thread.sleep(50);
                            } catch (InterruptedException ignored) {
                            }
                          }
                          progressDone.set(true);
                          checkAndFinish(
                              mp3FileRef.get(),
                              onSuccess,
                              onFail,
                              convertDone.get(),
                              progressDone.get());
                        })
                    .start();

                File mp3File = convertService.convertToMp3(m4aFile, downloadDir, songName);
                mp3FileRef.set(mp3File);
                convertDone.set(true);

                try {
                  if (!m4aFile.delete()) {
                    Log.w(
                        "AudioConverterManager",
                        "Không thể xoá file tạm: " + m4aFile.getAbsolutePath());
                  }
                } catch (Exception ex) {
                  Log.e("AudioConverterManager", "Lỗi khi xoá file m4a: " + ex.getMessage());
                }

                checkAndFinish(mp3File, onSuccess, onFail, convertDone.get(), progressDone.get());

              } catch (Exception e) {
                Log.e("AudioConverterManager", "Lỗi khi tải/convert: " + e.getMessage(), e);
                new Handler(Looper.getMainLooper()).post(onFail);
              }
            })
        .start();
  }

  private String sanitizeFileName(String input) {
    String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
    String noDiacritics =
        normalized
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
            .replaceAll("đ", "d")
            .replaceAll("Đ", "D");
    return noDiacritics.replaceAll("[\\\\/:*?\"<>|]", "_").replaceAll("[^\\x20-\\x7E]", "_");
  }

  private void checkAndFinish(
      File mp3File,
      Runnable onSuccess,
      Runnable onFail,
      boolean convertDone,
      boolean progressDone) {
    if (convertDone && progressDone) {
      new Handler(Looper.getMainLooper())
          .post(
              () -> {
                if (mp3File != null && mp3File.exists() && mp3File.length() > 200 * 1024) {
                  onSuccess.run();
                } else {
                  onFail.run();
                }
              });
    }
  }
}
