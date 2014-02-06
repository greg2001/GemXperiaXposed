package com.gem.util;

import java.util.*;

import android.os.*;
import android.preference.*;
import android.view.*;

public class GemPreferenceFragment extends PreferenceFragment
{
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    setPreferenceCategory(getActivity().getIntent().getStringExtra("category"));
    return super.onCreateView(inflater, container, savedInstanceState);
  }
  
  public void setPreferenceCategory(String category)
  {
    if(category != null)
      setPreferenceCategory(getPreferenceScreen(), category);
  }
  
  private void setPreferenceCategory(PreferenceGroup ps, String category)
  {
    List<Preference> prefs = new ArrayList<Preference>();
    for(int i = 0; i < ps.getPreferenceCount(); ++i)
      prefs.add(ps.getPreference(i));
    ps.removeAll();
  
    for(Preference p: prefs)
    {
      p.setKey(p.getKey() + "$" + category);
      ps.addPreference(p);
      if(p instanceof PreferenceGroup)
        setPreferenceCategory((PreferenceGroup)p, category);
    }
  }
}