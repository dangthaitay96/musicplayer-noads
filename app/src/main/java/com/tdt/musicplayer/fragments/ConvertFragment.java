package com.tdt.musicplayer.fragments;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.tdt.musicplayer.R;
import com.tdt.musicplayer.utils.ViewUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

public class ConvertFragment extends Fragment {

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View viewConvert = inflater.inflate(R.layout.convert_fragment, container, false);

    EditText editLink = viewConvert.findViewById(R.id.edit_youtube_link);
    Button btnConvert = viewConvert.findViewById(R.id.btn_convert);
    TextView textFeedback = viewConvert.findViewById(R.id.text_feedback);

    btnConvert.setOnClickListener(
        v -> {
          String url = editLink.getText().toString().trim();
          if (url.isEmpty()) {
            ViewUtils.showQuickFeedback(textFeedback, "Vui lòng nhập link YouTube");
            return;
          }
          download(url);
        });

    return viewConvert;
  }

    private void download(String youtubeUrl) {
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("youtubeUrl", youtubeUrl)
                .build();

        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/convert") // 🔁 dùng 10.0.2.2 nếu test trong emulator
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Lỗi kết nối tới server", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Convert thất bại", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                // ✅ Lấy tên file từ header
                String fileName;
                String disposition = response.header("Content-Disposition");
                if (disposition != null && disposition.contains("filename=")) {
                    fileName = disposition.split("filename=")[1].replace("\"", "").trim();
                } else {
                    fileName = "music.mp3";
                }

                // ✅ Ghi file vào thư mục Download
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File outFile = new File(downloadsDir, fileName);

                try (InputStream inputStream = response.body().byteStream();
                     FileOutputStream outputStream = new FileOutputStream(outFile)) {

                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, read);
                    }
                    outputStream.flush();

                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Đã lưu: " + fileName, Toast.LENGTH_LONG).show()
                    );

                } catch (IOException e) {
                    e.printStackTrace();
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Lỗi khi lưu file", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }


}
