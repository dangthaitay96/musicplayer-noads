package com.tdt.musicplayer.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadService {
  private final OkHttpClient client =
      new OkHttpClient.Builder()
          .connectTimeout(30, TimeUnit.SECONDS)
          .readTimeout(60, TimeUnit.SECONDS)
          .build();

  public File downloadAudio(
      String audioUrl, File outputDir, String songName, Consumer<Integer> onProgress)
      throws IOException {

    Request request = new Request.Builder().url(audioUrl).build();
    Response response = client.newCall(request).execute();

    if (!response.isSuccessful()) {
      throw new IOException("Download failed: HTTP " + response.code());
    }

    assert response.body() != null;
    long contentLength = response.body().contentLength();
    File outputFile = new File(outputDir, songName + System.currentTimeMillis() + ".m4a");

    try (InputStream is = response.body().byteStream();
        FileOutputStream fos = new FileOutputStream(outputFile)) {

      byte[] buffer = new byte[4096];
      long totalRead = 0;
      int lastProgress = 0;

      int len;
      while ((len = is.read(buffer)) != -1) {
        fos.write(buffer, 0, len);
        totalRead += len;

        if (onProgress != null && contentLength > 0) {
          int scaledProgress = Math.min(70, (int) ((totalRead * 70) / contentLength));
          if (scaledProgress != lastProgress) {
            new Handler(Looper.getMainLooper()).post(() -> onProgress.accept(scaledProgress));
            lastProgress = scaledProgress;
          }
        }
      }

    } catch (IOException e) {
      safeDelete(outputFile);
      throw new IOException("Đã xảy ra lỗi khi tải file: " + e.getMessage(), e);
    }

    if (!outputFile.exists() || outputFile.length() < 200 * 1024) {
      safeDelete(outputFile);
      throw new IOException("File tải về không hợp lệ (quá nhỏ)");
    }

    return outputFile;
  }

  private void safeDelete(File file) {
    if (file != null && file.exists() && !file.delete()) {
      Log.w("DownloadService", "Không thể xoá file: " + file.getAbsolutePath());
    }
  }
}
