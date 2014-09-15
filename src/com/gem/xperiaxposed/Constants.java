package com.gem.xperiaxposed;

public class Constants
{
  // log tag
  public static final String TAG = "GEMXX";
  
  // packages
  public static final String ANDROID  = "android";
  public static final String SYSTEMUI = "com.android.systemui";
  public static final String[] SE_HOME = 
  { 
    "com.sahaab.home", 
    "com.sonyericsson.home.z1", 
    "com.sonyericsson.home.z2", 
    "com.sonyericsson.home.z3", 
    "com.sonyericsson.home" 
  };
  public static final String SE_LOCK  = "com.sonyericsson.lockscreen.uxpnxt";
  public static final String KEYGUARD = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) ? "com.android.keyguard" : "com.android.internal.policy.impl.keyguard";
}
