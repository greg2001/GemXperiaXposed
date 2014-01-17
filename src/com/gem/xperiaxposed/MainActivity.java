package com.gem.xperiaxposed;

import android.annotation.*;
import android.app.*;
import android.os.*;
import android.preference.*;

public class MainActivity extends Activity 
{
  @SuppressWarnings("deprecation")
  @SuppressLint("WorldReadableFiles")
  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    
    getSharedPreferences(this.getApplicationContext().getPackageName() + "_preferences", MODE_WORLD_READABLE)
      .edit()
      .commit();
    
    getFragmentManager()
      .beginTransaction()
      .replace(android.R.id.content, new SettingsFragment())
      .commit();
  }
}

class SettingsFragment extends PreferenceFragment
{
  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.settings);
    
    findPreference("key_restart_launcher").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() 
    {
      @Override
      public boolean onPreferenceClick(Preference preference)
      {
        try
        {
          Runtime.getRuntime().exec("su -c pkill com.sonyericsson.home").waitFor();
        }
        catch(Exception ex)
        {
          ex.printStackTrace();
        }
        return true;
      }
    });
    
    initListPreference("key_dock_columns");
    initListPreference("key_folder_columns");
    initListPreference("key_desktop_rows");
    initListPreference("key_desktop_columns");
    initListPreference("key_drawer_rows");
    initListPreference("key_drawer_columns");
  }

  private void initListPreference(String key)
  {
    ListPreference listPreference = (ListPreference)findPreference(key);
    listPreference.setSummary(listPreference.getValue().toString());
    listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
    {
      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue)
      {
        preference.setSummary(newValue.toString());
        return true;
      }
    });
  }  
}
