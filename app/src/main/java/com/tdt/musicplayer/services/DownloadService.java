package com.tdt.musicplayer.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadService {
  private final OkHttpClient client =
      new OkHttpClient.Builder()
          .connectTimeout(30, TimeUnit.SECONDS)
          .readTimeout(60, TimeUnit.SECONDS)
          .build();

  public File downloadAudio(String audioUrl, File outputDir, String songName) throws IOException {
    Request request = new Request.Builder().url(audioUrl).build();
    Response response = client.newCall(request).execute();

    if (!response.isSuccessful()) throw new IOException("Download failed");

    File outputFile = new File(outputDir, songName + System.currentTimeMillis() + ".m4a");
    assert response.body() != null;
    try (InputStream is = response.body().byteStream();
        FileOutputStream fos = new FileOutputStream(outputFile)) {
      byte[] buffer = new byte[4096];
      int len;
      while ((len = is.read(buffer)) != -1) fos.write(buffer, 0, len);
    }

    return outputFile;
  }


}
