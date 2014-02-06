package com.gem.util;

import android.content.*;
import android.preference.*;
import android.util.*;

public class GemListPreference extends ListPreference
{
  private String enableDependentsState = null;
  
  public GemListPreference(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    if(attrs != null)
      enableDependentsState = attrs.getAttributeValue(null, "enableDependentsState");
  }
  
  public GemListPreference(Context context) 
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