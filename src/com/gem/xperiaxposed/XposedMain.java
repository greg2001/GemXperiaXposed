package com.gem.xperiaxposed;

import static de.robv.android.xposed.XposedBridge.*;
import static de.robv.android.xposed.XposedHelpers.*;

import java.util.*;

import android.appwidget.*;
import android.content.*;
import android.content.pm.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import com.gem.xperiaxposed.home.*;

import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.*;

////////////////////////////////////////////////////////////

public class XposedMain implements IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources
{

////////////////////////////////////////////////////////////
  
  public static final String ANDROID = "android";
  public static final String SYSTEMUI = "com.android.systemui";
  public static final String SE_HOME = "com.sonyericsson.home";
  public static final String SE_LOCK = "com.sonyericsson.lockscreen.uxpnxt";
  public static final String KEYGUARD_PACKAGE = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) ?
    "com.android.keyguard" : "com.android.internal.policy.impl.keyguard";
  
////////////////////////////////////////////////////////////
  
  public static final int SYSTEM_UI_TRANSPARENT_BACKGROUND      = 0x99000000;
  public static final int SYSTEM_UI_OPAQUE_BACKGROUND           = 0xff000000;
  public static final int SYSTEM_UI_LIGHT_BACKGROUND            = 0xff4d4d4d;
  
////////////////////////////////////////////////////////////

  public static String MODULE_PATH;
  public static XSharedPreferences prefs;

////////////////////////////////////////////////////////////

  public static int SYSTEM_UI_FLAG_TRANSPARENT = 0;
  public static int SYSTEM_UI_FLAG_FULL_TRANSPARENCY = 0;
  public static int SYSTEM_UI_FLAG_LIGHT = 0;
  public static int SYSTEM_UI_FLAG_ROUNDED_CORNERS = 0;
  public static int SYSTEM_UI_FLAG_DISABLE_ROUNDED_CORNERS = 0;
  public static int SYSTEM_UI_FLAG_SUPPRESS_NAVIGATION = 0;
  
  private static void initFlags()
  {
    SYSTEM_UI_FLAG_TRANSPARENT                  = getFlag("SYSTEM_UI_FLAG_TRANSPARENT");
    SYSTEM_UI_FLAG_FULL_TRANSPARENCY            = getFlag("SYSTEM_UI_FLAG_FULL_TRANSPARENCY");
    SYSTEM_UI_FLAG_LIGHT                        = getFlag("SYSTEM_UI_FLAG_LIGHT");
    SYSTEM_UI_FLAG_ROUNDED_CORNERS              = getFlag("SYSTEM_UI_FLAG_ROUNDED_CORNERS");
    SYSTEM_UI_FLAG_DISABLE_ROUNDED_CORNERS      = getFlag("SYSTEM_UI_FLAG_DISABLE_ROUNDED_CORNERS");
    SYSTEM_UI_FLAG_SUPPRESS_NAVIGATION          = getFlag("SYSTEM_UI_FLAG_SUPPRESS_NAVIGATION");
  }
  
  private static int getFlag(String flag)
  {
    try
    {
      return View.class.getField(flag).getInt(null);
    }
    catch(Exception ex)
    {
      return 0;
    }
  }

