package com.dindon.jppower;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dindon.jppower.utils.SendData;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.dindon.jppower.R.id.iv_control;
import static com.dindon.jppower.R.id.iv_generator;
import static java.lang.StrictMath.abs;

/**
 * Created by JimmyTai on 2018/1/28.
 */

public class HomeFragment extends Fragment {

    private static final String TAG = HomeFragment.class.getSimpleName();
    private MainActivity activity;

    private int modeState = 0;
    private int modeState_pre = 0;
    private float[] tramPower = {0, 500, 400, 200, 0, 400, 0, 100, 0, 0, 0, 0, 200, 200, 0, 0, 0, 0, 0, 0};
    private float[] renewablePower = {0, 0, 100, 300, 300, 300, 300, 300, 300, 200, 0, 300, 300, 300, 200, 300, 100, 100, 200, 100};
    private float[] generatorPower = {0, 0, 0, 0, 0, 0, 0, 100, 0, 0, 300, 300, 0, 0, 100, 0, 0, 200, 100, 200};
    private float[] socPower = {0, 0, 0, 0, 200, -200, 0, 0, 0, 100, 0, -300, 0, 0, 0, 0, 200, 0, 0, 0, 0};
    private float[] buildPower = {0, 500, 500, 500, 500, 500, 300, 300, 300, 300, 300, 300, 500, 500, 300, 300, 300, 300, 300, 300};

    Timer batteryTimer;
    Timer chartTimer;
    Timer iconTimer;
    boolean iconState = false;
    int batteryLevel = 0;
    float timeCount = 0;
    boolean timerState = false;

    SendData sendData = new SendData();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        activity = ((MainActivity) getActivity());
        activity.setSendData(sendData);
        unbinder = ButterKnife.bind(this, v);
        initViews();
        mode_Rec = 0;
        timerState = false;
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
        unbinder.unbind();
        if (batteryTimer != null)
            batteryTimer.cancel();
        if (chartTimer != null)
            chartTimer.cancel();
        if (iconTimer != null)
            iconTimer.cancel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (batteryTimer != null)
            batteryTimer.cancel();
        if (chartTimer != null)
            chartTimer.cancel();
        if (iconTimer != null)
            iconTimer.cancel();
    }

    private int mode_Rec = 0;

    private class SendData implements com.dindon.jppower.utils.SendData {

        @Override
        public void onDataListener(int mode) {
            Log.d(TAG, "mode_Rec mode: " + mode_Rec + "/" + mode);
            if (mode != mode_Rec && !timerState) {
                Log.d(TAG, "onDataListener mode: " + mode);
                mode_Rec = mode;
                modeState = mode_Rec;
                setModeAct(modeState);
            }
        }
    }

    /* --- Views --- */

    private void initViews() {
        modeState = activity.getMode();

        Legend legend = chart.getLegend();
        legend.setFormSize(20f);
        legend.setXEntrySpace(20f);

//        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
//        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);

        legend.setTextColor(Color.rgb(255, 255, 255));
        legend.setDrawInside(false);
        legend.setForm(Legend.LegendForm.LINE);

        chart.setDoubleTapToZoomEnabled(false);
        chart.setHighlightPerTapEnabled(false);
        chart.setHighlightPerDragEnabled(false);

        chart.getDescription().setEnabled(false);
        chart.setDrawBorders(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(11, true);
        xAxis.setAxisMinimum(10f);
        xAxis.setAxisMinimum(0f);
        xAxis.setTextColor(Color.rgb(255, 255, 255));
        xAxis.setAxisLineColor(Color.rgb(120, 120, 120));
        xAxis.setGridColor(Color.rgb(120, 120, 120));
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.valueOf(Integer.valueOf((int) value % 60));
//                return value == 0 ? String.valueOf(Integer.valueOf((int) value)) : "";
            }
        });

        YAxis leftAxis = chart.getAxisLeft();
        chart.getAxisRight().setEnabled(false);
        leftAxis.setAxisMaximum(520f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.mAxisRange = 100f;
        leftAxis.setTextColor(Color.rgb(255, 255, 255));
        leftAxis.setGridColor(Color.rgb(120, 120, 120));
        leftAxis.setAxisLineColor(Color.rgb(120, 120, 120));
        chartTimer = new Timer(true);
        chartTimer.schedule(new chartTimerTask(), 0, 1000);
    }

