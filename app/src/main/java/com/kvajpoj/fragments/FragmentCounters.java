package com.kvajpoj.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kvajpoj.R;
import com.kvajpoj.adapters.CountersListAdapter;
import com.kvajpoj.dao.CountersDAO;
import com.kvajpoj.events.BusEvent;
import com.kvajpoj.models.Counter;
import com.kvajpoj.touch.OnStartDragListener;
import com.kvajpoj.touch.SimpleItemTouchHelperCallback;

import org.greenrobot.eventbus.EventBus;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.RealmResults;

/**
 * Created by Andrej on 10.9.2015.
 */
public class FragmentCounters extends Fragment implements OnStartDragListener {

    public static final String REFRESH = "Refresh";
    public static final String SCROLL_UP = "ScrollUp";
    public static final String SCROLL_DOWN = "ScrollDown";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public SwipeRefreshLayout mSwipeContainer;
    RecyclerView mCountersRecView;
    private ItemTouchHelper mItemTouchHelper;
    private String mParam1;
    private String mParam2;
    private int mScrollOffset = 4;
    private CountersListAdapter mAdapter;

    public FragmentCounters() {

    }

    public static FragmentCounters newInstance(String param1, String param2) {
        FragmentCounters fragment = new FragmentCounters();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);



    }

    public void StopRefreshing() {
        if(null != mSwipeContainer) mSwipeContainer.setRefreshing(false);
    }

    public void StartRefreshing() {
        if(mSwipeContainer != null) mSwipeContainer.setRefreshing(true);
    }

    public void Search(String queryString) {
        RealmResults<Counter> query;
        query = CountersDAO.getInstance().findAll(getActivity(), queryString);

        mAdapter = new CountersListAdapter(getActivity(), query, true, null);
        mCountersRecView.setAdapter(mAdapter);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_counters, container, false);

        mSwipeContainer = view.findViewById(R.id.swipeContainer);
        mCountersRecView = view.findViewById(R.id.counters_rec_view);


        mSwipeContainer.setOnRefreshListener(() -> EventBus.getDefault().post(new BusEvent(FragmentCounters.this, REFRESH, "")));

        mCountersRecView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (Math.abs(dy) > mScrollOffset) {
                    if (dy > 0) {
                        EventBus.getDefault().post(new BusEvent(SCROLL_DOWN, ""));
                    } else {
                        EventBus.getDefault().post(new BusEvent(SCROLL_UP, ""));
                    }
                }
            }
        });

        // finaly we return view
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RealmResults<Counter> list;

        Bundle bundle = getArguments();


        if (((String) bundle.get(ARG_PARAM1)).equals("Favorites")) {
            list = CountersDAO.getInstance().findFavorites(getActivity());
            mAdapter = new CountersListAdapter(getActivity(), list, true, this);


        }

        if (((String) bundle.get(ARG_PARAM1)).equals("List")) {
            list = CountersDAO.getInstance().findAll(getActivity(), "");
            mAdapter = new CountersListAdapter(getActivity(), list, true, null);
        }

        mCountersRecView.setAdapter(mAdapter);
        mCountersRecView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (((String) bundle.get(ARG_PARAM1)).equals("Favorites")) {
            ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
            ((SimpleItemTouchHelperCallback) callback).setSwipeEnabled(false);
            mItemTouchHelper = new ItemTouchHelper(callback);
            mItemTouchHelper.attachToRecyclerView(mCountersRecView);
        }


    }

    public void setSwipeToRefreshEnabled(boolean enabled) {
        if (mSwipeContainer != null) mSwipeContainer.setEnabled(enabled);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
        mSwipeContainer.setEnabled(false);
    }

    @Override
    public void onStopDrag(RecyclerView.ViewHolder viewHolder) {
        mSwipeContainer.setEnabled(true);
    }
}