////////////////////////////////////////////////////////////

  @Override
  public void initZygote(IXposedHookZygoteInit.StartupParam param) throws Throwable
  {
    initFlags();
    
    MODULE_PATH = param.modulePath;
    prefs = new XSharedPreferences(XposedMain.class.getPackage().getName());
    prefs.makeWorldReadable();

    if(prefs.getBoolean("key_hide_shortcuts", false))
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
      prefs.reload();
      
      boolean status_bar_custom = "custom".equals(prefs.getString("key_systemui_status_color_set", "custom"));
      boolean nav_bar_custom = "custom".equals(prefs.getString("key_systemui_nav_color_set", "status"));
      boolean nav_bar_same = "status".equals(prefs.getString("key_systemui_nav_color_set", "status"));
      
      int system_ui_transparent_background = prefs.getInt("key_systemui_translucent_background", SYSTEM_UI_TRANSPARENT_BACKGROUND);
      int system_ui_opaque_background = prefs.getInt("key_systemui_dark_background", SYSTEM_UI_OPAQUE_BACKGROUND);
      int system_ui_light_background = prefs.getInt("key_systemui_light_background", SYSTEM_UI_LIGHT_BACKGROUND);
      int system_ui_nav_transparent_background = system_ui_transparent_background;
      int system_ui_nav_opaque_background = system_ui_opaque_background;
      int system_ui_nav_light_background = system_ui_light_background;
      if(!nav_bar_same)
      {
        system_ui_nav_transparent_background = prefs.getInt("key_systemui_nav_translucent_background", SYSTEM_UI_TRANSPARENT_BACKGROUND);
        system_ui_nav_opaque_background = prefs.getInt("key_systemui_nav_dark_background", SYSTEM_UI_OPAQUE_BACKGROUND);
        system_ui_nav_light_background = prefs.getInt("key_systemui_nav_light_background", SYSTEM_UI_LIGHT_BACKGROUND);
      }
      
      if(status_bar_custom)
      {
        param.res.setReplacement(SYSTEMUI, "color", "system_ui_transparent_background", system_ui_transparent_background);
        param.res.setReplacement(SYSTEMUI, "color", "system_ui_opaque_background", system_ui_opaque_background);
        param.res.setReplacement(SYSTEMUI, "color", "system_ui_light_background", system_ui_light_background);
        param.res.setReplacement(SYSTEMUI, "drawable", "status_bar_opaque_background", system_ui_opaque_background);
        param.res.setReplacement(SYSTEMUI, "drawable", "status_bar_transparent_background", system_ui_transparent_background);
        param.res.setReplacement(SYSTEMUI, "drawable", "status_bar_light_background", system_ui_light_background);
        param.res.setReplacement(SYSTEMUI, "drawable", "status_bar_lights_out_background", system_ui_opaque_background);
      }

      if(nav_bar_custom || (status_bar_custom && nav_bar_same))
      {
        param.res.setReplacement(SYSTEMUI, "drawable", "navigation_bar_opaque_background", system_ui_nav_opaque_background);
        param.res.setReplacement(SYSTEMUI, "drawable", "navigation_bar_opaque_background_land", system_ui_nav_opaque_background);
        param.res.setReplacement(SYSTEMUI, "drawable", "navigation_bar_transparent_background", system_ui_nav_transparent_background);
        param.res.setReplacement(SYSTEMUI, "drawable", "navigation_bar_transparent_background_land", system_ui_nav_transparent_background);
        param.res.setReplacement(SYSTEMUI, "drawable", "navigation_bar_light_background", system_ui_nav_light_background);
        param.res.setReplacement(SYSTEMUI, "drawable", "navigation_bar_light_background_land", system_ui_nav_light_background);
        param.res.setReplacement(SYSTEMUI, "drawable", "navigation_bar_lights_out_background", system_ui_nav_opaque_background);
        param.res.setReplacement(SYSTEMUI, "drawable", "navigation_bar_lights_out_background_land", system_ui_nav_opaque_background);
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
      }

      if(prefs.getBoolean("key_more_unlock_blinds", false))
      {
        param.res.setReplacement(SE_LOCK, "integer", "number_of_blinds", 28);
        param.res.setReplacement(SE_LOCK, "integer", "blinds_affected_by_touch", 10);
      }
    }    
    else if(param.packageName.equals(SE_HOME))
    {
      ModuleResources res = ModuleResources.createInstance(MODULE_PATH, param.res);
      prefs.reload();

      int desktopRows = Integer.parseInt(prefs.getString("key_desktop_rows", "4"));
      if(desktopRows != 4)
      {
//      unfortunately, way too small 
//      float height = param.res.getDimension(param.res.getIdentifier("desktop_cell_height", "dimen", SE_HOME));
        float height = res.getDimension(R.dimen.desktop_cell_height);
        height *= 4.0/desktopRows;
        res.setDimension(R.dimen.desktop_cell_height, height);

        param.res.setReplacement(SE_HOME, "integer", "desktop_grid_rows", desktopRows);
        param.res.setReplacement(SE_HOME, "dimen", "desktop_cell_height", res.fwd(R.dimen.desktop_cell_height));
      }

      int desktopColumns = Integer.parseInt(prefs.getString("key_desktop_columns", "4"));
      if(desktopColumns != 4)
      {
        float width = param.res.getDimension(param.res.getIdentifier("desktop_cell_width", "dimen", SE_HOME));
        width *= 4.0/desktopColumns;
        res.setDimension(R.dimen.desktop_cell_width, width);

        param.res.setReplacement(SE_HOME, "integer", "desktop_grid_columns", desktopColumns);
        param.res.setReplacement(SE_HOME, "dimen", "desktop_cell_width", res.fwd(R.dimen.desktop_cell_width));
      }
      
      if(prefs.getBoolean("key_desktop_multiline_labels", false))
        param.res.setReplacement(SE_HOME, "bool", "enable_desktop_multi_line_labels", true);
      if(prefs.getBoolean("key_desktop_autohide_pagination", false))
        param.res.setReplacement(SE_HOME, "bool", "desktop_pagination_autohide", true);

      int dockColumns = Integer.parseInt(prefs.getString("key_dock_columns", "5"));
      if(dockColumns != 5)
      {
        float width = param.res.getDimension(param.res.getIdentifier("stage_cell_width", "dimen", SE_HOME));
        width *= 5.0/dockColumns;
        res.setDimension(R.dimen.stage_cell_width, width);

        param.res.setReplacement(SE_HOME, "integer", "stage_grid_columns", dockColumns);
        param.res.setReplacement(SE_HOME, "integer", "max_stage_items", dockColumns-1);
        param.res.setReplacement(SE_HOME, "dimen", "stage_cell_width", res.fwd(R.dimen.stage_cell_width));
        param.res.setReplacement(SE_HOME, "dimen", "stage_cell_height", res.fwd(R.dimen.stage_cell_height));
      }
      
      int drawerRows = Integer.parseInt(prefs.getString("key_drawer_rows", "5"));
      if(drawerRows != 5)
      {
//      unfortunately, way too small 
//      float height = param.res.getDimension(param.res.getIdentifier("apptray_cell_height", "dimen", SE_HOME));
        float height = res.getDimension(R.dimen.apptray_cell_height);
        height *= 5.0/drawerRows;
        res.setDimension(R.dimen.apptray_cell_height, height);

        param.res.setReplacement(SE_HOME, "integer", "apptray_grid_rows", drawerRows);
        param.res.setReplacement(SE_HOME, "dimen", "apptray_cell_height", res.fwd(R.dimen.apptray_cell_height));
      }

      int drawerColumns = Integer.parseInt(prefs.getString("key_drawer_columns", "4"));
      if(drawerColumns != 4)
      {
        float width = param.res.getDimension(param.res.getIdentifier("apptray_cell_width", "dimen", SE_HOME));
        width *= 4.0/drawerColumns;
        res.setDimension(R.dimen.apptray_cell_width, width);

        param.res.setReplacement(SE_HOME, "integer", "apptray_grid_columns", drawerColumns);
        param.res.setReplacement(SE_HOME, "dimen", "apptray_cell_width", res.fwd(R.dimen.apptray_cell_width));
      }
      
      int iconSize = prefs.getInt("key_launcher_icon_size", 100);
      if(iconSize != 100)
      {
        float width = param.res.getDimension(param.res.getIdentifier("icon_image_width", "dimen", SE_HOME));
        width *= iconSize/100.0;
        res.setDimension(R.dimen.icon_image_width, width);
        
        float height = param.res.getDimension(param.res.getIdentifier("icon_image_height", "dimen", SE_HOME));
        height *= iconSize/100.0;
        res.setDimension(R.dimen.icon_image_height, height);
        
        param.res.setReplacement(SE_HOME, "dimen", "icon_image_width", res.fwd(R.dimen.icon_image_height));
        param.res.setReplacement(SE_HOME, "dimen", "icon_image_height", res.fwd(R.dimen.icon_image_height));
      }

      int textSize = prefs.getInt("key_launcher_label_text_size", 100);
      if(textSize != 100)
      {
        float size = param.res.getDimension(param.res.getIdentifier("icon_label_text_size", "dimen", SE_HOME));
        size *= textSize/100.0;
        res.setDimension(R.dimen.icon_label_text_size, size);
        
        param.res.setReplacement(SE_HOME, "dimen", "icon_label_text_size", res.fwd(R.dimen.icon_label_text_size));
      }

      if(prefs.getBoolean("key_large_dock_reflection", false))
      {
        float size = param.res.getDimension(param.res.getIdentifier("stage_mirror_size", "dimen", SE_HOME));
        size *= 2;
        res.setDimension(R.dimen.stage_mirror_size, size);

        param.res.setReplacement(SE_HOME, "dimen", "stage_mirror_size", res.fwd(R.dimen.stage_mirror_size));
      }

      if(prefs.getBoolean("key_enable_experimental", false))
      {
        param.res.setReplacement(Ids.home_apptray_dropzone_hide, res.fwd(R.drawable.home_apptray_dropzone_hide));
        param.res.setReplacement(Ids.home_apptray_dropzone_unhide, res.fwd(R.drawable.home_apptray_dropzone_unhide));
        param.res.setReplacement(Ids.app_tray_drawer_list_item_categories_hidden, res.fwd(R.string.app_tray_drawer_list_item_categories_hidden));
        param.res.setReplacement(Ids.app_tray_drawer_list_item_categories_settings, res.fwd(R.string.app_tray_drawer_list_item_categories_settings));
        param.res.setReplacement(Ids.drawer_icn_hidden, res.fwd(R.drawable.drawer_icn_hidden));
        param.res.setReplacement(Ids.drawer_icn_settings, res.fwd(R.drawable.drawer_icn_settings));
      }
    }
  }

