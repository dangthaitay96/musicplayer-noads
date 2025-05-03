package com.tdt.musicplayer.services;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.Session;
import java.io.File;

public class ConvertService {

  public File convertToMp3(File m4aFile, File outputDir, String songName) {
    String mp3Path =
        new File(outputDir, songName + System.currentTimeMillis() + ".mp3").getAbsolutePath();
    String command =
        "-i \""
            + m4aFile.getAbsolutePath()
            + "\" -vn -ar 44100 -ac 2 -b:a 192k \""
            + mp3Path
            + "\"";

    Session session = FFmpegKit.execute(command);
    if (ReturnCode.isSuccess(session.getReturnCode())) {
      return new File(mp3Path);
    } else {
      throw new RuntimeException("Convert failed: " + session.getFailStackTrace());
    }
  }
}
