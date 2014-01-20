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
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.*;

////////////////////////////////////////////////////////////

public class XposedMain implements IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources
{

////////////////////////////////////////////////////////////
  
  private static final String ANDROID = "android";
  private static final String SE_HOME = "com.sonyericsson.home";
  private static final String SE_LOCK = "com.sonyericsson.lockscreen.uxpnxt";
  private static final String KEYGUARD_PACKAGE = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) ?
    "com.android.keyguard" : "com.android.internal.policy.impl.keyguard";
  private static String MODULE_PATH;
  private static XSharedPreferences prefs;

////////////////////////////////////////////////////////////

  @Override
  public void initZygote(IXposedHookZygoteInit.StartupParam param) throws Throwable
  {
    MODULE_PATH = param.modulePath;
    prefs = new XSharedPreferences(XposedMain.class.getPackage().getName());
    prefs.makeWorldReadable();

    if(prefs.getBoolean("key_hide_shortcuts", false))
    {
      XResources.DrawableLoader EMPTY_DRAWABLE = new XResources.DrawableLoader() 
      {
        @Override
        public Drawable newDrawable(XResources res, int id) throws Throwable
        {
          return new ColorDrawable(0);
        }
      };
      XResources.setSystemWideReplacement("android", "drawable", "ic_lockscreen_camera_hint", EMPTY_DRAWABLE);
      XResources.setSystemWideReplacement("android", "drawable", "ic_lockscreen_other_widgets_hint", EMPTY_DRAWABLE);
    }
  }

