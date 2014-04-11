package com.gem.xperiaxposed.home;

import static com.gem.xperiaxposed.Constants.*;
import static com.gem.xposed.ReflectionUtils.*;
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

import com.gem.xposed.AutoHook;
import com.gem.xposed.ClassHook;
import com.gem.xposed.ReflectionUtils;
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
import com.sonymobile.home.transfer.TransferView;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class ExperimentalHooks extends AutoHook
{

////////////////////////////////////////////////////////////
  
  public static SortMode HIDDEN;
  public static AppTrayDrawerItemType APPTRAY_DRAWER_ITEM_TYPE_HIDDEN;
  public static AppTrayDrawerItemType APPTRAY_DRAWER_ITEM_TYPE_SETTINGS;

  public static int numberOfActivities = 0;
  public static int numberOfHiddenActivities = 0;
  
////////////////////////////////////////////////////////////

  private MissedItReceiver missedItReceiver = null;
  
////////////////////////////////////////////////////////////
  
  public ExperimentalHooks()
  {
    ReflectionUtils.addToEnum(AppTrayPreferenceManager.SortMode.class, "HIDDEN", 4, "hidden");
    ReflectionUtils.addToEnum(AppTrayDrawerLoadHelper.AppTrayDrawerItemType.class, "APPTRAY_DRAWER_ITEM_TYPE_HIDDEN", 9);
    ReflectionUtils.addToEnum(AppTrayDrawerLoadHelper.AppTrayDrawerItemType.class, "APPTRAY_DRAWER_ITEM_TYPE_SETTINGS", 9);
    HIDDEN = SortMode.valueOf("HIDDEN");
    APPTRAY_DRAWER_ITEM_TYPE_HIDDEN = AppTrayDrawerItemType.valueOf("APPTRAY_DRAWER_ITEM_TYPE_HIDDEN");
    APPTRAY_DRAWER_ITEM_TYPE_SETTINGS = AppTrayDrawerItemType.valueOf("APPTRAY_DRAWER_ITEM_TYPE_SETTINGS");
    
    ClassHook.hookClass(AppTrayDropZoneView.class, AppTrayDropZoneViewHook.class);
    ClassHook.hookClass(AppTrayModel.class, AppTrayModelHook.class);
  }
  
  public void after_getBadgeManager(StorageManager thiz, Context context, MethodHookParam param)
  {
    if(param.getResult() != null && missedItReceiver == null)
      missedItReceiver = new MissedItReceiver(context, (BadgeManager)param.getResult());
  }
  
  public void after_all_constructors(TransferView thiz)
  {
    List<Integer> targets = getField(thiz, "mTargets");
    targets.add(0, Ids.hide_drop_area);
  }
  
  public void before_filterItemsIfNeeded(AppTraySorter thiz, List<Item> items, SortMode mode)
  {
    filterAppTrayItems(thiz, items);
  }

  public void before_setModelItems(AppTrayAdapter thiz, List<Item> items, boolean b, MethodHookParam param)
  {
    AppTraySorter sorter = getField(getAppTray(thiz), "mAppTraySorter");
    if(sorter.getSortMode() == SortMode.OWN_ORDER)
    {
      if(items != null)
      {
        items = new ArrayList<Item>(items);
        filterAppTrayItems(sorter, items);
        param.args[0] = items;
      }
    }
  }

  public int after_getPageCount(AppTrayAdapter thiz, MethodHookParam param)
  {
    int pages = (Integer)param.getResult();
    return (pages == 0) ? 1 : pages;
  }

  public void before_sort(AppTraySorter thiz, SortMode mode, MethodHookParam param)
  {
    if(mode == HIDDEN)
      param.args[0] = SortMode.ALPHABETICAL;
  }

  public void after_loadItemData(AppTrayDrawerLoadHelper thiz, SortMode mode, int i1, int i2, Map<String, List<AppTrayDrawerItemData>> items)
  {
    injectAppTrayDrawerItems(thiz, mode, items);
  }

  public void after_onAppTrayDrawerItemClicked(AppTrayPresenter thiz, AppTrayDrawerItemData data)
  {
    if(data.mItemType == APPTRAY_DRAWER_ITEM_TYPE_SETTINGS)
      callMethod(thiz, "launchApplication", data.mIntent);
    else if(data.mItemType == APPTRAY_DRAWER_ITEM_TYPE_HIDDEN)
      callMethod(thiz, "handleSortModeItemClicked", HIDDEN);
  }
  
  public Object before_getCategoryTitleFromSortMode(AppTrayPresenter thiz, SortMode mode)
  {
    if(mode == SortMode.ALPHABETICAL)
      return getResources(thiz).getString(getResources(thiz).getIdentifier("app_tray_drawer_list_item_categories_alphabetical", "string", SE_HOME)) 
        + " (" + String.format("%d", numberOfActivities) + ")";
    else if(mode == HIDDEN)
      return getResources(thiz).getString(Ids.app_tray_drawer_list_item_categories_hidden) 
        + " (" + String.format("%d", numberOfHiddenActivities) + ")";
    else
      return NONE;
  }

  public Object before_convertSortModeToItemType(AppTrayDrawerAdapter thiz, SortMode mode)
  {
    if(mode == HIDDEN)
      return APPTRAY_DRAWER_ITEM_TYPE_HIDDEN;
    else
      return NONE;
  }

  public Object after_updateNumberOfMostUsedItems(AppTrayDrawerAdapter thiz, int i)
  {
    AppTrayDrawerLoadHelper loadHelper = getField(thiz, "mLoadHelper");
    callMethod(thiz, "setBadgeText", String.format("%d", numberOfActivities), loadHelper.getCategoriesTitle(), AppTrayDrawerItemType.APPTRAY_DRAWER_ITEM_TYPE_ALPHABETICAL);
    callMethod(thiz, "setBadgeText", String.format("%d", numberOfHiddenActivities), loadHelper.getCategoriesTitle(), APPTRAY_DRAWER_ITEM_TYPE_HIDDEN);
    return true;
  }

////////////////////////////////////////////////////////////
  
  private boolean filterAppTrayItems(AppTraySorter sorter, List<Item> items)
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
  
  private void injectAppTrayDrawerItems(AppTrayDrawerLoadHelper loadHelper, SortMode sortMode, Map<String, List<AppTrayDrawerItemData>> map)
  {
    Resources res = getResources(loadHelper);
    int iconSize = res.getDimensionPixelSize(res.getIdentifier("apptray_drawer_icon_width", "dimen", SE_HOME));
    Drawable hiddenIcon = res.getDrawable(Ids.drawer_icn_hidden);
    Drawable settingsIcon = res.getDrawable(Ids.drawer_icn_settings);
    
    try
    {
      Bitmap icon = BitmapFactory.decodeResource(res, res.getIdentifier("drawer_icn_own_order", "drawable", SE_HOME));
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
            (sortMode == HIDDEN) ? res.getDrawable(res.getIdentifier("drawer_list_selected", "drawable", SE_HOME)) : null,
            String.format("%d", numberOfHiddenActivities),
            APPTRAY_DRAWER_ITEM_TYPE_HIDDEN,
            null,
            null,
            null));
        }
      }
    }
  }
  
////////////////////////////////////////////////////////////
  
  public static Scene getScene(Object o)
  {
    return getField(o, "mScene");
  }

  public static MainView getMainView(Object o)
  {
    return (MainView)getScene(o).getView();
  }

  public static AppTray getAppTray(Object o)
  {
    return getField(getMainView(o), "mAppTray");
  }
  
////////////////////////////////////////////////////////////
  
}

////////////////////////////////////////////////////////////
