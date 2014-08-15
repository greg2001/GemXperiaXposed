package com.gem.xperiaxposed.home;

import static com.gem.xposed.ReflectionUtils.*;
import static de.robv.android.xposed.XposedHelpers.*;

import com.sonymobile.flix.components.Typed;
import com.sonymobile.flix.util.ListTouchHelper;
import com.sonymobile.flix.util.Scheduler;
import com.sonymobile.flix.util.SpringDynamics;
import com.sonymobile.home.ui.pageview.PageView;
import com.sonymobile.home.ui.pageview.PageViewGroup;

////////////////////////////////////////////////////////////

public class Animations
{

////////////////////////////////////////////////////////////

  public static final int CONVEX                = 0; 
  public static final int CONCAVE               = 1; 
  public static final int CYLINDER              = 2; 
  public static final int HOME5                 = 3; 
  public static final int BLINKFEED             = 4; 
  public static final int WINDMILL              = 5; 
  public static final int SLIGHT_WINDMILL       = 6; 
  public static final int SIMPLE                = 7; 
  public static final int DESKTOP_DEFAULT       = CONVEX; 
  public static final int DRAWER_DEFAULT        = SIMPLE; 
  
////////////////////////////////////////////////////////////
  
  private PageViewGroup thiz;
  private ListTouchHelper mScrollController;
  private SpringDynamics mFlickDynamics;
  private boolean mFlickDynamicsRunning = false;
  private boolean mExternalDynamics = false;
  
////////////////////////////////////////////////////////////
  
  public Animations(PageViewGroup pageViewGroup)
  {
    thiz = pageViewGroup;
    mScrollController = getField(thiz, "mScrollController");
    try
    {
      mFlickDynamics = getField(thiz, "mFlickDynamics");
    }
    catch(Throwable ex)
    {
      mFlickDynamics = new SpringDynamics(300, 1);
      mFlickDynamics.setAtRestDistance(0.01f);
      mExternalDynamics = true;
    }
  }

////////////////////////////////////////////////////////////
  
  public void animate(boolean b, int animation)
  {
    if(mExternalDynamics)
      mFlickDynamics.setTarget(getFloatField(mScrollController, "mVelocity"));
    
    if(!b && !mFlickDynamicsRunning && (animation == BLINKFEED)) 
    {
      mFlickDynamicsRunning = true;
      thiz.getScene().addTask(new Scheduler.Task()
      {
        @Override
        public boolean onUpdate(long l)
        {
          mFlickDynamics.update(l);
          if(mFlickDynamics.isAtRest())
          {
            mFlickDynamics.setValue(mFlickDynamics.getTarget());
            mFlickDynamicsRunning = false;
            return false;
          }
          callMethod(thiz, "updateFromTouch");
          return true;
        }
      });
    }
    
    thiz.getScrollableContent().setPosition(getFloatField(thiz, "mGridHorizontalPosition"), getFloatField(thiz, "mGridVerticalPosition"));
    thiz.getScrollableContent().setZ(getFloatField(thiz, "mZoomPosition") * thiz.getDepth());
    switch(animation)
    {
      case CONVEX:          convex(thiz);                    break;
      case CONCAVE:         concave(thiz);                   break;
      case CYLINDER:        cylinder(thiz);                  break;
      case HOME5:           home5(thiz);                     break;
      case BLINKFEED:       blinkfeed(thiz, mFlickDynamics); break;
      case WINDMILL:        windmill(thiz);                  break;
      case SLIGHT_WINDMILL: slightWindmill(thiz);            break;
      case SIMPLE:          blinkfeed(thiz, null);           break;
    }
    thiz.getScene().invalidate();
  }
  
////////////////////////////////////////////////////////////

