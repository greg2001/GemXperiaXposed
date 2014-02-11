package com.gem.xperiaxposed.home;

import java.util.*;

import android.content.*;
import android.os.*;
import android.text.*;

import com.sonymobile.flix.debug.*;
import com.sonymobile.home.badge.*;

public class MissedItReceiver extends BroadcastReceiver
{
  private BadgeManager badgeManager;
  private Map<String, Integer> gmail = new HashMap<String, Integer>();
  private Map<String, Integer> k9mail = new HashMap<String, Integer>();
  private Map<ComponentName, Integer> apps = new HashMap<ComponentName, Integer>();
  
  public MissedItReceiver(Context context, BadgeManager badgeManager)
  {
    this.badgeManager = badgeManager;
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction("net.igecelabs.android.MissedIt.COUNTERS_STATUS");
    intentFilter.addAction("net.igecelabs.android.MissedIt.CALL_NOTIFICATION");
    intentFilter.addAction("net.igecelabs.android.MissedIt.SMS_NOTIFICATION");
    intentFilter.addAction("net.igecelabs.android.MissedIt.VOICEMAIL_NOTIFICATION");
    intentFilter.addAction("net.igecelabs.android.MissedIt.GMAIL_NOTIFICATION");
    intentFilter.addAction("net.igecelabs.android.MissedIt.K9MAIL_NOTIFICATION");
    intentFilter.addAction("net.igecelabs.android.MissedIt.APP_NOTIFICATION");
    context.registerReceiver(this, intentFilter);
    try
    {
      context.startService(new Intent("net.igecelabs.android.MissedIt.action.REQUEST_COUNTERS"));
    }
    catch(Exception ex)
    {
      Logx.e(ex);
    }
  }

  public void onReceive(Context context, Intent intent)
  {
    try
    {
      if(intent == null)
        return;
  
      String action = intent.getAction();
      if("net.igecelabs.android.MissedIt.COUNTERS_STATUS".equals(action))
      {
        /*
        int calls = intent.getIntExtra("CALLS", 0);
        int sms = intent.getIntExtra("SMS", 0);
        int voicemails = intent.getIntExtra("VOICEMAILS", 0);
        */
        
        Bundle b1 = intent.getBundleExtra("GMAIL_ACCOUNTS");
        if(b1 != null)
        {
          for(String key: b1.keySet())
          {
            Bundle b2 = b1.getBundle(key);
            if(b2 != null)
            {
              String account = b2.getString("ACCOUNT");
              String label = b2.getString("LABEL");
              if(!TextUtils.isEmpty(account) && !TextUtils.isEmpty(label))
              {
                int count = b2.getInt("COUNT", 0);
                gmail.put(account + "/" + label, count);
              }
            }
          }
        }
        
        b1 = intent.getBundleExtra("K9MAIL_ACCOUNTS");
        if(b1 != null)
        {
          for(String key: b1.keySet())
          {
            Bundle b2 = b1.getBundle(key);
            if(b2 != null)
            {
              String account = b2.getString("ACCOUNT");
              String label = b2.getString("LABEL");
              if(!TextUtils.isEmpty(account) && !TextUtils.isEmpty(label))
              {
                int count = b2.getInt("COUNT", 0);
                k9mail.put(account + "/" + label, count);
              }
            }
          }
        }
        
        b1 = intent.getBundleExtra("APPLICATIONS");
        if(b1 != null)
        {
          for(String key: b1.keySet())
          {
            Bundle b2 = b1.getBundle(key);
            if(b2 != null)
            {
              String name = b2.getString("COMPONENTNAME");
              if(!TextUtils.isEmpty(name))
              {
                try
                {
                  ComponentName comp = ComponentName.unflattenFromString(name);
                  int count = b2.getInt("COUNT", 0);
                  apps.put(comp, count);
                }
                catch(Exception ex)
                {
                }
              }
            }
          }
        }
      }
      else if("net.igecelabs.android.MissedIt.CALL_NOTIFICATION".equals(action))
      {
        // int calls = intent.getIntExtra("COUNT", 0);
      }
      else if("net.igecelabs.android.MissedIt.SMS_NOTIFICATION".equals(action))
      {
        // int sms = intent.getIntExtra("COUNT", 0);
      }
      else if("net.igecelabs.android.MissedIt.VOICEMAIL_NOTIFICATION".equals(action))
      {
        // int voicemails = intent.getIntExtra("COUNT", 0);
      }
      else if("net.igecelabs.android.MissedIt.GMAIL_NOTIFICATION".equals(action))
      {
        String account = intent.getStringExtra("ACCOUNT");
        String label = intent.getStringExtra("LABEL");
        if(!TextUtils.isEmpty(account) && !TextUtils.isEmpty(label))
        {
          int count = intent.getIntExtra("COUNT", 0);
          gmail.put(account + "/" + label, count);
        }
      }
      else if("net.igecelabs.android.MissedIt.K9MAIL_NOTIFICATION".equals(action))
      {
        String account = intent.getStringExtra("ACCOUNT");
        String label = intent.getStringExtra("LABEL");
        if(!TextUtils.isEmpty(account) && !TextUtils.isEmpty(label))
        {
          int count = intent.getIntExtra("COUNT", 0);
          k9mail.put(account + "/" + label, count);
        }
      }
      else if("net.igecelabs.android.MissedIt.APP_NOTIFICATION".equals(action))
      {
        String name = intent.getStringExtra("COMPONENTNAME");
        if(!TextUtils.isEmpty(name))
        {
          try
          {
            ComponentName comp = ComponentName.unflattenFromString(name);
            int count = intent.getIntExtra("COUNT", 0);
            apps.put(comp, count);
          }
          catch(Exception ex)
          {
          }
        }
      }
      
      ComponentName gmailName = new ComponentName("com.google.android.gm", "com.google.android.gm.ConversationListActivityGmail");
      int gmailCount = 0;
      for(int c: gmail.values())
        gmailCount += c;
      apps.put(gmailName, gmailCount);
      
      ComponentName k9mailName = new ComponentName("com.fsck.k9", "com.fsck.k9.activity.Accounts");
      int k9mailCount = 0;
      for(int c: k9mail.values())
        k9mailCount += c;
      apps.put(k9mailName, k9mailCount);
      
      for(Map.Entry<ComponentName, Integer> e: apps.entrySet())
      {
        Intent i = new Intent();
        i.putExtra("com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME", e.getKey().getPackageName());
        i.putExtra("com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME", e.getKey().getClassName());
        i.putExtra("com.sonyericsson.home.intent.extra.badge.MESSAGE", Integer.toString(e.getValue()));
        badgeManager.onReceive(i);
      }
    }
    catch(Exception ex)
    {
      Logx.e(ex);
    }
  }
}