//    public void tramAnimStart() {
//        ObjectAnimator animator = ObjectAnimator.ofFloat(ic_energy, "translationX", 0, 200, -200,200);
//        animator.setDuration(5000);
//        animator.start();
//    }

    ArrayList<Entry> tramPowerValues = new ArrayList<>();
    ArrayList<Entry> generatorPowerValues = new ArrayList<>();
    ArrayList<Entry> renewablePowerValues = new ArrayList<>();
    ArrayList<Entry> socPowerValues = new ArrayList<>();
    ArrayList<Entry> buildPowerValues = new ArrayList<>();

    int[] checkStateTime = {0, 0, 0, 0, 0};

    public class chartTimerTask extends TimerTask {
        public void run() {
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            LineDataSet d;

            if (tramPower[modeState] == tramPower[modeState_pre])
                checkStateTime[0]++;
            else
                checkStateTime[0] = 0;
            if (tramPower[modeState] == tramPower[modeState_pre] && timeCount >= 2 && checkStateTime[0] >= 2)
                tramPowerValues.remove(tramPowerValues.size() - 1);
            tramPowerValues.add(new Entry(timeCount, tramPower[modeState]));
            if (modeState <= 7 || modeState == 12 || modeState == 13) {
                if (modeState == 7)
                    d = new LineDataSet(tramPowerValues, "輸出100 kW");
                else
                    d = new LineDataSet(tramPowerValues, "市電供應");
                d.setLineWidth(2.5f);
                d.setColor(colors[0]);
                d.setDrawCircles(false);
                dataSets.add(d);
            }
            if (renewablePower[modeState] == renewablePower[modeState_pre])
                checkStateTime[1]++;
            else
                checkStateTime[1] = 0;
            if (renewablePower[modeState] == renewablePower[modeState_pre] && timeCount >= 2 && checkStateTime[1] >= 2)
                renewablePowerValues.remove(renewablePowerValues.size() - 1);
            renewablePowerValues.add(new Entry(timeCount, renewablePower[modeState]));
            d = new LineDataSet(renewablePowerValues, "再生能源");
            d.setLineWidth(2.5f);
            d.setColor(colors[1]);
            d.setDrawCircles(false);
            dataSets.add(d);
            if (generatorPower[modeState] == generatorPower[modeState_pre])
                checkStateTime[2]++;
            else
                checkStateTime[2] = 0;
            if (generatorPower[modeState] == generatorPower[modeState_pre] && timeCount >= 2 && checkStateTime[2] >= 2)
                generatorPowerValues.remove(generatorPowerValues.size() - 1);
            generatorPowerValues.add(new Entry(timeCount, generatorPower[modeState]));
            d = new LineDataSet(generatorPowerValues, "分散式發電機");
//            if (modeState == 7) {
//                d.enableDashedLine(30f, 30f, 0f);
//                d.setFormLineWidth(2.5f);
//                d.setFormLineDashEffect(new DashPathEffect(new float[]{10, 10}, 0f));
//                d.setFormSize(20f);
//            }
            d.enableDashedLine(30f, 30f, 0f);
            d.setFormLineWidth(2.5f);
            d.setFormLineDashEffect(new DashPathEffect(new float[]{10, 10}, 0f));
            d.setFormSize(20f);

            d.setLineWidth(2.5f);
            d.setColor(colors[2]);
            d.setDrawCircles(false);
            dataSets.add(d);
            if (buildPower[modeState] == buildPower[modeState_pre])
                checkStateTime[3]++;
            else
                checkStateTime[3] = 0;
            if (buildPower[modeState] == buildPower[modeState_pre] && timeCount >= 2 && checkStateTime[3] >= 2)
                buildPowerValues.remove(buildPowerValues.size() - 1);
            buildPowerValues.add(new Entry(timeCount, buildPower[modeState]));
            d = new LineDataSet(buildPowerValues, "館舍用電");
            d.enableDashedLine(30f, 30f, 0f);
            d.setFormLineWidth(2.5f);
            d.setFormLineDashEffect(new DashPathEffect(new float[]{10, 10}, 0f));
            d.setFormSize(20f);
            d.setLineWidth(2.5f);
            d.setColor(colors[3]);
            d.setDrawCircles(false);
            dataSets.add(d);
            if (socPower[modeState] == socPower[modeState_pre])
                checkStateTime[4]++;
            else
                checkStateTime[4] = 0;
            if (socPower[modeState] == socPower[modeState_pre] && timeCount >= 2 && checkStateTime[4] >= 2)
                socPowerValues.remove(socPowerValues.size() - 1);
            socPowerValues.add(new Entry(timeCount, abs(socPower[modeState])));
            if (socPower[modeState] >= 0 && (modeState != 13 && modeState != 15)) {
                d = new LineDataSet(socPowerValues, "儲能放電");
            } else {
                d = new LineDataSet(socPowerValues, "儲能充電");
//                d.enableDashedLine(30f, 30f, 0f);
//                d.setFormLineWidth(2.5f);
//                d.setFormLineDashEffect(new DashPathEffect(new float[]{10, 10}, 0f));
//                d.setFormSize(20f);
            }
            d.enableDashedLine(30f, 30f, 0f);
            d.setFormLineWidth(2.5f);
            d.setFormLineDashEffect(new DashPathEffect(new float[]{10, 10}, 0f));
            d.setFormSize(20f);

            d.setLineWidth(2.5f);
            d.setColor(colors[4]);
            d.setDrawCircles(false);
            dataSets.add(d);

            ArrayList<Entry> AL = new ArrayList<>();
            if (timeCount < 10) {
                AL.add(new Entry(0, 0));
                AL.add(new Entry(10, 0));
                if (chart != null) {
                    chart.getXAxis().setAxisMinimum(10f);
                    chart.getXAxis().setAxisMinimum(0f);
                }
            } else {
                AL.add(new Entry(timeCount - 10, 0));
                AL.add(new Entry(timeCount, 0));
                if (chart != null) {
                    chart.getXAxis().setAxisMinimum(timeCount);
                    chart.getXAxis().setAxisMinimum(timeCount - 10);
                }
            }

            d = new LineDataSet(AL, "");
            d.setLineWidth(0f);
            d.setColor(Color.rgb(0, 0, 0));
            d.setDrawCircles(false);
            dataSets.add(d);

            LineData data = new LineData(dataSets);
            data.setDrawValues(false);
            if (chart != null) {
                chart.setData(data);
                chart.notifyDataSetChanged();
                chart.invalidate();
            }
//            activity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    tv_sec.setText(String.valueOf((int) timeCount));
//                }
//            });
            timeCount++;
//            if (timeCount >= 60) {
//                tramPowerValues.clear();
//                generatorPowerValues.clear();
//                renewablePowerValues.clear();
//                socPowerValues.clear();
//                buildPowerValues.clear();
//                timeCount = 0;
//            }
            if (tramPowerValues.size() > 20)
                tramPowerValues.remove(0);
            if (generatorPowerValues.size() > 20)
                generatorPowerValues.remove(0);
            if (renewablePowerValues.size() > 20)
                renewablePowerValues.remove(0);
            if (socPowerValues.size() > 20)
                socPowerValues.remove(0);
            if (buildPowerValues.size() > 20)
                buildPowerValues.remove(0);

            modeState_pre = modeState;
        }
    }

    private final int[] colors = new int[]{
            Color.rgb(101, 114, 254),
            Color.rgb(113, 228, 57),
            Color.rgb(146, 21, 196),
            Color.rgb(194, 19, 30),
            Color.rgb(34, 177, 213)
    };

    AnimationDrawable animationDrawable_tram;
    AnimationDrawable animationDrawable_solar;
    AnimationDrawable animationDrawable_wind;
    AnimationDrawable animationDrawable_generator;
    AnimationDrawable animationDrawable_soc;
    AnimationDrawable animationDrawable_build;

    private void changeAnimationDrawableSpeed(AnimationDrawable mAnimationDrawable, int mSpeed) {

        if (mAnimationDrawable.isRunning())
            mAnimationDrawable.stop();

        int numFrames = mAnimationDrawable.getNumberOfFrames();
        for (int ii = 0; ii < numFrames; ii++) {
            // change the animation speed.
            Drawable d = mAnimationDrawable.getFrame(ii);
            mAnimationDrawable.scheduleDrawable(d, mAnimationDrawable, mSpeed);
        }
    }

    private void setModeAct(int mode) {
        switch (mode) {
            case 0:
                iv_item_tram.setImageResource(R.mipmap.ic_uncheck);
                iv_item_solar.setImageResource(R.mipmap.ic_uncheck);
                iv_item_island.setImageResource(R.mipmap.ic_uncheck);
                iv_item_wind.setImageResource(R.mipmap.ic_uncheck);
                iv_item_output.setImageResource(R.mipmap.ic_uncheck);
                iv_item_charge.setImageResource(R.mipmap.ic_uncheck);
                iv_item_uninstall.setImageResource(R.mipmap.ic_uncheck);
                iv_item_discharge.setImageResource(R.mipmap.ic_uncheck);

                watts_tram.setText(500 + " kW");
                watts_solar.setText(100 + " kW");
                watts_wind.setText(200 + " kW");
                watts_generator.setText(100 + " kW");
                watts_soc.setText(200 + " kW");
                battery_soc.setText("90%");
                watts_build.setText(500 + " kW");
//                watts_control.setText(200 + "kW");

                if (animationDrawable_tram != null && animationDrawable_tram.isRunning())
                    animationDrawable_tram.stop();
                iv_tram_anim.setImageResource(R.drawable.anim_tram_normal);

                if (animationDrawable_solar != null && animationDrawable_solar.isRunning())
                    animationDrawable_solar.stop();
                iv_solar_anim.setImageResource(R.drawable.anim_solar_normal);

                if (animationDrawable_wind != null && animationDrawable_wind.isRunning())
                    animationDrawable_wind.stop();
                iv_wind_anim.setImageResource(R.drawable.anim_wind_normal);

                if (animationDrawable_generator != null && animationDrawable_generator.isRunning())
                    animationDrawable_generator.stop();
                iv_generator_anim.setImageResource(R.drawable.anim_generator_normal);

                if (animationDrawable_soc != null && animationDrawable_soc.isRunning())
                    animationDrawable_soc.stop();
                iv_soc_anim.setImageResource(R.drawable.anim_soc_normal);

                if (animationDrawable_build != null && animationDrawable_build.isRunning())
                    animationDrawable_build.stop();
                iv_build_anim.setImageResource(R.drawable.anim_build_normal);

                if (iconTimer != null)
                    iconTimer.cancel();
                iconTimer = new Timer(true);
                iconTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (iconState) {
                                    iconState = false;
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_solar.setVisibility(View.VISIBLE);
                                    iv_wind.setVisibility(View.VISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_build039.setVisibility(View.VISIBLE);
                                    iv_build048.setVisibility(View.VISIBLE);
                                } else {
                                    iconState = true;
                                    iv_solar.setVisibility(View.VISIBLE);
                                    iv_wind.setVisibility(View.VISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_build048.setVisibility(View.VISIBLE);
                                    iv_build039.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                }, 0, 500);
                break;

            case 1:
                iv_item_tram.setImageResource(R.mipmap.ic_check);
                iv_item_solar.setImageResource(R.mipmap.ic_uncheck);
                iv_item_island.setImageResource(R.mipmap.ic_uncheck);
                iv_item_wind.setImageResource(R.mipmap.ic_uncheck);
                iv_item_output.setImageResource(R.mipmap.ic_uncheck);
                iv_item_charge.setImageResource(R.mipmap.ic_uncheck);
                iv_item_uninstall.setImageResource(R.mipmap.ic_uncheck);
                iv_item_discharge.setImageResource(R.mipmap.ic_uncheck);

                watts_tram.setText(500 + " kW");
                watts_solar.setText("0 kW");
                watts_wind.setText("0 kW");
                watts_generator.setText("0 kW");
                watts_soc.setText("0 kW");
                battery_soc.setText("90%");
                watts_build.setText(500 + " kW");
//                watts_control.setText(500 + "kW");

                iv_tram_anim.setImageResource(R.drawable.anim_tram_output);
                animationDrawable_tram = (AnimationDrawable) iv_tram_anim.getDrawable();
                animationDrawable_tram.start();

                if (animationDrawable_solar != null && animationDrawable_solar.isRunning())
                    animationDrawable_solar.stop();
                iv_solar_anim.setImageResource(R.drawable.anim_solar_normal);

                if (animationDrawable_wind != null && animationDrawable_wind.isRunning())
                    animationDrawable_wind.stop();
                iv_wind_anim.setImageResource(R.drawable.anim_wind_normal);

                if (animationDrawable_generator != null && animationDrawable_generator.isRunning())
                    animationDrawable_generator.stop();
                iv_generator_anim.setImageResource(R.drawable.anim_generator_normal);

                if (animationDrawable_soc != null && animationDrawable_soc.isRunning())
                    animationDrawable_soc.stop();
                iv_soc_anim.setImageResource(R.drawable.anim_soc_normal);

                iv_build_anim.setImageResource(R.drawable.anim_build_output);
                animationDrawable_build = (AnimationDrawable) iv_build_anim.getDrawable();
                animationDrawable_build.start();

                if (iconTimer != null)
                    iconTimer.cancel();
                iconTimer = new Timer(true);
                iconTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (iconState) {
                                    iconState = false;
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_solar.setVisibility(View.VISIBLE);
                                    iv_wind.setVisibility(View.VISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_build039.setVisibility(View.VISIBLE);
                                    iv_build048.setVisibility(View.VISIBLE);
                                } else {
                                    iconState = true;
                                    iv_solar.setVisibility(View.VISIBLE);
                                    iv_wind.setVisibility(View.VISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_tram.setVisibility(View.INVISIBLE);
                                    iv_build048.setVisibility(View.INVISIBLE);
                                    iv_build039.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }, 0, 500);
                break;
            case 2:
                iv_item_tram.setImageResource(R.mipmap.ic_check);
                iv_item_solar.setImageResource(R.mipmap.ic_check);
                iv_item_island.setImageResource(R.mipmap.ic_uncheck);
                iv_item_wind.setImageResource(R.mipmap.ic_uncheck);
                iv_item_output.setImageResource(R.mipmap.ic_uncheck);
                iv_item_charge.setImageResource(R.mipmap.ic_uncheck);
                iv_item_uninstall.setImageResource(R.mipmap.ic_uncheck);
                iv_item_discharge.setImageResource(R.mipmap.ic_uncheck);

                watts_tram.setText(400 + " kW");
                watts_solar.setText(100 + " kW");
                watts_wind.setText("0 kW");
                watts_generator.setText("0 kW");
                watts_soc.setText("0 kW");
                battery_soc.setText("90%");
                watts_build.setText(500 + " kW");
//                watts_control.setText(500 + "kW");

                iv_tram_anim.setImageResource(R.drawable.anim_tram_output_mid);
                animationDrawable_tram = (AnimationDrawable) iv_tram_anim.getDrawable();
                animationDrawable_tram.start();

                iv_solar_anim.setImageResource(R.drawable.anim_solar_output_mid);
                animationDrawable_solar = (AnimationDrawable) iv_solar_anim.getDrawable();
                animationDrawable_solar.start();

                if (animationDrawable_wind != null && animationDrawable_wind.isRunning())
                    animationDrawable_wind.stop();
                iv_wind_anim.setImageResource(R.drawable.anim_wind_normal);

                if (animationDrawable_generator != null && animationDrawable_generator.isRunning())
                    animationDrawable_generator.stop();
                iv_generator_anim.setImageResource(R.drawable.anim_generator_normal);

                if (animationDrawable_soc != null && animationDrawable_soc.isRunning())
                    animationDrawable_soc.stop();
                iv_soc_anim.setImageResource(R.drawable.anim_soc_normal);

                iv_build_anim.setImageResource(R.drawable.anim_build_output_mid);
                animationDrawable_build = (AnimationDrawable) iv_build_anim.getDrawable();
                animationDrawable_build.start();

                if (iconTimer != null)
                    iconTimer.cancel();
                iconTimer = new Timer(true);
                iconTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (iconState) {
                                    iconState = false;
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_solar.setVisibility(View.VISIBLE);
                                    iv_wind.setVisibility(View.VISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_build039.setVisibility(View.VISIBLE);
                                    iv_build048.setVisibility(View.VISIBLE);
                                } else {
                                    iconState = true;
                                    iv_tram.setVisibility(View.INVISIBLE);
                                    iv_solar.setVisibility(View.INVISIBLE);
                                    iv_wind.setVisibility(View.VISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_build048.setVisibility(View.INVISIBLE);
                                    iv_build039.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }, 0, 500);
                break;
            case 3:
                iv_item_tram.setImageResource(R.mipmap.ic_check);
                iv_item_solar.setImageResource(R.mipmap.ic_check);
                iv_item_island.setImageResource(R.mipmap.ic_uncheck);
                iv_item_wind.setImageResource(R.mipmap.ic_check);
                iv_item_output.setImageResource(R.mipmap.ic_uncheck);
                iv_item_charge.setImageResource(R.mipmap.ic_uncheck);
                iv_item_uninstall.setImageResource(R.mipmap.ic_uncheck);
                iv_item_discharge.setImageResource(R.mipmap.ic_uncheck);

                watts_tram.setText(200 + " kW");
                watts_solar.setText(100 + " kW");
                watts_wind.setText(200 + " kW");
                watts_generator.setText("0 kW");
                watts_soc.setText("0 kW");
                battery_soc.setText("90%");
                watts_build.setText(500 + " kW");
//                watts_control.setText(500 + "kW");

                iv_tram_anim.setImageResource(R.drawable.anim_tram_output_mid);
                animationDrawable_tram = (AnimationDrawable) iv_tram_anim.getDrawable();
                animationDrawable_tram.start();

                iv_solar_anim.setImageResource(R.drawable.anim_solar_output_mid);
                animationDrawable_solar = (AnimationDrawable) iv_solar_anim.getDrawable();
                animationDrawable_solar.start();

                iv_wind_anim.setImageResource(R.drawable.anim_wind_output_mid);
                animationDrawable_wind = (AnimationDrawable) iv_wind_anim.getDrawable();
                animationDrawable_wind.start();

                if (animationDrawable_generator != null && animationDrawable_generator.isRunning())
                    animationDrawable_generator.stop();
                iv_generator_anim.setImageResource(R.drawable.anim_generator_normal);

                if (animationDrawable_soc != null && animationDrawable_soc.isRunning())
                    animationDrawable_soc.stop();
                iv_soc_anim.setImageResource(R.drawable.anim_soc_normal);

                iv_build_anim.setImageResource(R.drawable.anim_build_output_mid);
                animationDrawable_build = (AnimationDrawable) iv_build_anim.getDrawable();
                animationDrawable_build.start();

                if (iconTimer != null)
                    iconTimer.cancel();
                iconTimer = new Timer(true);
                iconTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (iconState) {
                                    iconState = false;
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_solar.setVisibility(View.VISIBLE);
                                    iv_wind.setVisibility(View.VISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_build039.setVisibility(View.VISIBLE);
                                    iv_build048.setVisibility(View.VISIBLE);
                                } else {
                                    iconState = true;
                                    iv_tram.setVisibility(View.INVISIBLE);
                                    iv_solar.setVisibility(View.INVISIBLE);
                                    iv_wind.setVisibility(View.INVISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_build048.setVisibility(View.INVISIBLE);
                                    iv_build039.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }, 0, 500);
                break;
            case 4:
                iv_item_tram.setImageResource(R.mipmap.ic_check);
                iv_item_solar.setImageResource(R.mipmap.ic_check);
                iv_item_island.setImageResource(R.mipmap.ic_uncheck);
                iv_item_wind.setImageResource(R.mipmap.ic_check);
                iv_item_output.setImageResource(R.mipmap.ic_uncheck);
                iv_item_charge.setImageResource(R.mipmap.ic_uncheck);
                iv_item_uninstall.setImageResource(R.mipmap.ic_uncheck);
                iv_item_discharge.setImageResource(R.mipmap.ic_check);

                watts_tram.setText("0 kW");
                watts_solar.setText(100 + " kW");
                watts_wind.setText(200 + " kW");
                watts_generator.setText("0 kW");
                watts_soc.setText(200 + " kW");
                battery_soc.setText("90%");
                watts_build.setText(500 + " kW");
//                watts_control.setText(500 + "kW");

                if (animationDrawable_tram != null && animationDrawable_tram.isRunning())
                    animationDrawable_tram.stop();
                iv_tram_anim.setImageResource(R.drawable.anim_tram_output1);

                iv_solar_anim.setImageResource(R.drawable.anim_solar_output_mid);
                animationDrawable_solar = (AnimationDrawable) iv_solar_anim.getDrawable();
                animationDrawable_solar.start();

                iv_wind_anim.setImageResource(R.drawable.anim_wind_output_mid);
                animationDrawable_wind = (AnimationDrawable) iv_wind_anim.getDrawable();
                animationDrawable_wind.start();

                if (animationDrawable_generator != null && animationDrawable_generator.isRunning())
                    animationDrawable_generator.stop();
                iv_generator_anim.setImageResource(R.drawable.anim_generator_normal);

                iv_soc_anim.setImageResource(R.drawable.anim_soc_output_mid);
                animationDrawable_soc = (AnimationDrawable) iv_soc_anim.getDrawable();
                animationDrawable_soc.start();

                iv_build_anim.setImageResource(R.drawable.anim_build_output_mid);
                animationDrawable_build = (AnimationDrawable) iv_build_anim.getDrawable();
                animationDrawable_build.start();

                if (iconTimer != null)
                    iconTimer.cancel();
                iconTimer = new Timer(true);
                iconTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (iconState) {
                                    iconState = false;
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_solar.setVisibility(View.VISIBLE);
                                    iv_wind.setVisibility(View.VISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_build039.setVisibility(View.VISIBLE);
                                    iv_build048.setVisibility(View.VISIBLE);
                                } else {
                                    iconState = true;
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_solar.setVisibility(View.INVISIBLE);
                                    iv_wind.setVisibility(View.INVISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.INVISIBLE);
                                    iv_build048.setVisibility(View.INVISIBLE);
                                    iv_build039.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }, 0, 500);

                batteryLevel = 90;
                batteryTimer = new Timer(true);
                batteryTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        batteryLevel--;
                        timerState = true;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                battery_soc.setText(batteryLevel + "%");
                                if (batteryLevel <= 20) {
                                    battery_soc.setText("20%");
                                    batteryTimer.cancel();
                                    timerState = false;
                                    iv_item_tram.setImageResource(R.mipmap.ic_check);
                                    iv_item_solar.setImageResource(R.mipmap.ic_check);
                                    iv_item_island.setImageResource(R.mipmap.ic_uncheck);
                                    iv_item_wind.setImageResource(R.mipmap.ic_check);
                                    iv_item_output.setImageResource(R.mipmap.ic_uncheck);
                                    iv_item_charge.setImageResource(R.mipmap.ic_uncheck);
                                    iv_item_uninstall.setImageResource(R.mipmap.ic_uncheck);
                                    iv_item_discharge.setImageResource(R.mipmap.ic_uncheck);

                                    batteryTimer.cancel();
                                    watts_tram.setText(200 + " kW");
                                    watts_soc.setText("0 kW");
                                    modeState = 12;

                                    iv_tram_anim.setImageResource(R.drawable.anim_tram_output_mid);
                                    animationDrawable_tram = (AnimationDrawable) iv_tram_anim.getDrawable();
                                    animationDrawable_tram.start();

                                    iv_solar_anim.setImageResource(R.drawable.anim_solar_output_mid);
                                    animationDrawable_solar = (AnimationDrawable) iv_solar_anim.getDrawable();
                                    animationDrawable_solar.start();

                                    iv_wind_anim.setImageResource(R.drawable.anim_wind_output_mid);
                                    animationDrawable_wind = (AnimationDrawable) iv_wind_anim.getDrawable();
                                    animationDrawable_wind.start();

                                    if (animationDrawable_generator != null && animationDrawable_generator.isRunning())
                                        animationDrawable_generator.stop();
                                    iv_generator_anim.setImageResource(R.drawable.anim_generator_normal);

                                    if (animationDrawable_soc != null && animationDrawable_soc.isRunning())
                                        animationDrawable_soc.stop();
                                    iv_soc_anim.setImageResource(R.drawable.anim_soc_normal);

                                    iv_build_anim.setImageResource(R.drawable.anim_build_output_mid);
                                    animationDrawable_build = (AnimationDrawable) iv_build_anim.getDrawable();
                                    animationDrawable_build.start();

                                    if (iconTimer != null)
                                        iconTimer.cancel();
                                    iconTimer = new Timer(true);
                                    iconTimer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (iconState) {
                                                        iconState = false;
                                                        iv_tram.setVisibility(View.VISIBLE);
                                                        iv_solar.setVisibility(View.VISIBLE);
                                                        iv_wind.setVisibility(View.VISIBLE);
                                                        iv_generator.setVisibility(View.VISIBLE);
                                                        iv_control.setVisibility(View.VISIBLE);
                                                        iv_soc.setVisibility(View.VISIBLE);
                                                        iv_build039.setVisibility(View.VISIBLE);
                                                        iv_build048.setVisibility(View.VISIBLE);
                                                    } else {
                                                        iconState = true;
                                                        iv_tram.setVisibility(View.INVISIBLE);
                                                        iv_solar.setVisibility(View.INVISIBLE);
                                                        iv_wind.setVisibility(View.INVISIBLE);
                                                        iv_generator.setVisibility(View.VISIBLE);
                                                        iv_control.setVisibility(View.VISIBLE);
                                                        iv_soc.setVisibility(View.VISIBLE);
                                                        iv_build048.setVisibility(View.INVISIBLE);
                                                        iv_build039.setVisibility(View.INVISIBLE);
                                                    }
                                                }
                                            });
                                        }
                                    }, 0, 500);
                                }
                            }
                        });
                    }
                }, 0, 150);
                break;
            case 5:
                iv_item_tram.setImageResource(R.mipmap.ic_check);
                iv_item_solar.setImageResource(R.mipmap.ic_check);
                iv_item_island.setImageResource(R.mipmap.ic_uncheck);
                iv_item_wind.setImageResource(R.mipmap.ic_check);
                iv_item_output.setImageResource(R.mipmap.ic_uncheck);
                iv_item_charge.setImageResource(R.mipmap.ic_check);
                iv_item_uninstall.setImageResource(R.mipmap.ic_uncheck);
                iv_item_discharge.setImageResource(R.mipmap.ic_uncheck);

                watts_tram.setText(400 + " kW");
                watts_solar.setText(100 + " kW");
                watts_wind.setText(200 + " kW");
                watts_generator.setText("0 kW");
                watts_soc.setText(200 + " kW");
                battery_soc.setText("20%");
                watts_build.setText(500 + " kW");
