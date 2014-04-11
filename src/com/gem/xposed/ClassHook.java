package com.gem.xposed;

import static de.robv.android.xposed.XposedBridge.*;
import static de.robv.android.xposed.XposedHelpers.*;
import de.robv.android.xposed.XC_MethodHook;

////////////////////////////////////////////////////////////

public class ClassHook<ClassToHook> extends AutoHook
{

////////////////////////////////////////////////////////////
  
  public final ClassToHook thiz;
  
  public ClassHook(final ClassToHook thiz)
  {
    super(false);
    this.thiz = thiz;
  }
  
////////////////////////////////////////////////////////////

  @SuppressWarnings("unchecked")
  public static <ClassToHook> void hookClass(final Class<ClassToHook> classToHook, final Class<? extends ClassHook<ClassToHook>> classHook)
  {
    hookAllConstructors(classToHook, new XC_MethodHook(XC_MethodHook.PRIORITY_HIGHEST)
    {
      @Override
      protected void afterHookedMethod(MethodHookParam param) throws Throwable
      {
        ClassHook<ClassToHook> hook = (ClassHook<ClassToHook>)newInstance(classHook, param.thisObject);
        setAdditionalInstanceField(param.thisObject, CLASSHOOK_KEY, hook);
      }
    });
    log("hooked " + classToHook.getName() + " all_constructors");
    installHooks(classToHook, classHook);
  }
  
////////////////////////////////////////////////////////////

  @SuppressWarnings("unchecked")
  public static <ClassToHook, T extends ClassHook<ClassToHook>> T getHook(ClassToHook o)
  {
    return (T)getAdditionalInstanceField(o, CLASSHOOK_KEY);
  }
  
////////////////////////////////////////////////////////////
  
}

////////////////////////////////////////////////////////////
