package com.gem.xperiaxposed;

import static de.robv.android.xposed.XposedHelpers.*;

import java.io.*;
import java.util.*;

////////////////////////////////////////////////////////////

public class ReflectionUtils
{

////////////////////////////////////////////////////////////
  
  public static void setParentClassLoader(ClassLoader classLoader, ClassLoader parent)
  {
    setObjectField(classLoader, "parent", parent);
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

}

////////////////////////////////////////////////////////////