//                watts_control.setText(500 + "kW");

                iv_tram_anim.setImageResource(R.drawable.anim_tram_output_mid);
                animationDrawable_tram = (AnimationDrawable) iv_tram_anim.getDrawable();
                animationDrawable_tram.start();

                iv_solar_anim.setImageResource(R.drawable.anim_solar_output_mid);
                animationDrawable_solar = (AnimationDrawable) iv_solar_anim.getDrawable();
                animationDrawable_solar.start();

                iv_wind_anim.setImageResource(R.drawable.anim_wind_output_mid);
                animationDrawable_wind = (AnimationDrawable) iv_wind_anim.getDrawable();
                animationDrawable_wind.start();

                if (animationDrawable_generator != null && animationDrawable_generator.isRunning())
                    animationDrawable_generator.stop();
                iv_generator_anim.setImageResource(R.drawable.anim_generator_normal);

                iv_soc_anim.setImageResource(R.drawable.anim_soc_input_mid);
                animationDrawable_soc = (AnimationDrawable) iv_soc_anim.getDrawable();
                animationDrawable_soc.start();

                iv_build_anim.setImageResource(R.drawable.anim_build_output_mid);
                animationDrawable_build = (AnimationDrawable) iv_build_anim.getDrawable();
                animationDrawable_build.start();

                if (iconTimer != null)
                    iconTimer.cancel();
                iconTimer = new Timer(true);
                iconTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (iconState) {
                                    iconState = false;
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_solar.setVisibility(View.VISIBLE);
                                    iv_wind.setVisibility(View.VISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_build039.setVisibility(View.VISIBLE);
                                    iv_build048.setVisibility(View.VISIBLE);
                                } else {
                                    iconState = true;
                                    iv_tram.setVisibility(View.INVISIBLE);
                                    iv_solar.setVisibility(View.INVISIBLE);
                                    iv_wind.setVisibility(View.INVISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.INVISIBLE);
                                    iv_build048.setVisibility(View.INVISIBLE);
                                    iv_build039.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }, 0, 500);

                batteryLevel = 20;
                batteryTimer = new Timer(true);
                batteryTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        batteryLevel++;
                        timerState = true;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                battery_soc.setText(batteryLevel + "%");
                                if (batteryLevel >= 90) {
                                    battery_soc.setText("90%");
                                    batteryTimer.cancel();
                                    timerState = false;
                                    iv_item_tram.setImageResource(R.mipmap.ic_check);
                                    iv_item_solar.setImageResource(R.mipmap.ic_check);
                                    iv_item_island.setImageResource(R.mipmap.ic_uncheck);
                                    iv_item_wind.setImageResource(R.mipmap.ic_check);
                                    iv_item_output.setImageResource(R.mipmap.ic_uncheck);
                                    iv_item_charge.setImageResource(R.mipmap.ic_uncheck);
                                    iv_item_uninstall.setImageResource(R.mipmap.ic_uncheck);
                                    iv_item_discharge.setImageResource(R.mipmap.ic_uncheck);

                                    batteryTimer.cancel();
                                    watts_tram.setText(200 + " kW");
                                    watts_soc.setText("0 kW");
                                    modeState = 13;

                                    iv_tram_anim.setImageResource(R.drawable.anim_tram_output_mid);
                                    animationDrawable_tram = (AnimationDrawable) iv_tram_anim.getDrawable();
                                    animationDrawable_tram.start();

                                    iv_solar_anim.setImageResource(R.drawable.anim_solar_output_mid);
                                    animationDrawable_solar = (AnimationDrawable) iv_solar_anim.getDrawable();
                                    animationDrawable_solar.start();

                                    iv_wind_anim.setImageResource(R.drawable.anim_wind_output_mid);
                                    animationDrawable_wind = (AnimationDrawable) iv_wind_anim.getDrawable();
                                    animationDrawable_wind.start();

                                    if (animationDrawable_generator != null && animationDrawable_generator.isRunning())
                                        animationDrawable_generator.stop();
                                    iv_generator_anim.setImageResource(R.drawable.anim_generator_normal);

                                    if (animationDrawable_soc != null && animationDrawable_soc.isRunning())
                                        animationDrawable_soc.stop();
                                    iv_soc_anim.setImageResource(R.drawable.anim_soc_normal);

                                    iv_build_anim.setImageResource(R.drawable.anim_build_output_mid);
                                    animationDrawable_build = (AnimationDrawable) iv_build_anim.getDrawable();
                                    animationDrawable_build.start();

                                    if (iconTimer != null)
                                        iconTimer.cancel();
                                    iconTimer = new Timer(true);
                                    iconTimer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (iconState) {
                                                        iconState = false;
                                                        iv_tram.setVisibility(View.VISIBLE);
                                                        iv_solar.setVisibility(View.VISIBLE);
                                                        iv_wind.setVisibility(View.VISIBLE);
                                                        iv_generator.setVisibility(View.VISIBLE);
                                                        iv_control.setVisibility(View.VISIBLE);
                                                        iv_soc.setVisibility(View.VISIBLE);
                                                        iv_build039.setVisibility(View.VISIBLE);
                                                        iv_build048.setVisibility(View.VISIBLE);
                                                    } else {
                                                        iconState = true;
                                                        iv_tram.setVisibility(View.INVISIBLE);
                                                        iv_solar.setVisibility(View.INVISIBLE);
                                                        iv_wind.setVisibility(View.INVISIBLE);
                                                        iv_generator.setVisibility(View.VISIBLE);
                                                        iv_control.setVisibility(View.VISIBLE);
                                                        iv_soc.setVisibility(View.VISIBLE);
                                                        iv_build048.setVisibility(View.INVISIBLE);
                                                        iv_build039.setVisibility(View.INVISIBLE);
                                                    }
                                                }
                                            });
                                        }
                                    }, 0, 500);
                                }
                            }
                        });
                    }
                }, 0, 150);
                break;
            case 6:
                iv_item_tram.setImageResource(R.mipmap.ic_check);
                iv_item_solar.setImageResource(R.mipmap.ic_check);
                iv_item_island.setImageResource(R.mipmap.ic_uncheck);
                iv_item_wind.setImageResource(R.mipmap.ic_check);
                iv_item_output.setImageResource(R.mipmap.ic_uncheck);
                iv_item_charge.setImageResource(R.mipmap.ic_uncheck);
                iv_item_uninstall.setImageResource(R.mipmap.ic_check);
                iv_item_discharge.setImageResource(R.mipmap.ic_uncheck);

                watts_tram.setText("0 kW");
                watts_solar.setText(100 + " kW");
                watts_wind.setText(200 + " kW");
                watts_generator.setText("0 kW");
                watts_soc.setText("0 kW");
                battery_soc.setText("90%");
                watts_build.setText(300 + " kW");
