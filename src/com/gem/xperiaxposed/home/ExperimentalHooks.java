package com.gem.xperiaxposed.home;

import static de.robv.android.xposed.XposedBridge.*;
import static de.robv.android.xposed.XposedHelpers.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.gem.xperiaxposed.ClassHook;
import com.gem.xperiaxposed.ReflectionUtils;
import com.sonymobile.flix.components.Scene;
import com.sonymobile.home.MainView;
import com.sonymobile.home.apptray.AppTray;
import com.sonymobile.home.apptray.AppTrayAdapter;
import com.sonymobile.home.apptray.AppTrayDrawerAdapter;
import com.sonymobile.home.apptray.AppTrayDrawerAdapter.AppTrayDrawerItemData;
import com.sonymobile.home.apptray.AppTrayDrawerLoadHelper;
import com.sonymobile.home.apptray.AppTrayDrawerLoadHelper.AppTrayDrawerItemType;
import com.sonymobile.home.apptray.AppTrayDropZoneView;
import com.sonymobile.home.apptray.AppTrayModel;
import com.sonymobile.home.apptray.AppTrayPreferenceManager;
import com.sonymobile.home.apptray.AppTrayPreferenceManager.SortMode;
import com.sonymobile.home.apptray.AppTrayPresenter;
import com.sonymobile.home.apptray.AppTraySorter;
import com.sonymobile.home.badge.BadgeManager;
import com.sonymobile.home.data.Item;
import com.sonymobile.home.storage.StorageManager;

import de.robv.android.xposed.XC_MethodHook;

public class ExperimentalHooks
{

////////////////////////////////////////////////////////////
  
  public static SortMode HIDDEN;
  public static AppTrayDrawerItemType APPTRAY_DRAWER_ITEM_TYPE_HIDDEN;
  public static AppTrayDrawerItemType APPTRAY_DRAWER_ITEM_TYPE_SETTINGS;
  public static int numberOfActivities = 0;
  public static int numberOfHiddenActivities = 0;
  public static MissedItReceiver missedItReceiver = null;
  
////////////////////////////////////////////////////////////
  
