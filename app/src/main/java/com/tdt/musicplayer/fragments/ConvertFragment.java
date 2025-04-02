package com.tdt.musicplayer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.tdt.musicplayer.R;
import com.tdt.musicplayer.services.ConvertService;
import com.tdt.musicplayer.utils.ViewUtils;

public class ConvertFragment extends Fragment {

  private ConvertService convertService;

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View viewConvert = inflater.inflate(R.layout.convert_fragment, container, false);
    convertService = new ConvertService(requireContext());

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
          convertService.download(url);
        });

    return viewConvert;
  }
}
