package com.gem.xperiaxposed.home;

import static com.gem.xperiaxposed.XposedMain.*;
import static de.robv.android.xposed.XposedBridge.*;
import static de.robv.android.xposed.XposedHelpers.*;

import java.util.WeakHashMap;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import com.sonymobile.flix.components.Scene;
import com.sonymobile.flix.util.Animation;
import com.sonymobile.home.MainView;
import com.sonymobile.home.apptray.AppTray;
import com.sonymobile.home.apptray.AppTrayPageIndicatorView;
import com.sonymobile.home.apptray.AppTrayPresenter;
import com.sonymobile.home.apptray.AppTrayView;
import com.sonymobile.home.data.Item;
import com.sonymobile.home.desktop.Desktop;
import com.sonymobile.home.desktop.DesktopView;
import com.sonymobile.home.presenter.view.IconLabelView;
import com.sonymobile.home.presenter.view.ItemViewCreatorBase;
import com.sonymobile.home.ui.pageview.PageViewInteractionListener;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

////////////////////////////////////////////////////////////

public class HomeHooks
{

////////////////////////////////////////////////////////////

  public static void hookTransparency(XC_LoadPackage.LoadPackageParam param)
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
    } catch(Throwable ex) { log(ex); }
    
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
      } catch(Throwable ex) { log(ex); }
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
      } catch(Throwable ex) { log(ex); }
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
      } catch(Throwable ex) { log(ex); }
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
      } catch(Throwable ex) { log(ex); }
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
      } catch(Throwable ex) { log(ex); }
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
      } catch(Throwable ex) { log(ex); }
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
      } catch(Throwable ex) { log(ex); }
    }
  }
  
////////////////////////////////////////////////////////////

  public static void hookFont(XC_LoadPackage.LoadPackageParam param)
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
      } catch(Throwable ex) { log(ex); }
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
      } catch(Throwable ex) { log(ex); }
    }
  }
  
////////////////////////////////////////////////////////////

  public static void hookLayout(XC_LoadPackage.LoadPackageParam param)
  {
    final boolean desktop_disable_pagination = prefs.getBoolean("key_desktop_disable_pagination", false);
    final boolean drawer_disable_pagination = prefs.getBoolean("key_drawer_disable_pagination", false);
    if(desktop_disable_pagination || drawer_disable_pagination)
    {
      try {
      findAndHookMethod(MainView.class, "onSceneCreated", 
        Scene.class,
        int.class,
        int.class,
        new XC_MethodHook() 
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          MainView mainView = (MainView)param.thisObject;
          if(desktop_disable_pagination)
          {
            Desktop desktop = (Desktop)getObjectField(mainView, "mDesktop");
            desktop.getView().removeChild(desktop.getPresenter().getPageIndicatorView());
          }
          if(drawer_disable_pagination)
          {
            AppTray appTray = (AppTray)getObjectField(mainView, "mAppTray");
            appTray.getView().removeChild(appTray.getPresenter().getPageIndicatorView());
          }
        }
      });      
      } catch(Throwable ex) { log(ex); }
    }

    final boolean desktop_disable_labels = prefs.getBoolean("key_desktop_disable_labels", false);
    final boolean folder_disable_labels = prefs.getBoolean("key_folder_disable_labels", false);
    final boolean drawer_disable_labels = prefs.getBoolean("key_drawer_disable_labels", false);
    if(desktop_disable_labels || folder_disable_labels || drawer_disable_labels)
    {
      try {
      findAndHookMethod(ItemViewCreatorBase.class, "includedLabel", 
        Item.class,
        new XC_MethodHook() 
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          Item item = (Item)param.args[0];
          String name = item.getPageViewName();
          if(desktop_disable_labels && "desktop".equals(name))
            param.setResult(false);
          if(folder_disable_labels && "folder".equals(name))
            param.setResult(false);
          if(drawer_disable_labels && "apptray".equals(name))
            param.setResult(false);
        }
      });      
      } catch(Throwable ex) { log(ex); }

      try {
      findAndHookMethod(IconLabelView.class, "setMaxTextSize", 
        int.class,
        new XC_MethodHook() 
      {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable
        {
          if(! getBooleanField(param.thisObject, "mIncludedLabel"))
            param.setResult(null);
        }
      });      
      } catch(Throwable ex) { log(ex); }
    }
  }
  
