package com.gem.xperiaxposed.systemui;

import static com.gem.xperiaxposed.Constants.*;
import static com.gem.xperiaxposed.XposedMain.*;

import java.util.HashSet;
import java.util.Set;

import android.content.res.XResources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.gem.xperiaxposed.R;
import com.gem.xposed.ModuleResources;

import de.robv.android.xposed.callbacks.XC_LayoutInflated;

////////////////////////////////////////////////////////////

public class SystemUIResources
{

////////////////////////////////////////////////////////////
  
  public static final int SYSTEM_UI_TRANSPARENT_BACKGROUND      = 0x99000000;
  public static final int SYSTEM_UI_OPAQUE_BACKGROUND           = 0xff000000;
  public static final int SYSTEM_UI_LIGHT_BACKGROUND            = 0xff4d4d4d;
  
  public static int SYSTEM_UI_FLAG_TRANSPARENT = 0;
  public static int SYSTEM_UI_FLAG_FULL_TRANSPARENCY = 0;
  public static int SYSTEM_UI_FLAG_LIGHT = 0;
  public static int SYSTEM_UI_FLAG_ROUNDED_CORNERS = 0;
  public static int SYSTEM_UI_FLAG_DISABLE_ROUNDED_CORNERS = 0;
  public static int SYSTEM_UI_FLAG_SUPPRESS_NAVIGATION = 0;
  
  public static void initFlags()
  {
    SYSTEM_UI_FLAG_TRANSPARENT                  = getFlag("SYSTEM_UI_FLAG_TRANSPARENT");
    SYSTEM_UI_FLAG_FULL_TRANSPARENCY            = getFlag("SYSTEM_UI_FLAG_FULL_TRANSPARENCY");
    SYSTEM_UI_FLAG_LIGHT                        = getFlag("SYSTEM_UI_FLAG_LIGHT");
    SYSTEM_UI_FLAG_ROUNDED_CORNERS              = getFlag("SYSTEM_UI_FLAG_ROUNDED_CORNERS");
    SYSTEM_UI_FLAG_DISABLE_ROUNDED_CORNERS      = getFlag("SYSTEM_UI_FLAG_DISABLE_ROUNDED_CORNERS");
    SYSTEM_UI_FLAG_SUPPRESS_NAVIGATION          = getFlag("SYSTEM_UI_FLAG_SUPPRESS_NAVIGATION");
  }
  
  private static int getFlag(String flag)
  {
    try
    {
      return View.class.getField(flag).getInt(null);
    }
    catch(Throwable ex)
    {
      return 0;
    }
  }

////////////////////////////////////////////////////////////

