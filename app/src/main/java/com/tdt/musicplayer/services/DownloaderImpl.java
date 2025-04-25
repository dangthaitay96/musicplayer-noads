package com.tdt.musicplayer.services;

import okhttp3.*;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DownloaderImpl extends Downloader {

    // ✅ Cấu hình timeout cao hơn để tránh bị SocketTimeoutException
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private static final DownloaderImpl INSTANCE = new DownloaderImpl();

    public static DownloaderImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public Response execute(org.schabi.newpipe.extractor.downloader.Request request) throws IOException, ReCaptchaException {
        Request.Builder builder = new Request.Builder().url(request.url());

        // ✅ Thêm headers tùy chỉnh
        Map<String, List<String>> headers = request.headers();
        if (headers != null) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                for (String value : entry.getValue()) {
                    builder.addHeader(entry.getKey(), value);
                }
            }
        }

        // ✅ Thêm User-Agent và ngôn ngữ (bắt buộc cho YouTube)
        builder.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36");
        builder.addHeader("Accept-Language", request.localization().getLanguageCode());

        // ✅ Xử lý method
        String method = request.httpMethod();
        if ("POST".equalsIgnoreCase(method)) {
            byte[] postData = request.dataToSend();
            RequestBody body = RequestBody.create(
                    postData != null ? postData : new byte[0],
                    MediaType.parse("application/json")
            );
            builder.post(body);
        } else if ("HEAD".equalsIgnoreCase(method)) {
            builder.head();
        } else {
            builder.get();
        }

        // ✅ Gửi request
        okhttp3.Response okResponse = client.newCall(builder.build()).execute();
        okhttp3.ResponseBody body = okResponse.body();
        String bodyStr = body != null ? body.string() : "";

        // ✅ Gỡ lỗi nếu trả về HTML (thay vì JSON)
        if (!okResponse.isSuccessful() || bodyStr.startsWith("<!DOCTYPE html")) {
            System.out.println("❌ HTTP Error: " + okResponse.code());
            System.out.println("🔍 HTML instead of JSON (snippet): " + bodyStr.substring(0, Math.min(bodyStr.length(), 300)));
        }

        // ✅ Ném lỗi nếu gặp captcha hoặc thông báo cần JavaScript
        if (bodyStr.contains("enable JavaScript") || bodyStr.toLowerCase().contains("recaptcha")) {
            try {
                throw new Exception("Captcha or JS required in response");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // ✅ Trả về response chuẩn cho NewPipe
        return new Response(
                okResponse.code(),
                okResponse.message(),
                okResponse.headers().toMultimap(),
                bodyStr,
                request.url()
        );
    }
}