////////////////////////////////////////////////////////////

  @Override
  public void handleLoadPackage(XC_LoadPackage.LoadPackageParam param) throws Throwable
  {
    if(param.packageName.equals(ANDROID))
    {
      prefs.reload();
      hookWindowManager(param);
      hookKeyguard(param);
    }
    else if(param.packageName.equals(SE_HOME))
    {
      prefs.reload();
      hookLauncherTransparency(param);
      hookLauncherFont(param);
      hookLauncherDock(param);
      hookLauncherDrawer(param);
      hookLauncherFolders(param);
      hookLauncherWidgets(param);
      hookLauncherExperimental(param);
    }
  }
  
////////////////////////////////////////////////////////////

  private void hookWindowManager(XC_LoadPackage.LoadPackageParam param)
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
      } catch(Exception ex) { log(ex); }
    }
  }
  
////////////////////////////////////////////////////////////

  private void hookKeyguard(XC_LoadPackage.LoadPackageParam param)
  {
    if(prefs.getBoolean("key_transparent_lockscreen", false))
    {
      try {
      findAndHookMethod(KEYGUARD_PACKAGE + ".KeyguardHostView", param.classLoader, "onFinishInflate", new XC_MethodHook()
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          setFullTransparent((View)param.thisObject, true);
        }
      });
      } catch(Exception ex) { log(ex); }
    }
    
    final String carrierText = prefs.getString("key_carrier_text", "");
    if(!carrierText.isEmpty())
    {
      try {
      findAndHookMethod(KEYGUARD_PACKAGE + ".CarrierText", param.classLoader, "updateCarrierText", 
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
      } catch(Exception ex) { log(ex); }
    }

    if(prefs.getBoolean("key_enable_standard_lockscreen", false))
    {
      try {
      findAndHookMethod(KEYGUARD_PACKAGE + ".KeyguardViewManager", param.classLoader, "maybeCreateKeyguardLocked", boolean.class, boolean.class, Bundle.class, new XC_MethodHook()
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
      } catch(Exception ex) { log(ex); }
      try {
      findAndHookMethod(KEYGUARD_PACKAGE + ".KeyguardSelectorView", param.classLoader, "onFinishInflate", new XC_MethodHook()
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          View mGlowPadView = (View)XposedHelpers.getObjectField(param.thisObject, "mGlowPadView");
          setIntField(mGlowPadView, "mGravity", Gravity.CENTER);
        }
      });
      } catch(Exception ex) { log(ex); }
      try {
      findAndHookMethod(KEYGUARD_PACKAGE + ".ExternalLockScreen", param.classLoader, "validateExternalLockScreen", Context.class, ComponentName.class, new XC_MethodReplacement()
      {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
        {
          return false;
        }
      });
      } catch(Exception ex) { log(ex); }
    }    

    if(prefs.getBoolean("key_hide_widget_backplate", false))
    {
      try {
      hookAllConstructors(findClass(KEYGUARD_PACKAGE + ".KeyguardWidgetFrame", param.classLoader), new XC_MethodHook()
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          setObjectField(param.thisObject, "mBackgroundDrawable", new ColorDrawable(0));
        }
      });
      } catch(Exception ex) { log(ex); }
    }
  }

  ////////////////////////////////////////////////////////////

  private void hookLauncherTransparency(XC_LoadPackage.LoadPackageParam param)
  {
    final int system_ui_transparent_background = prefs.getInt("key_systemui_translucent_background", SYSTEM_UI_TRANSPARENT_BACKGROUND);
    try {
    findAndHookMethod("com.sonymobile.home.util.SystemUiExtensions", param.classLoader, "getSystemUiBackgroundColor", Context.class, new XC_MethodReplacement() 
    {
      @Override
      protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
      {
        return system_ui_transparent_background;
      }
    });
    } catch(Exception ex) { log(ex); }
    
    if(!prefs.getBoolean("key_menu_dark_bars", false))
    {
      try {
      findAndHookMethod("com.sonymobile.home.apptray.AppTrayPresenter", param.classLoader, "setSystemUiTransparent", boolean.class, new XC_MethodReplacement() 
      {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
        {
          return null;
        }
      });
      } catch(Exception ex) { log(ex); }
    }

    final boolean transparentDesktop = prefs.getBoolean("key_transparent_desktop", false);
    final boolean transparentDrawer = prefs.getBoolean("key_transparent_drawer", false);

    if(transparentDesktop || transparentDrawer)
    {
      // transparent SystemUI
      try {
      hookAllConstructors(findClass("com.sonymobile.home.MainView", param.classLoader), new XC_MethodHook()
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          setFullTransparent((View)param.thisObject, transparentDesktop);
        }
      });
      } catch(Exception ex) { log(ex); }
      try {
      findAndHookMethod("com.sonymobile.home.MainView", param.classLoader, "showApptray", boolean.class, new XC_MethodHook() 
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          if((Boolean)param.args[0])
            setFullTransparent((View)param.thisObject, transparentDrawer);
        }
      });
      } catch(Exception ex) { log(ex); }
      try {
      findAndHookMethod("com.sonymobile.home.MainView", param.classLoader, "showDesktop", boolean.class, new XC_MethodHook() 
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          if((Boolean)param.args[0])
            setFullTransparent((View)param.thisObject, transparentDesktop);
        }
      });
      } catch(Exception ex) { log(ex); }
      try {
      findAndHookMethod("com.sonymobile.home.HomeFragment", param.classLoader, "setFocused", boolean.class, new XC_MethodHook() 
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          if((Boolean)param.args[0])
          {
            Object mainView = getObjectField(param.thisObject, "mMainView");
            if(mainView != null)
            {
              if((Boolean)callMethod(mainView, "isDesktopOpen"))
                setFullTransparent((View)mainView, transparentDesktop);
              else if((Boolean)callMethod(mainView, "isAppTrayOpen"))
                setFullTransparent((View)mainView, transparentDrawer);
            }
          }
        }
      });
      } catch(Exception ex) { log(ex); }
      try {
      findAndHookMethod("com.sonymobile.ui.support.SystemUiVisibilityWrapper", param.classLoader, "apply", new XC_MethodHook() 
      {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable
        {
          Object mainView = getObjectField(param.thisObject, "mView");
          if(mainView != null)
          {
            if((Boolean)callMethod(mainView, "isDesktopOpen"))
            {
              if(transparentDesktop)
                callMethod(param.thisObject, "setFlag", SYSTEM_UI_FLAG_FULL_TRANSPARENCY, true);
            }
            else if((Boolean)callMethod(mainView, "isAppTrayOpen"))
            {
              if(transparentDrawer)
                callMethod(param.thisObject, "setFlag", SYSTEM_UI_FLAG_FULL_TRANSPARENCY, true);
            }
          }
        }
      });
      } catch(Exception ex) { log(ex); }
    }

    if(transparentDrawer)
    {
      try {
      hookAllConstructors(findClass("com.sonymobile.home.apptray.AppTrayView", param.classLoader), new XC_MethodHook()
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          setIntField(param.thisObject, "mBackgroundColor", 0);
        }
      });
      } catch(Exception ex) { log(ex); }
    }
  }
  