  public static void updateResources(final XResources pres, final ModuleResources res)
  {
    boolean status_bar_custom = "custom".equals(prefs.getString("key_systemui_status_color_set", "system"));
    boolean nav_bar_custom = "custom".equals(prefs.getString("key_systemui_nav_color_set", "status"));
    boolean nav_bar_same = "status".equals(prefs.getString("key_systemui_nav_color_set", "status"));
    
    int system_ui_transparent_background = prefs.getInt("key_systemui_translucent_background", SYSTEM_UI_TRANSPARENT_BACKGROUND);
    int system_ui_opaque_background = prefs.getInt("key_systemui_dark_background", SYSTEM_UI_OPAQUE_BACKGROUND);
    int system_ui_light_background = prefs.getInt("key_systemui_light_background", SYSTEM_UI_LIGHT_BACKGROUND);
    int system_ui_nav_transparent_background = system_ui_transparent_background;
    int system_ui_nav_opaque_background = system_ui_opaque_background;
    int system_ui_nav_light_background = system_ui_light_background;
    if(!nav_bar_same)
    {
      system_ui_nav_transparent_background = prefs.getInt("key_systemui_nav_translucent_background", SYSTEM_UI_TRANSPARENT_BACKGROUND);
      system_ui_nav_opaque_background = prefs.getInt("key_systemui_nav_dark_background", SYSTEM_UI_OPAQUE_BACKGROUND);
      system_ui_nav_light_background = prefs.getInt("key_systemui_nav_light_background", SYSTEM_UI_LIGHT_BACKGROUND);
    }
    
    if(status_bar_custom)
    {
      pres.setReplacement(SYSTEMUI, "color", "system_ui_transparent_background", system_ui_transparent_background);
      pres.setReplacement(SYSTEMUI, "color", "system_ui_opaque_background", system_ui_opaque_background);
      pres.setReplacement(SYSTEMUI, "color", "system_ui_light_background", system_ui_light_background);
      pres.setReplacement(SYSTEMUI, "drawable", "status_bar_opaque_background", system_ui_opaque_background);
      pres.setReplacement(SYSTEMUI, "drawable", "status_bar_transparent_background", system_ui_transparent_background);
      pres.setReplacement(SYSTEMUI, "drawable", "status_bar_light_background", system_ui_light_background);
      pres.setReplacement(SYSTEMUI, "drawable", "status_bar_lights_out_background", system_ui_opaque_background);
    }

    if(nav_bar_custom || (status_bar_custom && nav_bar_same))
    {
      pres.setReplacement(SYSTEMUI, "drawable", "navigation_bar_opaque_background", system_ui_nav_opaque_background);
      pres.setReplacement(SYSTEMUI, "drawable", "navigation_bar_opaque_background_land", system_ui_nav_opaque_background);
      pres.setReplacement(SYSTEMUI, "drawable", "navigation_bar_transparent_background", system_ui_nav_transparent_background);
      pres.setReplacement(SYSTEMUI, "drawable", "navigation_bar_transparent_background_land", system_ui_nav_transparent_background);
      pres.setReplacement(SYSTEMUI, "drawable", "navigation_bar_light_background", system_ui_nav_light_background);
      pres.setReplacement(SYSTEMUI, "drawable", "navigation_bar_light_background_land", system_ui_nav_light_background);
      pres.setReplacement(SYSTEMUI, "drawable", "navigation_bar_lights_out_background", system_ui_nav_opaque_background);
      pres.setReplacement(SYSTEMUI, "drawable", "navigation_bar_lights_out_background_land", system_ui_nav_opaque_background);
    }
    
    Set<String> status_gradient = prefs.getStringSet("key_systemui_status_gradient", new HashSet<String>());
    final int status_gradient_visible = (status_gradient.size() == 4) ? View.VISIBLE : View.GONE;
    if(!status_gradient.isEmpty())
    {
      pres.setReplacement(Ids.status_background, res.fwd(R.drawable.status_background));
      
      pres.hookLayout("com.android.systemui", "layout", "status_bar", new XC_LayoutInflated()
      {
        @Override
        public void handleLayoutInflated(LayoutInflatedParam param) throws Throwable
        {
          FrameLayout statusBar = (FrameLayout)param.view;
          View gradient = new View(statusBar.getContext());
          gradient.setId(Ids.status_bar_gradient_view);
          gradient.setBackgroundResource(Ids.status_background);
          gradient.setVisibility(status_gradient_visible);
          statusBar.addView(gradient, 1, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        }
      });
    }
    
    Set<String> nav_gradient = prefs.getStringSet("key_systemui_nav_gradient", new HashSet<String>());
    final int nav_gradient_visible = (nav_gradient.size() == 4) ? View.VISIBLE : View.GONE;
    if(!nav_gradient.isEmpty())
    {
      pres.setReplacement(Ids.nav_background, res.fwd(R.drawable.nav_background));
      pres.setReplacement(Ids.nav_background_land, res.fwd(R.drawable.nav_background_land));
      
      pres.hookLayout("com.android.systemui", "layout", "navigation_bar", new XC_LayoutInflated()
      {
        @Override
        public void handleLayoutInflated(LayoutInflatedParam param) throws Throwable
        {
          ViewGroup navBar = (ViewGroup)param.view;
          FrameLayout portNavBar = (FrameLayout)navBar.getChildAt(0);
          FrameLayout landNavBar = (FrameLayout)navBar.getChildAt(1);

          View gradient = new View(navBar.getContext());
          gradient.setId(Ids.navigation_bar_gradient_view);
          gradient.setBackgroundResource(Ids.nav_background);
          gradient.setVisibility(nav_gradient_visible);
          portNavBar.addView(gradient, 1, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
          
          gradient = new View(navBar.getContext());
          gradient.setId(Ids.navigation_bar_gradient_view_land);
          gradient.setBackgroundResource(Ids.nav_background_land);
          gradient.setVisibility(nav_gradient_visible);
          landNavBar.addView(gradient, 1, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        }
      });
    }
  }

////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////
