package com.tdt.musicplayer.services;

import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.ResponseBody;

public class DownloaderImpl extends Downloader {

    private static DownloaderImpl instance;
    private final OkHttpClient client;

    private DownloaderImpl() {
        client = new OkHttpClient();
    }

    public static DownloaderImpl getInstance() {
        if (instance == null) {
            instance = new DownloaderImpl();
        }
        return instance;
    }

    @Override
    public Response execute(Request request) throws IOException, ReCaptchaException {
        Builder requestBuilder = new Builder().url(request.url());


        for (Map.Entry<String, List<String>> entry : request.headers().entrySet()) {
            for (String value : entry.getValue()) {
                requestBuilder.addHeader(entry.getKey(), value);
            }
        }

        okhttp3.Request okRequest = requestBuilder.build();
        okhttp3.Response okResponse = client.newCall(okRequest).execute();
        ResponseBody body = okResponse.body();

        return new Response(okResponse.code(), okResponse.message(), okResponse.headers().toMultimap(), body != null ? body.string() : "", request.url() // dùng làm latestUrl
        );
    }
}
