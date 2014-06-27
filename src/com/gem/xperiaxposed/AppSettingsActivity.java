package com.gem.xperiaxposed;

import android.os.Bundle;

import com.gem.util.GemActivity;
import com.gem.util.GemPreferenceFragment;

public class AppSettingsActivity extends GemActivity 
{
  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    
    getFragmentManager()
      .beginTransaction()
      .replace(android.R.id.content, new AppSettingsFragment())
      .commit();
  }

  public static class AppSettingsFragment extends GemPreferenceFragment
  {
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.app_settings);
    }
  }
}
