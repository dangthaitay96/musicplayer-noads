package com.tdt.musicplayer.fragments;

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

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View viewConvert = inflater.inflate(R.layout.convert_fragment, container, false);

    // Khởi tạo ViewModel
    viewModel = new ViewModelProvider(requireActivity()).get(ConvertViewModel.class);

    // Khởi tạo view và service
    editLink = viewConvert.findViewById(R.id.edit_youtube_link);
    Button btnConvert = viewConvert.findViewById(R.id.btn_convert);
    TextView textFeedback = viewConvert.findViewById(R.id.text_feedback);
    progressBar = viewConvert.findViewById(R.id.progress_download);
    audioConverterManager = new AudioConverterManager(requireContext());

    // Quan sát LiveData và cập nhật EditText khi có thay đổi
    viewModel
        .getLink()
        .observe(
            getViewLifecycleOwner(),
            link -> {
              if (link != null && !link.equals(editLink.getText().toString())) {
                editLink.setText(link);
                editLink.setSelection(link.length()); // Đặt con trỏ về cuối
              }
            });

    // Ghi lại thay đổi trong EditText vào ViewModel
    editLink.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            viewModel.setYoutubeLink(s.toString());
          }

          @Override
          public void afterTextChanged(Editable s) {}
        });

    // Xử lý nút Convert
    btnConvert.setOnClickListener(
        v -> {
          String url = editLink.getText().toString().trim();
          if (url.isEmpty()) {
            ViewUtils.showQuickFeedback(textFeedback, "Vui lòng nhập đường dẫn diu túp ");
            return;
          }

          audioConverterManager.startDownloadAndConvert(
              url,
              () -> progressBar.setVisibility(View.VISIBLE), // onStart
              () -> {
                progressBar.setVisibility(View.GONE);
                ViewUtils.showQuickFeedback(textFeedback, "Tải và chuyển đổi thành công");
                editLink.setText(""); // 🧹 reset input sau khi thành công
                viewModel.setYoutubeLink(""); // cập nhật luôn ViewModel nếu cần
              }, // onSuccess
              () -> {
                progressBar.setVisibility(View.GONE);
                ViewUtils.showQuickFeedback(textFeedback, "Đã xảy ra lỗi khi xử lý link");
              }
              // onError
              );
        });

    return viewConvert;
  }
}