//                watts_control.setText(300 + "kW");

                if (animationDrawable_tram != null && animationDrawable_tram.isRunning())
                    animationDrawable_tram.stop();
                iv_tram_anim.setImageResource(R.drawable.anim_tram_output1);

                iv_solar_anim.setImageResource(R.drawable.anim_solar_output_low);
                animationDrawable_solar = (AnimationDrawable) iv_solar_anim.getDrawable();
                animationDrawable_solar.start();

                iv_wind_anim.setImageResource(R.drawable.anim_wind_output_low);
                animationDrawable_wind = (AnimationDrawable) iv_wind_anim.getDrawable();
                animationDrawable_wind.start();

                if (animationDrawable_generator != null && animationDrawable_generator.isRunning())
                    animationDrawable_generator.stop();
                iv_generator_anim.setImageResource(R.drawable.anim_generator_normal);

                if (animationDrawable_soc != null && animationDrawable_soc.isRunning())
                    animationDrawable_soc.stop();
                iv_soc_anim.setImageResource(R.drawable.anim_soc_normal);

                iv_build_anim.setImageResource(R.drawable.anim_build_output_low);
                animationDrawable_build = (AnimationDrawable) iv_build_anim.getDrawable();
                animationDrawable_build.start();

                if (iconTimer != null)
                    iconTimer.cancel();
                iconTimer = new Timer(true);
                iconTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (iconState) {
                                    iconState = false;
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_solar.setVisibility(View.VISIBLE);
                                    iv_wind.setVisibility(View.VISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_build039.setVisibility(View.VISIBLE);
                                    iv_build048.setVisibility(View.VISIBLE);
                                } else {
                                    iconState = true;
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_solar.setVisibility(View.INVISIBLE);
                                    iv_wind.setVisibility(View.INVISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_build048.setVisibility(View.INVISIBLE);
                                    iv_build039.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }, 0, 500);
                break;
            case 7:
                iv_item_tram.setImageResource(R.mipmap.ic_check);
                iv_item_solar.setImageResource(R.mipmap.ic_check);
                iv_item_island.setImageResource(R.mipmap.ic_uncheck);
                iv_item_wind.setImageResource(R.mipmap.ic_check);
                iv_item_output.setImageResource(R.mipmap.ic_check);
                iv_item_charge.setImageResource(R.mipmap.ic_uncheck);
                iv_item_uninstall.setImageResource(R.mipmap.ic_uncheck);
                iv_item_discharge.setImageResource(R.mipmap.ic_uncheck);

                watts_tram.setText(100 + " kW");
                watts_solar.setText(100 + " kW");
                watts_wind.setText(200 + " kW");
                watts_generator.setText(100 + " kW");
                watts_soc.setText("0 kW");
                battery_soc.setText("90%");
                watts_build.setText(300 + " kW");
//                watts_control.setText(400 + "kW");

                iv_tram_anim.setImageResource(R.drawable.anim_tram_input);
                animationDrawable_tram = (AnimationDrawable) iv_tram_anim.getDrawable();
                animationDrawable_tram.start();

                iv_solar_anim.setImageResource(R.drawable.anim_solar_output_low);
                animationDrawable_solar = (AnimationDrawable) iv_solar_anim.getDrawable();
                animationDrawable_solar.start();

                iv_wind_anim.setImageResource(R.drawable.anim_wind_output_low);
                animationDrawable_wind = (AnimationDrawable) iv_wind_anim.getDrawable();
                animationDrawable_wind.start();

                iv_generator_anim.setImageResource(R.drawable.anim_generator_output_low);
                animationDrawable_generator = (AnimationDrawable) iv_generator_anim.getDrawable();
                animationDrawable_generator.start();

                if (animationDrawable_soc != null && animationDrawable_soc.isRunning())
                    animationDrawable_soc.stop();
                iv_soc_anim.setImageResource(R.drawable.anim_soc_normal);

                iv_build_anim.setImageResource(R.drawable.anim_build_output_low);
                animationDrawable_build = (AnimationDrawable) iv_build_anim.getDrawable();
                animationDrawable_build.start();

                if (iconTimer != null)
                    iconTimer.cancel();
                iconTimer = new Timer(true);
                iconTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (iconState) {
                                    iconState = false;
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_solar.setVisibility(View.VISIBLE);
                                    iv_wind.setVisibility(View.VISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_build039.setVisibility(View.VISIBLE);
                                    iv_build048.setVisibility(View.VISIBLE);
                                } else {
                                    iconState = true;
                                    iv_tram.setVisibility(View.INVISIBLE);
                                    iv_solar.setVisibility(View.INVISIBLE);
                                    iv_wind.setVisibility(View.INVISIBLE);
                                    iv_generator.setVisibility(View.INVISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_build048.setVisibility(View.INVISIBLE);
                                    iv_build039.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }, 0, 500);
                break;
            case 8:
                iv_item_tram.setImageResource(R.mipmap.ic_uncheck);
                iv_item_solar.setImageResource(R.mipmap.ic_check);
                iv_item_island.setImageResource(R.mipmap.ic_check);
                iv_item_wind.setImageResource(R.mipmap.ic_check);
                iv_item_output.setImageResource(R.mipmap.ic_uncheck);
                iv_item_charge.setImageResource(R.mipmap.ic_uncheck);
                iv_item_uninstall.setImageResource(R.mipmap.ic_uncheck);
                iv_item_discharge.setImageResource(R.mipmap.ic_uncheck);

                watts_tram.setText("0 kW");
                watts_solar.setText(100 + " kW");
                watts_wind.setText(200 + " kW");
                watts_generator.setText("0 kW");
                watts_soc.setText("0 kW");
                battery_soc.setText("90%");
                watts_build.setText(300 + " kW");
//                watts_control.setText(300 + "kW");

                if (animationDrawable_tram != null && animationDrawable_tram.isRunning())
                    animationDrawable_tram.stop();
                iv_tram_anim.setImageResource(R.drawable.anim_tram_normal);

                iv_solar_anim.setImageResource(R.drawable.anim_solar_output_low);
                animationDrawable_solar = (AnimationDrawable) iv_solar_anim.getDrawable();
                animationDrawable_solar.start();

                iv_wind_anim.setImageResource(R.drawable.anim_wind_output_low);
                animationDrawable_wind = (AnimationDrawable) iv_wind_anim.getDrawable();
                animationDrawable_wind.start();

                if (animationDrawable_generator != null && animationDrawable_generator.isRunning())
                    animationDrawable_generator.stop();
                iv_generator_anim.setImageResource(R.drawable.anim_generator_normal);

                if (animationDrawable_soc != null && animationDrawable_soc.isRunning())
                    animationDrawable_soc.stop();
                iv_soc_anim.setImageResource(R.drawable.anim_soc_normal);

                iv_build_anim.setImageResource(R.drawable.anim_build_output_low);
                animationDrawable_build = (AnimationDrawable) iv_build_anim.getDrawable();
                animationDrawable_build.start();

                if (iconTimer != null)
                    iconTimer.cancel();
                iconTimer = new Timer(true);
                iconTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (iconState) {
                                    iconState = false;
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_solar.setVisibility(View.VISIBLE);
                                    iv_wind.setVisibility(View.VISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_build039.setVisibility(View.VISIBLE);
                                    iv_build048.setVisibility(View.VISIBLE);
                                } else {
                                    iconState = true;
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_solar.setVisibility(View.INVISIBLE);
                                    iv_wind.setVisibility(View.INVISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_build048.setVisibility(View.INVISIBLE);
                                    iv_build039.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }, 0, 500);
                break;
            case 9:
                iv_item_tram.setImageResource(R.mipmap.ic_uncheck);
                iv_item_solar.setImageResource(R.mipmap.ic_uncheck);
                iv_item_island.setImageResource(R.mipmap.ic_check);
                iv_item_wind.setImageResource(R.mipmap.ic_check);
                iv_item_output.setImageResource(R.mipmap.ic_uncheck);
                iv_item_charge.setImageResource(R.mipmap.ic_uncheck);
                iv_item_uninstall.setImageResource(R.mipmap.ic_uncheck);
                iv_item_discharge.setImageResource(R.mipmap.ic_check);

                watts_tram.setText("0 kW");
                watts_solar.setText("0 kW");
                watts_wind.setText(200 + " kW");
                watts_generator.setText("0 kW");
                watts_soc.setText(100 + " kW");
                battery_soc.setText("90%");
                watts_build.setText(300 + " kW");
//                watts_control.setText(300 + "kW");

                if (animationDrawable_tram != null && animationDrawable_tram.isRunning())
                    animationDrawable_tram.stop();
                iv_tram_anim.setImageResource(R.drawable.anim_tram_normal);

                if (animationDrawable_solar != null && animationDrawable_solar.isRunning())
                    animationDrawable_solar.stop();
                iv_solar_anim.setImageResource(R.drawable.anim_solar_normal);

                iv_wind_anim.setImageResource(R.drawable.anim_wind_output_low);
                animationDrawable_wind = (AnimationDrawable) iv_wind_anim.getDrawable();
                animationDrawable_wind.start();

                if (animationDrawable_generator != null && animationDrawable_generator.isRunning())
                    animationDrawable_generator.stop();
                iv_generator_anim.setImageResource(R.drawable.anim_generator_normal);

                iv_soc_anim.setImageResource(R.drawable.anim_soc_output_low);
                animationDrawable_soc = (AnimationDrawable) iv_soc_anim.getDrawable();
                animationDrawable_soc.start();

                iv_build_anim.setImageResource(R.drawable.anim_build_output_low);
                animationDrawable_build = (AnimationDrawable) iv_build_anim.getDrawable();
                animationDrawable_build.start();

                if (iconTimer != null)
                    iconTimer.cancel();
                iconTimer = new Timer(true);
                iconTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (iconState) {
                                    iconState = false;
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_solar.setVisibility(View.VISIBLE);
                                    iv_wind.setVisibility(View.VISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_build039.setVisibility(View.VISIBLE);
                                    iv_build048.setVisibility(View.VISIBLE);
                                } else {
                                    iconState = true;
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_solar.setVisibility(View.VISIBLE);
                                    iv_wind.setVisibility(View.INVISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.INVISIBLE);
                                    iv_build048.setVisibility(View.INVISIBLE);
                                    iv_build039.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }, 0, 500);

                batteryLevel = 90;
                batteryTimer = new Timer(true);
                batteryTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        batteryLevel--;
                        timerState = true;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                battery_soc.setText(batteryLevel + "%");
                                if (batteryLevel <= 20) {
                                    battery_soc.setText("20%");
                                    batteryTimer.cancel();
                                    timerState = false;
                                    iv_item_tram.setImageResource(R.mipmap.ic_uncheck);
                                    iv_item_solar.setImageResource(R.mipmap.ic_uncheck);
                                    iv_item_island.setImageResource(R.mipmap.ic_check);
                                    iv_item_wind.setImageResource(R.mipmap.ic_check);
                                    iv_item_output.setImageResource(R.mipmap.ic_uncheck);
                                    iv_item_charge.setImageResource(R.mipmap.ic_uncheck);
                                    iv_item_uninstall.setImageResource(R.mipmap.ic_uncheck);
                                    iv_item_discharge.setImageResource(R.mipmap.ic_uncheck);

                                    batteryTimer.cancel();
                                    watts_generator.setText(100 + " kW");
                                    watts_soc.setText("0 kW");
                                    modeState = 14;

                                    if (animationDrawable_tram != null && animationDrawable_tram.isRunning())
                                        animationDrawable_tram.stop();
                                    iv_tram_anim.setImageResource(R.drawable.anim_tram_normal);

                                    if (animationDrawable_solar != null && animationDrawable_solar.isRunning())
                                        animationDrawable_solar.stop();
                                    iv_solar_anim.setImageResource(R.drawable.anim_solar_normal);

                                    iv_wind_anim.setImageResource(R.drawable.anim_wind_output_low);
                                    animationDrawable_wind = (AnimationDrawable) iv_wind_anim.getDrawable();
                                    animationDrawable_wind.start();

                                    iv_generator_anim.setImageResource(R.drawable.anim_generator_output_low);
                                    animationDrawable_generator = (AnimationDrawable) iv_generator_anim.getDrawable();
                                    animationDrawable_generator.start();

                                    if (animationDrawable_soc != null && animationDrawable_soc.isRunning())
                                        animationDrawable_soc.stop();
                                    iv_soc_anim.setImageResource(R.drawable.anim_soc_normal);

                                    iv_build_anim.setImageResource(R.drawable.anim_build_output_low);
                                    animationDrawable_build = (AnimationDrawable) iv_build_anim.getDrawable();
                                    animationDrawable_build.start();

                                    if (iconTimer != null)
                                        iconTimer.cancel();
                                    iconTimer = new Timer(true);
                                    iconTimer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (iconState) {
                                                        iconState = false;
                                                        iv_tram.setVisibility(View.VISIBLE);
                                                        iv_solar.setVisibility(View.VISIBLE);
                                                        iv_wind.setVisibility(View.VISIBLE);
                                                        iv_generator.setVisibility(View.VISIBLE);
                                                        iv_control.setVisibility(View.VISIBLE);
                                                        iv_soc.setVisibility(View.VISIBLE);
                                                        iv_build039.setVisibility(View.VISIBLE);
                                                        iv_build048.setVisibility(View.VISIBLE);
                                                    } else {
                                                        iconState = true;
                                                        iv_tram.setVisibility(View.VISIBLE);
                                                        iv_solar.setVisibility(View.VISIBLE);
                                                        iv_wind.setVisibility(View.INVISIBLE);
                                                        iv_generator.setVisibility(View.INVISIBLE);
                                                        iv_control.setVisibility(View.VISIBLE);
                                                        iv_soc.setVisibility(View.VISIBLE);
                                                        iv_build048.setVisibility(View.INVISIBLE);
                                                        iv_build039.setVisibility(View.INVISIBLE);
                                                    }
                                                }
                                            });
                                        }
                                    }, 0, 500);
                                }
                            }
                        });
                    }
                }, 0, 300);
                break;
            case 10:
                iv_item_tram.setImageResource(R.mipmap.ic_uncheck);
                iv_item_solar.setImageResource(R.mipmap.ic_uncheck);
                iv_item_island.setImageResource(R.mipmap.ic_check);
                iv_item_wind.setImageResource(R.mipmap.ic_uncheck);
                iv_item_output.setImageResource(R.mipmap.ic_uncheck);
                iv_item_charge.setImageResource(R.mipmap.ic_uncheck);
                iv_item_uninstall.setImageResource(R.mipmap.ic_uncheck);
                iv_item_discharge.setImageResource(R.mipmap.ic_uncheck);

                watts_tram.setText("0 kW");
                watts_solar.setText("0 kW");
                watts_wind.setText("0 kW");
                watts_generator.setText(300 + " kW");
                watts_soc.setText("0 kW");
                battery_soc.setText("20%");
                watts_build.setText(300 + " kW");
