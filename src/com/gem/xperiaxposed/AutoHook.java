package com.gem.xperiaxposed;

import static de.robv.android.xposed.XposedBridge.*;
import static de.robv.android.xposed.XposedHelpers.*;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

////////////////////////////////////////////////////////////

public class AutoHook
{

////////////////////////////////////////////////////////////
  
  public static final Object NONE = null;
  public static final Object VOID = new Object();
  public static final Object NULL = VOID;

////////////////////////////////////////////////////////////
  
  public AutoHook()
  {
    installHooks(this);
  }
  
////////////////////////////////////////////////////////////
  
  public static void installHooks(final Object hook)
  {
    Class<?> hookClass = hook.getClass();
    for(Method[] entry: getHookMethods(hookClass))
    {
      try
      {
        final Method beforeMethod = entry[0];
        final Method afterMethod = entry[1];
        final boolean beforeMethodHasParam = (beforeMethod != null) && (beforeMethod.getParameterTypes()[beforeMethod.getParameterTypes().length-1] == MethodHookParam.class);
        final boolean afterMethodHasParam = (afterMethod != null) && (afterMethod.getParameterTypes()[afterMethod.getParameterTypes().length-1] == MethodHookParam.class);

        String methodName = (beforeMethod != null) ? beforeMethod.getName().substring("before_".length()) : afterMethod.getName().substring("after_".length());
        Class<?>[] parameters = (beforeMethod != null) ? beforeMethod.getParameterTypes() : afterMethod.getParameterTypes();
        Class<?> classToHook = parameters[0];
        
        if(methodName.equals("all_constructors"))
        {
          hookAllConstructors(classToHook, new XC_MethodHook()
          {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable
            {
              if(beforeMethod != null)
                invokeAllConstructorsHook(hook, beforeMethod, param, beforeMethodHasParam);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable
            {
              if(afterMethod != null)
                invokeAllConstructorsHook(hook, afterMethod, param, afterMethodHasParam);
            }
          });
        }
        else
        {
          parameters = Arrays.copyOfRange(parameters, 1, parameters.length - ((parameters[parameters.length-1] == MethodHookParam.class) ? 1 : 0)); // strip thiz and param, if any
          Member peer = methodName.equals("constructor") ? findConstructorBestMatch(classToHook, parameters) : findMethodBestMatch(classToHook, methodName, parameters);
          hookMethod(peer, new XC_MethodHook()
          {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable
            {
              if(beforeMethod != null)
                invokeHook(hook, beforeMethod, param, beforeMethodHasParam);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable
            {
              if(afterMethod != null)
                invokeHook(hook, afterMethod, param, afterMethodHasParam);
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
      if(key != null)
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
  
  private static String methodKey(Method method)
  {
    String key = method.getName();
    key = key.startsWith("before_") ? key.substring("before_".length()) : key.startsWith("after_") ? key.substring("after_".length()) : null;
    if(key == null)
      return null;
    
    Class<?>[] parameters = method.getParameterTypes();
    if(parameters.length < 1)
      return null;
    
    for(Class<?> clazz: parameters)
      if(clazz != MethodHookParam.class)
        key += "@" + clazz.getName(); 
    
    return key;
  }
  
////////////////////////////////////////////////////////////

  private static void invokeAllConstructorsHook(Object hook, Method method, MethodHookParam param, boolean hasParam) throws Exception
  {
    Object result = hasParam ? method.invoke(hook, param.thisObject, param) : method.invoke(hook, param.thisObject);
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

  private static void invokeHook(Object hook, Method method, MethodHookParam param, boolean hasParam) throws Exception
  {
    Object[] params = new Object[param.args.length + 1 + (hasParam ? 1 : 0)]; // add thiz and param, if any
    params[0] = param.thisObject;
    System.arraycopy(param.args, 0, params, 1, param.args.length);
    if(hasParam)
      params[params.length-1] = param;
    Object result = method.invoke(hook, params);
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
  
}

////////////////////////////////////////////////////////////
