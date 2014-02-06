package com.sonymobile.home.apptray;

import static com.gem.xperiaxposed.home.Hooks.*;
import static de.robv.android.xposed.XposedHelpers.*;

import java.util.*;

import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;

import com.gem.xperiaxposed.home.*;
import com.sonyericsson.home.*;
import com.sonymobile.flix.components.*;
import com.sonymobile.flix.components.util.*;
import com.sonymobile.flix.debug.*;
import com.sonymobile.home.data.*;
import com.sonymobile.home.transfer.*;

public class AppTrayDropZoneView extends Component
{
  private AppTrayDropZoneSpaceCallback mAppTrayDropZoneSpaceCallback;
  private Bitmap mDropZoneBg;
  private Bitmap mHideBitmap;
  private Bitmap mUnhideBitmap;
  private Image mBackground;
  private Image mHideBackground;
  private Component mDropArea;
  private Component mHideDropArea;
  private Image mIcon;
  private Image mHideIcon;
  
  public AppTrayDropZoneView(Scene scene, int id, int daid)
  {
    super(scene);
    setId(id);
    mBackground = new Image(mScene);
    mHideBackground = new Image(mScene);
    mDropArea = new Component(mScene);
    mDropArea.setId(daid);
    mHideDropArea = new Component(mScene);
    mHideDropArea.setId(Ids.hide_drop_area);
    mIcon = new Image(mScene);
    mHideIcon = new Image(mScene);
    
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
            SharedPreferences prefs = getScene().getContext().getSharedPreferences("hidden", 0);
            AppTray appTray = getAppTray(AppTrayDropZoneView.this);
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
    
    getListeners().addChangeListener(new ComponentListeners.ChangeListenerAdapter()
    {
      public void onVisibilityChanged(Component component, boolean visible)
      {
        if(visible)
        {
          if((mAppTrayDropZoneSpaceCallback != null) && !mAppTrayDropZoneSpaceCallback.isSpaceAvailable())
            mBackground.setBitmap(R.drawable.home_apptray_dropzone_full);
          else
            mBackground.setBitmap(mDropZoneBg);

          mHideDropArea.setBackgroundColor(0);
          if(getAppTray(AppTrayDropZoneView.this).getPresenter().getSorter().getSortMode() == HIDDEN)
            mHideIcon.setBitmap(mUnhideBitmap);
          else
            mHideIcon.setBitmap(mHideBitmap);
        }
      }
    });
  }
  
  public Component getDropArea()
  {
    return mDropArea;
  }
  
  public void onAddedTo(Component parent)
  {
    addChild(mDropArea);
    addChild(mHideDropArea);
    addChild(mBackground);
    addChild(mHideBackground);
    addChild(mIcon);
    addChild(mHideIcon);
    updateConfiguration();
  }
  
  public void onDestroy()
  {
    mAppTrayDropZoneSpaceCallback = null;
  }
  
  public void setAppTrayDropZoneSpaceCallback(AppTrayDropZoneSpaceCallback callback)
  {
    mAppTrayDropZoneSpaceCallback = callback;
  }
  
  public void updateConfiguration()
  {
    Scene scene = getScene();
    Resources res = scene.getContext().getResources();
    
    setSize(scene.getWidth(), res.getDimension(R.dimen.dropzone_height));
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

    Layouter.place(mDropArea, LEFT, BOTTOM, this, LEFT, BOTTOM);
    Layouter.place(mHideDropArea, RIGHT, BOTTOM, this, RIGHT, BOTTOM);
    Layouter.place(mBackground, CENTER, BOTTOM, this, 0.25f, BOTTOM);
    Layouter.place(mHideBackground, CENTER, BOTTOM, this, 0.75f, BOTTOM);
    Layouter.place(mIcon, CENTER, BOTTOM, this, 0.25f, BOTTOM);
    Layouter.place(mHideIcon, CENTER, BOTTOM, this, 0.75f, BOTTOM);
  }
  
  public static interface AppTrayDropZoneSpaceCallback
  {
    public boolean isSpaceAvailable();
  }
}
