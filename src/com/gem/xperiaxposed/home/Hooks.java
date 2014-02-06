package com.gem.xperiaxposed.home;

import static de.robv.android.xposed.XposedBridge.*;
import static de.robv.android.xposed.XposedHelpers.*;

import java.util.*;

import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.*;

import com.gem.xperiaxposed.*;
import com.sonymobile.flix.components.*;
import com.sonymobile.home.*;
import com.sonymobile.home.apptray.*;
import com.sonymobile.home.apptray.AppTrayDrawerAdapter.AppTrayDrawerItemData;
import com.sonymobile.home.apptray.AppTrayDrawerLoadHelper.AppTrayDrawerItemType;
import com.sonymobile.home.apptray.AppTrayPreferenceManager.SortMode;
import com.sonymobile.home.data.*;
import com.sonymobile.home.model.*;

import de.robv.android.xposed.*;

public class Hooks
{

////////////////////////////////////////////////////////////
  
  public static SortMode HIDDEN;
  public static AppTrayDrawerItemType APPTRAY_DRAWER_ITEM_TYPE_HIDDEN;
  public static AppTrayDrawerItemType APPTRAY_DRAWER_ITEM_TYPE_SETTINGS;
  public static int numberOfActivities = 0;
  public static int numberOfHiddenActivities = 0;
  
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
    } catch(Exception ex) { log(ex); }
    
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
    } catch(Exception ex) { log(ex); }

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
          List<Item> items = new ArrayList<Item>((List<Item>)param.args[0]);
          filterAppTrayItems(sorter, items);
          param.args[0] = items;
        }
      }
    });
    } catch(Exception ex) { log(ex); }

    try {
    findAndHookMethod(AppTrayModel.class, "updateModelAndItems",
      List.class,
      List.class,
      new XC_MethodHook() 
    {
      @Override
      protected void beforeHookedMethod(MethodHookParam param) throws Throwable
      {
        updateModelAndItems((AppTrayModel)param.thisObject, (List<Item>)param.args[0], (List<Item>)param.args[1]);
      }
    });
    } catch(Exception ex) { log(ex); }
    
    
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
    } catch(Exception ex) { log(ex); }

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
    } catch(Exception ex) { log(ex); }

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
    } catch(Exception ex) { log(ex); }

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
    } catch(Exception ex) { log(ex); }

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
    } catch(Exception ex) { log(ex); }

    /*
     * Badges
     */
    
    try {
    findAndHookMethod(AppTrayModel.class, "getTotalNumberOfActivities",
      new XC_MethodReplacement()
      {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
        {
          return getTotalNumberOfActivities(getField(param.thisObject, PackageHandler.class));
        }
      });
    } catch(Exception ex) { log(ex); }

    try {
    findAndHookMethod(AppTrayModel.class, "getNumberOfDownloadedActivities",
      new XC_MethodReplacement()
      {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
        {
          return getNumberOfDownloadedActivities(getField(param.thisObject, PackageHandler.class));
        }
      });
    } catch(Exception ex) { log(ex); }

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
    } catch(Exception ex) { log(ex); }
  }
  
////////////////////////////////////////////////////////////
  
  public static boolean filterAppTrayItems(AppTraySorter sorter, List<Item> items)
  {
    Context context = getContext(getObjectField(sorter, "mPreferences"));
    SharedPreferences prefs = context.getSharedPreferences("hidden", 0);
    for(Iterator<Item> i = items.iterator(); i.hasNext(); )
    {
      Item item = i.next();
      if(prefs.getBoolean(item.getPackageName(), false))
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
  
  public static void updateModelAndItems(AppTrayModel model, List<Item> list1, List<Item> list2)
  {
    Context context = getContext(getObjectField(model, "mPreferences"));
    SharedPreferences prefs = context.getSharedPreferences("hidden", 0);
    List<Item> items = getField(model, "mItems");
    for(Item item: items)
      if(prefs.getBoolean(item.getPackageName(), false) && !list1.contains(item))
        list1.add(item);
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
    catch(Exception ex)
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
  
  public static int getNumberOfDownloadedActivities(PackageHandler packageHandler)
  {
    SharedPreferences prefs = getContext(packageHandler).getSharedPreferences("hidden", 0);
    int i = 0;
    for(ActivityItem item: packageHandler.getActivityItemSet())
      if(packageHandler.isPackageDownloaded(item.getPackageName()) && !prefs.getBoolean(item.getPackageName(), false))
        ++i;
    return i;
  }
  
  public static int getTotalNumberOfActivities(PackageHandler packageHandler)
  {
    SharedPreferences prefs = getContext(packageHandler).getSharedPreferences("hidden", 0);
    numberOfActivities = 0;
    numberOfHiddenActivities = 0;
    for(ActivityItem item: packageHandler.getActivityItemSet())
    {
      if(!prefs.getBoolean(item.getPackageName(), false))
        ++numberOfActivities;
      else
        ++numberOfHiddenActivities;
    }
    return numberOfActivities;
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

  static <T> T getField(Object o, String name)
  {
    return (T)getObjectField(o, name);
  }

////////////////////////////////////////////////////////////
  
}

////////////////////////////////////////////////////////////
