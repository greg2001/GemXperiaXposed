package com.gem.xperiaxposed;

import static com.gem.xperiaxposed.Conditionals.*;
import static com.gem.xperiaxposed.Constants.*;
import static de.robv.android.xposed.XposedBridge.*;
import static de.robv.android.xposed.XposedHelpers.*;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.XResources;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewManager;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.gem.xperiaxposed.home.HomeResources;
import com.gem.xperiaxposed.systemui.SystemUIHooks;
import com.gem.xperiaxposed.systemui.SystemUIResources;
import com.gem.xposed.ModuleResources;
import com.gem.xposed.ReflectionUtils;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

////////////////////////////////////////////////////////////

public class XposedMain implements IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources
{

////////////////////////////////////////////////////////////
  
  public static String MODULE_PATH;
  public static String SE_HOME_PACKAGE;
  public static XSharedPreferences prefs;

////////////////////////////////////////////////////////////
  
  public void setupClassLoader(XC_LoadPackage.LoadPackageParam param)
  {
    try
    {
      ClassLoader moduleClassLoader = getClass().getClassLoader();
      ClassLoader xposedClassLoader = moduleClassLoader.getParent();
      ClassLoader packageClassLoader = param.classLoader;
      ReflectionUtils.setParentClassLoader(moduleClassLoader, packageClassLoader, xposedClassLoader);
    }
    catch(Throwable ex)
    {
      log(ex);
    }
  }

////////////////////////////////////////////////////////////

  @Override
  public void initZygote(IXposedHookZygoteInit.StartupParam param) throws Throwable
  {
    SystemUIResources.initFlags();
    
    MODULE_PATH = param.modulePath;
    prefs = new XSharedPreferences(XposedMain.class.getPackage().getName());
    prefs.makeWorldReadable();

    if(JELLYBEAN && prefs.getBoolean("key_hide_shortcuts", false))
    {
      XResources.setSystemWideReplacement("android", "drawable", "ic_lockscreen_camera_hint", 0);
      XResources.setSystemWideReplacement("android", "drawable", "ic_lockscreen_other_widgets_hint", 0);
    }
  }

////////////////////////////////////////////////////////////

  @Override
  public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam param) throws Throwable
  {
    if(param.packageName.equals(SYSTEMUI))
    {
      if(JELLYBEAN)
      {
        prefs.reload();
        ModuleResources res = ModuleResources.createInstance(MODULE_PATH, param.res);
        SystemUIResources.updateResources(param.res, res);
      }
    }
    if(param.packageName.equals(SE_LOCK))
    {
      prefs.reload();
      
      if(prefs.getBoolean("key_hide_hint_arrows", false))
      {
        param.res.setReplacement(SE_LOCK, "drawable", "arrow_unlock_hint_down", 0);
        param.res.setReplacement(SE_LOCK, "drawable", "arrow_unlock_hint_up", 0);
      }

      String hintText = prefs.getString("key_hint_text", "");
      if(!hintText.isEmpty())
      {
        param.res.setReplacement(SE_LOCK, "string", "lockscreen_unlock_hint", hintText);
        param.res.setReplacement(SE_LOCK, "string", "lockscreen_accessibility_unlock_hint", hintText);
        if(KITKAT)
          param.res.setReplacement(SE_LOCK, "string", "lockscreen_short_unlock_hint", hintText);
      }

      if(JELLYBEAN && prefs.getBoolean("key_more_unlock_blinds", false))
      {
        param.res.setReplacement(SE_LOCK, "integer", "number_of_blinds", 28);
        param.res.setReplacement(SE_LOCK, "integer", "blinds_affected_by_touch", 10);
      }
    }    
    else if(param.packageName.equals(SE_HOME) || param.packageName.equals(SE_HOME+".z1") || param.packageName.equals(SE_HOME+".z2"))
    {
      SE_HOME_PACKAGE = param.packageName;
      prefs.reload();
      
      ModuleResources res = ModuleResources.createInstance(MODULE_PATH, param.res);
      HomeResources.updateResources(param.res, res);
    }
  }

