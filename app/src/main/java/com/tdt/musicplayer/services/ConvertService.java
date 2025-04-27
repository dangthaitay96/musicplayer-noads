package com.tdt.musicplayer.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.widget.Toast;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.Session;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.StreamInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ConvertService {

  private final Context context;
  private final Runnable onStart;
  private final Runnable onFinish;

  public ConvertService(Context context, Runnable onStart, Runnable onFinish) {
    this.context = context;
    this.onStart = onStart;
    this.onFinish = onFinish;
  }

  @SuppressLint("SuspiciousIndentation")
  public void download(String youtubeUrl) {
    if (onStart != null) onStart.run();

    new Thread(() -> {
      try {
        // Normalize URL
        String finalUrl = youtubeUrl;
        if (finalUrl.contains("youtu.be/")) {
          finalUrl = finalUrl.replace("youtu.be/", "www.youtube.com/watch?v=");
        }

        NewPipe.init(DownloaderImpl.getInstance());

        StreamInfo streamInfo = StreamInfo.getInfo(NewPipe.getService(0), finalUrl);

        List<AudioStream> audioStreams = streamInfo.getAudioStreams();
        if (audioStreams == null || audioStreams.isEmpty()) {
          showToast("❌ Không tìm thấy audio stream");
          return;
        }

        AudioStream audio = audioStreams.get(0);
        String audioUrl = audio.getUrl();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        Request request = new Request.Builder().url(audioUrl).build();
        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
          showToast("❌ Tải file thất bại");
          return;
        }

        File downloadDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                "Converted"
        );
        if (!downloadDir.exists()) downloadDir.mkdirs();

        File m4aFile = new File(downloadDir, "temp_audio.m4a");
        try (InputStream is = response.body().byteStream();
             FileOutputStream fos = new FileOutputStream(m4aFile)) {
          byte[] buffer = new byte[4096];
          int len;
          while ((len = is.read(buffer)) != -1) {
            fos.write(buffer, 0, len);
          }
        }

        String mp3Path = new File(downloadDir, "converted_" + System.currentTimeMillis() + ".mp3").getAbsolutePath();
        String command = "-i \"" + m4aFile.getAbsolutePath() + "\" -vn -ar 44100 -ac 2 -b:a 192k \"" + mp3Path + "\"";

        Session session = FFmpegKit.execute(command);
        if (ReturnCode.isSuccess(session.getReturnCode())) {
          m4aFile.delete();
          showToast("🎉 Convert thành công: " + mp3Path);
          MediaScannerConnection.scanFile(context, new String[]{mp3Path}, null, null);
        } else {
          showToast("❌ Lỗi khi convert MP3: " + session.getFailStackTrace());
        }
      } catch (Exception e) {
        e.printStackTrace();
        showToast("❌ Lỗi: " + e.getMessage());
      } finally {
        if (onFinish != null) onFinish.run();
      }
    }).start();
  }

  private void showToast(String msg) {
    new android.os.Handler(context.getMainLooper())
            .post(() -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show());
  }
}
