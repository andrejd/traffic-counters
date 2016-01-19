package com.kvajpoj.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.kvajpoj.R;
import com.kvajpoj.events.FinishedEvent;
import com.kvajpoj.events.ProcessEvent;
import com.kvajpoj.models.Counter;
import com.kvajpoj.models.CounterEvent;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Andrej on 3.10.2015.
 */
public class CounterDetailActivity extends AppCompatActivity implements
        OnChartValueSelectedListener,
        FloatingActionMenu.OnMenuToggleListener {

    private static final String LOG = "CounterDetailActivity";

    @Bind(R.id.chart1)
    CombinedChart mChart;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.lblCounterName)
    TextView mCounterName;
    @Bind(R.id.lblCounterTime)
    TextView mCounterTime;
    @Bind(R.id.lblCounterCars)
    TextView mCounterCars;
    @Bind(R.id.lblCounterSpeed)
    TextView mCounterSpeed;
    @Bind(R.id.lblCounterStatus)
    TextView mCounterStatus;
    @Bind(R.id.lblCounterTimeout)
    TextView mCounterTimeout;

    @Bind(R.id.lblCounterDetail1)
    TextView mCounterDetail;

    @Bind(R.id.fabOption1)
    FloatingActionButton mFabOption1;
    @Bind(R.id.fabOption2)
    FloatingActionButton mFabOption2;
    @Bind(R.id.fab_menu)
    FloatingActionMenu mFabMenu;

    @Bind(R.id.btnCousin1)
    Button mBtnCousin1;
    @Bind(R.id.btnCousin2)
    Button mBtnCousin2;
    @Bind(R.id.btnCousin3)
    Button mBtnCousin3;
    @Bind(R.id.btnCousin4)
    Button mBtnCousin4;

    @Bind(R.id.avLoadingIndicatorView)
    AVLoadingIndicatorView mLoadingIndicatorView;

    SimpleDateFormat mSDF = new SimpleDateFormat("HH:mm");
    private List<GraphEvent> mEvents;
    private List<String> mHours;
    private String mCounterId;
    private int mShowMode = ProcessEvent.HOUR08;
    private double mAverageSpeed = 0;
    private double mAverageGap = 0;
    private double mAverageCars = 0;
    private double mMaxSpeed = 0;
    private double mNbrCounterEvents = 0;
    private boolean mButtonsAreSet = false;
    private List<Button> btnCousins = new ArrayList<>(4);

    void showLoader() {
        mLoadingIndicatorView.setVisibility(View.VISIBLE);
    }
    void stopLoader() {
        mLoadingIndicatorView.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.counter_details_activity);
        ButterKnife.bind(this);
        initToolbar();
        EventBus.getDefault().register(this);

        setTitle(getString(R.string.counter_detail_activity_title));

        mFabMenu.setClosedOnTouchOutside(true);
        mFabMenu.setOnMenuToggleListener(this);


        // here we create collection of all cousin buttons
        btnCousins.add(0, mBtnCousin1);
        btnCousins.add(1, mBtnCousin2);
        btnCousins.add(2, mBtnCousin3);
        btnCousins.add(3, mBtnCousin4);

        mButtonsAreSet = false;

        // hide all cousin buttons
        for (int i = 0; i < btnCousins.size(); i++) {
            btnCousins.get(i).setVisibility(View.INVISIBLE);
        }

        // get starting counter id
        Intent intent = getIntent();

        if (!intent.getStringExtra("COUNTER_ID").equals("")) {
            initialize(intent.getStringExtra("COUNTER_ID"));
        }

        // set initial state of fabmenu button
        onMenuToggle(false);
    }

    private void initialize(String counterId) {

        int lastEventUpdateTime = 0;

        if (!counterId.equals("")) { // if we have id

            mCounterId = counterId;
            Counter tmp;
            Realm realm = null;

            showLoader();

            try {
                realm = Realm.getDefaultInstance();

                // get counter
                tmp = realm.where(Counter.class).equalTo("Id", mCounterId).findFirst();

                if (tmp != null) {
                    String lane = "";

                    //TODO: extract this and place it under counter method
                    if (tmp.getLane().equals("(v)")) lane = "(vozni pas)";
                    if (tmp.getLane().equals("(p)")) lane = "(prehitevalni pas)";

                    if (mButtonsAreSet == false) {

                        // get cousins
                        RealmResults<Counter> cousins = realm.where(Counter.class)
                                .equalTo("Location", tmp.getLocation())
                                //.findAllSorted("Id", true);
                                .findAllSorted("Id", Sort.ASCENDING);

                        for (int i = 0; i < cousins.size(); i++) {
                            final Counter c = cousins.get(i);

                            String laneC = "";

                            //TODO: extract this and place it under counter method
                            if (c.getLane().equals("(v)")) laneC = "(vozni pas)";
                            if (c.getLane().equals("(p)")) laneC = "(prehitevalni pas)";

                            Log.i("Counters", "Cousin " + i + ": " + c.getDirection() + " " + laneC);

                            Button btn = btnCousins.get(i);

                            btn.setEnabled(!mCounterId.equals(c.getId()));

                            btn.setText(c.getLocation() + ", " + c.getDirection() + " " + laneC);
                            btn.setVisibility(View.VISIBLE);
                            btn.setOnClickListener(null);
                            btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    for (int i = 0; i < btnCousins.size(); i++) {
                                        btnCousins.get(i).setEnabled(true);
                                    }
                                    ((Button) v).setEnabled(false);
                                    initialize(c.getId());

                                }
                            });

                            if (i == 3) break;
                        }
                        mButtonsAreSet = true;
                    }

                    mCounterName.setText(tmp.getLocation() + ", " + tmp.getDirection());

                    if (!lane.equals("")) {
                        mCounterName.setText(mCounterName.getText() + "\n" + lane);
                    }


                    CounterEvent last = tmp.getEvents().last();

                    if (last != null) {

                        mMaxSpeed = last.getMaxSpeed();
                        mNbrCounterEvents = tmp.getEvents().size();
                        lastEventUpdateTime = last.getUpdated();

                    } else {

                        mMaxSpeed = 0;
                        mNbrCounterEvents = 0;

                        Calendar c = Calendar.getInstance();
                        int minutes = c.get(Calendar.MINUTE);

                        minutes = minutes - (minutes % 5);

                        GregorianCalendar gc = new GregorianCalendar(c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH),
                                c.get(Calendar.DAY_OF_MONTH),
                                c.get(Calendar.HOUR_OF_DAY),
                                minutes);

                        lastEventUpdateTime = (int) (gc.getTimeInMillis() / 1000L);

                    }
                } else {

                    mCounterId = "";

                }
            } catch (Exception exception) {

                Log.e("Counters", "Error initializing data: " + exception.toString());
                stopLoader();

            } finally {

                if (realm != null) realm.close();
            }
        }

        mChart.setDescription("");
        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(false);
        mChart.setScaleEnabled(false);
        mChart.setScaleXEnabled(true);
        mChart.setPinchZoom(false);

        // draw bars behind lines
        mChart.setDrawOrder(new CombinedChart.DrawOrder[]{
            CombinedChart.DrawOrder.BAR,
            CombinedChart.DrawOrder.LINE,
        });

        mChart.getLegend().setEnabled(true);

        /*if (mNbrCounterEvents > 0) {

            mChart.setNoDataText("Samo trenutek ...");

        } else {

            mChart.setNoDataText("Ups, ta števec je brez podatkov ...");

        }*/

        mChart.setOnChartValueSelectedListener(null);
        mChart.setOnChartValueSelectedListener(this);

        // legend
        Legend l = mChart.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        l.setTextSize(9f);
        l.setTextColor(Color.WHITE);
        l.setXEntrySpace(2f); // set the space between the legend entries on the x-axis
        l.setYEntrySpace(5f); // set the space between the legend entries on the y-axis
        l.setFormToTextSpace(2f);
        l.setXOffset(0f);

        // set custom labels and colors
        l.setCustom(new int[]{
                        getResources().getColor(R.color.colorLevel1),
                        getResources().getColor(R.color.colorLevel2),
                        getResources().getColor(R.color.colorLevel3),
                        getResources().getColor(R.color.colorLevel4),
                        getResources().getColor(R.color.colorLevel5),
                        Color.TRANSPARENT,
                        Color.GRAY},
                new String[]{"", "", "", "", "ŠTEVILO VOZIL/URO", "", "POVPREČNA HITROST"});

        Paint p = mChart.getPaint(Chart.PAINT_INFO);
        p.setTextSize(30);
        p.setColor(Color.WHITE);

        // y axis
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setEnabled(false);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setTextColor(Color.WHITE);
        rightAxis.setEnabled(false);

        LimitLine ll = new LimitLine((float) mMaxSpeed, (int) mMaxSpeed + " km/h");
        ll.setLineColor(Color.GRAY);
        ll.setLineWidth(0.5f);
        ll.setTextSize(8f);
        ll.setTextColor(Color.GRAY);
        ll.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        ll.setLabel("");

        rightAxis.removeAllLimitLines();
        rightAxis.addLimitLine(ll);

        // x axis
        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setTextSize(9f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLimitLinesBehindData(false);


        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        mShowMode = sharedPref.getInt(getString(R.string.current_show_mode), ProcessEvent.HOUR08);


        //////////////// Dummy data generation
        // if we clear these values label would jumps around and create strange flicker

        // mAverageSpeed = 0;
        // mAverageGap = 0;
        // mAverageCars = 0;
        // mMaxSpeed = 0;


        List<String> dummyHours = generateDummyTimes(lastEventUpdateTime, mShowMode);
        String[] hours = new String[dummyHours.size()];
        hours = dummyHours.toArray(hours);

        CombinedData data = new CombinedData(dummyHours);
        data.setData(generateDummyBarData(dummyHours.size()));
        data.setData(generateDummyLineData(dummyHours.size()));

        mToolbar.setSubtitle("v zadnjih 24 urah");
        String msg = "V zadnjih 24 urah je bila povprečna hitrost ";

        switch (mShowMode) {
            case ProcessEvent.HOUR08:
                mToolbar.setSubtitle("v zadnjih 8 urah");
                msg = "V zadnjih 8 urah je bila povprečna hitrost ";
                break;

            case ProcessEvent.HOUR01:
                mToolbar.setSubtitle("v zadnji uri");
                msg = "V zadnji uri je bila povprečna hitrost ";
                break;
        }


        //mCounterDetail.setText(msg + (int) mAverageSpeed + " km/h pri dovoljeni hitrosti " + (int) mMaxSpeed + " km/h, povprečno je po cesti peljalo " + (int) mAverageCars +
        //        " vozil/h s povprečnim razmikom " + (int) mAverageGap + " sekund med dvema voziloma");

        //mCounterDetail.setText(msg + "    km/h pri dovoljeni hitrosti      km/h, povprečno je po cesti peljalo     vozil/h s povprečnim razmikom "
        //        + "    sekund med dvema voziloma");

        mCounterTime.setText("");
        mCounterCars.setText("");
        mCounterTimeout.setText("");
        mCounterStatus.setText(R.string.no_data);
        mCounterSpeed.setText("");

        mChart.setData(data);
        mChart.invalidate();

        mChart.highlightValue(-1, 0);

        // end of dummy data generation
        /////////////////////////////////////////////////////////////

        // generate real data

        EventBus.getDefault().post(new ProcessEvent(mShowMode));
    }

    private String formatDate(int timeStampInSeconds) {

        return mSDF.format(new Date(timeStampInSeconds * 1000L));
    }


    @Override
    public void onMenuToggle(final boolean isOpen) {

        if (!isOpen) {
            if (mShowMode == ProcessEvent.HOUR24) {

                mFabOption1.setImageResource(R.drawable.ic_1h);
                mFabOption2.setImageResource(R.drawable.ic_8h);

            } else if (mShowMode == ProcessEvent.HOUR01) {

                mFabOption1.setImageResource(R.drawable.ic_8h);
                mFabOption2.setImageResource(R.drawable.ic_24h);

            } else if (mShowMode == ProcessEvent.HOUR08) {

                mFabOption1.setImageResource(R.drawable.ic_24h);
                mFabOption2.setImageResource(R.drawable.ic_1h);

            }
        }
    }


    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
        finish();
    }


    public List<String> generateDummyTimes(int lastTime, int mode) {

        int showTime = 60 * 60 * 24;
        if (mode == ProcessEvent.HOUR08) showTime = 60 * 60 * 8;
        if (mode == ProcessEvent.HOUR01) showTime = 60 * 60 * 1;

        int firstTime = lastTime - showTime;

        int eventTime;
        /*int lastEvent = 0;
        int index = 0;
        int valueCnt = 0;
        boolean skipAdding = true;*/

        List<String> mDummyHours = new ArrayList<>();
        mHours = new ArrayList<>();

        for (int t = firstTime; t <= lastTime; t += (5 * 60)) {

            eventTime = t;
            String formatedDate = formatDate(eventTime);
            mDummyHours.add(formatedDate);
            mHours.add(formatedDate);

        }

        return mDummyHours;
    }


    @Subscribe(threadMode = ThreadMode.Async)
    public void process(ProcessEvent event) {

        // data calculation
        // get last event time; this will be our end time

        Counter cnt = null;
        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();
            cnt = realm.where(Counter.class).equalTo("Id", mCounterId).findFirst();

            int mode = event.getMode();

            RealmList<CounterEvent> counterEvents = cnt.getEvents();

            CounterEvent last = counterEvents.last();

            if (last != null) {

                int lastTime = last.getUpdated(); //time in seconds from 1.1.1970

                // now we get time for first event, lastTime - 24h, older will be dumped
                int showTime = 60 * 60 * 24;

                if (mode == ProcessEvent.HOUR08) showTime = 60 * 60 * 8;
                if (mode == ProcessEvent.HOUR01) showTime = 60 * 60 * 1;

                int firstTime = lastTime - showTime; // 60 seconds * 60 minutes *24 hours

                int eventTime;
                int lastEvent = 0;
                int index = 0;
                int valueCnt = 0;
                boolean skipAdding = true;

                mEvents = new ArrayList<>();
                mHours = new ArrayList<>();

                mAverageCars = 0;
                mAverageSpeed = 0;
                mAverageGap = 0;

                for (int t = firstTime; t <= lastTime; t += (5 * 60)) {

                    eventTime = t;
                    // build proxy event
                    GraphEvent ge = null;

                    for (int i = lastEvent; i < counterEvents.size(); i++) {

                        CounterEvent cev = counterEvents.get(i);

                        if (cev.getUpdated() == eventTime) {
                            ge = new GraphEvent();

                            ge.setId(cev.getId());
                            ge.setCars(cev.getNbrOfCars());
                            mAverageCars += ge.getCars();

                            ge.setSpeed(cev.getAvgSpeed());
                            mAverageSpeed += ge.getSpeed();

                            ge.setStatus(cev.getStatus());
                            ge.setEvent(cev);
                            ge.setTimeString(cev.getTime());
                            ge.setAvgGap(cev.getAvgGap());
                            mAverageGap += Double.parseDouble(ge.getAvgGap().replace(",", "."));

                            ge.setStatusString(cev.getStatusOpis());

                            valueCnt += 1;

                            lastEvent = i;
                            break;
                        }

                        if (cev.getUpdated() < eventTime) {
                            lastEvent = i;
                        }

                    }

                    if (ge == null) {
                        ge = new GraphEvent();
                        ge.setStatus(-1);
                    }

                    ge.setTime(eventTime);

                    // if ge has no event it means it is fake
                    if (ge.getEvent() == null) {
                        if (skipAdding == false) {
                            mHours.add(formatDate(eventTime));
                            mEvents.add(ge);
                        }
                    } else {
                        skipAdding = false;
                        mHours.add(formatDate(eventTime));
                        mEvents.add(ge);
                    }

                    index++;
                }

                mAverageCars /= valueCnt;
                mAverageSpeed /= valueCnt;
                mAverageGap /= valueCnt;
            }

            EventBus.getDefault().post(new FinishedEvent());

        } catch (Exception exception) {

            Log.e("Counters", "Error processing data:" + exception.toString());

        } finally {

            if (realm != null) realm.close();

        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEvent(FinishedEvent event) {
        String[] hours = new String[mHours.size()];
        hours = mHours.toArray(hours);

        CombinedData data = new CombinedData(hours);
        data.setData(generateLineData());
        data.setData(generateBarData());

        mToolbar.setSubtitle("v zadnjih 24 urah");
        String msg = "V zadnjih 24 urah je bila povprečna hitrost ";

        switch (mShowMode) {
            case ProcessEvent.HOUR08:
                mToolbar.setSubtitle("v zadnjih 8 urah");
                msg = "V zadnjih 8 urah je bila povprečna hitrost ";
                break;

            case ProcessEvent.HOUR01:
                mToolbar.setSubtitle("v zadnji uri");
                msg = "V zadnji uri je bila povprečna hitrost ";
                break;
        }

        mCounterDetail.setText(msg + (int) mAverageSpeed + " km/h pri dovoljeni hitrosti " + (int) mMaxSpeed + " km/h, povprečno je po cesti peljalo " + (int) mAverageCars +
                " vozil/h s povprečnim razmikom " + (int) mAverageGap + " sekund med dvema voziloma");

        mChart.setData(data);
        mChart.animateY(1000, Easing.EasingOption.EaseInSine);

        if (mEvents.size() > 0) {
            mChart.highlightValue(mEvents.size() - 1, 0);
            updateUI(mEvents.size() - 1);
        }

        stopLoader();
    }


    @OnClick({R.id.fabOption1, R.id.fabOption2})
    public void onOptionClick(FloatingActionButton button) {
        mFabMenu.close(true);

        if (button == mFabOption1) {
            if (mShowMode == ProcessEvent.HOUR01) mShowMode = ProcessEvent.HOUR08;
            else if (mShowMode == ProcessEvent.HOUR08) mShowMode = ProcessEvent.HOUR24;
            else if (mShowMode == ProcessEvent.HOUR24) mShowMode = ProcessEvent.HOUR01;
        } else {
            if (mShowMode == ProcessEvent.HOUR01) mShowMode = ProcessEvent.HOUR24;
            else if (mShowMode == ProcessEvent.HOUR08) mShowMode = ProcessEvent.HOUR01;
            else if (mShowMode == ProcessEvent.HOUR24) mShowMode = ProcessEvent.HOUR08;
        }

        // remember last used show mode
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.current_show_mode), mShowMode);
        editor.commit();

        initialize(mCounterId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private LineData generateDummyLineData(int size) {

        long startTime = System.currentTimeMillis();

        List<GraphEvent> mDummyEvents = new ArrayList<>();
        ArrayList<Entry> dummyEntries = new ArrayList<Entry>();

        for (int i = 0; i < size; i++) {
            mDummyEvents.add(new GraphEvent());
        }

        LineData d = new LineData();

        if (mDummyEvents != null && mDummyEvents.size() > 0) {

            for (int i = 0; i < mDummyEvents.size(); i++) {

                GraphEvent ge = mDummyEvents.get(i);

                // check if speed is 0 and event is null -> to interpolate value
                Entry ld = new Entry(ge.getSpeed(), i);
                dummyEntries.add(ld);
            }
        }

        LineDataSet set = new LineDataSet(dummyEntries, "Line DataSet");
        set.setColor(Color.GRAY);
        set.setLineWidth(1.0f);
        set.setDrawCircleHole(false);
        set.setDrawCircles(false);
        set.setFillColor(Color.rgb(240, 238, 70));
        set.setDrawCubic(true);
        set.setDrawValues(false);
        set.setHighLightColor(Color.WHITE);
        set.setHighlightLineWidth(1.5f);
        set.setDrawHighlightIndicators(true);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setAxisDependency(YAxis.AxisDependency.RIGHT);

        d.addDataSet(set);

        return d;
    }


    private LineData generateLineData() {

        LineData d = new LineData();
        ArrayList<Entry> entries = new ArrayList<Entry>();

        if (mEvents != null && mEvents.size() > 0) {
            for (int i = 0; i < mEvents.size(); i++) {
                GraphEvent ge = mEvents.get(i);

                // check if speed is 0 and event is null -> to interpolate value
                if (ge.getSpeed() == 0 && ge.getEvent() == null)
                    ge.setSpeed((int) mAverageSpeed);

                Entry ld = new Entry(ge.getSpeed(), i);
                entries.add(ld);
            }
        }

        LineDataSet set = new LineDataSet(entries, "Line DataSet");
        set.setColor(Color.GRAY);
        set.setLineWidth(1.0f);
        set.setDrawCircleHole(false);
        set.setDrawCircles(false);
        set.setFillColor(Color.rgb(240, 238, 70));
        set.setDrawCubic(true);
        set.setDrawValues(false);
        set.setHighLightColor(Color.WHITE);
        set.setHighlightLineWidth(1.5f);
        set.setDrawHighlightIndicators(true);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setAxisDependency(YAxis.AxisDependency.RIGHT);

        d.addDataSet(set);
        return d;
    }


    private BarData generateDummyBarData(int size) {

        BarData d = new BarData();

        List<GraphEvent> mDummyEvents = new ArrayList<>();
        ArrayList<BarEntry> dummyEntries = new ArrayList<BarEntry>();
        mEvents = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            mDummyEvents.add(new GraphEvent());
        }

        int[] colors = new int[mDummyEvents.size()];

        if (mDummyEvents.size() > 0) {

            for (int i = 0; i < mDummyEvents.size(); i++) {
                GraphEvent ge = mDummyEvents.get(i);
                ge.setStatus(-1);
                mEvents.add(ge);
                BarEntry be = new BarEntry(ge.getCars(), i);
                colors[i] = R.color.colorLevel6;
                dummyEntries.add(be);
            }
        }

        BarDataSet set = new BarDataSet(dummyEntries, "");
        set.setColors(colors, this);
        set.setDrawValues(false);
        set.setValueTextColor(Color.rgb(60, 220, 78));
        set.setBarSpacePercent(0);
        set.setValueTextSize(10f);
        d.addDataSet(set);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        return d;
    }

    private BarData generateBarData() {

        BarData d = new BarData();

        ArrayList<BarEntry> entries = new ArrayList<BarEntry>();
        int[] colors = new int[mEvents.size()];

        if (mEvents.size() > 0)
        {
            for (int i = 0; i < mEvents.size(); i++) {
                GraphEvent ge = mEvents.get(i);

                BarEntry be = new BarEntry(ge.getCars(), i);


                switch (ge.getStatus()) {
                    case -1: //no data
                        colors[i] = R.color.colorLevel6;
                        break;
                    case 0:
                        colors[i] = R.color.colorLevel0;
                        break;

                    case 1:
                        colors[i] = R.color.colorLevel1;
                        break;

                    case 2:
                        colors[i] = R.color.colorLevel2;
                        break;

                    case 3:
                        colors[i] = R.color.colorLevel3;
                        break;

                    case 4:
                        colors[i] = R.color.colorLevel4;
                        break;

                    case 5:
                        colors[i] = R.color.colorLevel5;
                        break;

                    case 6:
                        colors[i] = R.color.colorLevel6;
                        break;

                    default:
                        colors[i] = R.color.colorLevel6;
                        break;
                }
                entries.add(be);
            }
        }


        BarDataSet set = new BarDataSet(entries, "");
        set.setColors(colors, this);

        set.setDrawValues(false);
        set.setValueTextColor(Color.rgb(60, 220, 78));
        set.setBarSpacePercent(0);
        set.setValueTextSize(10f);

        d.addDataSet(set);

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        return d;
    }


    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

        try {

            updateUI(e.getXIndex());
        }
        catch (Exception ex) {

            Log.e(LOG, "Exception during item selection " + ex.toString());

        }

    }

    private void updateUI(int index) {

        try {

            GraphEvent ge = mEvents.get(index);

            if (ge.getEvent() == null) {

                mCounterTime.setText(mHours.get(index));
                mCounterCars.setText("");
                mCounterTimeout.setText("");
                mCounterStatus.setText(R.string.no_data);
                mCounterSpeed.setText("");

            } else {

                mCounterCars.setText(ge.getCars() + " vozil/h");
                mCounterTimeout.setText("razmik " + ge.getAvgGap() + " s");
                mCounterStatus.setText(ge.getStatusString());
                mCounterSpeed.setText(ge.getSpeed() + " km/h");
                mCounterTime.setText(ge.getTimeString());

            }

        } catch (Exception ex) {

            Log.e("Counters", "Exception during graph data selection: " + ex.toString());
            mCounterTime.setText(mHours.get(index));
            mCounterCars.setText("");
            mCounterTimeout.setText("");
            mCounterStatus.setText(R.string.no_data);
            mCounterSpeed.setText("");
        }
    }

    @Override
    public void onNothingSelected() {

    }

    private class GraphEvent {

        int time;
        String id = "";
        int cars = 0;
        int speed = 0;
        String description = "";
        String statusString = "";
        String avgGap = "";
        CounterEvent event = null;
        int status = 0;
        String timeString = "";

        public String getAvgGap() {
            return avgGap;
        }

        public void setAvgGap(String avgGap) {
            this.avgGap = avgGap;
        }

        public String getStatusString() {
            return statusString;
        }

        public void setStatusString(String statusString) {
            this.statusString = statusString;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getTimeString() {
            return timeString;
        }

        public void setTimeString(String timeString) {
            this.timeString = timeString;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public CounterEvent getEvent() {
            return event;
        }

        public void setEvent(CounterEvent event) {
            this.event = event;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getCars() {
            return cars;
        }

        public void setCars(int cars) {
            this.cars = cars;
        }

        public int getSpeed() {
            return speed;
        }

        public void setSpeed(int speed) {
            this.speed = speed;
        }
    }

}

