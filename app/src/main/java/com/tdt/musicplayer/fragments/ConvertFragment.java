package com.tdt.musicplayer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.tdt.musicplayer.R;
import com.tdt.musicplayer.services.ConvertService;
import com.tdt.musicplayer.utils.ViewUtils;

import org.schabi.newpipe.extractor.exceptions.ExtractionException;

import java.io.IOException;

public class ConvertFragment extends Fragment {

    private ConvertService convertService;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View viewConvert = inflater.inflate(R.layout.convert_fragment, container, false);

        EditText editLink = viewConvert.findViewById(R.id.edit_youtube_link);
        Button btnConvert = viewConvert.findViewById(R.id.btn_convert);
        TextView textFeedback = viewConvert.findViewById(R.id.text_feedback);
        progressBar = viewConvert.findViewById(R.id.progress_bar);

        // ✅ Khởi tạo ConvertService với callback loading
        convertService = new ConvertService(
                requireContext(),
                () -> requireActivity().runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE)),
                () -> requireActivity().runOnUiThread(() -> progressBar.setVisibility(View.GONE))
        );

        btnConvert.setOnClickListener(v -> {
            String url = editLink.getText().toString().trim();
            if (url.isEmpty()) {
                ViewUtils.showQuickFeedback(textFeedback, "Vui lòng nhập đường dẫn diu túp ");
                return;
            }

            try {
                convertService.download(url);
            } catch (ExtractionException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return viewConvert;
    }
}
