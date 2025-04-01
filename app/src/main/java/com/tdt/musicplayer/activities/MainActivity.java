package com.tdt.musicplayer.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tdt.musicplayer.R;
import com.tdt.musicplayer.fragments.ConvertFragment;
import com.tdt.musicplayer.fragments.HomeFragment;
import com.tdt.musicplayer.fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    loadFragment(new HomeFragment()); // mặc định là tab Home

    BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
    bottomNav.setOnItemSelectedListener(
        item -> {
          Fragment selectedFragment = null;
          if (item.getItemId() == R.id.home_play) {
            selectedFragment = new HomeFragment();
          } else if (item.getItemId() == R.id.nav_convert) {
            selectedFragment = new ConvertFragment();
          } else if (item.getItemId() == R.id.nav_settings) {
            selectedFragment = new SettingsFragment();
          }
          if (selectedFragment != null) {
            loadFragment(selectedFragment);
          }
          return true;
        });
  }

  private void loadFragment(Fragment fragment) {
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .commit();
  }
}