////////////////////////////////////////////////////////////
  
  public static void hookDesktop(XC_LoadPackage.LoadPackageParam param)
  {
    final int desktop_animation = Integer.valueOf(prefs.getString("key_desktop_animation", "0"));
    if(desktop_animation != 0)
    {
      try {
      hookAllConstructors(DesktopView.class, new XC_MethodHook() 
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          setIntField(param.thisObject, "mAnimNbr", desktop_animation);
        }
      });      
      } catch(Throwable ex) { log(ex); }
    }
  }
  
////////////////////////////////////////////////////////////

  public static void hookDock(XC_LoadPackage.LoadPackageParam param)
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
      } catch(Throwable ex) { log(ex); }
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
      } catch(Throwable ex) { log(ex); }
    }
  }
  
////////////////////////////////////////////////////////////

  public static void hookDrawer(XC_LoadPackage.LoadPackageParam param)
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
      } catch(Throwable ex) { log(ex); }
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
      } catch(Throwable ex) { log(ex); }
    }

    final int drawer_menu_opacity = prefs.getInt("key_drawer_menu_opacity", 100);
    if(drawer_menu_opacity != 100)
    {
      try {
      findAndHookMethod("com.sonymobile.home.apptray.AppTrayDrawerView", param.classLoader, "initialize", 
        float.class,
        float.class,
        float.class,
        new XC_MethodHook()
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          View menu = (View)getObjectField(param.thisObject, "mListView");
          menu.getBackground().setAlpha((int)(2.55 * drawer_menu_opacity));
        }
      });
      } catch(Throwable ex) { log(ex); }
    }

    if(prefs.getBoolean("key_drawer_autohide_pagination", false))
    {
      try {
      hookAllConstructors(AppTrayPageIndicatorView.class, new XC_MethodHook()
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          callMethod(param.thisObject, "setAutoHide", true);
        }
      });
      } catch(Throwable ex) { log(ex); }
      try {
      findAndHookMethod(AppTrayPresenter.class, "setView",
        AppTrayView.class,
        new XC_MethodHook()
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          final AppTrayView view = (AppTrayView)param.args[0];
          final AppTrayPresenter presenter = (AppTrayPresenter)param.thisObject;
          view.addInteractionListener(new PageViewInteractionListener()
          {
            @Override
            public void onInteractionStart()
            {
              presenter.getPageIndicatorView().onInteractionStart();
            }
            @Override
            public void onInteractionEnd()
            {
              presenter.getPageIndicatorView().onInteractionEnd();
            }
          });
        }
      });
      } catch(Throwable ex) { log(ex); }
      try {
      findAndHookMethod(AppTrayPageIndicatorView.class, "setTitle",
        String.class,
        new XC_MethodHook()
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          final AppTrayPageIndicatorView indicator = (AppTrayPageIndicatorView)param.thisObject;
          ((Animation)getObjectField(indicator, "mPageIndicatorAnimation")).addListener(new Animation.Listener()
          {
            @Override
            public void onStart(Animation arg0)
            {
              indicator.onInteractionStart();
            }
            @Override
            public void onFinish(Animation arg0)
            {
              indicator.onInteractionEnd();
            }
          });
        }
      });
      } catch(Throwable ex) { log(ex); }
      try {
      findAndHookMethod(AppTrayPresenter.class, "onAppTrayDrawerVisibilityChanged",
        float.class,
        new XC_MethodHook()
      {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable
        {
          callMethod(param.thisObject, "setSystemUiTransparent", (Float)param.args[0] <= 0.05);
          param.setResult(null);
        }
      });
      } catch(Throwable ex) { log(ex); }
    }
  }

////////////////////////////////////////////////////////////

  public static void hookFolders(XC_LoadPackage.LoadPackageParam param)
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
      } catch(Throwable ex) { log(ex); }
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
      } catch(Throwable ex) { log(ex); }
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
      } catch(Throwable ex) { log(ex); }
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
      } catch(Throwable ex) { log(ex); }
    }
  }
  
////////////////////////////////////////////////////////////
  
  private static WeakHashMap<Object, int[]> advWidgetSizes = new WeakHashMap<Object, int[]>();
  
  public static void hookWidgets(XC_LoadPackage.LoadPackageParam param)
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
      } catch(Throwable ex) { log(ex); }
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
      } catch(Throwable ex) { log(ex); }
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
      } catch(Throwable ex) { log(ex); }
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
      } catch(Throwable ex) { log(ex); }
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
      } catch(Throwable ex) { log(ex); }
    }
  } 
  
////////////////////////////////////////////////////////////
  
  public static void hookExperimental(XC_LoadPackage.LoadPackageParam param) throws Exception
  {
    if(prefs.getBoolean("key_enable_experimental", false))
    {
      com.gem.xperiaxposed.home.ExperimentalHooks.installHooks();
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