  public static void installHooks()
  {
    ReflectionUtils.addToEnum(AppTrayPreferenceManager.SortMode.class, "HIDDEN", 4, "hidden");
    ReflectionUtils.addToEnum(AppTrayDrawerLoadHelper.AppTrayDrawerItemType.class, "APPTRAY_DRAWER_ITEM_TYPE_HIDDEN", 9);
    ReflectionUtils.addToEnum(AppTrayDrawerLoadHelper.AppTrayDrawerItemType.class, "APPTRAY_DRAWER_ITEM_TYPE_SETTINGS", 9);
    HIDDEN = SortMode.valueOf("HIDDEN");
    APPTRAY_DRAWER_ITEM_TYPE_HIDDEN = AppTrayDrawerItemType.valueOf("APPTRAY_DRAWER_ITEM_TYPE_HIDDEN");
    APPTRAY_DRAWER_ITEM_TYPE_SETTINGS = AppTrayDrawerItemType.valueOf("APPTRAY_DRAWER_ITEM_TYPE_SETTINGS");
    
    /*
     * MissedIt broadcast receiver
     */
    
    try {
    findAndHookMethod(StorageManager.class, "getBadgeManager",
      Context.class,
      new XC_MethodHook() 
    {
      @Override
      protected void afterHookedMethod(MethodHookParam param) throws Throwable
      {
        if(param.getResult() != null && missedItReceiver == null)
          missedItReceiver = new MissedItReceiver((Context)param.args[0], (BadgeManager)param.getResult());
      }
    });
    } catch(Throwable ex) { log(ex); }
    
    /*
     * AppTray drop zone 
     */

    ClassHook.hookClass(AppTrayDropZoneView.class, AppTrayDropZoneViewHook.class);
    ClassHook.hookClass(AppTrayModel.class, AppTrayModelHook.class);

    /*
     * Hide/unhide drop zone registration 
     */

    try {
    hookAllConstructors(com.sonymobile.home.transfer.TransferView.class, new XC_MethodHook() 
    {
      @Override
      protected void afterHookedMethod(MethodHookParam param) throws Throwable
      {
        List<Integer> mTargets = getField(param.thisObject, "mTargets");
        mTargets.add(0, Ids.hide_drop_area);
      }
    });
    } catch(Throwable ex) { log(ex); }
    
    /*
     * Hidden item filtering 
     */

    try {
    findAndHookMethod(AppTraySorter.class, "filterItemsIfNeeded",
      List.class,
      AppTrayPreferenceManager.SortMode.class,
      new XC_MethodHook() 
    {
      @Override
      protected void beforeHookedMethod(MethodHookParam param) throws Throwable
      {
        filterAppTrayItems((AppTraySorter)param.thisObject, (List<Item>)param.args[0]);
      }
    });
    } catch(Throwable ex) { log(ex); }

    try {
    findAndHookMethod(AppTrayAdapter.class, "setModelItems",
      List.class,
      boolean.class,
      new XC_MethodHook() 
    {
      @Override
      protected void beforeHookedMethod(MethodHookParam param) throws Throwable
      {
        AppTraySorter sorter = getField(getAppTray(param.thisObject), AppTraySorter.class);
        if(sorter.getSortMode() == SortMode.OWN_ORDER)
        {
          if(param.args[0] != null)
          {
            List<Item> items = new ArrayList<Item>((List<Item>)param.args[0]);
            filterAppTrayItems(sorter, items);
            param.args[0] = items;
          }
        }
      }
    });
    } catch(Throwable ex) { log(ex); }

    try {
    findAndHookMethod(AppTraySorter.class, "sort",
      AppTrayPreferenceManager.SortMode.class,
      new XC_MethodHook() 
    {
      @Override
      protected void beforeHookedMethod(MethodHookParam param) throws Throwable
      {
        if(param.args[0] == HIDDEN)
          param.args[0] = SortMode.ALPHABETICAL;
      }
    });
    } catch(Throwable ex) { log(ex); }

    /*
     * Navigation drawer 
     */

    try {
    findAndHookMethod(AppTrayDrawerLoadHelper.class, "loadItemData",
      AppTrayPreferenceManager.SortMode.class,
      int.class,
      int.class,
      Map.class,
      new XC_MethodHook() 
    {
      @Override
      protected void afterHookedMethod(MethodHookParam param) throws Throwable
      {
        injectAppTrayDrawerItems((AppTrayDrawerLoadHelper)param.thisObject, (SortMode)param.args[0], (Map<String, List<AppTrayDrawerItemData>>)param.args[3]);
      }
    });
    } catch(Throwable ex) { log(ex); }

    try {
    findAndHookMethod(AppTrayPresenter.class, "onAppTrayDrawerItemClicked",
      AppTrayDrawerAdapter.AppTrayDrawerItemData.class,
      new XC_MethodHook()
    {
      @Override
      protected void afterHookedMethod(MethodHookParam param) throws Throwable
      {
        onAppTrayDrawerItemClicked((AppTrayPresenter)param.thisObject, (AppTrayDrawerItemData)param.args[0]);
      }
    });
    } catch(Throwable ex) { log(ex); }

    try {
    findAndHookMethod(AppTrayPresenter.class, "getCategoryTitleFromSortMode",
      AppTrayPreferenceManager.SortMode.class,
      new XC_MethodHook()
    {
      @Override
      protected void beforeHookedMethod(MethodHookParam param) throws Throwable
      {
        if(param.args[0] == SortMode.ALPHABETICAL)
          param.setResult(getResources(param.thisObject).getString(com.sonyericsson.home.R.string.app_tray_drawer_list_item_categories_alphabetical) 
            + " (" + String.format("%d", numberOfActivities) + ")");
        else if(param.args[0] == HIDDEN)
          param.setResult(getResources(param.thisObject).getString(Ids.app_tray_drawer_list_item_categories_hidden) 
            + " (" + String.format("%d", numberOfHiddenActivities) + ")");
      }
    });
    } catch(Throwable ex) { log(ex); }

    try {
    findAndHookMethod(AppTrayDrawerAdapter.class, "convertSortModeToItemType",
      AppTrayPreferenceManager.SortMode.class,
      new XC_MethodHook()
    {
      @Override
      protected void beforeHookedMethod(MethodHookParam param) throws Throwable
      {
        if(param.args[0] == HIDDEN)
          param.setResult(APPTRAY_DRAWER_ITEM_TYPE_HIDDEN);
      }
    });
    } catch(Throwable ex) { log(ex); }

    /*
     * Badges
     */
    
    try {
    findAndHookMethod(AppTrayDrawerAdapter.class, "updateNumberOfMostUsedItems",
      int.class,
      new XC_MethodHook()
      {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          if(updateNumberOfActivities((AppTrayDrawerAdapter)param.thisObject))
            param.setResult(true);
        }
      });
    } catch(Throwable ex) { log(ex); }
  }
  
