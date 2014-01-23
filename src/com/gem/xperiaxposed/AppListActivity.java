package com.gem.xperiaxposed;

import static com.gem.xperiaxposed.Util.*;

import java.util.*;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.drawable.*;
import android.os.*;
import android.preference.*;

////////////////////////////////////////////////////////////

public class AppListActivity extends Activity 
{
  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    makeSharedPreferencesWorldReadable(this);
     
    getFragmentManager()
      .beginTransaction()
      .replace(android.R.id.content, new AppListFragment())
      .commit();
  }
}

////////////////////////////////////////////////////////////

class AppListFragment extends PreferenceFragment
{
  public static final String PREFIX = "\u2714 ";
  
@Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setPreferenceScreen(getPreferenceManager().createPreferenceScreen(getActivity()));
    new LoadAppsTask().execute();
  }
  
  @Override
  public void onResume()
  {
    super.onResume();
    updateAppState();
  }
  
////////////////////////////////////////////////////////////
  
  private void updateAppState()
  {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    for(int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i)
    {
      Preference p = getPreferenceScreen().getPreference(i);
      String title = p.getTitle().toString();
      if("Default".equals(prefs.getString("key_systemui_app_color$" + p.getKey(), "Default")) &&
         "Default".equals(prefs.getString("key_systemui_app_rounded_borders$" + p.getKey(), "Default")))
      {
        if(title.startsWith(PREFIX))
          p.setTitle(title.substring(PREFIX.length()));
      }
      else
      {
        if(!title.startsWith(PREFIX))
          p.setTitle(PREFIX + title);
      }
    }
  }
  
////////////////////////////////////////////////////////////

  private class LoadAppsTask extends AsyncTask<Void, Void, Void>
  {
    private ProgressDialog dialog;
    private ArrayList<ApplicationInfo> appList = new ArrayList<ApplicationInfo>();

    @Override
    protected void onPreExecute()
    {
      dialog = new ProgressDialog(getActivity());
      dialog.setMessage(getString(R.string.app_list_loading));
      dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
      dialog.setCancelable(false);
      dialog.show();
    }

    @Override
    protected Void doInBackground(Void... params)
    {
      PackageManager pm = getActivity().getPackageManager();
      List<PackageInfo> pkgs = pm.getInstalledPackages(0);
      dialog.setMax(pkgs.size());
      
      int i = 0;
      for(PackageInfo pkgInfo : pkgs)
      {
        dialog.setProgress(++i);

        ApplicationInfo appInfo = pkgInfo.applicationInfo;
        if(appInfo != null)
        {
          appInfo.name = appInfo.loadLabel(pm).toString();
          appList.add(appInfo);
        }
      }

      Collections.sort(appList, new Comparator<ApplicationInfo>()
      {
        @Override
        public int compare(ApplicationInfo lhs, ApplicationInfo rhs)
        {
          return (lhs.name == null) ? -1 : (rhs.name == null) ? 1 : lhs.name.toUpperCase().compareTo(rhs.name.toUpperCase());
        }
      });
      return null;
    }

    @Override
    protected void onPostExecute(Void result)
    {
      for(final ApplicationInfo info: appList)
      {
        final Preference p = new Preference(getActivity());
        p.setKey(info.packageName);
        p.setTitle(info.name);
        p.setSummary(info.packageName);
        p.setIntent(new Intent(getActivity(), AppSettingsActivity.class).putExtra("info", info));
        new AsyncTask<Void, Void, Drawable>()
        {
          @Override
          protected Drawable doInBackground(Void... params) 
          {
            return info.loadIcon(getActivity().getPackageManager());
          }
          @Override
          protected void onPostExecute(Drawable result)
          {
            p.setIcon(result);
          }
        }.execute();
        getPreferenceScreen().addPreference(p);
      }
      updateAppState();
      dialog.dismiss();
    }
  }

////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////