//                watts_control.setText(300 + "kW");

                if (animationDrawable_tram != null && animationDrawable_tram.isRunning())
                    animationDrawable_tram.stop();
                iv_tram_anim.setImageResource(R.drawable.anim_tram_normal);

                if (animationDrawable_solar != null && animationDrawable_solar.isRunning())
                    animationDrawable_solar.stop();
                iv_solar_anim.setImageResource(R.drawable.anim_solar_normal);

                if (animationDrawable_wind != null && animationDrawable_wind.isRunning())
                    animationDrawable_wind.stop();
                iv_wind_anim.setImageResource(R.drawable.anim_wind_normal);

                iv_generator_anim.setImageResource(R.drawable.anim_generator_output_low);
                animationDrawable_generator = (AnimationDrawable) iv_generator_anim.getDrawable();
                animationDrawable_generator.start();

                if (animationDrawable_soc != null && animationDrawable_soc.isRunning())
                    animationDrawable_soc.stop();
                iv_soc_anim.setImageResource(R.drawable.anim_soc_normal);

                iv_build_anim.setImageResource(R.drawable.anim_build_output_low);
                animationDrawable_build = (AnimationDrawable) iv_build_anim.getDrawable();
                animationDrawable_build.start();

                if (iconTimer != null)
                    iconTimer.cancel();
                iconTimer = new Timer(true);
                iconTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (iconState) {
                                    iconState = false;
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_solar.setVisibility(View.VISIBLE);
                                    iv_wind.setVisibility(View.VISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_build039.setVisibility(View.VISIBLE);
                                    iv_build048.setVisibility(View.VISIBLE);
                                } else {
                                    iconState = true;
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_solar.setVisibility(View.VISIBLE);
                                    iv_wind.setVisibility(View.VISIBLE);
                                    iv_generator.setVisibility(View.INVISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_build048.setVisibility(View.INVISIBLE);
                                    iv_build039.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }, 0, 500);
                break;
            case 11:
                iv_item_tram.setImageResource(R.mipmap.ic_uncheck);
                iv_item_solar.setImageResource(R.mipmap.ic_check);
                iv_item_island.setImageResource(R.mipmap.ic_check);
                iv_item_wind.setImageResource(R.mipmap.ic_check);
                iv_item_output.setImageResource(R.mipmap.ic_uncheck);
                iv_item_charge.setImageResource(R.mipmap.ic_check);
                iv_item_uninstall.setImageResource(R.mipmap.ic_uncheck);
                iv_item_discharge.setImageResource(R.mipmap.ic_uncheck);

                watts_tram.setText("0 kW");
                watts_solar.setText(100 + " kW");
                watts_wind.setText(200 + " kW");
                watts_generator.setText(300 + " kW");
                watts_soc.setText(300 + " kW");
                battery_soc.setText("20%");
                watts_build.setText(300 + " kW");
//                watts_control.setText(300 + "kW");

                if (animationDrawable_tram != null && animationDrawable_tram.isRunning())
                    animationDrawable_tram.stop();
                iv_tram_anim.setImageResource(R.drawable.anim_tram_normal);

                iv_solar_anim.setImageResource(R.drawable.anim_solar_output_low);
                animationDrawable_solar = (AnimationDrawable) iv_solar_anim.getDrawable();
                animationDrawable_solar.start();

                iv_wind_anim.setImageResource(R.drawable.anim_wind_output_low);
                animationDrawable_wind = (AnimationDrawable) iv_wind_anim.getDrawable();
                animationDrawable_wind.start();

                iv_generator_anim.setImageResource(R.drawable.anim_generator_output_low);
                animationDrawable_generator = (AnimationDrawable) iv_generator_anim.getDrawable();
                animationDrawable_generator.start();

                iv_soc_anim.setImageResource(R.drawable.anim_soc_input);
                animationDrawable_soc = (AnimationDrawable) iv_soc_anim.getDrawable();
                animationDrawable_soc.start();

                iv_build_anim.setImageResource(R.drawable.anim_build_output_low);
                animationDrawable_build = (AnimationDrawable) iv_build_anim.getDrawable();
                animationDrawable_build.start();

                if (iconTimer != null)
                    iconTimer.cancel();
                iconTimer = new Timer(true);
                iconTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (iconState) {
                                    iconState = false;
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_solar.setVisibility(View.VISIBLE);
                                    iv_wind.setVisibility(View.VISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_build039.setVisibility(View.VISIBLE);
                                    iv_build048.setVisibility(View.VISIBLE);
                                } else {
                                    iconState = true;
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_solar.setVisibility(View.INVISIBLE);
                                    iv_wind.setVisibility(View.INVISIBLE);
                                    iv_generator.setVisibility(View.INVISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.INVISIBLE);
                                    iv_build048.setVisibility(View.INVISIBLE);
                                    iv_build039.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }, 0, 500);

                batteryLevel = 20;
                batteryTimer = new Timer(true);
                batteryTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        batteryLevel++;
                        timerState = true;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                battery_soc.setText(batteryLevel + "%");
                                if (batteryLevel >= 90) {
                                    battery_soc.setText("90%");
                                    batteryTimer.cancel();
                                    timerState = false;
                                    iv_item_tram.setImageResource(R.mipmap.ic_uncheck);
                                    iv_item_solar.setImageResource(R.mipmap.ic_check);
                                    iv_item_island.setImageResource(R.mipmap.ic_check);
                                    iv_item_wind.setImageResource(R.mipmap.ic_check);
                                    iv_item_output.setImageResource(R.mipmap.ic_uncheck);
                                    iv_item_charge.setImageResource(R.mipmap.ic_uncheck);
                                    iv_item_uninstall.setImageResource(R.mipmap.ic_uncheck);
                                    iv_item_discharge.setImageResource(R.mipmap.ic_uncheck);

                                    batteryTimer.cancel();
                                    watts_generator.setText("0 kW");
                                    watts_soc.setText("0 kW");
                                    modeState = 15;

                                    if (animationDrawable_tram != null && animationDrawable_tram.isRunning())
                                        animationDrawable_tram.stop();
                                    iv_tram_anim.setImageResource(R.drawable.anim_tram_normal);

                                    iv_solar_anim.setImageResource(R.drawable.anim_solar_output_low);
                                    animationDrawable_solar = (AnimationDrawable) iv_solar_anim.getDrawable();
                                    animationDrawable_solar.start();

                                    iv_wind_anim.setImageResource(R.drawable.anim_wind_output_low);
                                    animationDrawable_wind = (AnimationDrawable) iv_wind_anim.getDrawable();
                                    animationDrawable_wind.start();

                                    if (animationDrawable_generator != null && animationDrawable_generator.isRunning())
                                        animationDrawable_generator.stop();
                                    iv_generator_anim.setImageResource(R.drawable.anim_generator_normal);

                                    if (animationDrawable_soc != null && animationDrawable_soc.isRunning())
                                        animationDrawable_soc.stop();
                                    iv_soc_anim.setImageResource(R.drawable.anim_soc_normal);

                                    iv_build_anim.setImageResource(R.drawable.anim_build_output_low);
                                    animationDrawable_build = (AnimationDrawable) iv_build_anim.getDrawable();
                                    animationDrawable_build.start();

                                    if (iconTimer != null)
                                        iconTimer.cancel();
                                    iconTimer = new Timer(true);
                                    iconTimer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (iconState) {
                                                        iconState = false;
                                                        iv_tram.setVisibility(View.VISIBLE);
                                                        iv_solar.setVisibility(View.VISIBLE);
                                                        iv_wind.setVisibility(View.VISIBLE);
                                                        iv_generator.setVisibility(View.VISIBLE);
                                                        iv_control.setVisibility(View.VISIBLE);
                                                        iv_soc.setVisibility(View.VISIBLE);
                                                        iv_build039.setVisibility(View.VISIBLE);
                                                        iv_build048.setVisibility(View.VISIBLE);
                                                    } else {
                                                        iconState = true;
                                                        iv_tram.setVisibility(View.VISIBLE);
                                                        iv_solar.setVisibility(View.INVISIBLE);
                                                        iv_wind.setVisibility(View.INVISIBLE);
                                                        iv_generator.setVisibility(View.VISIBLE);
                                                        iv_control.setVisibility(View.VISIBLE);
                                                        iv_soc.setVisibility(View.VISIBLE);
                                                        iv_build048.setVisibility(View.INVISIBLE);
                                                        iv_build039.setVisibility(View.INVISIBLE);
                                                    }
                                                }
                                            });
                                        }
                                    }, 0, 500);
                                }
                            }
                        });
                    }
                }, 0, 100);
                break;
            case 16:
                iv_item_tram.setImageResource(R.mipmap.ic_uncheck);
                iv_item_solar.setImageResource(R.mipmap.ic_check);
                iv_item_island.setImageResource(R.mipmap.ic_check);
                iv_item_wind.setImageResource(R.mipmap.ic_uncheck);
                iv_item_output.setImageResource(R.mipmap.ic_uncheck);
                iv_item_charge.setImageResource(R.mipmap.ic_uncheck);
                iv_item_uninstall.setImageResource(R.mipmap.ic_uncheck);
                iv_item_discharge.setImageResource(R.mipmap.ic_check);

                watts_tram.setText("0 kW");
                watts_solar.setText(100 + " kW");
                watts_wind.setText("0 kW");
                watts_generator.setText("0 kW");
                watts_soc.setText(200 + " kW");
                battery_soc.setText("90%");
                watts_build.setText(300 + " kW");