  public static void convex(PageViewGroup thiz)
  {
    Typed.Group<PageView> pages = getField(thiz, "mPages");
    
    float f;
    float f2;
    int n = pages.getNbrChildren();
    if(n == 0)
      return;
    float f3 = 1.3f * thiz.getScrollableContent().getWidth();
    boolean bl = thiz.getScene().getWidth() > thiz.getScene().getHeight();
    boolean bl2 = thiz.getZoomPosition() < -0.05f;
    if(bl)
    {
      f2 = bl2 ? 30.00001f : 30.0f;
      f = bl2 ? 50.0f : 44.0f;
    }
    else
    {
      f2 = bl2 ? 55.0f : 44.0f;
      f = bl2 ? 55.0f : 44.0f;
    }
    for(int i = 0; i < n; ++i)
    {
      PageView pageView = pages.getChild(i);
      float f4 = pageView.getPagePosition() - thiz.getScrollPosition();
      float f5 = f4 * 45.0f;
      float f6 = (float)(3.141592653589793 * (double)f5 / 180.0);
      float f7 = (float)Math.cos(f6);
      float f8 = Math.abs(f5);
      boolean bl3 = f8 < f;
      pageView.setOnScreen(bl3);
      if(bl3)
      {
        float f9 = (float)Math.sin(f6);
        pageView.setY(0.0f);
        pageView.setZ(f7 * f3 - f3);
        pageView.setX(f9 * f3);
        pageView.setRotation(0.0f, f5, 0.0f);
        if(f8 > f2)
        {
          pageView.setDescendantAlpha(1.0f - (f8 - f2) / (f - f2));
          continue;
        }
        pageView.setDescendantAlpha(1.0f);
        continue;
      }
      pageView.setY(0.0f);
      pageView.setZ(f7 * f3 - f3);
      pageView.setX(f4 * f3);
      pageView.setRotation(0.0f, 0.0f, 0.0f);
    }
  }

  public static void concave(PageViewGroup thiz)
  {
    Typed.Group<PageView> pages = getField(thiz, "mPages");
    
    int n = pages.getNbrChildren();
    if(n == 0)
      return;
    float f = 1.3f * thiz.getScrollableContent().getWidth();
    for(int i = 0; i < n; ++i)
    {
      PageView pageView = pages.getChild(i);
      float f2 = pageView.getPagePosition() - thiz.getScrollPosition();
      float f3 = (float)(3.141592653589793 * (double)f2 / 4.0);
      float f4 = 180.0f * f2 / 4.0f;
      float f5 = (float)Math.cos(f3);
      pageView.setX(f * (float)Math.sin(f3));
      pageView.setY(0.0f);
      pageView.setZ(f + f * (-f5));
      pageView.setRotation(0.0f, -f4, 0.0f);
      boolean bl = f4 > -55.0f && f4 < 55.0f;
      pageView.setOnScreen(bl);
    }
  }

  public static void cylinder(PageViewGroup thiz)
  {
    Typed.Group<PageView> pages = getField(thiz, "mPages");
    
    pages.setSortingEnabled(true);
    int n = pages.getNbrChildren();
    if(n == 0)
      return;
    float f = 1.3f * thiz.getScrollableContent().getWidth();
    for(int i = 0; i < n; ++i)
    {
      PageView pageView = pages.getChild(i);
      float f2 = pageView.getPagePosition() - thiz.getScrollPosition();
      float f3 = (float)(3.141592653589793 * (double)f2 / 4.0);
      float f4 = 180.0f * f2 / 4.0f;
      float f5 = (float)Math.cos(f3);
      pageView.setX(f * (float)Math.sin(f3));
      pageView.setY(0.0f);
      pageView.setZ(f5 * f - f);
      pageView.setRotation(0.0f, f4, 0.0f);
      pageView.setOrder(pageView.getZ());
      pageView.setOnScreen(true);
    }
  }

  public static void home5(PageViewGroup thiz)
  {
    Typed.Group<PageView> pages = getField(thiz, "mPages");
    
    int n = pages.getNbrChildren();
    if(n == 0)
      return;
    float f = 1.5f * thiz.getScrollableContent().getWidth();
    for(int i = 0; i < n; ++i)
    {
      PageView pageView = pages.getChild(i);
      float f2 = pageView.getPagePosition() - thiz.getScrollPosition();
      float f3 = (float)(3.141592653589793 * (double)f2 / 4.0);
      float f4 = 180.0f * f2 / 4.0f;
      float f5 = (float)Math.cos(f3);
      pageView.setX(f * (float)Math.sin(f3));
      pageView.setY(0.0f);
      pageView.setZ(f5 * f - f);
      pageView.setRotation(0.0f, -f4, 0.0f);
      boolean bl = f4 > -55.0f && f4 < 55.0f;
      pageView.setOnScreen(bl);
    }
  }

