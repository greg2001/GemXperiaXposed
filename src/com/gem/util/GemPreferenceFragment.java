package com.gem.util;

import static com.gem.xperiaxposed.Constants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gem.xperiaxposed.Conditionals;

public class GemPreferenceFragment extends PreferenceFragment
{
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    processPreferences(getPreferenceScreen(), getActivity().getIntent().getStringExtra("category"));
    return super.onCreateView(inflater, container, savedInstanceState);
  }
  
  public void setPreferenceCategory(String category)
  {
    processPreferences(getPreferenceScreen(), category);
  }
  
  private void processPreferences(PreferenceGroup ps, String category)
  {
    List<Preference> prefs = new ArrayList<Preference>();
    for(int i = 0; i < ps.getPreferenceCount(); ++i)
      prefs.add(ps.getPreference(i));
    ps.removeAll();
  
    for(Preference p: prefs)
    {
      if(evaluate(p.getExtras().getString("hideIf")))
        continue;
      if(evaluate(p.getExtras().getString("hideSummaryIf")))
        p.setSummary(null);
      if(category != null)
        p.setKey(p.getKey() + "$" + category);
      
      ps.addPreference(p);
      
      if(p instanceof PreferenceGroup)
        processPreferences((PreferenceGroup)p, category);
    }
  }
  
  private boolean evaluate(String conditionals)
  {
    if(conditionals != null)
    {
      StringTokenizer tok = new StringTokenizer(conditionals, ", ");
      do
      {
        String c = tok.nextToken();
        try
        {
          if(Conditionals.class.getField(c).getBoolean(null))
            return true;
        }
        catch(Exception ex)
        {
          Log.w(TAG, ex);
        }
      }
      while(tok.hasMoreTokens());
    }
    return false;
  }
  
}
