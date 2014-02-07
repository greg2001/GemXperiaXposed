package com.gem.xperiaxposed;

import static de.robv.android.xposed.XposedBridge.*;
import static de.robv.android.xposed.XposedHelpers.*;

import java.lang.reflect.*;
import java.util.*;

import de.robv.android.xposed.*;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

////////////////////////////////////////////////////////////

public class ClassHook<ClassToHook>
{

////////////////////////////////////////////////////////////
  
  public static final Object NONE = null;
  public static final Object VOID = new Object();
  public static final Object NULL = VOID;

////////////////////////////////////////////////////////////
  
  public final ClassToHook _this;
  
  public ClassHook(final ClassToHook _this)
  {
    this._this = _this;
  }
  
////////////////////////////////////////////////////////////
  
  @SuppressWarnings("unchecked")
  public static <ClassToHook> void hookClass(final Class<ClassToHook> classToHook, final Class<? extends ClassHook<ClassToHook>> classHook)
  {
    final String KEY = classHook.getName();
    hookAllConstructors(classToHook, new XC_MethodHook()
    {
      @Override
      protected void afterHookedMethod(MethodHookParam param) throws Throwable
      {
        ClassHook<ClassToHook> hook = (ClassHook<ClassToHook>)newInstance(classHook, param.thisObject);
        setAdditionalInstanceField(param.thisObject, KEY, hook);
        try
        {
          callMethod(hook, "after_constructor", param.args);
        }
        catch(NoSuchMethodError ex)
        {
          // nothing to do
        }
      }
    });
    
    for(Map.Entry<String, Method[]> entry: sortMethods(classHook).entrySet())
    {
      final String methodName = entry.getKey();
      final Method beforeMethod = entry.getValue()[0];
      final Method afterMethod = entry.getValue()[1];
      Object[] parameters = (beforeMethod != null) ? beforeMethod.getParameterTypes() : afterMethod.getParameterTypes();
      parameters = Arrays.copyOf(parameters, parameters.length+1, Object[].class);
      parameters[parameters.length-1] = new XC_MethodHook()
      {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable
        {
          if(beforeMethod != null)
          {
            ClassHook<ClassToHook> hook = (ClassHook<ClassToHook>)getAdditionalInstanceField(param.thisObject, KEY);
            if(hook != null)
              updateResult(param, beforeMethod.invoke(hook, param.args));
          }
        }
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
          if(afterMethod != null)
          {
            ClassHook<ClassToHook> hook = (ClassHook<ClassToHook>)getAdditionalInstanceField(param.thisObject, KEY);
            if(hook != null)
              updateResult(param, afterMethod.invoke(hook, param.args));
          }
        }
      };
      
      try
      {
        findAndHookMethod(classToHook, methodName, parameters);
      }
      catch(Exception ex)
      {
        log(ex);
      }
    }
  }
  
////////////////////////////////////////////////////////////
  
  private static Map<String, Method[]> sortMethods(Class<?> classHook)
  {
    Map<String, Method[]> methodPairs = new HashMap<String, Method[]>();
    Method[] methods = classHook.getDeclaredMethods();
    for(Method method: methods)
    {
      String name = method.getName();
      if(name.startsWith("before_"))
      {
        name = name.substring("before_".length());
        Method[] methodPair = methodPairs.get(name);
        if(methodPair == null)
          methodPairs.put(name, methodPair = new Method[2]);
        methodPair[0] = method;
      }
      else if(name.startsWith("after_"))
      {
        name = name.substring("after_".length());
        Method[] methodPair = methodPairs.get(name);
        if(methodPair == null)
          methodPairs.put(name, methodPair = new Method[2]);
        methodPair[1] = method;
      }
    }
    return methodPairs;
  }

  private static void updateResult(MethodHookParam param, Object result)
  {
    if(result != null)
    {
      if(result instanceof Throwable)
        param.setThrowable((Throwable)result);
      else if(result == NULL)
        param.setResult(null);
      else
        param.setResult(result);
    }
  }
  
////////////////////////////////////////////////////////////
  
}

////////////////////////////////////////////////////////////