////////////////////////////////////////////////////////////

  private void hookLauncherFont(XC_LoadPackage.LoadPackageParam param)
  {
    if(prefs.getBoolean("key_condensed_font", false))
    {
      final Typeface CONDENSED_FONT = Typeface.createFromFile("/system/fonts/RobotoCondensed-Regular.ttf");
      try {
      findAndHookMethod("com.sonymobile.home.textview.TextViewUtilities", param.classLoader, "createTextView", 
                        Context.class, float.class, int.class,
                        new XC_MethodHook() 
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          ((TextView)param.getResult()).getPaint().setTypeface(CONDENSED_FONT);
        }
      });
      } catch(Exception ex) { log(ex); }
      try {
      findAndHookMethod("com.sonymobile.home.textview.TextViewUtilities", param.classLoader, "createTextView", 
                        Context.class, String.class, float.class, int.class, int.class, Rect.class, Typeface.class, int.class, 
                        new XC_MethodHook() 
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          ((TextView)param.getResult()).getPaint().setTypeface(CONDENSED_FONT);
        }
      });
      } catch(Exception ex) { log(ex); }
    }
  }
  
////////////////////////////////////////////////////////////

  private void hookLauncherDock(XC_LoadPackage.LoadPackageParam param)
  {
    if(prefs.getBoolean("key_disable_dock_stage", false))
    {
      try {
      findAndHookMethod("com.sonymobile.home.stage.StageView", param.classLoader, "updateBackground", new XC_MethodHook() 
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          callMethod(getObjectField(param.thisObject, "mBackground"), "setBitmap", (Object)null);
        }
      });      
      } catch(Exception ex) { log(ex); }
    }
      
    if(prefs.getBoolean("key_disable_dock_reflection", false))
    {
      try {
      hookAllConstructors(findClass("com.sonymobile.home.bitmap.MirrorBitmapDrawable", param.classLoader), new XC_MethodHook()
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          setBooleanField(param.thisObject, "mMirror", false);
        }
      });
      } catch(Exception ex) { log(ex); }
    }
  }
  