////////////////////////////////////////////////////////////

  @Override
  public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam param) throws Throwable
  {
    if(param.packageName.equals(SE_LOCK))
    {
      prefs.reload();
      
      if(prefs.getBoolean("key_hide_hint_arrows", false))
      {
        XResources.DrawableLoader EMPTY_DRAWABLE = new XResources.DrawableLoader() 
        {
          @Override
          public Drawable newDrawable(XResources res, int id) throws Throwable
          {
            return new ColorDrawable(0);
          }
        };
        param.res.setReplacement(SE_LOCK, "drawable", "arrow_unlock_hint_down", EMPTY_DRAWABLE);
        param.res.setReplacement(SE_LOCK, "drawable", "arrow_unlock_hint_up", EMPTY_DRAWABLE);
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

    if(param.packageName.equals(SE_HOME))
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
    }
  }

////////////////////////////////////////////////////////////

  @Override
  public void handleLoadPackage(XC_LoadPackage.LoadPackageParam param) throws Throwable
  {
    if(param.packageName.equals(ANDROID))
    {
      prefs.reload();
      hookKeyguard(param);
    }
    else if(param.packageName.equals(SE_LOCK))
    {
      prefs.reload();
      hookLockscreen(param);
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
    }
  }
  
////////////////////////////////////////////////////////////

  private void hookKeyguard(XC_LoadPackage.LoadPackageParam param)
  {
    if(prefs.getBoolean("key_transparent_lockscreen", false))
    {
      findAndHookMethod(KEYGUARD_PACKAGE + ".KeyguardHostView", param.classLoader, "onFinishInflate", new XC_MethodHook()
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          setFullTransparent((View)param.thisObject, true);
        }
      });
    }
    
    final String carrierText = prefs.getString("key_carrier_text", "");
    if(!carrierText.isEmpty())
    {
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
    }

    if(prefs.getBoolean("key_enable_standard_lockscreen", false))
    {
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
      findAndHookMethod(KEYGUARD_PACKAGE + ".KeyguardSelectorView", param.classLoader, "onFinishInflate", new XC_MethodHook()
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          View mGlowPadView = (View)XposedHelpers.getObjectField(param.thisObject, "mGlowPadView");
          setIntField(mGlowPadView, "mGravity", Gravity.CENTER);
        }
      });
      findAndHookMethod(KEYGUARD_PACKAGE + ".ExternalLockScreen", param.classLoader, "validateExternalLockScreen", Context.class, ComponentName.class, new XC_MethodReplacement()
      {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
        {
          return false;
        }
      });
    }    

    if(prefs.getBoolean("key_hide_widget_backplate", false))
    {
      findAndHookMethod(KEYGUARD_PACKAGE + ".KeyguardWidgetFrame", param.classLoader, "getBackgroundDrawable", Resources.class, new XC_MethodReplacement()
      {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
        {
          return new ColorDrawable(0);
        }
      });
      findAndHookMethod(KEYGUARD_PACKAGE + ".CameraWidgetFrame", param.classLoader, "getBackgroundDrawable", Resources.class, new XC_MethodReplacement()
      {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
        {
          return new ColorDrawable(0);
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////

  private void hookLockscreen(XC_LoadPackage.LoadPackageParam param)
  {
  }
  
////////////////////////////////////////////////////////////

  private void hookLauncherTransparency(XC_LoadPackage.LoadPackageParam param)
  {
    findAndHookMethod("com.sonymobile.home.apptray.AppTrayPresenter", param.classLoader, "setSystemUiTransparent", boolean.class, new XC_MethodReplacement() 
    {
      @Override
      protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
      {
        return null;
      }
    });

    final boolean transparentDesktop = prefs.getBoolean("key_transparent_desktop", false);
    final boolean transparentDrawer = prefs.getBoolean("key_transparent_drawer", false);

    if(transparentDesktop)
    {
      // transparent SystemUI
      hookAllConstructors(findClass("com.sonymobile.home.MainView", param.classLoader), new XC_MethodHook()
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          setFullTransparent((View)param.thisObject, true);
        }
      });
      findAndHookMethod("com.sonymobile.ui.support.SystemUiVisibilityWrapper", param.classLoader, "apply", new XC_MethodHook() 
      {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable
        {
          callMethod(param.thisObject, "setFlag", getIntField(param.thisObject, "SYSTEM_UI_FLAG_FULL_TRANSPARENCY"), true);
        }
      });
    }

    if(transparentDrawer)
    {
      hookAllConstructors(findClass("com.sonymobile.home.apptray.AppTrayView", param.classLoader), new XC_MethodHook()
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          setIntField(param.thisObject, "mBackgroundColor", 0);
        }
      });
    }

    if(transparentDrawer != transparentDesktop)
    {
      findAndHookMethod("com.sonymobile.home.MainView", param.classLoader, "showApptray", boolean.class, new XC_MethodHook() 
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          if((Boolean)param.args[0])
            setFullTransparent((View)param.thisObject, transparentDrawer);
        }
      });
      findAndHookMethod("com.sonymobile.home.MainView", param.classLoader, "showDesktop", boolean.class, new XC_MethodHook() 
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          if((Boolean)param.args[0])
            setFullTransparent((View)param.thisObject, transparentDesktop);
        }
      });
    }
    
    if(transparentDesktop || transparentDrawer != transparentDesktop)
    {
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
    }
  }
  
////////////////////////////////////////////////////////////

  private void hookLauncherFont(XC_LoadPackage.LoadPackageParam param)
  {
    if(prefs.getBoolean("key_condensed_font", false))
    {
      final Typeface CONDENSED_FONT = Typeface.createFromFile("/system/fonts/RobotoCondensed-Regular.ttf");
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
    }
  }
  
////////////////////////////////////////////////////////////

  private void hookLauncherDock(XC_LoadPackage.LoadPackageParam param)
  {
    if(prefs.getBoolean("key_disable_dock_stage", false))
    {
      findAndHookMethod("com.sonymobile.home.stage.StageView", param.classLoader, "updateBackground", new XC_MethodHook() 
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          callMethod(getObjectField(param.thisObject, "mBackground"), "setBitmap", (Object)null);
        }
      });      
    }
      
    if(prefs.getBoolean("key_disable_dock_reflection", false))
    {
      hookAllConstructors(findClass("com.sonymobile.home.bitmap.MirrorBitmapDrawable", param.classLoader), new XC_MethodHook()
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          setBooleanField(param.thisObject, "mMirror", false);
        }
      });
    }
  }
  
