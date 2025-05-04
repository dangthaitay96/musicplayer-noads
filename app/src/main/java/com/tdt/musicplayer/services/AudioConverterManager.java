package com.tdt.musicplayer.services;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
              File mp3File = null;
              try {
                // Khởi tạo NewPipe
                NewPipe.init(DownloaderImpl.getInstance());
                StreamInfo streamInfo = StreamInfo.getInfo(NewPipe.getService(0), youtubeUrl);

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

                // 📥 Tải file M4A
                File m4aFile = downloadService.downloadAudio(audioUrl, downloadDir, songName);

                // 🔄 Chuyển đổi sang MP3
                mp3File = convertService.convertToMp3(m4aFile, downloadDir, songName);

                // ❌ Xoá file tạm .m4a (không để lỗi này ảnh hưởng)
                try {
                  if (!m4aFile.delete()) {
                    Log.w(
                        "AudioConverterManager",
                        "Không thể xoá file tạm: " + m4aFile.getAbsolutePath());
                  }
                } catch (Exception ex) {
                  Log.e("AudioConverterManager", "Lỗi khi xoá file m4a: " + ex.getMessage());
                }

              } catch (Exception e) {
                Log.e("AudioConverterManager", "Lỗi khi tải/convert: " + e.getMessage(), e);
              }

              // ✅ Kiểm tra kết quả và phản hồi UI
              File finalMp3File = mp3File; // để dùng trong lambda
              new Handler(Looper.getMainLooper())
                  .post(
                      () -> {
                        if (finalMp3File != null && finalMp3File.exists()) {
                          onSuccess.run();
                        } else {
                          onFail.run();
                        }
                      });
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
}