////////////////////////////////////////////////////////////

  private void hookLauncherDrawer(XC_LoadPackage.LoadPackageParam param)
  {
    if(prefs.getBoolean("key_disable_drawer_backplate", false))
    {
      try {
      hookAllConstructors(findClass("com.sonymobile.home.apptray.AppTrayPageView", param.classLoader), new XC_MethodHook()
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          callMethod(getObjectField(param.thisObject, "mContent"), "removeChild", getObjectField(param.thisObject, "mDefaultBackplate"));
          callMethod(getObjectField(param.thisObject, "mContent"), "removeChild", getObjectField(param.thisObject, "mUninstallBackplate"));
        }
      });
      } catch(Exception ex) { log(ex); }
    }

    if(prefs.getBoolean("key_remember_drawer_page", false))
    {
      try {
      findAndHookMethod("com.sonymobile.home.apptray.AppTrayView", param.classLoader, "gotoDefaultPage", new XC_MethodReplacement()
      {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
        {
          return null;
        }
      });
      } catch(Exception ex) { log(ex); }
    }
  }

////////////////////////////////////////////////////////////

  private void hookLauncherFolders(XC_LoadPackage.LoadPackageParam param)
  {
    final int folderColumns = Integer.parseInt(prefs.getString("key_folder_columns", "4"));
    if(folderColumns != 4)
    {
      try {
      findAndHookMethod("com.sonymobile.home.folder.GridView", param.classLoader, "setCellWidth", float.class, new XC_MethodReplacement() 
      {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
        {
          setFloatField(param.thisObject, "mCellWidth", (Float)param.args[0] * 4 / folderColumns);
          return null;
        }
      });
      } catch(Exception ex) { log(ex); }
    }

    if(prefs.getBoolean("key_folder_multiline_labels", false))
    {
      try {
      findAndHookMethod("com.sonymobile.home.folder.OpenFolderAdapter", param.classLoader, "getItemView", int.class, new XC_MethodHook() 
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          try
          {
            setBooleanField(param.getResult(), "mCenterVertically", false);
          }
          catch(Throwable ex)
          {
          }
        }
      });
      } catch(Exception ex) { log(ex); }
      try {
      findAndHookMethod("com.sonymobile.home.presenter.view.ItemViewCreatorBase", param.classLoader, "getItemViewTextLines", String.class, new XC_MethodHook() 
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          if("folder".equals(param.args[0]))
            param.setResult(2);
        }
      });
      } catch(Exception ex) { log(ex); }
    }
    
    if(prefs.getBoolean("key_folder_disable_background_dim", false))
    {
      try {
      findAndHookMethod("com.sonymobile.home.folder.OpenFolderView$DimAnimation", param.classLoader, "onUpdate", float.class, float.class, new XC_MethodReplacement() 
      {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
        {
          return null;
        }
      });
      } catch(Exception ex) { log(ex); }
    }
  }
  
