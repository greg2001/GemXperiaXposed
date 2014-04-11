package com.gem.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class GemListPreference extends ListPreference
{
  private String enableDependentsState = null;
  private String defaultValue;
  
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
  
  public void resetValue()
  {
    setValue(defaultValue);
  }
  
  @Override
  public void setValue(String value)
  {
    super.setValue(value);
    setSummary(getEntry().toString());
    notifyDependencyChange(shouldDisableDependents());
  }
  
  @Override
  public boolean shouldDisableDependents()
  {
    return super.shouldDisableDependents() || ((enableDependentsState != null) && !enableDependentsState.equals(getValue()));
  }
  
  @Override
  protected Object onGetDefaultValue(TypedArray a, int index)
  {
    return defaultValue = (String)super.onGetDefaultValue(a, index);
  }
}