////////////////////////////////////////////////////////////

  @Override
  public void handleLoadPackage(XC_LoadPackage.LoadPackageParam param) throws Throwable
  {
    if(param.packageName.equals(ANDROID))
    {
      prefs.reload();
      SystemUIHooks.hookWindowManager(param);
      if(JELLYBEAN)
        hookKeyguard(param);
    }
    else if(param.packageName.equals(KEYGUARD))
    {
      if(KITKAT)
        hookKeyguard(param);
    }
    else if(param.packageName.equals(SYSTEMUI))
    {
      if(JELLYBEAN)
      {
        setupClassLoader(param);
        prefs.reload();
        SystemUIHooks.hookSystemUI(param);
      }
    }
    else if(param.packageName.equals(SE_HOME) || param.packageName.equals(SE_HOME+".z1") || param.packageName.equals(SE_HOME+".z2"))
    {
      SE_HOME_PACKAGE = param.packageName;
      prefs.reload();

      setupClassLoader(param);
      Conditionals.initLauncher();
      com.gem.xperiaxposed.home.HomeHooks.hookTransparency(param);
      com.gem.xperiaxposed.home.HomeHooks.hookFont(param);
      com.gem.xperiaxposed.home.HomeHooks.hookLayout(param);
      com.gem.xperiaxposed.home.HomeHooks.hookDesktop(param);
      com.gem.xperiaxposed.home.HomeHooks.hookDock(param);
      com.gem.xperiaxposed.home.HomeHooks.hookDrawer(param);
      com.gem.xperiaxposed.home.HomeHooks.hookFolders(param);
      com.gem.xperiaxposed.home.HomeHooks.hookWidgets(param);
      com.gem.xperiaxposed.home.HomeHooks.hookExperimental(param);
    }
    else if(param.packageName.equals(SE_LOCK))
    {
      prefs.reload();
      hookLockscreen(param);
    }
  }
  