////////////////////////////////////////////////////////////
  
  private WeakHashMap<Object, int[]> advWidgetSizes = new WeakHashMap<Object, int[]>();
  
  private void hookLauncherWidgets(XC_LoadPackage.LoadPackageParam param)
  {
    if(prefs.getBoolean("key_all_widgets_resizable", false))
    {
      try {
      findAndHookMethod("com.sonymobile.home.ui.widget.HomeAppWidgetManager", param.classLoader, "getResizeMode", int.class, new XC_MethodHook() 
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          param.setResult(AppWidgetProviderInfo.RESIZE_BOTH);
        }
      });
      } catch(Exception ex) { log(ex); }
      try {
      findAndHookMethod("com.sonymobile.home.ui.widget.HomeAdvWidget", param.classLoader, "createAppWidgetInfo", PackageManager.class, new XC_MethodHook() 
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          AppWidgetProviderInfo info = (AppWidgetProviderInfo)getObjectField(param.thisObject, "mAppWidgetProviderInfo");
          info.resizeMode = AppWidgetProviderInfo.RESIZE_BOTH;
        }
      });
      } catch(Exception ex) { log(ex); }
      try {
      findAndHookMethod("com.sonymobile.home.cui.CuiWidgetLoadHelper", param.classLoader, "getVanillaSpanXY", Context.class, AppWidgetProviderInfo.class, new XC_MethodHook() 
      {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable
        {
          AppWidgetProviderInfo info = (AppWidgetProviderInfo)param.args[1];
          info.resizeMode = AppWidgetProviderInfo.RESIZE_BOTH;
        }
      });
      } catch(Exception ex) { log(ex); }
      try {
      findAndHookMethod("com.sonymobile.home.presenter.view.AdvWidgetItemView", param.classLoader, "setAdvancedWidget",
        findClass("com.sonymobile.home.ui.widget.HomeAdvWidget", param.classLoader),
        findClass("com.sonymobile.home.ui.widget.HomeAdvWidgetManager", param.classLoader),
        boolean.class,
        new XC_MethodHook() 
      {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable
        {
          Object mItem = getObjectField(param.thisObject, "mItem");
          Object mLocation = getObjectField(mItem, "mLocation");
          Object grid = getObjectField(mLocation, "grid");
          int colSpan = getIntField(grid, "colSpan");
          int rowSpan = getIntField(grid, "rowSpan");
          advWidgetSizes.put(param.args[0], new int[] { colSpan, rowSpan });
        }
      });
      } catch(Exception ex) { log(ex); }
      try {
      findAndHookMethod("com.sonymobile.home.ui.widget.HomeAdvWidget", param.classLoader, "getSpanXY", new XC_MethodHook() 
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          int[] span = advWidgetSizes.get(param.thisObject);
          if(span != null)
            param.setResult(span);
        }
      });
      } catch(Exception ex) { log(ex); }
    }
  } 
  
////////////////////////////////////////////////////////////
  
  private void hookLauncherExperimental(XC_LoadPackage.LoadPackageParam param) throws Exception
  {
    if(prefs.getBoolean("key_enable_experimental", false))
    {
      ClassLoader moduleClassLoader = getClass().getClassLoader();
      ClassLoader xposedClassLoader = moduleClassLoader.getParent();
      ClassLoader packageClassLoader = param.classLoader;
      
      ReflectionUtils.addToClassPath(packageClassLoader, MODULE_PATH, true);
      ReflectionUtils.setParentClassLoader(packageClassLoader, xposedClassLoader);
      ReflectionUtils.setParentClassLoader(moduleClassLoader, packageClassLoader);
      
      com.gem.xperiaxposed.home.Hooks.installHooks();
    }
  } 
  
////////////////////////////////////////////////////////////

  private static void setFullTransparent(View view, boolean value) throws Throwable
  {
    if(value)
      view.setSystemUiVisibility(view.getSystemUiVisibility() | SYSTEM_UI_FLAG_FULL_TRANSPARENCY);
    else
      view.setSystemUiVisibility(view.getSystemUiVisibility() & ~SYSTEM_UI_FLAG_FULL_TRANSPARENCY);
  }

////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////