////////////////////////////////////////////////////////////

  private void hookLauncherDrawer(XC_LoadPackage.LoadPackageParam param)
  {
    if(prefs.getBoolean("key_disable_drawer_backplate", false))
    {
      hookAllConstructors(findClass("com.sonymobile.home.apptray.AppTrayPageView", param.classLoader), new XC_MethodHook()
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          callMethod(getObjectField(param.thisObject, "mContent"), "removeChild", getObjectField(param.thisObject, "mDefaultBackplate"));
          callMethod(getObjectField(param.thisObject, "mContent"), "removeChild", getObjectField(param.thisObject, "mUninstallBackplate"));
        }
      });
    }
  }

////////////////////////////////////////////////////////////

  private void hookLauncherFolders(XC_LoadPackage.LoadPackageParam param)
  {
    final int folderColumns = Integer.parseInt(prefs.getString("key_folder_columns", "4"));
    if(folderColumns != 4)
    {
      findAndHookMethod("com.sonymobile.home.folder.GridView", param.classLoader, "setCellWidth", float.class, new XC_MethodReplacement() 
      {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
        {
          setFloatField(param.thisObject, "mCellWidth", (Float)param.args[0] * 4 / folderColumns);
          return null;
        }
      });
    }

    if(prefs.getBoolean("key_folder_multiline_labels", false))
    {
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
      findAndHookMethod("com.sonymobile.home.presenter.view.ItemViewCreatorBase", param.classLoader, "getItemViewTextLines", String.class, new XC_MethodHook() 
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          if("folder".equals(param.args[0]))
            param.setResult(2);
        }
      });
    }
    
    if(prefs.getBoolean("key_folder_disable_background_dim", false))
    {
      findAndHookMethod("com.sonymobile.home.folder.OpenFolderView$DimAnimation", param.classLoader, "onUpdate", float.class, float.class, new XC_MethodReplacement() 
      {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
        {
          return null;
        }
      });
    }
  }
  
////////////////////////////////////////////////////////////
  
  private WeakHashMap<Object, int[]> advWidgetSizes = new WeakHashMap<Object, int[]>();
  
  private void hookLauncherWidgets(XC_LoadPackage.LoadPackageParam param)
  {
    if(prefs.getBoolean("key_all_widgets_resizable", false))
    {
      findAndHookMethod("com.sonymobile.home.ui.widget.HomeAppWidgetManager", param.classLoader, "getResizeMode", int.class, new XC_MethodHook() 
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          param.setResult(AppWidgetProviderInfo.RESIZE_BOTH);
        }
      });
      findAndHookMethod("com.sonymobile.home.ui.widget.HomeAdvWidget", param.classLoader, "createAppWidgetInfo", PackageManager.class, new XC_MethodHook() 
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          AppWidgetProviderInfo info = (AppWidgetProviderInfo)getObjectField(param.thisObject, "mAppWidgetProviderInfo");
          info.resizeMode = AppWidgetProviderInfo.RESIZE_BOTH;
        }
      });
      findAndHookMethod("com.sonymobile.home.cui.CuiWidgetLoadHelper", param.classLoader, "getVanillaSpanXY", Context.class, AppWidgetProviderInfo.class, new XC_MethodHook() 
      {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable
        {
          AppWidgetProviderInfo info = (AppWidgetProviderInfo)param.args[1];
          info.resizeMode = AppWidgetProviderInfo.RESIZE_BOTH;
        }
      });
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
    }
  } 
  
////////////////////////////////////////////////////////////

  private static void setFullTransparent(View view, boolean value) throws Throwable
  {
    if(value)
      view.setSystemUiVisibility(view.getSystemUiVisibility() | View.class.getField("SYSTEM_UI_FLAG_FULL_TRANSPARENCY").getInt(null));
    else
      view.setSystemUiVisibility(view.getSystemUiVisibility() & ~View.class.getField("SYSTEM_UI_FLAG_FULL_TRANSPARENCY").getInt(null));
  }
}
