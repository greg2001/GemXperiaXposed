package com.gem.xperiaxposed;

import android.content.*;
import android.util.*;

public class ListPreference extends android.preference.ListPreference
{
  private String enableDependentsState = null;
  
  public ListPreference(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    if(attrs != null)
      enableDependentsState = attrs.getAttributeValue(null, "enableDependentsState");
  }
  
  public ListPreference(Context context) 
  {
    this(context, null);
  }
  
  public void setValue(String value)
  {
    super.setValue(value);
    setSummary(getEntry().toString());
    notifyDependencyChange(shouldDisableDependents());
  }
  
  public boolean shouldDisableDependents()
  {
    return super.shouldDisableDependents() || ((enableDependentsState != null) && !enableDependentsState.equals(getValue()));
  }
}