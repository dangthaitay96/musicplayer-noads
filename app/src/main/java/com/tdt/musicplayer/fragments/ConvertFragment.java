package com.tdt.musicplayer.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.tdt.musicplayer.R;
import com.tdt.musicplayer.models.ConvertViewModel;
import com.tdt.musicplayer.services.AudioConverterManager;
import com.tdt.musicplayer.utils.ViewUtils;

public class ConvertFragment extends Fragment {

  private AudioConverterManager audioConverterManager;
  private ProgressBar progressBar;
  private EditText editLink;
  private ConvertViewModel viewModel;

  @SuppressLint("SetTextI18n")
  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View viewConvert = inflater.inflate(R.layout.convert_fragment, container, false);
    viewModel = new ViewModelProvider(requireActivity()).get(ConvertViewModel.class);
    editLink = viewConvert.findViewById(R.id.edit_youtube_link);
    Button btnConvert = viewConvert.findViewById(R.id.btn_convert);
    TextView textFeedback = viewConvert.findViewById(R.id.text_feedback);
    progressBar = viewConvert.findViewById(R.id.progress_download);
    audioConverterManager = new AudioConverterManager(requireContext());
    TextView textDescription = viewConvert.findViewById(R.id.text_description);
    TextView progressText = viewConvert.findViewById(R.id.progress_text);

    viewModel
        .getLink()
        .observe(
            getViewLifecycleOwner(),
            link -> {
              if (link != null && !link.equals(editLink.getText().toString())) {
                editLink.setText(link);
                editLink.setSelection(link.length());
              }
            });
    viewModel
        .getIsConverting()
        .observe(
            getViewLifecycleOwner(),
            converting -> {
              String link = viewModel.getLink().getValue();
              boolean hasLink = link != null && !link.trim().isEmpty();
              btnConvert.setEnabled(!converting && hasLink);
            });

    viewModel
        .getSongTitle()
        .observe(
            getViewLifecycleOwner(),
            title -> {
              if (title != null && !title.isEmpty()) {
                textDescription.setText("🎵 Bài hát: " + title);
              } else {
                textDescription.setText(
                    "Nhập đường dẫn diu túp và nhấn lấy file nhạc về thiết bị.");
              }
            });

    viewModel
        .getProgress()
        .observe(
            getViewLifecycleOwner(),
            progress -> {
              progressBar.setProgress(progress);
              progressText.setText(progress + "%");

              if (progress > 0) {
                progressBar.setVisibility(View.VISIBLE);
                progressText.setVisibility(View.VISIBLE);
              }
            });

    editLink.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            viewModel.setLink(s.toString());
            btnConvert.setEnabled(!s.toString().trim().isEmpty());
          }

          @Override
          public void afterTextChanged(Editable s) {}
        });

    btnConvert.setOnClickListener(
        v -> {
          String url = editLink.getText().toString().trim();
          if (url.isEmpty()) {
            ViewUtils.showQuickFeedback(textFeedback, "Vui lòng nhập đường dẫn diu túp ");
            return;
          }
          btnConvert.setEnabled(false);
          viewModel.setIsConverting(true);

          audioConverterManager.startDownloadAndConvert(
              url,
              () -> {
                progressBar.setProgress(0);
                progressBar.setIndeterminate(false);
                progressBar.setVisibility(View.VISIBLE);
                progressText.setVisibility(View.VISIBLE);
              },
              progress -> {
                progressBar.setProgress(progress);
                progressText.setText(progress + "%");
                viewModel.setProgress(progress);
              },
              () -> {
                progressBar.setVisibility(View.VISIBLE);
                progressText.setVisibility(View.VISIBLE);
                progressText.setText("100%");
                ViewUtils.showQuickFeedback(textFeedback, "Tải và chuyển đổi thành công");
                editLink.setText("");
                viewModel.setLink("");
                btnConvert.setEnabled(false);
                textDescription.setText(
                    "Nhập đường dẫn diu túp và nhấn lấy file nhạc về thiết bị.");
              },
              () -> {
                progressBar.setVisibility(View.GONE);
                progressText.setVisibility(View.GONE);
                ViewUtils.showQuickFeedback(textFeedback, "Đã xảy ra lỗi khi xử lý link");
                btnConvert.setEnabled(true);
              },
              title -> {
                viewModel.setSongTitle(title);
              });
        });

    return viewConvert;
  }
}
