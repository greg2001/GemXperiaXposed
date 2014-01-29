package com.gem.xperiaxposed;

import static com.gem.xperiaxposed.Util.*;
import android.app.*;
import android.os.*;
import android.preference.*;

public class AppSettingsActivity extends Activity 
{
  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    
    makeSharedPreferencesWorldReadable(this);
    if(getIntent().getStringExtra("title") != null)
      setTitle(getIntent().getStringExtra("title"));
    
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
    setPreferenceCategory(this, getActivity().getIntent().getStringExtra("packageName"));
  }
}
