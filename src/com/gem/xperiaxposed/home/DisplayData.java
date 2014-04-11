package com.gem.xperiaxposed.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Point;
import android.view.Display;
import de.robv.android.xposed.XposedBridge;

public final class DisplayData
{
  private static int sBottomOffset = 0;
  private static int sRightOffset = 0;
  private static int sTopOffset = 0;
  
  public static int getBottomOffset()
  {
    return sBottomOffset;
  }
  
  public static int getRightOffset()
  {
    return sRightOffset;
  }
  
  public static int getTopOffset()
  {
    return sTopOffset;
  }
  
  @SuppressLint("NewApi")
  public static void updateConfiguration(Activity activity, Resources res, Display display)
  {
    Point size = new Point();
    display.getSize(size);

    Point realSize = new Point();
    if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1)
      display.getRealSize(realSize);
    else
    {
      try
      {
        realSize.x = (Integer)Display.class.getMethod("getRawWidth").invoke(display);
        realSize.y = (Integer)Display.class.getMethod("getRawHeight").invoke(display);
      }
      catch(Throwable e)
      {
        XposedBridge.log(e);
      }
    }

    sTopOffset = res.getDimensionPixelSize(res.getIdentifier("status_bar_height", "dimen", "android"));
    sBottomOffset = realSize.y - size.y;
    sRightOffset = realSize.x - size.x;
  }
}
