package com.gem.xperiaxposed.home;

import static com.gem.xperiaxposed.home.Hooks.*;
import static com.sonymobile.flix.components.Component.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;

import com.gem.xperiaxposed.*;
import com.sonyericsson.home.R;
import com.sonymobile.flix.components.*;
import com.sonymobile.flix.components.util.*;
import com.sonymobile.flix.debug.*;
import com.sonymobile.home.apptray.*;
import com.sonymobile.home.data.*;
import com.sonymobile.home.transfer.*;

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
    
    thiz.setSize(scene.getWidth(), res.getDimension(R.dimen.dropzone_height));
    mDropZoneBg = BitmapFactory.decodeResource(res, R.drawable.home_apptray_dropzone);
    mHideBitmap = ((BitmapDrawable)res.getDrawable(Ids.home_apptray_dropzone_hide)).getBitmap();
    mUnhideBitmap = ((BitmapDrawable)res.getDrawable(Ids.home_apptray_dropzone_unhide)).getBitmap();

    mDropArea.setSize(scene.getWidth()/2, 3.0F * res.getDimension(R.dimen.dropzone_height));
    mHideDropArea.setSize(scene.getWidth()/2, 3.0F * res.getDimension(R.dimen.dropzone_height));
    
    mBackground.setBitmap(mDropZoneBg);
    mBackground.setScalingToSize(scene.getWidth()/2, mBackground.getHeight());
    mHideBackground.setBitmap(mDropZoneBg);
    mHideBackground.setScalingToSize(scene.getWidth()/2, mBackground.getHeight());

    mIcon.setBitmap(R.drawable.home_apptray_dropzone_home);
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
