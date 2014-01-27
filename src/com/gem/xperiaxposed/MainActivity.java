package com.gem.xperiaxposed;

import static com.gem.xperiaxposed.Util.*;
import net.margaritov.preference.colorpicker.*;
import android.app.*;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.*;
import android.preference.*;

public class MainActivity extends Activity 
{
  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    makeSharedPreferencesWorldReadable(this);
    if("dark".equals(PreferenceManager.getDefaultSharedPreferences(this).getString("key_about_theme", "light")))
      setTheme(android.R.style.Theme_Holo);

    super.onCreate(savedInstanceState);
    
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
    initPreferences(this);
    
    try
    {
      findPreference("key_about_app").setTitle(getActivity().getTitle() + " " + 
        getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
    }
    catch(NameNotFoundException ex)
    {
    }
    
    findPreference("key_restart_launcher").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() 
    {
      @Override
      public boolean onPreferenceClick(Preference preference)
      {
        try
        {
          Runtime.getRuntime().exec("su -c pkill " + XposedMain.SE_HOME).waitFor();
        }
        catch(Exception ex)
        {
          ex.printStackTrace();
        }
        return true;
      }
    });
    
    findPreference("key_restart_systemui").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() 
    {
      @Override
      public boolean onPreferenceClick(Preference preference)
      {
        try
        {
          Runtime.getRuntime().exec("su -c pkill " + XposedMain.SYSTEMUI).waitFor();
        }
        catch(Exception ex)
        {
          ex.printStackTrace();
        }
        return true;
      }
    });

    findPreference("key_reset_to_default").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() 
    {
      @Override
      public boolean onPreferenceClick(Preference preference)
      {
        ((CheckBoxPreference)findPreference("key_systemui_same_nav_colors")).setChecked(true);      
        ((ColorPickerPreference)findPreference("key_systemui_dark_background")).setValue(XposedMain.SYSTEM_UI_OPAQUE_BACKGROUND);      
        ((ColorPickerPreference)findPreference("key_systemui_light_background")).setValue(XposedMain.SYSTEM_UI_LIGHT_BACKGROUND);      
        ((ColorPickerPreference)findPreference("key_systemui_translucent_background")).setValue(XposedMain.SYSTEM_UI_TRANSPARENT_BACKGROUND);      
        ((ColorPickerPreference)findPreference("key_systemui_nav_dark_background")).setValue(XposedMain.SYSTEM_UI_OPAQUE_BACKGROUND);      
        ((ColorPickerPreference)findPreference("key_systemui_nav_light_background")).setValue(XposedMain.SYSTEM_UI_LIGHT_BACKGROUND);      
        ((ColorPickerPreference)findPreference("key_systemui_nav_translucent_background")).setValue(XposedMain.SYSTEM_UI_TRANSPARENT_BACKGROUND);      
        return true;
      }
    });
    
    findPreference("key_about_theme").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
    {
      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue)
      {
        getActivity().recreate();
        return true;
      }
    });
  }
}
