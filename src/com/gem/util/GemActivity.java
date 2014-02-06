package com.gem.util;

import android.annotation.*;
import android.content.*;
import android.os.*;
import android.preference.*;
import android.support.v4.app.*;

import com.gem.xperiaxposed.*;

public class GemActivity extends FragmentActivity 
{
  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    makeSharedPreferencesWorldReadable();
    if("dark".equals(PreferenceManager.getDefaultSharedPreferences(this).getString("key_about_theme", "light")))
      setTheme(R.style.AppBaseThemeDark);

    super.onCreate(savedInstanceState);
    
    if(getIntent().getStringExtra("title") != null)
      setTitle(getIntent().getStringExtra("title"));
  }
  
  @SuppressWarnings("deprecation")
  @SuppressLint("WorldReadableFiles")
  public void makeSharedPreferencesWorldReadable()
  {
    getSharedPreferences(getApplicationContext().getPackageName() + "_preferences", Context.MODE_WORLD_READABLE).edit().commit();
  }
}
