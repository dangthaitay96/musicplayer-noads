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

  public File convertToMp3(File m4aFile, File outputDir, String songName) {
    String mp3Path = new File(outputDir, songName + System.currentTimeMillis() + ".mp3").getAbsolutePath();
    String command = "-i \"" + m4aFile.getAbsolutePath() + "\" -vn -ar 44100 -ac 2 -b:a 192k \"" + mp3Path + "\"";

    Session session = FFmpegKit.execute(command);
    if (ReturnCode.isSuccess(session.getReturnCode())) {
      return new File(mp3Path);
    } else {
      throw new RuntimeException("Convert failed: " + session.getFailStackTrace());
    }
  }
}
