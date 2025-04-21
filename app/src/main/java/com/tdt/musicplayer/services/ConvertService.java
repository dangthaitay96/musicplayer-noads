package com.tdt.musicplayer.services;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import java.io.*;
import okhttp3.*;

public class ConvertService {

  private final Context context;
  private final Runnable onStart;
  private final Runnable onFinish;

  public ConvertService(Context context, Runnable onStart, Runnable onFinish) {
    this.context = context;
    this.onStart = onStart;
    this.onFinish = onFinish;
  }

  public void download(String youtubeUrl) {
    if (onStart != null) onStart.run();

    OkHttpClient client =
        new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(5, java.util.concurrent.TimeUnit.MINUTES)
            .writeTimeout(1, java.util.concurrent.TimeUnit.MINUTES)
            .build();

    RequestBody formBody = new FormBody.Builder().add("youtubeUrl", youtubeUrl).build();

    Request request =
        new Request.Builder().url("http://10.0.2.2:8080/convert").post(formBody).build();

    client
        .newCall(request)
        .enqueue(
            new Callback() {
              @Override
              public void onFailure(Call call, IOException e) {
                Log.e("API_CALL", "Error: " + e.getMessage());
                e.printStackTrace();
                showToast("Lỗi kết nối tới server: " + e.getMessage());
                if (onFinish != null) onFinish.run();
              }

              @Override
              public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                  showToast("Convert thất bại");
                  if (onFinish != null) onFinish.run();
                  return;
                }

                String fileName = "music.mp3";
                String disposition = response.header("Content-Disposition");
                if (disposition != null && disposition.contains("filename=")) {
                  fileName = disposition.split("filename=")[1].replace("\"", "").trim();
                }

                File musicDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                if (!musicDir.exists()) {
                  musicDir.mkdirs();
                }

                File outFile = new File(musicDir, fileName);
                try (InputStream inputStream = response.body().byteStream();
                    FileOutputStream outputStream = new FileOutputStream(outFile)) {

                  byte[] buffer = new byte[4096];
                  int read;
                  while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                  }

                  outputStream.flush();
                  showToast("Đã lưu: " + fileName);

                  MediaScannerConnection.scanFile(
                      context,
                      new String[] {outFile.getAbsolutePath()},
                      new String[] {"audio/mpeg"},
                      (path, uri) ->
                          Log.d("ConvertService", "File scanned: " + path + ", URI: " + uri));

                } catch (IOException e) {
                  e.printStackTrace();
                  showToast("Lỗi khi lưu file");
                }

                if (onFinish != null) onFinish.run();
              }
            });
  }

  private void showToast(String msg) {
    android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
    mainHandler.post(() -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show());
  }
}
