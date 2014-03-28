package com.gem.xperiaxposed;

import static de.robv.android.xposed.XposedBridge.*;
import static de.robv.android.xposed.XposedHelpers.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

////////////////////////////////////////////////////////////

public class AutoHook
{

  @Target(ElementType.METHOD)
  @Inherited
  @Retention(RetentionPolicy.RUNTIME)
  public @interface EnableIf
  {
    String[] value();
  }

  @Target(ElementType.METHOD)
  @Inherited
  @Retention(RetentionPolicy.RUNTIME)
  public @interface DisableIf
  {
    String[] value();
  }

////////////////////////////////////////////////////////////
  
  public static final Object NONE = null;
  public static final Object VOID = new Object();
  public static final Object NULL = VOID;

////////////////////////////////////////////////////////////

  protected static final String CLASSHOOK_KEY = "CLASSHOOK";

////////////////////////////////////////////////////////////

  public AutoHook()
  {
    this(true);
  }
  
  public AutoHook(boolean autoInstallHooks)
  {
    if(autoInstallHooks)
      installHooks(this);
  }
  
////////////////////////////////////////////////////////////
  
  public static void installHooks(Object hook)
  {
    installHooks(null, hook);
  }
  
  protected static void installHooks(Class<?> cth, Object h)
  {
    final Object hook = (h instanceof Class) ? null : h;
    final Class<?> hookClass = (h instanceof Class) ? (Class<?>)h : h.getClass();
    
    for(Method[] entry: getHookMethods(hookClass))
    {
      try
      {
        final Method beforeMethod = entry[0];
        final Method afterMethod = entry[1];
        Class<?>[] beforeMethodArgs = (beforeMethod != null) ? beforeMethod.getParameterTypes() : null;
        Class<?>[] afterMethodArgs = (afterMethod != null) ? afterMethod.getParameterTypes() : null;
        final boolean beforeMethodHasParam = (beforeMethodArgs != null) && (beforeMethodArgs.length != 0) && (beforeMethodArgs[beforeMethodArgs.length-1] == MethodHookParam.class);
        final boolean afterMethodHasParam = (afterMethodArgs != null) && (afterMethodArgs.length != 0) && (afterMethodArgs[afterMethodArgs.length-1] == MethodHookParam.class);

        String methodName = (beforeMethod != null) ? beforeMethod.getName().substring("before_".length()) : afterMethod.getName().substring("after_".length());
        Class<?>[] args = (beforeMethodArgs != null) ? beforeMethodArgs : afterMethodArgs;
        boolean argsHasParam = (beforeMethodArgs != null) ? beforeMethodHasParam : afterMethodHasParam;
        final boolean hasThis = (cth == null) || ((args.length != 0) && (args[0] == cth));
        Class<?> classToHook = hasThis ? args[0] : cth;
        
        if(methodName.equals("all_constructors"))
        {
          hookAllConstructors(classToHook, new XC_MethodHook()
          {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable
            {
              if(beforeMethod != null)
                invokeHook(hook, beforeMethod, param, hasThis, false, beforeMethodHasParam);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable
            {
              if(afterMethod != null)
                invokeHook(hook, afterMethod, param, hasThis, false, afterMethodHasParam);
            }
          });
        }
        else
        {
          args = Arrays.copyOfRange(args, (hasThis ? 1 : 0), args.length - (argsHasParam ? 1 : 0)); // strip thiz and param, if any
          final boolean hasArgs = args.length != 0;
          Member peer = methodName.equals("constructor") ? findConstructorBestMatch(classToHook, args) : findMethodBestMatch(classToHook, methodName, args);
          hookMethod(peer, new XC_MethodHook()
          {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable
            {
              if(beforeMethod != null)
                invokeHook(hook, beforeMethod, param, hasThis, hasArgs, beforeMethodHasParam);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable
            {
              if(afterMethod != null)
                invokeHook(hook, afterMethod, param, hasThis, hasArgs, afterMethodHasParam);
            }
          });
        }
        
        log("hooked " + classToHook.getName() + " " + methodName);
      }
      catch(Throwable ex)
      {
        log(ex);
      }
    }
  }
  
////////////////////////////////////////////////////////////
  
  private static Collection<Method[]> getHookMethods(Class<?> classHook)
  {
    Map<String, Method[]> methodPairs = new HashMap<String, Method[]>();
    Method[] methods = classHook.getDeclaredMethods();
    for(Method method: methods)
    {
      String key = methodKey(method);
      if(key != null && isEnabled(method.getAnnotation(EnableIf.class), method.getAnnotation(DisableIf.class)))
      {
        Method[] methodPair = methodPairs.get(key);
        if(methodPair == null)
          methodPairs.put(key, methodPair = new Method[2]);
        methodPair[method.getName().startsWith("before_") ? 0 : 1] = method;
      }
    }
    return methodPairs.values();
  }
  
////////////////////////////////////////////////////////////
  
  private static boolean isEnabled(EnableIf e, DisableIf d)
  {
    if(d != null)
    {
      for(String c: d.value())
      {
        try
        {
          if(Conditionals.class.getField(c).getBoolean(null))
            return false;
        }
        catch(Exception ex)
        {
          log(ex);
        }
      }
    }
    
    if(e != null)
    {
      for(String c: e.value())
      {
        try
        {
          if(Conditionals.class.getField(c).getBoolean(null))
            return true;
        }
        catch(Exception ex)
        {
          log(ex);
        }
      }
      return false;
    }
    
    return true;
  }
  
////////////////////////////////////////////////////////////
  
  private static String methodKey(Method method)
  {
    String key = method.getName();
    key = key.startsWith("before_") ? key.substring("before_".length()) : key.startsWith("after_") ? key.substring("after_".length()) : null;
    if(key == null)
      return null;
    
    for(Class<?> clazz: method.getParameterTypes())
      if(clazz != MethodHookParam.class)
        key += "@" + clazz.getName(); 
    
    return key;
  }
  
////////////////////////////////////////////////////////////

  private static void invokeHook(Object hook, Method method, MethodHookParam param, boolean hasThis, boolean hasArgs, boolean hasParam) throws Exception
  {
    if((hook == null) && (method.getModifiers() & Modifier.STATIC) == 0)
      if((hook = getAdditionalInstanceField(param.thisObject, CLASSHOOK_KEY)) == null)
        return;

    Object result;
    if(hasArgs)
    {
      if(hasThis && hasParam)
        result = method.invoke(hook, array(param.thisObject, param.args, param));
      else if(hasThis)
        result = method.invoke(hook, array(param.thisObject, param.args));
      else if(hasParam)
        result = method.invoke(hook, array(param.args, param));
      else
        result = method.invoke(hook, param.args);
    }
    else
    {
      if(hasThis && hasParam)
        result = method.invoke(hook, param.thisObject, param);
      else if(hasThis)
        result = method.invoke(hook, param.thisObject);
      else if(hasParam)
        result = method.invoke(hook, param);
      else
        result = method.invoke(hook);
    }
    
    if(result == null)
      ;
    else if(result instanceof Throwable)
      param.setThrowable((Throwable)result);
    else if(result == NULL)
      param.setResult(null);
    else
      param.setResult(result);
  }
  
////////////////////////////////////////////////////////////

  private static Object[] array(Object f, Object[] a, Object l)
  {
    Object[] r = new Object[a.length + 2];
    r[0] = f;
    System.arraycopy(a, 0, r, 1, a.length);
    r[r.length-1] = l;
    return r;
  }
  
  private static Object[] array(Object f, Object[] a)
  {
    Object[] r = new Object[a.length + 1];
    r[0] = f;
    System.arraycopy(a, 0, r, 1, a.length);
    return r;
  }
  
  private static Object[] array(Object[] a, Object l)
  {
    Object[] r = Arrays.copyOf(a, a.length+1);
    r[r.length-1] = l;
    return r;
  }
  
////////////////////////////////////////////////////////////
  
}

////////////////////////////////////////////////////////////
