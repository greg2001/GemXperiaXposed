package com.gem.xperiaxposed;

import android.os.*;

import com.gem.util.*;

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
}

class AppSettingsFragment extends GemPreferenceFragment
{
  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.app_settings);
  }
}
