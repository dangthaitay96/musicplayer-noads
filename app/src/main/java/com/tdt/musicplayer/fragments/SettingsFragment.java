package com.tdt.musicplayer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.tdt.musicplayer.R;

public class SettingsFragment extends Fragment {
  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View viewSetting = inflater.inflate(R.layout.setting_fragment, container, false);

    return viewSetting;
  }
}