////////////////////////////////////////////////////////////

  private void hookKeyguard(XC_LoadPackage.LoadPackageParam param)
  {
    if(prefs.getBoolean("key_transparent_lockscreen", false))
    {
      try {
      findAndHookMethod(KEYGUARD + ".KeyguardHostView", param.classLoader, "onFinishInflate", new XC_MethodHook()
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          View view = (View)param.thisObject;
          view.setSystemUiVisibility(view.getSystemUiVisibility() | SystemUIResources.SYSTEM_UI_FLAG_FULL_TRANSPARENCY);
        }
      });
      } catch(Throwable ex) { log(ex); }
    }
    
    final String carrierText = prefs.getString("key_carrier_text", "");
    if(!carrierText.isEmpty())
    {
      try {
      findAndHookMethod(KEYGUARD + ".CarrierText", param.classLoader, "updateCarrierText", 
                        findClass("com.android.internal.telephony.IccCardConstants.State", param.classLoader), 
                        CharSequence.class, CharSequence.class, new XC_MethodReplacement() 
      {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
        {
          ((TextView)param.thisObject).setText(carrierText);
          return null;
        }
      });
      } catch(Throwable ex) { log(ex); }
    }

    if(prefs.getBoolean("key_enable_standard_lockscreen", false))
    {
      try {
      findAndHookMethod(KEYGUARD + ".KeyguardViewManager", param.classLoader, "maybeCreateKeyguardLocked", boolean.class, boolean.class, Bundle.class, new XC_MethodHook()
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          WindowManager.LayoutParams params = (WindowManager.LayoutParams)getObjectField(param.thisObject, "mWindowLayoutParams");
          if((params.flags & WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER) == 0 || params.format != PixelFormat.TRANSLUCENT)
          {
            params.flags |= WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;
            params.format = PixelFormat.TRANSLUCENT;
            ViewManager viewManager = (ViewManager)getObjectField(param.thisObject, "mViewManager");
            FrameLayout keyguardHost = (FrameLayout)getObjectField(param.thisObject, "mKeyguardHost");
            viewManager.updateViewLayout(keyguardHost, params);
          }
        }
      });
      } catch(Throwable ex) { log(ex); }
      try {
      findAndHookMethod(KEYGUARD + ".KeyguardSelectorView", param.classLoader, "onFinishInflate", new XC_MethodHook()
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          View mGlowPadView = (View)XposedHelpers.getObjectField(param.thisObject, "mGlowPadView");
          setIntField(mGlowPadView, "mGravity", Gravity.CENTER);
        }
      });
      } catch(Throwable ex) { log(ex); }
      try {
      findAndHookMethod(KEYGUARD + ".ExternalLockScreen", param.classLoader, "validateExternalLockScreen", Context.class, ComponentName.class, new XC_MethodReplacement()
      {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
        {
          return false;
        }
      });
      } catch(Throwable ex) { log(ex); }
    }    

    if(prefs.getBoolean("key_hide_widget_backplate", false))
    {
      if(JELLYBEAN)
      {
        try {
        hookAllConstructors(findClass(KEYGUARD + ".KeyguardWidgetFrame", param.classLoader), new XC_MethodHook()
        {
          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable
          {
            setObjectField(param.thisObject, "mBackgroundDrawable", new ColorDrawable(0));
          }
        });
        } catch(Throwable ex) { log(ex); }
      }
      if(KITKAT)
      {
        try {
        findAndHookMethod(KEYGUARD + ".KeyguardWidgetFrame", param.classLoader, "drawBg", Canvas.class, new XC_MethodHook()
        {
          @Override
          protected void beforeHookedMethod(MethodHookParam param) throws Throwable
          {
            param.setResult(null);
          }
        });
        } catch(Throwable ex) { log(ex); }
      }
    }
    
    if(prefs.getBoolean("key_slide_before_unlock", false))
    {
      try {
      final Object SecurityModeNone = getStaticObjectField(findClass(KEYGUARD + ".KeyguardSecurityModel$SecurityMode", param.classLoader), "None");
      findAndHookMethod(KEYGUARD + ".KeyguardHostView", param.classLoader, "showPrimarySecurityScreen", boolean.class, new XC_MethodHook()
      {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable
        {
          callMethod(param.thisObject, "showSecurityScreen", SecurityModeNone);
          param.setResult(null);
        }
      });
      findAndHookMethod(KEYGUARD + ".KeyguardHostView", param.classLoader, "showNextSecurityScreenOrFinish", boolean.class, new XC_MethodHook()
      {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable
        {
          if(!(Boolean)param.args[0])
          {
            Object mode = callMethod(getObjectField(param.thisObject, "mSecurityModel"), "getSecurityMode");
            if(mode != SecurityModeNone)
            {
              callMethod(param.thisObject, "showSecurityScreen", mode);
              param.setResult(null);
            }
          }
        }
      });
      } catch(Throwable ex) { log(ex); }
    }
  }

////////////////////////////////////////////////////////////

  private void hookLockscreen(XC_LoadPackage.LoadPackageParam param)
  {
    if(JELLYBEAN && prefs.getBoolean("key_slide_before_unlock", false) && !prefs.getBoolean("key_enable_standard_lockscreen", false))
    {
      try {
      findAndHookMethod("com.sonymobile.lockscreen.xperia.widget.blindslayout.BlindsRelativeLayout", param.classLoader, "onExitTransitionFinished", 
        new XC_MethodHook()
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          setBooleanField(param.thisObject, "mSkipDraw", false);
          setBooleanField(param.thisObject, "mDrawingBlinds", false);
        }
      });
      } catch(Throwable ex) { log(ex); }
      try {
      findAndHookMethod("com.sonymobile.lockscreen.xperia.FadeAllUnlockTransitionStrategy", param.classLoader, "startUnlockTransition",
        new XC_MethodHook()
      {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable
        {
          param.setResult(null);
        }
      });
      } catch(Throwable ex) { log(ex); }
    }
  }
  
////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////
