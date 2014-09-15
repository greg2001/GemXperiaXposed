package com.gem.xperiaxposed;

import static com.gem.xperiaxposed.Constants.*;

import java.lang.reflect.Field;

import android.content.Context;
import android.util.Log;
import de.robv.android.xposed.XposedBridge;

public class Conditionals
{
  public static final boolean KITKAT = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT;
  public static final boolean JELLYBEAN = !KITKAT;
  public static boolean Z3_KITKAT_LAUNCHER;
  public static boolean NEW_KITKAT_LAUNCHER;
  public static boolean KITKAT_LAUNCHER;
  public static boolean JELLYBEAN_LAUNCHER;
  public static boolean LAUNCHER_HAS_ANIMATIONS;
  public static String SE_HOME_PACKAGE;
  public static String SE_HOME_VERSION;
  
  private static boolean inited = false;
  
  public static void init(Context context)
  {
    if(inited)
      return;
    inited = true;
    
    try
    {
      for(String pkg: SE_HOME)
      {
        try
        {
          SE_HOME_VERSION = context.getPackageManager().getPackageInfo(pkg, 0).versionName;
          SE_HOME_PACKAGE = pkg;
          break;
        }
        catch(Throwable ex)
        {
        }
      }
      Log.i(TAG, "Xperia launcher package: " + SE_HOME_PACKAGE);
      Log.i(TAG, "Xperia launcher version: " + SE_HOME_VERSION);
      JELLYBEAN_LAUNCHER = SE_HOME_VERSION.startsWith("6.1");
      KITKAT_LAUNCHER = !JELLYBEAN_LAUNCHER;
    }
    catch(Throwable ex)
    {
      Log.w(TAG, "Unable to retrieve Xperia launcher version", ex);
      JELLYBEAN_LAUNCHER = true;
      KITKAT_LAUNCHER = !JELLYBEAN_LAUNCHER;
    }

    Log.i(TAG, "JELLYBEAN_LAUNCHER: " + JELLYBEAN_LAUNCHER);
    Log.i(TAG, "KITKAT_LAUNCHER: " + KITKAT_LAUNCHER);
  }

  public static void initLauncher()
  {
    try
    {
      Class.forName("com.sonymobile.home.configprovider.ConfigProvider");
      KITKAT_LAUNCHER = true;
      try
      {
        Class.forName("com.sonymobile.home.LifeCycle");
      }
      catch(Throwable ex)
      {
        NEW_KITKAT_LAUNCHER = true;
        try
        {
          Class.forName("com.sonymobile.home.MainViewSwitcher");
          Z3_KITKAT_LAUNCHER = true;
        }
        catch(Throwable exx)
        {
        }
      }
    }
    catch(Throwable ex)
    {
      JELLYBEAN_LAUNCHER = true;
    }
    
    try
    {
      LAUNCHER_HAS_ANIMATIONS = false;
      for(Field f: Class.forName("com.sonymobile.home.desktop.DesktopView").getDeclaredFields())
      {
        if("mAnimNbr".equals(f.getName()))
        {
          LAUNCHER_HAS_ANIMATIONS = true;
          break;
        }
      }
    }
    catch(Throwable ex)
    {
    }
    
    XposedBridge.log("JELLYBEAN_LAUNCHER: " + JELLYBEAN_LAUNCHER);
    XposedBridge.log("KITKAT_LAUNCHER: " + KITKAT_LAUNCHER);
    XposedBridge.log("NEW_KITKAT_LAUNCHER: " + NEW_KITKAT_LAUNCHER);
    XposedBridge.log("Z3_KITKAT_LAUNCHER: " + Z3_KITKAT_LAUNCHER);
    XposedBridge.log("LAUNCHER_HAS_ANIMATIONS: " + LAUNCHER_HAS_ANIMATIONS);
  }
}
