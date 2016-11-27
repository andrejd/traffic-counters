package com.kvajpoj.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.kvajpoj.R;
import com.kvajpoj.adapters.CountersListAdapter;
import com.kvajpoj.adapters.ViewPagerAdapter;
import com.kvajpoj.dao.CountersDAO;
import com.kvajpoj.dao.PreferencesDAO;
import com.kvajpoj.events.BusEvent;
import com.kvajpoj.fragments.FragmentCounters;
import com.kvajpoj.service.CountersUpdateService;
import com.kvajpoj.utils.SearchHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements
        AppBarLayout.OnOffsetChangedListener {

    private static final String SEARCH_TAPPED = "SearchTapped";
    private static final String TAB_SELECTED = "TabSelected";

    @Bind(R.id.app_bar)
    Toolbar mToolbar;
    @Bind(R.id.viewPager)
    ViewPager mPager;
    @Bind(R.id.tabs)
    TabLayout mTabs;
    @Bind(R.id.coordinator)
    CoordinatorLayout mCoordinator;
    @Bind(R.id.drawer_layout)
    DrawerLayout mLayout;
    @Bind(R.id.card_search)
    CardView card_search;
    @Bind(R.id.image_search_back)
    ImageView image_search_back;
    @Bind(R.id.clearSearch)
    ImageView clearSearch;
    @Bind(R.id.edit_text_search)
    EditText edit_text_search;
    @Bind(R.id.app_bar_layout)
    AppBarLayout mAppBarLayout;
    @Bind(R.id.toolbarWrapper)

    FrameLayout mToolbarWrapper;
    int index = 0;
    private ViewPagerAdapter mAdapter;
    private Object mCurrentFragment;
    private SearchHelper mSearchHelper;
    private MenuItem mSearchMenuItem;
    private Snackbar mSnackbarNoInternet;
    private String mLastSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupToolbar();
        setupPages();
        InitiateSearch();
    }

    ///////////////////////////////////////
    // EVENT HANDLERS
    ///////////////////////////////////////

    private void InitiateSearch() {

        mSearchHelper = new SearchHelper();
        mSearchHelper.initSearch(MainActivity.this, card_search, clearSearch, image_search_back, edit_text_search, mTabs, mToolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
    }

    private void setupPages() {

        mTabs.setTabTextColors(Color.WHITE, Color.WHITE);
        mTabs.setTabMode(TabLayout.MODE_SCROLLABLE);

        mAdapter = new ViewPagerAdapter(MainActivity.this, getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
        mTabs.setupWithViewPager(mPager);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Log.i("Pager", "Page scrolled ..."  + position);
            }

            @Override
            public void onPageSelected(int position) {
                EventBus.getDefault().post(new BusEvent(TAB_SELECTED, position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //Log.i("Pager", "Page scroll state changed ..."  + state);
            }
        });

        if (CountersDAO.getInstance().findFavorites(this).size() == 0) {
            mPager.setCurrentItem(ViewPagerAdapter.TAB_LIST);
            manageSearchIcon(true); // hide search
        }
        else {
            manageSearchIcon(false); // hide search
        }
    }

    @Subscribe
    public void onEvent(BusEvent event) {

        switch (event.getEventType()) {
            case CountersDAO.EVENT_DATA_LOAD:
                StopRefreshing();
                break;

            case CountersDAO.EVENT_DATA_LOAD_ERROR:
                StopRefreshing();
                Snackbar.make(mCoordinator, "Napaka pri povezovanju na strežnik!", Snackbar.LENGTH_LONG).show();
                break;

            case CountersDAO.EVENT_DATA_UP_TO_DATE:
                StopRefreshing();
                break;

            case CountersDAO.NO_INTERNET_CONNECTION:
                StopRefreshing();

                if (mSnackbarNoInternet != null) return;
                mSnackbarNoInternet = Snackbar.make(mLayout, "Nedelujoča internetna povezava!", Snackbar.LENGTH_INDEFINITE);
                mSnackbarNoInternet.getView().setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                mSnackbarNoInternet.setAction("ZAPRI", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mSnackbarNoInternet.dismiss();
                        mSnackbarNoInternet = null;
                    }
                })
                .setActionTextColor(getResources().getColor(R.color.colorAccent))
                .show();
                break;

            case FragmentCounters.SCROLL_DOWN:
                mSearchHelper.showHideKeyboard(false);
                break;

            case FragmentCounters.SCROLL_UP:
                mSearchHelper.showHideKeyboard(false);
                break;

            case FragmentCounters.REFRESH:
                mCurrentFragment = event.getSender();
                Intent intent = new Intent(Intent.ACTION_SYNC, null, this, CountersUpdateService.class);
                intent.putExtra("manual", true);
                startService(intent);
                break;

            case TAB_SELECTED:
                if (mSearchHelper.isSearchVisible()) mSearchHelper.toggleSearch();
                manageSearchIcon(event.getValue() == 0 ? false : true); // hide search
                break;

            case SearchHelper.SEARCH_CLOSED:
                setToolbarScrollingEnabled(true);
                mPager.setEnabled(true);
                break;

            case SearchHelper.SEARCH_OPENED:
                mPager.setEnabled(false);
                setToolbarScrollingEnabled(false);
                break;

            case SEARCH_TAPPED:
                mSearchHelper.toggleSearch();
                mAppBarLayout.setExpanded(true);
                break;

            case SearchHelper.SEARCH_QUERY:
                FragmentCounters list = (FragmentCounters) mAdapter.getItem(ViewPagerAdapter.TAB_LIST);
                String searchQuery = event.getInfo();
                if (!searchQuery.equals(mLastSearchQuery)) list.Search(event.getInfo());
                mLastSearchQuery = searchQuery;
                break;

            case CountersListAdapter.COUNTER_TAPPED:
                if (!PreferencesDAO.getInstance(this).isRefreshManual()) {
                    Intent detailIntent = new Intent(this, CounterDetailActivity.class);
                    detailIntent.putExtra("COUNTER_ID", event.getInfo());
                    startActivity(detailIntent);
                }
                break;

            default:
                break;
        }
    }

    private void StopRefreshing() {
        if (mCurrentFragment instanceof FragmentCounters) {
            ((FragmentCounters) mCurrentFragment).StopRefreshing();
        }
    }

    private void setToolbarScrollingEnabled(boolean value) {
        AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) mToolbarWrapper.getLayoutParams();

        if (value) {
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                    | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);

        } else {
            params.setScrollFlags(0);
        }
    }


    ///////////////////////////////////////
    // ACTIVITY OVERRIDES
    ///////////////////////////////////////

    // enables search menu item
    private void manageSearchIcon(Boolean show) {
        if (mSearchMenuItem != null) mSearchMenuItem.setVisible(show);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mSearchMenuItem = menu.findItem(R.id.action_search);

        if (mPager.getCurrentItem() == ViewPagerAdapter.TAB_FAVORITES) {
            mSearchMenuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, PreferenceActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_search) {
            EventBus.getDefault().post(new BusEvent(SEARCH_TAPPED, ""));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mSearchHelper.isSearchVisible()) {
            mSearchHelper.toggleSearch();
        } else {
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        mAppBarLayout.addOnOffsetChangedListener(this);

        // here we call getInstance to initialize PreferencesDao, if needed;
        // this will effectively start background update service
        PreferencesDAO.getInstance(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        int expiresTime = CountersDAO.getInstance().getDataExpiresTime();
        int currentTime = (int) (System.currentTimeMillis() / 1000L);

        if (expiresTime == 0 || (currentTime > expiresTime)) { // data on server changes every 10 minutes
            Log.i("Counters", "Refreshing counters as data has expired!");
            CountersDAO.getInstance().refreshCounters(MainActivity.this, true, true);
        }
        else {
            Log.i("Counters", "Counters data still valid! " + expiresTime + " < " + currentTime);
        }
    }

    @Override
    protected void onStop() {
        StopRefreshing();
        EventBus.getDefault().unregister(this);
        mAppBarLayout.removeOnOffsetChangedListener(this);
        super.onStop();
    }
    ///////////////////////////////////////
    // SWIPE TO REFRESH FIX
    ///////////////////////////////////////



    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        index = i;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                try {

                    FragmentCounters fr = (FragmentCounters) mAdapter.getItem(ViewPagerAdapter.TAB_LIST);
                    fr.setSwipeToRefreshEnabled(index == 0);
                    fr = (FragmentCounters) mAdapter.getItem(ViewPagerAdapter.TAB_FAVORITES);
                    fr.setSwipeToRefreshEnabled(index == 0);

                } catch (Exception ex) {

                    Log.e("Counters", ex.getMessage());

                }
                break;
        }

        return super.dispatchTouchEvent(ev);
    }

}
