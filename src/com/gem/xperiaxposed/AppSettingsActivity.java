package com.gem.xperiaxposed;

import static com.gem.xperiaxposed.Util.*;
import android.app.*;
import android.content.pm.*;
import android.os.*;
import android.preference.*;

public class AppSettingsActivity extends Activity 
{
  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    
    makeSharedPreferencesWorldReadable(this);
    setTitle(((ApplicationInfo)getIntent().getParcelableExtra("info")).name);
    
    getFragmentManager()
      .beginTransaction()
      .replace(android.R.id.content, new AppSettingsFragment())
      .commit();
  }
}

class AppSettingsFragment extends PreferenceFragment
{
  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.app_settings);
    initPreferences(this, ((ApplicationInfo)getActivity().getIntent().getParcelableExtra("info")).packageName);
  }
}
