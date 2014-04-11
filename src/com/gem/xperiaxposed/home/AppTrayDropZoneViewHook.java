package com.gem.xperiaxposed.home;

import static com.gem.xperiaxposed.Constants.*;
import static com.gem.xperiaxposed.home.ExperimentalHooks.*;
import static com.gem.xposed.AutoHook.*;
import static com.gem.xposed.ReflectionUtils.*;
import static com.sonymobile.flix.components.Component.*;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import com.gem.xposed.ClassHook;
import com.sonymobile.flix.components.Component;
import com.sonymobile.flix.components.ComponentListeners;
import com.sonymobile.flix.components.Image;
import com.sonymobile.flix.components.Scene;
import com.sonymobile.flix.components.util.Layouter;
import com.sonymobile.flix.debug.Logx;
import com.sonymobile.home.apptray.AppTray;
import com.sonymobile.home.apptray.AppTrayDropZoneView;
import com.sonymobile.home.apptray.AppTrayModel;
import com.sonymobile.home.data.ActivityItem;
import com.sonymobile.home.transfer.DropTarget;
import com.sonymobile.home.transfer.Transferable;

public class AppTrayDropZoneViewHook extends ClassHook<AppTrayDropZoneView>
{
  private Bitmap mDropZoneBg;
  private Bitmap mHideBitmap;
  private Bitmap mUnhideBitmap;
  private Image mBackground;
  private Image mHideBackground;
  private Component mDropArea;
  private Component mHideDropArea;
  private Image mIcon;
  private Image mHideIcon;

  public AppTrayDropZoneViewHook(final AppTrayDropZoneView thiz)
  {
    super(thiz);
    mBackground = getField(thiz, "mBackground");
    mHideBackground = new Image(thiz.getScene());
    mDropArea = getField(thiz, "mDropArea");
    mHideDropArea = new Component(thiz.getScene());
    mHideDropArea.setId(Ids.hide_drop_area);
    mIcon = getField(thiz, "mIcon");
    mHideIcon = new Image(thiz.getScene());
    
    mHideDropArea.setProperty(DropTarget.PROPERTY_DROP_TARGET, new DropTarget()
    {
      @Override
      public void drop(Transferable transferable, int action, Image image, DropCallback callback)
      {
        callback.dropFinished(0, null);
        try
        {
          if(transferable.getItem() instanceof ActivityItem)
          {
            AppTray appTray = getAppTray(thiz);
            AppTrayModel model = appTray.getModel();
            AppTrayModelHook modelHook = AppTrayModelHook.getHook(model);
            modelHook.setHidden(transferable.getItem(), appTray.getPresenter().getSorter().getSortMode() != HIDDEN);
          }
        }
        catch(Exception ex)
        {
          Logx.e("", ex);
        }
      }

      @Override
      public boolean enter(Transferable transferable, Image image, TransferEvent event)
      {
        if(transferable.getItem() instanceof ActivityItem)
          mHideDropArea.setBackgroundColor(0x2000FF00);
        else
          mHideDropArea.setBackgroundColor(0x30FF0000);
        return true;
      }

      @Override
      public void exit(Transferable transferable, Image image)
      {
        mHideDropArea.setBackgroundColor(0);
      }

      @Override
      public void over(Transferable transferable, Image image, TransferEvent event)
      {
      }
    });
    
    thiz.getListeners().addChangeListener(new ComponentListeners.ChangeListenerAdapter()
    {
      public void onVisibilityChanged(Component component, boolean visible)
      {
        if(visible)
        {
          mHideDropArea.setBackgroundColor(0);
          if(getAppTray(thiz).getPresenter().getSorter().getSortMode() == HIDDEN)
            mHideIcon.setBitmap(mUnhideBitmap);
          else
            mHideIcon.setBitmap(mHideBitmap);
        }
      }
    });
  }
  
  public Object before_onAddedTo(Component parent)
  {
    thiz.addChild(mDropArea);
    thiz.addChild(mHideDropArea);
    thiz.addChild(mBackground);
    thiz.addChild(mHideBackground);
    thiz.addChild(mIcon);
    thiz.addChild(mHideIcon);
    thiz.updateConfiguration();
    return VOID;
  }
  
  public Object before_updateConfiguration()
  {
    Scene scene = thiz.getScene();
    Resources res = scene.getContext().getResources();
    
    thiz.setSize(scene.getWidth(), res.getDimension(res.getIdentifier("dropzone_height", "dimen", SE_HOME)));
    mDropZoneBg = BitmapFactory.decodeResource(res, res.getIdentifier("home_apptray_dropzone", "drawable", SE_HOME));
    mHideBitmap = ((BitmapDrawable)res.getDrawable(Ids.home_apptray_dropzone_hide)).getBitmap();
    mUnhideBitmap = ((BitmapDrawable)res.getDrawable(Ids.home_apptray_dropzone_unhide)).getBitmap();

    mDropArea.setSize(scene.getWidth()/2, 3.0F * res.getDimension(res.getIdentifier("dropzone_height", "dimen", SE_HOME)));
    mHideDropArea.setSize(scene.getWidth()/2, 3.0F * res.getDimension(res.getIdentifier("dropzone_height", "dimen", SE_HOME)));
    
    mBackground.setBitmap(mDropZoneBg);
    mBackground.setScalingToSize(scene.getWidth()/2, mBackground.getHeight());
    mHideBackground.setBitmap(mDropZoneBg);
    mHideBackground.setScalingToSize(scene.getWidth()/2, mBackground.getHeight());

    mIcon.setBitmap(res.getIdentifier("home_apptray_dropzone_home", "drawable", SE_HOME));
    mHideIcon.setBitmap(mHideBitmap);

    Layouter.place(mDropArea, LEFT, BOTTOM, thiz, LEFT, BOTTOM);
    Layouter.place(mHideDropArea, RIGHT, BOTTOM, thiz, RIGHT, BOTTOM);
    Layouter.place(mBackground, CENTER, BOTTOM, thiz, 0.25f, BOTTOM);
    Layouter.place(mHideBackground, CENTER, BOTTOM, thiz, 0.75f, BOTTOM);
    Layouter.place(mIcon, CENTER, BOTTOM, thiz, 0.25f, BOTTOM);
    Layouter.place(mHideIcon, CENTER, BOTTOM, thiz, 0.75f, BOTTOM);
    return VOID;
  }
}
