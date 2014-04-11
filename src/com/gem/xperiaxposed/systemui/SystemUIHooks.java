package com.gem.xperiaxposed.systemui;

import static com.gem.xperiaxposed.Constants.*;
import static com.gem.xperiaxposed.XposedMain.*;
import static com.gem.xperiaxposed.systemui.SystemUIResources.*;
import static com.gem.xposed.ReflectionUtils.*;
import static de.robv.android.xposed.XposedBridge.*;
import static de.robv.android.xposed.XposedHelpers.*;

import java.util.HashSet;
import java.util.Set;

import android.view.KeyEvent;
import android.view.View;

import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.gem.xposed.AutoHook;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

////////////////////////////////////////////////////////////

public class SystemUIHooks
{

////////////////////////////////////////////////////////////

  public static void hookWindowManager(XC_LoadPackage.LoadPackageParam param)
  {
    if(prefs.getBoolean("key_systemui_enable_appearance_customization", false))
    {
      try {
      findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", param.classLoader, "filterSystemUiVisibility", int.class, int.class, new XC_MethodHook()
      {
        private String lastPackageName = null;
        private int enableFlags = 0;  
        private int disableFlags = 0;  
        
        private void updateRoundedCorners(String value)
        {
          if("Enable".equals(value))
          {
            enableFlags |= SYSTEM_UI_FLAG_ROUNDED_CORNERS;
            disableFlags |= SYSTEM_UI_FLAG_DISABLE_ROUNDED_CORNERS;
          }
          else if("Disable".equals(value))
          {
            enableFlags |= SYSTEM_UI_FLAG_DISABLE_ROUNDED_CORNERS;
            disableFlags |= SYSTEM_UI_FLAG_ROUNDED_CORNERS;
          }
        }
        
        private void updateColor(String value)
        {
          if("Dark".equals(value))
          {
            enableFlags |= 0;
            disableFlags |= SYSTEM_UI_FLAG_TRANSPARENT | SYSTEM_UI_FLAG_FULL_TRANSPARENCY | SYSTEM_UI_FLAG_LIGHT;
          }
          else if("Light".equals(value))
          {
            enableFlags |= SYSTEM_UI_FLAG_LIGHT;
            disableFlags |= SYSTEM_UI_FLAG_TRANSPARENT | SYSTEM_UI_FLAG_FULL_TRANSPARENCY;
          }
          else if("Translucent".equals(value))
          {
            enableFlags |= SYSTEM_UI_FLAG_TRANSPARENT;
            disableFlags |= SYSTEM_UI_FLAG_FULL_TRANSPARENCY | SYSTEM_UI_FLAG_LIGHT;
          }
          else if("Transparent".equals(value))
          {
            enableFlags |= SYSTEM_UI_FLAG_TRANSPARENT | SYSTEM_UI_FLAG_FULL_TRANSPARENCY;
            disableFlags |= SYSTEM_UI_FLAG_LIGHT;
          }
        }
        
        private void updatePackage(MethodHookParam param)
        {
          Object mFocusedWindow = getObjectField(param.thisObject, "mFocusedWindow");
          String packageName = (String)callMethod(mFocusedWindow, "getOwningPackage");
          if(packageName == lastPackageName || (packageName != null && packageName.equals(lastPackageName)))
            return;
          
          lastPackageName = packageName;
          enableFlags = 0;
          disableFlags = 0;
  
          prefs.reload();
          
          String corners = prefs.getString("key_systemui_app_rounded_corners$" + packageName, "Default");
          if("Default".equals(corners))
            corners = prefs.getString("key_systemui_app_rounded_corners", "Default");
          updateRoundedCorners(corners);
          
          if(!ANDROID.equals(packageName))
          {
            String color = prefs.getString("key_systemui_app_color$" + packageName, "Default");
            if("Default".equals(color))
              color = prefs.getString("key_systemui_app_color", "Default");
            updateColor(color);
          }
        }
        
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          updatePackage(param);
          if(enableFlags != 0 || disableFlags != 0)
            param.setResult(((Integer)param.getResult() | enableFlags) & ~disableFlags);
        }
      });
      } catch(Throwable ex) { log(ex); }
    }
    
    if(prefs.getBoolean("key_volume_keys_wake", false))
    {
      try {
      findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", param.classLoader, "interceptKeyBeforeQueueing", 
        KeyEvent.class,
        int.class,
        boolean.class,
        new XC_MethodHook()
      {
        private static final int FLAG_WAKE = 0x00000001;
        private static final int FLAG_WAKE_DROPPED = 0x00000002;
        
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable
        {
          KeyEvent event = (KeyEvent)param.args[0];
          int keyCode = event.getKeyCode();
          if(keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
          {
            int flags = (Integer)param.args[1];
            flags |= FLAG_WAKE | FLAG_WAKE_DROPPED;
            param.args[1] = flags;
          }
        }
      });
      } catch(Throwable ex) { log(ex); }

      try {
      findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", param.classLoader, "isWakeKeyWhenScreenOff", 
        int.class,
        new XC_MethodHook()
      {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable
        {
          int keyCode = (Integer)param.args[0];
          if(keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
            param.setResult(true);
        }
      });
      } catch(Throwable ex) { log(ex); }
    }
  }
  
  @SuppressWarnings("unused")
  public static void hookSystemUI(XC_LoadPackage.LoadPackageParam param)
  {
    Set<String> status_gradient = prefs.getStringSet("key_systemui_status_gradient", new HashSet<String>());
    final boolean status_gradient_active = !status_gradient.isEmpty() && status_gradient.size() != 4; 
    Set<String> nav_gradient = prefs.getStringSet("key_systemui_nav_gradient", new HashSet<String>());
    final boolean nav_gradient_active = !nav_gradient.isEmpty() && nav_gradient.size() != 4; 
    if(status_gradient_active || nav_gradient_active)
    {
      final int transparent_status_gradient = status_gradient.contains("transparent") ? View.VISIBLE : View.GONE;
      final int translucent_status_gradient = status_gradient.contains("translucent") ? View.VISIBLE : View.GONE;
      final int light_status_gradient = status_gradient.contains("light") ? View.VISIBLE : View.GONE;
      final int dark_status_gradient = status_gradient.contains("dark") ? View.VISIBLE : View.GONE;
      
      final int transparent_nav_gradient = nav_gradient.contains("transparent") ? View.VISIBLE : View.GONE;
      final int translucent_nav_gradient = nav_gradient.contains("translucent") ? View.VISIBLE : View.GONE;
      final int light_nav_gradient = nav_gradient.contains("light") ? View.VISIBLE : View.GONE;
      final int dark_nav_gradient = nav_gradient.contains("dark") ? View.VISIBLE : View.GONE;
      
      new AutoHook()
      {
        public void after_swapViews(PhoneStatusBar thiz, View view, int id1, int id2, long l)
        {
          if(status_gradient_active)
          {
            View statusBar = getField(thiz, "mStatusBarWindow");
            if(view == statusBar)
            {
              int vis = getField(thiz, "mSystemUiVisibility");
              int visible =
                (vis & SYSTEM_UI_FLAG_FULL_TRANSPARENCY) != 0 ? transparent_status_gradient :
                (vis & SYSTEM_UI_FLAG_TRANSPARENT) != 0 ? translucent_status_gradient :
                (vis & SYSTEM_UI_FLAG_LIGHT) != 0 ? light_status_gradient : dark_status_gradient;
              updateVisibility(statusBar.findViewById(Ids.status_bar_gradient_view), visible);
              return;
            }
          }
          
          if(nav_gradient_active)
          {
            View navBar = getField(thiz, "mNavigationBarView");
            if(view.getParent() == navBar)
            {
              int vis = getField(thiz, "mSystemUiVisibility");
              int visible =
                (vis & SYSTEM_UI_FLAG_FULL_TRANSPARENCY) != 0 ? transparent_nav_gradient :
                (vis & SYSTEM_UI_FLAG_TRANSPARENT) != 0 ? translucent_nav_gradient :
                (vis & SYSTEM_UI_FLAG_LIGHT) != 0 ? light_nav_gradient : dark_nav_gradient;
              updateVisibility(navBar.findViewById(Ids.navigation_bar_gradient_view), visible);
              updateVisibility(navBar.findViewById(Ids.navigation_bar_gradient_view_land), visible);
              return;
            }
          }
        }
        
        private void updateVisibility(View gradient, int visible)
        {
          if(gradient != null && gradient.getVisibility() != visible)
            gradient.setVisibility(visible);
        }
      };
    }
  }
  
////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////