//                watts_control.setText(300 + "kW");

                if (animationDrawable_tram != null && animationDrawable_tram.isRunning())
                    animationDrawable_tram.stop();
                iv_tram_anim.setImageResource(R.drawable.anim_tram_normal);

                iv_solar_anim.setImageResource(R.drawable.anim_solar_output_low);
                animationDrawable_solar = (AnimationDrawable) iv_solar_anim.getDrawable();
                animationDrawable_solar.start();

                if (animationDrawable_wind != null && animationDrawable_wind.isRunning())
                    animationDrawable_wind.stop();
                iv_wind_anim.setImageResource(R.drawable.anim_wind_normal);

                if (animationDrawable_generator != null && animationDrawable_generator.isRunning())
                    animationDrawable_generator.stop();
                iv_generator_anim.setImageResource(R.drawable.anim_generator_normal);

                iv_soc_anim.setImageResource(R.drawable.anim_soc_output_low);
                animationDrawable_soc = (AnimationDrawable) iv_soc_anim.getDrawable();
                animationDrawable_soc.start();

                iv_build_anim.setImageResource(R.drawable.anim_build_output_low);
                animationDrawable_build = (AnimationDrawable) iv_build_anim.getDrawable();
                animationDrawable_build.start();

                if (iconTimer != null)
                    iconTimer.cancel();
                iconTimer = new Timer(true);
                iconTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (iconState) {
                                    iconState = false;
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_solar.setVisibility(View.VISIBLE);
                                    iv_wind.setVisibility(View.VISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_build039.setVisibility(View.VISIBLE);
                                    iv_build048.setVisibility(View.VISIBLE);
                                } else {
                                    iconState = true;
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_solar.setVisibility(View.INVISIBLE);
                                    iv_wind.setVisibility(View.VISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.INVISIBLE);
                                    iv_build048.setVisibility(View.INVISIBLE);
                                    iv_build039.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }, 0, 500);

                batteryLevel = 90;
                batteryTimer = new Timer(true);
                batteryTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        batteryLevel--;
                        timerState = true;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                battery_soc.setText(batteryLevel + "%");
                                if (batteryLevel <= 20) {
                                    battery_soc.setText("20%");
                                    batteryTimer.cancel();
                                    timerState = false;
                                    iv_item_tram.setImageResource(R.mipmap.ic_uncheck);
                                    iv_item_solar.setImageResource(R.mipmap.ic_check);
                                    iv_item_island.setImageResource(R.mipmap.ic_check);
                                    iv_item_wind.setImageResource(R.mipmap.ic_uncheck);
                                    iv_item_output.setImageResource(R.mipmap.ic_uncheck);
                                    iv_item_charge.setImageResource(R.mipmap.ic_uncheck);
                                    iv_item_uninstall.setImageResource(R.mipmap.ic_uncheck);
                                    iv_item_discharge.setImageResource(R.mipmap.ic_uncheck);

                                    batteryTimer.cancel();
                                    watts_generator.setText(200 + " kW");
                                    watts_wind.setText("0 kW");
                                    modeState = 19;

                                    if (animationDrawable_tram != null && animationDrawable_tram.isRunning())
                                        animationDrawable_tram.stop();
                                    iv_tram_anim.setImageResource(R.drawable.anim_tram_normal);

                                    iv_solar_anim.setImageResource(R.drawable.anim_solar_output_low);
                                    animationDrawable_solar = (AnimationDrawable) iv_solar_anim.getDrawable();
                                    animationDrawable_solar.start();

                                    if (animationDrawable_wind != null && animationDrawable_wind.isRunning())
                                        animationDrawable_wind.stop();
                                    iv_wind_anim.setImageResource(R.drawable.anim_wind_normal);

                                    iv_generator_anim.setImageResource(R.drawable.anim_generator_output_low);
                                    animationDrawable_generator = (AnimationDrawable) iv_generator_anim.getDrawable();
                                    animationDrawable_generator.start();

                                    if (animationDrawable_soc != null && animationDrawable_soc.isRunning())
                                        animationDrawable_soc.stop();
                                    iv_soc_anim.setImageResource(R.drawable.anim_soc_normal);

                                    iv_build_anim.setImageResource(R.drawable.anim_build_output_low);
                                    animationDrawable_build = (AnimationDrawable) iv_build_anim.getDrawable();
                                    animationDrawable_build.start();

                                    if (iconTimer != null)
                                        iconTimer.cancel();
                                    iconTimer = new Timer(true);
                                    iconTimer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (iconState) {
                                                        iconState = false;
                                                        iv_tram.setVisibility(View.VISIBLE);
                                                        iv_solar.setVisibility(View.VISIBLE);
                                                        iv_wind.setVisibility(View.VISIBLE);
                                                        iv_generator.setVisibility(View.VISIBLE);
                                                        iv_control.setVisibility(View.VISIBLE);
                                                        iv_soc.setVisibility(View.VISIBLE);
                                                        iv_build039.setVisibility(View.VISIBLE);
                                                        iv_build048.setVisibility(View.VISIBLE);
                                                    } else {
                                                        iconState = true;
                                                        iv_tram.setVisibility(View.VISIBLE);
                                                        iv_solar.setVisibility(View.INVISIBLE);
                                                        iv_wind.setVisibility(View.VISIBLE);
                                                        iv_generator.setVisibility(View.INVISIBLE);
                                                        iv_control.setVisibility(View.VISIBLE);
                                                        iv_soc.setVisibility(View.VISIBLE);
                                                        iv_build048.setVisibility(View.INVISIBLE);
                                                        iv_build039.setVisibility(View.INVISIBLE);
                                                    }
                                                }
                                            });
                                        }
                                    }, 0, 500);
                                }
                            }
                        });
                    }
                }, 0, 150);
                break;
            case 17:
                iv_item_tram.setImageResource(R.mipmap.ic_uncheck);
                iv_item_solar.setImageResource(R.mipmap.ic_check);
                iv_item_island.setImageResource(R.mipmap.ic_check);
                iv_item_wind.setImageResource(R.mipmap.ic_uncheck);
                iv_item_output.setImageResource(R.mipmap.ic_uncheck);
                iv_item_charge.setImageResource(R.mipmap.ic_uncheck);
                iv_item_uninstall.setImageResource(R.mipmap.ic_uncheck);
                iv_item_discharge.setImageResource(R.mipmap.ic_uncheck);

                watts_tram.setText("0 kW");
                watts_solar.setText(100 + " kW");
                watts_wind.setText("0 kW");
                watts_generator.setText(200 + " kW");
                watts_soc.setText("0 kW");
                battery_soc.setText("20%");
                watts_build.setText(300 + " kW");
