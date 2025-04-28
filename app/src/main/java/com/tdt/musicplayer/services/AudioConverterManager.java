package com.tdt.musicplayer.services;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import java.io.File;
import java.text.Normalizer;
import java.util.List;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.StreamInfo;

public class AudioConverterManager {

  private final Context context;
  private final DownloadService downloadService;
  private final ConvertService convertService;

  public AudioConverterManager(Context context) {
    this.context = context;
    this.downloadService = new DownloadService();
    this.convertService = new ConvertService();
  }

  public void startDownloadAndConvert(
      String youtubeUrl, Runnable onStart, Runnable onSuccess, Runnable onFail) {
    onStart.run();

    new Thread(
            () -> {
              try {
                NewPipe.init(DownloaderImpl.getInstance());
                StreamInfo streamInfo = StreamInfo.getInfo(NewPipe.getService(0), youtubeUrl);

                List<AudioStream> audioStreams = streamInfo.getAudioStreams();
                if (audioStreams == null || audioStreams.isEmpty()) {
                  throw new Exception("Không tìm thấy audio");
                }

                AudioStream audioStream = audioStreams.get(0);
                String audioUrl = audioStream.getUrl();

                File downloadDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                if (!downloadDir.exists()) downloadDir.mkdirs();
                String songName = sanitizeFileName(streamInfo.getName());
                // 📥 Download
                File m4aFile = downloadService.downloadAudio(audioUrl, downloadDir, songName);

                // 🔥 Convert
                File mp3File = convertService.convertToMp3(m4aFile, downloadDir, songName);

                // ✅ Delete temp file
                m4aFile.delete();

                // 📢 Notify success
                new Handler(Looper.getMainLooper()).post(onSuccess);

              } catch (Exception e) {
                e.printStackTrace();
                // 📢 Notify fail
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

    // Loại bỏ các ký tự cấm
    return noDiacritics.replaceAll("[\\\\/:*?\"<>|]", "_").replaceAll("[^\\x20-\\x7E]", "_");
  }
}
