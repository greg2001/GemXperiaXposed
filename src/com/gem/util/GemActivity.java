package com.gem.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;

import com.gem.xperiaxposed.Conditionals;
import com.gem.xperiaxposed.R;

public class GemActivity extends FragmentActivity 
{
  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    Conditionals.init(this);
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
