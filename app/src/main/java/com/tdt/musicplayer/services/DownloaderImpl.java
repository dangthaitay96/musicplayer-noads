package com.tdt.musicplayer.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.*;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;

public class DownloaderImpl extends Downloader {

  private static final OkHttpClient client =
      new OkHttpClient.Builder()
          .connectTimeout(30, TimeUnit.SECONDS)
          .readTimeout(60, TimeUnit.SECONDS)
          .writeTimeout(60, TimeUnit.SECONDS)
          .build();

  private static final DownloaderImpl INSTANCE = new DownloaderImpl();

  public static DownloaderImpl getInstance() {
    return INSTANCE;
  }

  @Override
  public Response execute(org.schabi.newpipe.extractor.downloader.Request request)
      throws IOException, ReCaptchaException {
    Request.Builder builder = new Request.Builder().url(request.url());

    // Headers
    Map<String, List<String>> headers = request.headers();
    if (headers != null) {
      for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
        for (String value : entry.getValue()) {
          builder.addHeader(entry.getKey(), value);
        }
      }
    }

    // User-Agent và Language
    builder.addHeader(
        "User-Agent",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
    builder.addHeader("Accept-Language", request.localization().getLanguageCode());
    builder.addHeader("X-YouTube-Client-Name", "1");
    builder.addHeader(
        "X-YouTube-Client-Version", "18.13.35"); // hoặc phiên bản mới nhất của YouTube app

    // Method
    String method = request.httpMethod();
    if ("POST".equalsIgnoreCase(method)) {
      byte[] postData = request.dataToSend();
      RequestBody body =
          RequestBody.create(
              postData != null ? postData : new byte[0], MediaType.parse("application/json"));
      builder.post(body);
    } else if ("HEAD".equalsIgnoreCase(method)) {
      builder.head();
    } else {
      builder.get();
    }

    okhttp3.Response okResponse = client.newCall(builder.build()).execute();
    String bodyStr = okResponse.body() != null ? okResponse.body().string() : "";

    if (!okResponse.isSuccessful() || bodyStr.startsWith("<!DOCTYPE html")) {
      System.out.println("❌ HTTP Error: " + okResponse.code());
      System.out.println(
          "🔍 HTML instead of JSON (snippet): "
              + bodyStr.substring(0, Math.min(bodyStr.length(), 300)));
    }

    if (bodyStr.contains("enable JavaScript")
        || bodyStr.toLowerCase().contains("recaptcha")
        || okResponse.code() == 403
        || okResponse.code() == 405) {
      throw new ReCaptchaException("Captcha or access blocked!", request.url());
    }

    return new Response(
        okResponse.code(),
        okResponse.message(),
        okResponse.headers().toMultimap(),
        bodyStr,
        request.url());
  }
}
