package com.gem.xperiaxposed;

import java.util.*;

import android.content.res.*;
import android.os.*;
import android.support.v4.app.*;
import android.support.v4.view.*;
import android.view.*;
import android.widget.*;

import com.gem.util.*;
import com.viewpagerindicator.*;

public class TutorialActivity extends GemActivity
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
    TypedArray titles = getResources().obtainTypedArray(getResources().getIdentifier(getIntent().getStringExtra("titles"), "array", getPackageName()));
    TypedArray drawables = getResources().obtainTypedArray(getResources().getIdentifier(getIntent().getStringExtra("drawables"), "array", getPackageName()));
    
    List<Fragment> list = new ArrayList<Fragment>();
    for(int i = 0; i < titles.length(); ++i)
      list.add(PageFragment.newInstance(drawables.getResourceId(i, 0), titles.getString(i)));
    
    titles.recycle();
    drawables.recycle();
    
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
      return ((PageFragment)getItem(position)).getTitle();
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

  static final PageFragment newInstance(int image, String text)
  {
    PageFragment f = new PageFragment();
    Bundle bdl = new Bundle(2);
    bdl.putInt(EXTRA_IMAGE, image);
    bdl.putString(EXTRA_TEXT, text);
    f.setArguments(bdl);
    return f;
  }
  
  public String getTitle()
  {
    return getArguments().getString(EXTRA_TEXT);
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

