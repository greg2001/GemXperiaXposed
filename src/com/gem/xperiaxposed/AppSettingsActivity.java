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
    
    ApplicationInfo info = getIntent().getParcelableExtra("info");
    setTitle(info.name);
    
    getFragmentManager()
      .beginTransaction()
      .replace(android.R.id.content, new AppSettingsFragment(info))
      .commit();
  }
}

class AppSettingsFragment extends PreferenceFragment
{
  ApplicationInfo info;
  
  public AppSettingsFragment(ApplicationInfo info)
  {
    this.info = info;    
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.app_settings);
    initPreferences(this, info.packageName);
  }
}
