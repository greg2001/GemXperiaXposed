package com.gem.xperiaxposed;

import java.util.*;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.preference.*;

public class Util 
{
  
  @SuppressWarnings("deprecation")
  @SuppressLint("WorldReadableFiles")
  public static void makeSharedPreferencesWorldReadable(Activity activity)
  {
    activity.getSharedPreferences(activity.getApplicationContext().getPackageName() + "_preferences", Context.MODE_WORLD_READABLE)
      .edit()
      .commit();
  }

  public static void initPreferences(PreferenceFragment pf)
  {
    initPreferences(pf, null);
  }

  public static void initPreferences(PreferenceFragment pf, String category)
  {
    initPreferenceGroup(pf.getPreferenceScreen(), category);
  }
  
  private static void initPreferenceGroup(PreferenceGroup ps, String category)
  {
    if(category != null)
    {
      List<Preference> prefs = new ArrayList<Preference>();
      for(int i = 0; i < ps.getPreferenceCount(); ++i)
        prefs.add(ps.getPreference(i));
      ps.removeAll();
    
      for(Preference p: prefs)
      {
        p.setKey(p.getKey() + "$" + category);
        ps.addPreference(p);
      }
    }

    for(int i = 0; i < ps.getPreferenceCount(); ++i)
    {
      Preference p = ps.getPreference(i);
      if(p instanceof ListPreference)
        initListPreference((ListPreference)p);
      else if(p instanceof PreferenceGroup)
        initPreferenceGroup((PreferenceGroup)p, category);
    }
  }
  
  private static void initListPreference(ListPreference listPreference)
  {
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