  public static void blinkfeed(PageViewGroup thiz, SpringDynamics flickDynamics)
  {
    Typed.Group<PageView> pages = getField(thiz, "mPages");
    
    int n = pages.getNbrChildren();
    if(n == 0)
      return;
    float f = 1.1f * thiz.getScrollableContent().getWidth();
    float f2 = 4.0f * ((flickDynamics != null) ? flickDynamics.getValue() : 0);
    for(int i = 0; i < n; ++i)
    {
      PageView pageView = pages.getChild(i);
      float f3 = pageView.getPagePosition() - thiz.getScrollPosition();
      pageView.setX(f3 * f);
      pageView.setY(0.0f);
      pageView.setZ(0.0f);
      pageView.setRotation(0.0f, f2, 0.0f);
      boolean bl = f3 < 1.1f;
      pageView.setOnScreen(bl);
    }
  }

  public static void windmill(PageViewGroup thiz)
  {
    Typed.Group<PageView> pages = getField(thiz, "mPages");
    
    int n = pages.getNbrChildren();
    if(n == 0)
      return;
    float f = 3.0f * thiz.getScrollableContent().getWidth();
    int n2 = thiz.getLeftmostPage();
    for(int i = 0; i < n; ++i)
    {
      PageView pageView = pages.getChild(i);
      float f2 = (float)(n2 + i) - thiz.getScrollPosition();
      float f3 = (float)(3.141592653589793 * (double)f2 / 7.0);
      float f4 = 180.0f * f2 / 7.0f;
      float f5 = (float)Math.cos(f3);
      pageView.setX(f * (float)Math.sin(f3));
      pageView.setY(f + f * (-f5));
      pageView.setZ(0.0f);
      pageView.setRotation(0.0f, 0.0f, -f4);
      boolean bl = f4 > -55.0f && f4 < 55.0f;
      pageView.setOnScreen(bl);
    }
  }

  public static void slightWindmill(PageViewGroup thiz)
  {
    Typed.Group<PageView> pages = getField(thiz, "mPages");
    
    boolean enableEditButtons = false;
    try
    {
      enableEditButtons = getBooleanField(thiz, "mEnableEditButtons");
    }
    catch(Throwable ex)
    {
    }

    float f;
    float f2;
    int n = pages.getNbrChildren();
    if(n == 0)
      return;
    float f3 = 6.0f * thiz.getScrollableContent().getWidth();
    boolean bl = enableEditButtons;
    boolean bl2 = thiz.getScene().getWidth() > thiz.getScene().getHeight();
    if(bl2)
    {
      f2 = bl ? 4.0f : 6.0f;
      f = bl ? 13.0f : 9.9f;
    }
    else
    {
      f2 = bl ? 4.0f : 6.00001f;
      f = bl ? 13.0f : 9.90001f;
    }
    for(int i = 0; i < n; ++i)
    {
      PageView pageView = pages.getChild(i);
      float f4 = 10.0f * (pageView.getPagePosition() - thiz.getScrollPosition());
      float f5 = (float)(3.141592653589793 * (double)f4 / 180.0);
      float f6 = (float)Math.cos(f5);
      pageView.setX(f3 * (float)Math.sin(f5));
      pageView.setY(f3 + f3 * (-f6));
      pageView.setZ(12.0f * (f6 * f3 - f3));
      pageView.setRotation(0.0f, 2.0f * f4, -f4);
      float f7 = f4 < 0.0f ? -f4 : f4;
      boolean bl3 = f7 < f;
      pageView.setOnScreen(bl3);
      if(!bl3)
        continue;
      if(f7 > f2)
      {
        pageView.setDescendantAlpha(1.0f - (f7 - f2) / (f - f2));
        continue;
      }
      pageView.setDescendantAlpha(1.0f);
    }
  }

////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////
