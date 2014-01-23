package com.gem.xperiaxposed;

import static com.gem.xperiaxposed.Util.*;
import net.margaritov.preference.colorpicker.*;
import android.app.*;
import android.os.*;
import android.preference.*;

public class MainActivity extends Activity 
{
  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    makeSharedPreferencesWorldReadable(this);
    
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
        ((ColorPickerPreference)findPreference("key_systemui_dark_background")).setValue(XposedMain.SYSTEM_UI_OPAQUE_BACKGROUND);      
        ((ColorPickerPreference)findPreference("key_systemui_light_background")).setValue(XposedMain.SYSTEM_UI_LIGHT_BACKGROUND);      
        ((ColorPickerPreference)findPreference("key_systemui_translucent_background")).setValue(XposedMain.SYSTEM_UI_TRANSPARENT_BACKGROUND);      
        return true;
      }
    });
  }
}