//                watts_control.setText(300 + "kW");

                if (animationDrawable_tram != null && animationDrawable_tram.isRunning())
                    animationDrawable_tram.stop();
                iv_tram_anim.setImageResource(R.drawable.anim_tram_normal);

                iv_solar_anim.setImageResource(R.drawable.anim_solar_output_low);
                animationDrawable_solar = (AnimationDrawable) iv_solar_anim.getDrawable();
                animationDrawable_solar.start();

                if (animationDrawable_wind != null && animationDrawable_wind.isRunning())
                    animationDrawable_wind.stop();
                iv_wind_anim.setImageResource(R.drawable.anim_wind_normal);

                iv_generator_anim.setImageResource(R.drawable.anim_generator_output_low);
                animationDrawable_generator = (AnimationDrawable) iv_generator_anim.getDrawable();
                animationDrawable_generator.start();

                if (animationDrawable_soc != null && animationDrawable_soc.isRunning())
                    animationDrawable_soc.stop();
                iv_soc_anim.setImageResource(R.drawable.anim_soc_normal);

                iv_build_anim.setImageResource(R.drawable.anim_build_output_low);
                animationDrawable_build = (AnimationDrawable) iv_build_anim.getDrawable();
                animationDrawable_build.start();

                if (iconTimer != null)
                    iconTimer.cancel();
                iconTimer = new Timer(true);
                iconTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (iconState) {
                                    iconState = false;
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_solar.setVisibility(View.VISIBLE);
                                    iv_wind.setVisibility(View.VISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_build039.setVisibility(View.VISIBLE);
                                    iv_build048.setVisibility(View.VISIBLE);
                                } else {
                                    iconState = true;
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_solar.setVisibility(View.INVISIBLE);
                                    iv_wind.setVisibility(View.VISIBLE);
                                    iv_generator.setVisibility(View.INVISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_build048.setVisibility(View.INVISIBLE);
                                    iv_build039.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }, 0, 500);
                break;
            case 18:
                iv_item_tram.setImageResource(R.mipmap.ic_uncheck);
                iv_item_solar.setImageResource(R.mipmap.ic_uncheck);
                iv_item_island.setImageResource(R.mipmap.ic_check);
                iv_item_wind.setImageResource(R.mipmap.ic_check);
                iv_item_output.setImageResource(R.mipmap.ic_uncheck);
                iv_item_charge.setImageResource(R.mipmap.ic_uncheck);
                iv_item_uninstall.setImageResource(R.mipmap.ic_uncheck);
                iv_item_discharge.setImageResource(R.mipmap.ic_uncheck);

                watts_tram.setText("0 kW");
                watts_solar.setText("0 kW");
                watts_wind.setText(200 + " kW");
                watts_generator.setText(100 + " kW");
                watts_soc.setText("0 kW");
                battery_soc.setText("20%");
                watts_build.setText(300 + " kW");
//                watts_control.setText(300 + "kW");

                if (animationDrawable_tram != null && animationDrawable_tram.isRunning())
                    animationDrawable_tram.stop();
                iv_tram_anim.setImageResource(R.drawable.anim_tram_normal);

                if (animationDrawable_solar != null && animationDrawable_solar.isRunning())
                    animationDrawable_solar.stop();
                iv_solar_anim.setImageResource(R.drawable.anim_solar_normal);

                iv_wind_anim.setImageResource(R.drawable.anim_wind_output_low);
                animationDrawable_wind = (AnimationDrawable) iv_wind_anim.getDrawable();
                animationDrawable_wind.start();

                iv_generator_anim.setImageResource(R.drawable.anim_generator_output_low);
                animationDrawable_generator = (AnimationDrawable) iv_generator_anim.getDrawable();
                animationDrawable_generator.start();

                if (animationDrawable_soc != null && animationDrawable_soc.isRunning())
                    animationDrawable_soc.stop();
                iv_soc_anim.setImageResource(R.drawable.anim_soc_normal);

                iv_build_anim.setImageResource(R.drawable.anim_build_output_low);
                animationDrawable_build = (AnimationDrawable) iv_build_anim.getDrawable();
                animationDrawable_build.start();

                if (iconTimer != null)
                    iconTimer.cancel();
                iconTimer = new Timer(true);
                iconTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (iconState) {
                                    iconState = false;
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_solar.setVisibility(View.VISIBLE);
                                    iv_wind.setVisibility(View.VISIBLE);
                                    iv_generator.setVisibility(View.VISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_build039.setVisibility(View.VISIBLE);
                                    iv_build048.setVisibility(View.VISIBLE);
                                } else {
                                    iconState = true;
                                    iv_tram.setVisibility(View.VISIBLE);
                                    iv_solar.setVisibility(View.VISIBLE);
                                    iv_wind.setVisibility(View.INVISIBLE);
                                    iv_generator.setVisibility(View.INVISIBLE);
                                    iv_control.setVisibility(View.VISIBLE);
                                    iv_soc.setVisibility(View.VISIBLE);
                                    iv_build048.setVisibility(View.INVISIBLE);
                                    iv_build039.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }, 0, 500);
                break;
        }
    }

    private void iconInit() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iv_tram.setVisibility(View.VISIBLE);
                iv_solar.setVisibility(View.VISIBLE);
                iv_wind.setVisibility(View.VISIBLE);
                iv_control.setVisibility(View.VISIBLE);
                iv_soc.setVisibility(View.VISIBLE);
                iv_build039.setVisibility(View.VISIBLE);
                iv_build048.setVisibility(View.VISIBLE);
            }
        });
    }


    @BindView(R.id.iv_tram_anim)
    ImageView iv_tram_anim;
    @BindView(R.id.iv_solar_anim)
    ImageView iv_solar_anim;
    @BindView(R.id.iv_wind_anim)
    ImageView iv_wind_anim;
    @BindView(R.id.iv_generator_anim)
    ImageView iv_generator_anim;
    @BindView(R.id.iv_soc_anim)
    ImageView iv_soc_anim;
    @BindView(R.id.iv_build_anim)
    ImageView iv_build_anim;

    @BindView(R.id.iv_item_tram)
    ImageView iv_item_tram;
    @BindView(R.id.iv_item_solar)
    ImageView iv_item_solar;
    @BindView(R.id.iv_item_island)
    ImageView iv_item_island;
    @BindView(R.id.iv_item_wind)
    ImageView iv_item_wind;
    @BindView(R.id.iv_item_output)
    ImageView iv_item_output;
    @BindView(R.id.iv_item_charge)
    ImageView iv_item_charge;
    @BindView(R.id.iv_item_uninstall)
    ImageView iv_item_uninstall;
    @BindView(R.id.iv_item_discharge)
    ImageView iv_item_discharge;

    @BindView(R.id.watts_tram)
    TextView watts_tram;
    @BindView(R.id.watts_wind)
    TextView watts_wind;
    @BindView(R.id.watts_solar)
    TextView watts_solar;
    @BindView(R.id.watts_soc)
    TextView watts_soc;
    @BindView(R.id.battery_soc)
    TextView battery_soc;
    @BindView(R.id.watts_generator)
    TextView watts_generator;
    @BindView(R.id.watts_control)
    TextView watts_control;
    @BindView(R.id.watts_build)
    TextView watts_build;

    @BindView(R.id.chart_power)
    LineChart chart;
    @BindView(R.id.tv_sec)
    TextView tv_sec;

    @BindView(R.id.et_mode)
    EditText et_mode;

    @BindView(R.id.cl_home)
    ConstraintLayout cl_home;
    @BindView(R.id.cl_des)
    ConstraintLayout cl_des;

    @BindView(R.id.iv_tram)
    ImageView iv_tram;
    @BindView(R.id.iv_solar)
    ImageView iv_solar;
    @BindView(R.id.iv_wind)
    ImageView iv_wind;
    @BindView(R.id.iv_generator)
    ImageView iv_generator;
    @BindView(R.id.iv_control)
    ImageView iv_control;
    @BindView(R.id.iv_soc)
    ImageView iv_soc;
    @BindView(R.id.iv_build039)
    ImageView iv_build039;
    @BindView(R.id.iv_build048)
    ImageView iv_build048;

    IntroDialogFragment dialog;

    @OnClick({R.id.bt_qa, R.id.bt_des, R.id.iv_home, R.id.bt_mode, R.id.iv_control, R.id.iv_tram, R.id.iv_solar, R.id.iv_wind, R.id.iv_generator, R.id.iv_soc, R.id.iv_build039, R.id.iv_build048})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bt_qa:
                activity.fragments.transaction(QA_Fragment.class);
                if (batteryTimer != null)
                    batteryTimer.cancel();
                break;
            case R.id.bt_des:
                cl_home.setVisibility(View.INVISIBLE);
                cl_des.setVisibility(View.VISIBLE);
                break;
            case R.id.iv_home:
                cl_home.setVisibility(View.VISIBLE);
                cl_des.setVisibility(View.INVISIBLE);
                mode_Rec = 0;
                break;
            case R.id.bt_mode:
                modeState = Integer.valueOf(String.valueOf(et_mode.getText()));
                if (!timerState)
                    setModeAct(modeState);
                break;
            case R.id.iv_control:
                dialog = IntroDialogFragment.newInstance(0);
                dialog.show(activity.getFragmentManager(), IntroDialogFragment.class.getSimpleName());
                break;
            case R.id.iv_tram:
                dialog = IntroDialogFragment.newInstance(1);
                dialog.show(activity.getFragmentManager(), IntroDialogFragment.class.getSimpleName());
                break;
            case R.id.iv_solar:
                dialog = IntroDialogFragment.newInstance(2);
                dialog.show(activity.getFragmentManager(), IntroDialogFragment.class.getSimpleName());
                break;
            case R.id.iv_wind:
                dialog = IntroDialogFragment.newInstance(3);
                dialog.show(activity.getFragmentManager(), IntroDialogFragment.class.getSimpleName());
                break;
            case R.id.iv_generator:
                dialog = IntroDialogFragment.newInstance(4);
                dialog.show(activity.getFragmentManager(), IntroDialogFragment.class.getSimpleName());
                break;
            case R.id.iv_soc:
                dialog = IntroDialogFragment.newInstance(5);
                dialog.show(activity.getFragmentManager(), IntroDialogFragment.class.getSimpleName());
                break;
            case R.id.iv_build039:
                dialog = IntroDialogFragment.newInstance(6);
                dialog.show(activity.getFragmentManager(), IntroDialogFragment.class.getSimpleName());
                break;
            case R.id.iv_build048:
                dialog = IntroDialogFragment.newInstance(6);
                dialog.show(activity.getFragmentManager(), IntroDialogFragment.class.getSimpleName());
                break;
        }
    }

    Unbinder unbinder;
}
