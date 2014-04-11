package com.gem.xperiaxposed.home;

import static com.gem.xperiaxposed.home.ExperimentalHooks.*;
import static com.gem.xposed.ReflectionUtils.*;
import static de.robv.android.xposed.XposedHelpers.*;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;

import com.gem.xposed.ClassHook;
import com.sonymobile.home.apptray.AppTrayModel;
import com.sonymobile.home.data.ActivityItem;
import com.sonymobile.home.data.Item;
import com.sonymobile.home.data.ItemLocation;
import com.sonymobile.home.model.PackageHandler;

public class AppTrayModelHook extends ClassHook<AppTrayModel>
{
  private Context context;
  private SharedPreferences prefs;
  private PackageHandler packageHandler;

  public AppTrayModelHook(AppTrayModel thiz)
  {
    super(thiz);
    context = getContext(getObjectField(thiz, "mPreferences"));
    prefs = context.getSharedPreferences("hidden", 0);
    packageHandler = getField(thiz, "mPackageHandler");
  }
  
  public boolean isHidden(Item item)
  {
    return prefs.getBoolean(item.getPackageName(), false);
  }
  
  public void setHidden(Item item, boolean hidden)
  {
    if(hidden)
    {
      item.setLocation(new ItemLocation(10000, 1));
      prefs.edit().putBoolean(item.getPackageName(), true).commit();
    }
    else
    {
      item.setLocation((ItemLocation)callMethod(thiz, "getNewLocation"));
      prefs.edit().remove(item.getPackageName()).commit();
    }
    thiz.updateModel(new ArrayList<Item>());
    callMethod(thiz, "notifyAppTrayModelAppListener", thiz.getTotalNumberOfActivities(), thiz.getNumberOfDownloadedActivities());
  }
  
  public int before_getLastPage()
  {
    int i = 0;
    List<Item> items = getField(thiz, "mItems");
    for(Item item: items)
      if(!isHidden(item) && item.getLocation().page > i)
        i = item.getLocation().page;
    return i;
  }

  public int before_getPageItemCount(int page)
  {
    int i = 0;
    List<Item> items = getField(thiz, "mItems");
    for(Item item: items)
      if(!isHidden(item) && item.getLocation().page == page)
        ++i;
    return i;
  }
  
  public boolean before_removeGapsAndEmptyPages(List<Item> items)
  {
    int lastPage = -1;
    int currentPage = -1;
    int currentPosition = -1;
    for(Item i: items)
    {
      if(!isHidden(i))
      {
        ItemLocation l = i.getLocation();
        if(l.page != lastPage)
        {
          lastPage = l.page;
          ++currentPage;
          currentPosition = -1;
        }
        l.page = currentPage;
        l.position = ++currentPosition;
      }
    }
    return true;
  }
    
  public void before_updateModelAndItems(List<Item> list1, List<Item> list2)
  {
    List<Item> items = getField(thiz, "mItems");
    for(Item item: items)
      if(isHidden(item) && !list1.contains(item))
        list1.add(item);
  }
  
  public int before_getNumberOfDownloadedActivities()
  {
    int i = 0;
    for(ActivityItem item: packageHandler.getActivityItemSet())
      if(packageHandler.isPackageDownloaded(item.getPackageName()) && !isHidden(item))
        ++i;
    return i;
  }
  
  public int before_getTotalNumberOfActivities()
  {
    numberOfActivities = 0;
    numberOfHiddenActivities = 0;
    for(ActivityItem item: packageHandler.getActivityItemSet())
    {
      if(!isHidden(item))
        ++numberOfActivities;
      else
        ++numberOfHiddenActivities;
    }
    return numberOfActivities;
  }
  
}
