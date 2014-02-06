package com.gem.xperiaxposed;

import java.util.*;

import android.os.*;
import android.support.v4.app.*;
import android.support.v4.view.*;
import android.view.*;
import android.widget.*;

import com.gem.util.*;
import com.viewpagerindicator.*;

public class ExperimentalTutorialActivity extends GemActivity
{
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    
    setContentView(R.layout.experimental_tutorial);
    ViewPager pager = (ViewPager)findViewById(R.id.experimental_tutorial_pager);
    pager.setAdapter(new PageAdapter(getSupportFragmentManager(), getFragments()));
    
    CirclePageIndicator circleIndicator = (CirclePageIndicator)findViewById(R.id.experimental_tutorial_circles);
    circleIndicator.setViewPager(pager);    
  }    
  
  private List<Fragment> getFragments()
  {
    List<Fragment> list = new ArrayList<Fragment>();
    list.add(PageFragment.newInstance(R.drawable.experimental_settings, R.string.experimental_settings));
    list.add(PageFragment.newInstance(R.drawable.experimental_alphabetical_badge, R.string.experimental_alphabetical_badge));
    list.add(PageFragment.newInstance(R.drawable.experimental_hide_app, R.string.experimental_hide_app));
    list.add(PageFragment.newInstance(R.drawable.experimental_hidden_apps, R.string.experimental_hidden_apps));
    list.add(PageFragment.newInstance(R.drawable.experimental_unhide_app, R.string.experimental_unhide_app));
    return list;
  }
  
  private class PageAdapter extends FragmentPagerAdapter
  {
    private List<Fragment> fragments;

    public PageAdapter(FragmentManager fm, List<Fragment> fragments)
    {
      super(fm);
      this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position)
    {
      return this.fragments.get(position);
    }
/*    
    @Override
    public String getPageTitle(int position)
    {
      return getResources().getString(((PageFragment)getItem(position)).getTitle());
    }
*/
    @Override
    public int getCount()
    {
      return this.fragments.size();
    }
  }
}

class PageFragment extends Fragment
{
  public static final String EXTRA_IMAGE = "EXTRA_IMAGE";
  public static final String EXTRA_TEXT = "EXTRA_TEXT";

  static final PageFragment newInstance(int image, int text)
  {
    PageFragment f = new PageFragment();
    Bundle bdl = new Bundle(2);
    bdl.putInt(EXTRA_IMAGE, image);
    bdl.putInt(EXTRA_TEXT, text);
    f.setArguments(bdl);
    return f;
  }
  
  public int getTitle()
  {
    return getArguments().getInt(EXTRA_TEXT);
  }

  public int getImage()
  {
    return getArguments().getInt(EXTRA_IMAGE);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View v = inflater.inflate(R.layout.tutorial_page, container, false);
    ((TextView)v.findViewById(R.id.tutorial_page_text)).setText(getTitle());
    ((ImageView)v.findViewById(R.id.tutorial_page_image)).setImageResource(getImage());
    return v;
  }
}

