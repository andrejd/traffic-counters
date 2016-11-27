package com.kvajpoj.utils;

import android.animation.Animator;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.kvajpoj.R;
import com.kvajpoj.events.BusEvent;

import org.greenrobot.eventbus.EventBus;


public class SearchHelper {

    public static final String SEARCH_CLOSED = "SearchClosed";
    public static final String SEARCH_OPENED = "SearchOpened";
    public static final String SEARCH_QUERY = "SearchQuery";


    private CardView mSearchView;
    private ImageView mCleanSearch;
    private ImageView mCancelSearch;
    private EditText mEditText;
    private Context mContext;
    private TabLayout mTabs;
    private Toolbar mToolbar;

    private int mTabsHeight;

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }

    public void initSearch(final Context context,
                           final CardView searchView,
                           final ImageView clearSearch,
                           final ImageView exitSearch,
                           final EditText editText,
                           final TabLayout tabs,
                           final Toolbar toolbar) {

        mSearchView = searchView;
        mCleanSearch = clearSearch;
        mCancelSearch = exitSearch;
        mEditText = editText;
        mContext = context;
        mTabs = tabs;
        mTabsHeight = mTabs.getHeight();
        mToolbar = toolbar;

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                EventBus.getDefault().post(new BusEvent(SEARCH_QUERY, editText.getText().toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().length() == 0) {

                } else {

                    editText.setText("");
                    ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                            .toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

                }
            }
        });

        exitSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchHelper.this.toggleSearch();
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (editText.getText().toString().trim().length() > 0) {

                        ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                                .hideSoftInputFromWindow(mEditText.getWindowToken(), 0);

                    }
                    return true;
                }
                return false;
            }
        });
    }

    public void showHideKeyboard(boolean show) {
        if (!show) {
            ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        } else {
            mEditText.requestFocus();
            ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE))
                    .toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    public boolean isSearchVisible() {
        return mSearchView.getVisibility() == View.VISIBLE;
    }

    public void toggleSearch() {

        final Animation fade_in = AnimationUtils.loadAnimation(mContext.getApplicationContext(), android.R.anim.fade_in);
        final Animation fade_out = AnimationUtils.loadAnimation(mContext.getApplicationContext(), android.R.anim.fade_out);

        if (mTabsHeight == 0) mTabsHeight = mTabs.getHeight();

        final HeightAnimation collapse = new HeightAnimation(mTabs, mTabsHeight, 0);
        collapse.setDuration(100);

        final HeightAnimation expand = new HeightAnimation(mTabs, 0, mTabsHeight);
        expand.setDuration(100);

        if (mSearchView.getVisibility() == View.VISIBLE) { // gona hide it ...

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                final Animator animatorHide = ViewAnimationUtils.createCircularReveal(mSearchView,
                        mSearchView.getWidth() - (int) convertDpToPixel(70, mContext),
                        (int) convertDpToPixel(23, mContext),
                        (float) Math.hypot(mSearchView.getWidth(), mSearchView.getHeight()),
                        0);

                animatorHide.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mTabs.startAnimation(expand);
                        mToolbar.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimary));
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mSearchView.setVisibility(View.GONE);
                        showHideKeyboard(false);
                        EventBus.getDefault().post(new BusEvent(SEARCH_CLOSED));
                        mEditText.setText("");
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                animatorHide.setDuration(300);
                animatorHide.start();

            } else // older than lollipop
            {
                showHideKeyboard(false);
                mSearchView.setVisibility(View.GONE);
                mTabs.startAnimation(expand);
                mToolbar.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimary));
                EventBus.getDefault().post(new BusEvent(SEARCH_CLOSED));
                mEditText.setText("");
            }

            //
            mSearchView.setEnabled(false);
        } else //search is not visible, gona show it ...
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final Animator animator = ViewAnimationUtils.createCircularReveal(mSearchView,
                        mSearchView.getWidth() - (int) convertDpToPixel(70, mContext),
                        (int) convertDpToPixel(23, mContext),
                        0,
                        (float) Math.hypot(mSearchView.getWidth(), mSearchView.getHeight()));

                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mTabs.startAnimation(collapse);

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        showHideKeyboard(true);
                        EventBus.getDefault().post(new BusEvent(SEARCH_OPENED));
                        mToolbar.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimaryDark));
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

                mSearchView.setVisibility(View.VISIBLE);

                if (mSearchView.getVisibility() == View.VISIBLE) {
                    animator.setDuration(300);
                    animator.start();
                    mSearchView.setEnabled(true);
                }

                fade_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            } else // older than lollipop
            {
                mSearchView.setVisibility(View.VISIBLE);
                mSearchView.setEnabled(true);
                mTabs.startAnimation(collapse);
                mToolbar.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimaryDark));
                showHideKeyboard(true);
                EventBus.getDefault().post(new BusEvent(SEARCH_OPENED));
            }
        }
    }

    public class HeightAnimation extends Animation {
        protected final int originalHeight;
        protected final View view;
        protected float perValue;

        public HeightAnimation(View view, int fromHeight, int toHeight) {
            this.view = view;
            this.originalHeight = fromHeight;
            this.perValue = (toHeight - fromHeight);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            view.getLayoutParams().height = (int) (originalHeight + perValue * interpolatedTime);
            view.requestLayout();
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }
}