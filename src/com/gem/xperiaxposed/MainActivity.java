package com.gem.xperiaxposed;

import static com.gem.xperiaxposed.Constants.*;

import java.util.HashSet;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;

import com.gem.util.GemActivity;
import com.gem.util.GemListPreference;
import com.gem.util.GemPreferenceFragment;

public class MainActivity extends GemActivity
{
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    getFragmentManager()
      .beginTransaction()
      .replace(android.R.id.content, new SettingsFragment())
      .commit();
  }
}

class SettingsFragment extends GemPreferenceFragment
{
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.settings);

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
          Runtime.getRuntime().exec("su -c pkill " + SE_HOME).waitFor();
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
          Runtime.getRuntime().exec("su -c pkill " + SYSTEMUI).waitFor();
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
        ((MultiSelectListPreference)findPreference("key_systemui_status_gradient")).setValues(new HashSet<String>());
        ((GemListPreference)findPreference("key_systemui_status_color_set")).resetValue();
        ((ColorPickerPreference)findPreference("key_systemui_dark_background")).resetValue();
        ((ColorPickerPreference)findPreference("key_systemui_light_background")).resetValue();
        ((ColorPickerPreference)findPreference("key_systemui_translucent_background")).resetValue();
        ((MultiSelectListPreference)findPreference("key_systemui_nav_gradient")).setValues(new HashSet<String>());
        ((GemListPreference)findPreference("key_systemui_nav_color_set")).resetValue();
        ((ColorPickerPreference)findPreference("key_systemui_nav_dark_background")).resetValue();
        ((ColorPickerPreference)findPreference("key_systemui_nav_light_background")).resetValue();
        ((ColorPickerPreference)findPreference("key_systemui_nav_translucent_background")).resetValue();
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