////////////////////////////////////////////////////////////
  
  public static boolean filterAppTrayItems(AppTraySorter sorter, List<Item> items)
  {
    AppTrayModel appTrayModel = getField(sorter, "mOwnOrderModel");
    AppTrayModelHook appTrayModelHook = AppTrayModelHook.getHook(appTrayModel);
    for(Iterator<Item> i = items.iterator(); i.hasNext(); )
    {
      if(appTrayModelHook.isHidden(i.next()))
      {
        if(sorter.getSortMode() != HIDDEN)
          i.remove();
      }
      else
      {
        if(sorter.getSortMode() == HIDDEN)
          i.remove();
      }
    }
    return !items.isEmpty();
  }
  
  public static void injectAppTrayDrawerItems(AppTrayDrawerLoadHelper loadHelper, SortMode sortMode, Map<String, List<AppTrayDrawerItemData>> map)
  {
    Resources res = getResources(loadHelper);
    int iconSize = res.getDimensionPixelSize(com.sonyericsson.home.R.dimen.apptray_drawer_icon_width);
    Drawable hiddenIcon = res.getDrawable(Ids.drawer_icn_hidden);
    Drawable settingsIcon = res.getDrawable(Ids.drawer_icn_settings);
    
    try
    {
      Bitmap icon = BitmapFactory.decodeResource(res, com.sonyericsson.home.R.drawable.drawer_icn_own_order);
      final int color = icon.getPixel(icon.getWidth()/2, icon.getHeight()/2);
      if(color != Color.WHITE)
      {
        hiddenIcon = new BitmapDrawable(res, ((BitmapDrawable)hiddenIcon).getBitmap())
        {
          {
            super.setColorFilter(new PorterDuffColorFilter(color, Mode.SRC_ATOP));
          }
          @Override
          public void setColorFilter(ColorFilter cf)
          {
          }
        };
      }
      settingsIcon = new BitmapDrawable(res, ((BitmapDrawable)settingsIcon).getBitmap())
      {
        {
          super.setColorFilter(new PorterDuffColorFilter(color, Mode.SRC_ATOP));
        }
        @Override
        public void setColorFilter(ColorFilter cf)
        {
        }
      };
    }
    catch(Throwable ex)
    {
      log(ex);
    }

    hiddenIcon.setBounds(0,  0, iconSize, iconSize);
    settingsIcon.setBounds(0,  0, iconSize, iconSize);

    for(Map.Entry<String, List<AppTrayDrawerItemData>> e: map.entrySet())
    {
      List<AppTrayDrawerItemData> items = e.getValue(); 
      for(int i = 0; i < items.size(); ++i)
      {
        AppTrayDrawerItemData data = items.get(i);
        if(data.mItemType == AppTrayDrawerItemType.APPTRAY_DRAWER_ITEM_TYPE_UNINSTALL)
        {
          items.add(i+1, new AppTrayDrawerItemData(
            res.getString(Ids.app_tray_drawer_list_item_categories_settings), 
            settingsIcon, 
            null,
            null,
            APPTRAY_DRAWER_ITEM_TYPE_SETTINGS,
            getContext(loadHelper).getPackageManager().getLaunchIntentForPackage("com.gem.xperiaxposed"),
            null,
            null));
        }
        else if(data.mItemType == AppTrayDrawerItemType.APPTRAY_DRAWER_ITEM_TYPE_ALPHABETICAL)
        {
          data.mBadgeText = String.format("%d", numberOfActivities);
          items.add(i+1, new AppTrayDrawerItemData(
            res.getString(Ids.app_tray_drawer_list_item_categories_hidden), 
            hiddenIcon, 
            (sortMode == HIDDEN) ? res.getDrawable(com.sonyericsson.home.R.drawable.drawer_list_selected) : null,
            String.format("%d", numberOfHiddenActivities),
            APPTRAY_DRAWER_ITEM_TYPE_HIDDEN,
            null,
            null,
            null));
        }
      }
    }
  }
  
  public static void onAppTrayDrawerItemClicked(AppTrayPresenter presenter, AppTrayDrawerItemData data)
  {
    if(data.mItemType == APPTRAY_DRAWER_ITEM_TYPE_SETTINGS)
      callMethod(presenter, "launchApplication", data.mIntent);
    else if(data.mItemType == APPTRAY_DRAWER_ITEM_TYPE_HIDDEN)
      callMethod(presenter, "handleSortModeItemClicked", HIDDEN);
  }
  
  public static boolean updateNumberOfActivities(AppTrayDrawerAdapter adapter)
  {
    AppTrayDrawerLoadHelper loadHelper = getField(adapter, "mLoadHelper");
    callMethod(adapter, "setBadgeText", String.format("%d", numberOfActivities), loadHelper.getCategoriesTitle(), AppTrayDrawerItemType.APPTRAY_DRAWER_ITEM_TYPE_ALPHABETICAL);
    callMethod(adapter, "setBadgeText", String.format("%d", numberOfHiddenActivities), loadHelper.getCategoriesTitle(), APPTRAY_DRAWER_ITEM_TYPE_HIDDEN);
    return true;
  }
  
////////////////////////////////////////////////////////////
  
  public static Context getContext(Object o)
  {
    return getField(o, Context.class);
  }

  public static Resources getResources(Object o)
  {
    return getContext(o).getResources();
  }

  public static Scene getScene(Object o)
  {
    return getField(o, Scene.class);
  }

  public static MainView getMainView(Object o)
  {
    return (MainView)getScene(o).getView();
  }

  public static AppTray getAppTray(Object o)
  {
    return getField(getMainView(o), AppTray.class);
  }
  
  static <T> T getField(Object o, Class<T> clazz)
  {
    return getField(o, "m" + clazz.getSimpleName());
  }

  @SuppressWarnings("unchecked")
  static <T> T getField(Object o, String name)
  {
    return (T)getObjectField(o, name);
  }

////////////////////////////////////////////////////////////
  
}

////////////////////////////////////////////////////////////
