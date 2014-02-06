package com.gem.util;

import android.content.*;
import android.content.res.*;
import android.os.*;
import android.preference.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import com.gem.xperiaxposed.*;

public class GemSliderPreference extends Preference implements SeekBar.OnSeekBarChangeListener, View.OnClickListener, Runnable
{
  private int minimum = 0;
  private int maximum = 100;
  private int step = 1;
  private int defaultValue = minimum;
  private String currentValueFormat = "%d";

  private TextView currentValueTextView;
  private SeekBar slider;
  private Button plusButton;
  private Button minusButton;

  private int currentValue;
  private int tmpValue;
  
  private boolean tracking = false;
  private boolean repeating = false;
  private Handler handler = new Handler();

  public GemSliderPreference(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    if(attrs != null)
    {
      minimum = attrs.getAttributeIntValue(null, "minimum", 0);
      maximum = attrs.getAttributeIntValue(null, "maximum", 100);
      step = attrs.getAttributeIntValue(null, "step", 1);
      defaultValue = minimum;
      currentValueFormat = attrs.getAttributeValue(null, "currentValueFormat");
    }
  }

  @Override
  protected View onCreateView(ViewGroup parent)
  {
    View layout = View.inflate(getContext(), R.layout.slider_preference, null);
    currentValueTextView = (TextView)layout.findViewById(R.id.slider_preference_current);
    currentValueTextView.setVisibility((currentValueFormat != null && !currentValueFormat.isEmpty()) ? View.VISIBLE : View.GONE);
    slider = (SeekBar)layout.findViewById(R.id.slider_preference_slider);
    slider.setMax(maximum - minimum);
    slider.setOnSeekBarChangeListener(this);
    slider.setProgress(currentValue - minimum);
    plusButton = (Button)layout.findViewById(R.id.slider_preference_plus);
    plusButton.setOnClickListener(this);
    minusButton = (Button)layout.findViewById(R.id.slider_preference_minus);
    minusButton.setOnClickListener(this);
    updateCurrentValueText(currentValue);
    return layout;
  }

  @Override
  protected Object onGetDefaultValue(TypedArray a, int index)
  {
    return a.getInt(index, defaultValue);
  }

  @Override
  protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
  {
    updateValue(restoreValue ? getPersistedInt(currentValue) : (Integer)defaultValue);
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
  {
    if(fromUser)
    {
      progress = Math.round(((float)progress) / step) * step + minimum;
      if(tracking)
        updateCurrentValueText(progress);
      else
        updateValue(progress);
    }
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar)
  {
    tracking = true;
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar)
  {
    tracking = false;
    onProgressChanged(seekBar, seekBar.getProgress(), true);
  }

  @Override
  public void onClick(View v)
  {
    if(repeating)
      handler.removeCallbacks(this);
    else
    {
      repeating = true;
      tmpValue = currentValue;
    }
    handler.postDelayed(this, 500);

    tmpValue = (v == plusButton) ? Math.min(tmpValue+step, maximum) : Math.max(tmpValue-step, minimum);
    slider.setProgress(tmpValue - minimum);
    updateCurrentValueText(tmpValue);
  }
    
  @Override
  public void run()
  {
    repeating = false;
    updateValue(tmpValue);
  }

  private void updateValue(int value)
  {
    currentValue = value;
    persistInt(currentValue);
    if(slider != null)
    {
      slider.setProgress(currentValue - minimum);
      updateCurrentValueText(currentValue);
    }
  }

  private void updateCurrentValueText(int value)
  {
    if(currentValueFormat != null && !currentValueFormat.isEmpty())
      currentValueTextView.setText(String.format(currentValueFormat, value));
  }
}