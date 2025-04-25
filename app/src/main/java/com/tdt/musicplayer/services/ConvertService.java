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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
            // Khởi tạo thư viện NewPipe
            NewPipe.init(DownloaderImpl.getInstance());

            // Lấy thông tin video từ link YouTube
            StreamInfo streamInfo = null;
            try {
                streamInfo = StreamInfo.getInfo(NewPipe.getServiceByUrl(youtubeUrl), youtubeUrl);

            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ExtractionException e) {
                throw new RuntimeException(e);
            }
            List<AudioStream> audioStreams = streamInfo.getAudioStreams();
            if (audioStreams == null || audioStreams.isEmpty()) {
                throw new Exception("Không tìm thấy audio stream");
            }
            AudioStream audio = audioStreams.get(0);
            String audioUrl = audio.getUrl();

            // Tải file audio (m4a)
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(audioUrl).build();
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                showToast("Tải file thất bại");
                return;
            }

            File downloadDir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "Converted"
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

            // Convert sang MP3 bằng MobileFFmpeg
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

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        }).start();
    }

    private void showToast(String msg) {
        android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
        mainHandler.post(() -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show());
    }
}
