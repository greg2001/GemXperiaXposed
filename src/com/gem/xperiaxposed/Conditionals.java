package com.gem.xperiaxposed;

import static com.gem.xperiaxposed.Constants.*;
import android.content.Context;
import android.util.Log;
import de.robv.android.xposed.XposedBridge;

public class Conditionals
{
  public static final boolean KITKAT = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT;
  public static final boolean JELLYBEAN = !KITKAT;
  public static boolean KITKAT_LAUNCHER;
  public static boolean JELLYBEAN_LAUNCHER;
  
  private static boolean inited = false;
  
  public static void init(Context context)
  {
    if(inited)
      return;
    inited = true;
    
    try
    {
      String version = context.getPackageManager().getPackageInfo(SE_HOME, 0).versionName;
      Log.i(TAG, "Xperia launcher version: " + version);
      JELLYBEAN_LAUNCHER = version.startsWith("6.1");
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
      JELLYBEAN_LAUNCHER = false;
    }
    catch(Throwable ex)
    {
      KITKAT_LAUNCHER = false;
      JELLYBEAN_LAUNCHER = true;
    }
    
    XposedBridge.log("JELLYBEAN_LAUNCHER: " + JELLYBEAN_LAUNCHER);
    XposedBridge.log("KITKAT_LAUNCHER: " + KITKAT_LAUNCHER);
  }
}
