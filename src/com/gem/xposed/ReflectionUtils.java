package com.gem.xposed;

import static de.robv.android.xposed.XposedHelpers.*;

import java.io.File;
import java.util.Arrays;

import android.content.Context;
import android.content.res.Resources;

////////////////////////////////////////////////////////////

public class ReflectionUtils
{

////////////////////////////////////////////////////////////
  
  public static void setParentClassLoader(ClassLoader... cl)
  {
    for(int i = cl.length-2; i >= 0; --i)
      setObjectField(cl[i], "parent", cl[i+1]);
  }
  
////////////////////////////////////////////////////////////

  public static void addToClassPath(ClassLoader classLoader, String apk, boolean toFront) throws Exception
  {
    Object dexPathList = getObjectField(classLoader, "pathList");
    Object[] dexElements = (Object[])getObjectField(dexPathList, "dexElements");

    File zip = new File(apk);
    Object dex = callStaticMethod(dexPathList.getClass(), "loadDexFile", zip, null);
    Object dexElement = newInstance(dexElements[0].getClass(), zip, false, zip, dex);
    
    dexElements = Arrays.copyOf(dexElements, dexElements.length+1);
    if(toFront)
    {
      System.arraycopy(dexElements, 0, dexElements, 1, dexElements.length-1);
      dexElements[0] = dexElement;
    }
    else
      dexElements[dexElements.length-1] = dexElement;
    setObjectField(dexPathList, "dexElements", dexElements);
  }

////////////////////////////////////////////////////////////
  
  public static void addToEnum(Class<?> enumClass, Object... args)
  {
    Object[] values = (Object[])getStaticObjectField(enumClass, "$VALUES");
    values = Arrays.copyOf(values, values.length+1);
    values[values.length-1] = newInstance(enumClass, args);
    setStaticObjectField(enumClass, "$VALUES", values);
  }
  
////////////////////////////////////////////////////////////
  
  @SuppressWarnings("unchecked")
  public static <T> T getField(Object o, String name)
  {
    return (T)getObjectField(o, name);
  }
  
  public static Context getContext(Object o)
  {
    return getField(o, "mContext");
  }

  public static Resources getResources(Object o)
  {
    return getContext(o).getResources();
  }

////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////
