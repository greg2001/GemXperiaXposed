package com.gem.xposed;

import static de.robv.android.xposed.XposedHelpers.*;
import android.annotation.SuppressLint;
import android.app.AndroidAppHelper;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XResForwarder;
import android.content.res.XResources;
import android.util.DisplayMetrics;
import android.util.SparseArray;

@SuppressLint("UseSparseArrays")
public class ModuleResources extends Resources
{
  private SparseArray<Float> overridenDimensions = new SparseArray<Float>();
  
  private ModuleResources(AssetManager assets, DisplayMetrics metrics, Configuration config)
  {
    super(assets, metrics, config);
  }

  public static ModuleResources createInstance(String modulePath, XResources origRes)
  {
    if(modulePath == null)
      throw new IllegalArgumentException("modulePath must not be null");

    AssetManager assets = (AssetManager)newInstance(AssetManager.class);
    callMethod(assets, "addAssetPath", modulePath);
    
    ModuleResources res;
    if(origRes != null)
      res = new ModuleResources(assets, origRes.getDisplayMetrics(), origRes.getConfiguration());
    else
      res = new ModuleResources(assets, null, null);

    AndroidAppHelper.addActiveResource(modulePath, res.hashCode(), false, res);
    return res;
  }
  
  public void setDimension(int id, float dimension)
  {
    overridenDimensions.put(id, dimension);
  }

  @Override
  public float getDimension(int id) throws NotFoundException
  {
    Float val = overridenDimensions.get(id);
    return (val != null) ? val : super.getDimension(id);
  }
  
  @Override
  public int getDimensionPixelOffset(int id) throws NotFoundException
  {
    Float val = overridenDimensions.get(id);
    return (val != null) ? (int)(float)val : super.getDimensionPixelOffset(id);
  }
  
  @Override
  public int getDimensionPixelSize(int id) throws NotFoundException
  {
    Float val = overridenDimensions.get(id);
    if(val != null)
    {
      float value = val;
      int res = (int)(getDimension(id)+0.5f);
      return (res != 0) ? res : (value == 0) ? 0 : (value > 0) ? 1 : -1;
    }
    return super.getDimensionPixelSize(id);
  }
  
  /**
   * Create an {@link XResForwarder} instances that forwards requests to
   * {@code id} in this resource.
   */
  public XResForwarder fwd(int id)
  {
    return new XResForwarder(this, id);
  }
}
