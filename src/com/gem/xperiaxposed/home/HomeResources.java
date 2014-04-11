package com.gem.xperiaxposed.home;

import static com.gem.xperiaxposed.Constants.*;
import static com.gem.xperiaxposed.XposedMain.*;
import android.content.res.XResources;

import com.gem.xperiaxposed.R;
import com.gem.xposed.ModuleResources;

////////////////////////////////////////////////////////////

public class HomeResources
{

////////////////////////////////////////////////////////////

  public static void updateResources(XResources pres, ModuleResources res)
  {
    int desktopRows = Integer.parseInt(prefs.getString("key_desktop_rows", "4"));
    if(desktopRows != 4)
    {
//    unfortunately, way too small 
//    float height = pres.getDimension(pres.getIdentifier("desktop_cell_height", "dimen", SE_HOME));
      float height = res.getDimension(R.dimen.desktop_cell_height);
      height *= 4.0/desktopRows;
      res.setDimension(R.dimen.desktop_cell_height, height);

      pres.setReplacement(SE_HOME, "integer", "desktop_grid_rows", desktopRows);
      pres.setReplacement(SE_HOME, "dimen", "desktop_cell_height", res.fwd(R.dimen.desktop_cell_height));
    }

    int desktopColumns = Integer.parseInt(prefs.getString("key_desktop_columns", "4"));
    if(desktopColumns != 4)
    {
      float width = pres.getDimension(pres.getIdentifier("desktop_cell_width", "dimen", SE_HOME));
      width *= 4.0/desktopColumns;
      res.setDimension(R.dimen.desktop_cell_width, width);

      pres.setReplacement(SE_HOME, "integer", "desktop_grid_columns", desktopColumns);
      pres.setReplacement(SE_HOME, "dimen", "desktop_cell_width", res.fwd(R.dimen.desktop_cell_width));
    }
    
    if(prefs.getBoolean("key_desktop_multiline_labels", false))
      pres.setReplacement(SE_HOME, "bool", "enable_desktop_multi_line_labels", true);
    if(prefs.getBoolean("key_desktop_autohide_pagination", false))
      pres.setReplacement(SE_HOME, "bool", "desktop_pagination_autohide", true);

    int dockColumns = Integer.parseInt(prefs.getString("key_dock_columns", "5"));
    if(dockColumns != 5)
    {
      float width = pres.getDimension(pres.getIdentifier("stage_cell_width", "dimen", SE_HOME));
      width *= 5.0/dockColumns;
      res.setDimension(R.dimen.stage_cell_width, width);

      pres.setReplacement(SE_HOME, "integer", "stage_grid_columns", dockColumns);
      pres.setReplacement(SE_HOME, "integer", "max_stage_items", dockColumns-1);
      pres.setReplacement(SE_HOME, "dimen", "stage_cell_width", res.fwd(R.dimen.stage_cell_width));
      pres.setReplacement(SE_HOME, "dimen", "stage_cell_height", res.fwd(R.dimen.stage_cell_height));
    }
    
    int folderColumns = Integer.parseInt(prefs.getString("key_folder_columns", "4"));
    if(folderColumns != 4)
    {
      float width = pres.getDimension(pres.getIdentifier("open_folder_cell_width", "dimen", SE_HOME));
      width *= 4.0/folderColumns;
      res.setDimension(R.dimen.open_folder_cell_width, width);
      pres.setReplacement(SE_HOME, "dimen", "open_folder_cell_width", res.fwd(R.dimen.open_folder_cell_width));
    }

    int drawerRows = Integer.parseInt(prefs.getString("key_drawer_rows", "5"));
    if(drawerRows != 5)
    {
//    unfortunately, way too small 
//    float height = pres.getDimension(pres.getIdentifier("apptray_cell_height", "dimen", SE_HOME));
      float height = res.getDimension(R.dimen.apptray_cell_height);
      height *= 5.0/drawerRows;
      res.setDimension(R.dimen.apptray_cell_height, height);

      pres.setReplacement(SE_HOME, "integer", "apptray_grid_rows", drawerRows);
      pres.setReplacement(SE_HOME, "dimen", "apptray_cell_height", res.fwd(R.dimen.apptray_cell_height));
    }

    int drawerColumns = Integer.parseInt(prefs.getString("key_drawer_columns", "4"));
    if(drawerColumns != 4)
    {
      float width = pres.getDimension(pres.getIdentifier("apptray_cell_width", "dimen", SE_HOME));
      width *= 4.0/drawerColumns;
      res.setDimension(R.dimen.apptray_cell_width, width);

      pres.setReplacement(SE_HOME, "integer", "apptray_grid_columns", drawerColumns);
      pres.setReplacement(SE_HOME, "dimen", "apptray_cell_width", res.fwd(R.dimen.apptray_cell_width));
    }
    
/*    
    int iconSize = prefs.getInt("key_launcher_icon_size", 100);
    if(iconSize != 100)
    {
      float width = pres.getDimension(pres.getIdentifier("icon_image_width", "dimen", SE_HOME));
      width *= iconSize/100.0;
      res.setDimension(R.dimen.icon_image_width, width);
      
      float height = pres.getDimension(pres.getIdentifier("icon_image_height", "dimen", SE_HOME));
      height *= iconSize/100.0;
      res.setDimension(R.dimen.icon_image_height, height);
      
      pres.setReplacement(SE_HOME, "dimen", "icon_image_width", res.fwd(R.dimen.icon_image_height));
      pres.setReplacement(SE_HOME, "dimen", "icon_image_height", res.fwd(R.dimen.icon_image_height));
    }
*/
    
    int textSize = prefs.getInt("key_launcher_label_text_size", 100);
    if(textSize != 100)
    {
      float size = pres.getDimension(pres.getIdentifier("icon_label_text_size", "dimen", SE_HOME));
      size *= textSize/100.0;
      res.setDimension(R.dimen.icon_label_text_size, size);
      
      pres.setReplacement(SE_HOME, "dimen", "icon_label_text_size", res.fwd(R.dimen.icon_label_text_size));
      pres.setReplacement(SE_HOME, "dimen", "desktop_icon_label_max_text_size", res.fwd(R.dimen.icon_label_text_size));
      pres.setReplacement(SE_HOME, "dimen", "apptray_icon_label_max_text_size", res.fwd(R.dimen.icon_label_text_size));
    }
    
    if(prefs.getBoolean("key_large_dock_reflection", false))
    {
      float size = pres.getDimension(pres.getIdentifier("stage_mirror_size", "dimen", SE_HOME));
      size *= 2;
      res.setDimension(R.dimen.stage_mirror_size, size);

      pres.setReplacement(SE_HOME, "dimen", "stage_mirror_size", res.fwd(R.dimen.stage_mirror_size));
    }

    if(prefs.getBoolean("key_enable_experimental", false))
    {
      pres.setReplacement(Ids.home_apptray_dropzone_hide, res.fwd(R.drawable.home_apptray_dropzone_hide));
      pres.setReplacement(Ids.home_apptray_dropzone_unhide, res.fwd(R.drawable.home_apptray_dropzone_unhide));
      pres.setReplacement(Ids.app_tray_drawer_list_item_categories_hidden, res.fwd(R.string.app_tray_drawer_list_item_categories_hidden));
      pres.setReplacement(Ids.app_tray_drawer_list_item_categories_settings, res.fwd(R.string.app_tray_drawer_list_item_categories_settings));
      pres.setReplacement(Ids.drawer_icn_hidden, res.fwd(R.drawable.drawer_icn_hidden));
      pres.setReplacement(Ids.drawer_icn_settings, res.fwd(R.drawable.drawer_icn_settings));
    }
  }

////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////
