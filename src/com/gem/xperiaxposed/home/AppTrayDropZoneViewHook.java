package com.gem.xperiaxposed.home;

import static com.gem.xperiaxposed.home.Hooks.*;
import static com.sonymobile.flix.components.Component.*;
import static de.robv.android.xposed.XposedHelpers.*;

import java.util.*;

import android.content.*;
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

  public AppTrayDropZoneViewHook(final AppTrayDropZoneView _this)
  {
    super(_this);
    mBackground = getField(_this, "mBackground");
    mHideBackground = new Image(_this.getScene());
    mDropArea = getField(_this, "mDropArea");
    mHideDropArea = new Component(_this.getScene());
    mHideDropArea.setId(Ids.hide_drop_area);
    mIcon = getField(_this, "mIcon");
    mHideIcon = new Image(_this.getScene());
    
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
            SharedPreferences prefs = _this.getScene().getContext().getSharedPreferences("hidden", 0);
            AppTray appTray = getAppTray(_this);
            boolean hide = appTray.getPresenter().getSorter().getSortMode() != HIDDEN;

            String packageName = transferable.getItem().getPackageName();
            if(hide)
              prefs.edit().putBoolean(packageName, true).commit();
            else
              prefs.edit().remove(packageName).commit();
            
            AppTrayModel model = appTray.getModel();
            model.updateModel(new ArrayList<Item>());
            callMethod(model, "notifyAppTrayModelAppListener", model.getTotalNumberOfActivities(), model.getNumberOfDownloadedActivities());
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
    
    _this.getListeners().addChangeListener(new ComponentListeners.ChangeListenerAdapter()
    {
      public void onVisibilityChanged(Component component, boolean visible)
      {
        if(visible)
        {
          mHideDropArea.setBackgroundColor(0);
          if(getAppTray(_this).getPresenter().getSorter().getSortMode() == HIDDEN)
            mHideIcon.setBitmap(mUnhideBitmap);
          else
            mHideIcon.setBitmap(mHideBitmap);
        }
      }
    });
  }
  
  public Object before_onAddedTo(Component parent)
  {
    _this.addChild(mDropArea);
    _this.addChild(mHideDropArea);
    _this.addChild(mBackground);
    _this.addChild(mHideBackground);
    _this.addChild(mIcon);
    _this.addChild(mHideIcon);
    _this.updateConfiguration();
    return VOID;
  }
  
  public Object before_updateConfiguration()
  {
    Scene scene = _this.getScene();
    Resources res = scene.getContext().getResources();
    
    _this.setSize(scene.getWidth(), res.getDimension(R.dimen.dropzone_height));
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

    Layouter.place(mDropArea, LEFT, BOTTOM, _this, LEFT, BOTTOM);
    Layouter.place(mHideDropArea, RIGHT, BOTTOM, _this, RIGHT, BOTTOM);
    Layouter.place(mBackground, CENTER, BOTTOM, _this, 0.25f, BOTTOM);
    Layouter.place(mHideBackground, CENTER, BOTTOM, _this, 0.75f, BOTTOM);
    Layouter.place(mIcon, CENTER, BOTTOM, _this, 0.25f, BOTTOM);
    Layouter.place(mHideIcon, CENTER, BOTTOM, _this, 0.75f, BOTTOM);
    return VOID;
  }
}
