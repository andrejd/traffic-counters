package com.kvajpoj.adapters;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kvajpoj.R;
import com.kvajpoj.dao.CountersDAO;
import com.kvajpoj.events.BusEvent;
import com.kvajpoj.models.Counter;
import com.kvajpoj.models.CounterEvent;
import com.kvajpoj.touch.ItemTouchHelperAdapter;
import com.kvajpoj.touch.ItemTouchHelperViewHolder;
import com.kvajpoj.touch.OnStartDragListener;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

/**
 * Created by Andrej on 29.8.2015.
 */
public class CountersListAdapter extends RealmRecyclerViewAdapter<Counter, CountersListAdapter.MyViewHolder>
        implements ItemTouchHelperAdapter {


    public static final String COUNTER_TAPPED = "CounterTapped";

    private final OnStartDragListener mDragStartListener;
    private int mPreviousPosition = 0;
    private int origPosition = -1;
    private int endPosition = -1;

    public CountersListAdapter(Context context, RealmResults realmResults,
                               boolean automaticUpdate,
                               OnStartDragListener dragStartListener) {

        super(context, realmResults, automaticUpdate);
        Context context1 = context;
        mDragStartListener = dragStartListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View view = inflater.inflate(R.layout.counter_list_item, viewGroup, false);
        MyViewHolder mvh = new MyViewHolder(view);
        return mvh;
    }

    /*private Realm getRealm(RealmResults<Counter> realmResults) {
        // "getRealm" is not accessible outside package io.realm
        Realm realm = null;

        try {
            Method method = RealmResults.class.getDeclaredMethod("getRealm");
            method.setAccessible(true);
            realm = (Realm)method.invoke(realmResults);
        } catch(NoSuchMethodException e) {
            e.printStackTrace();
        } catch(IllegalAccessException e) {
            e.printStackTrace();
        } catch(InvocationTargetException e) {
            e.printStackTrace();
        }
        //class defined in io.realm package so getRealm method is accessible
        //realm = getRealm(realmResults);
        return realm;
    }*/

    @Override
    public void onBindViewHolder(final MyViewHolder myViewHolder, final int position) {

        //final Counter tc = realmResults.get(position);
        final Counter tc = getData().get(position);
        final String tcId = tc.getId();


        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new BusEvent(COUNTER_TAPPED, tcId));
            }
        });


        String lane = "";

        if(tc.getLane().equals("(v)")) lane = "(vozni pas)";
        if(tc.getLane().equals("(p)")) lane = "(prehitevalni pas)";

        myViewHolder.title.setText(tc.getLocation() + ", " + tc.getDirection() + " " + lane);// + " " + tc.getEvents().last().getStatus() + "");
        //myViewHolder.subtitle.setText(tc.getStatus() + ", " + tc.getUpdated() + " " + tc.getEvents().size());

        CounterEvent e = tc.getEvents().last();

        String summary = "";
        String lblTime = "";

        if(e != null)
        {
            summary = e.getStatusOpis() + ", " + e.getNbrOfCars() + " vozil/h, povpr. hitrost "
                    //+ e.getAvgSpeed() + " km/h (omejitev " + e.getMaxSpeed() + " km/h), razmik " + e.getAvgGap() + " s";
                    + e.getAvgSpeed() + " km/h, razmik " + e.getAvgGap() + " s";

            lblTime = e.getTime().toUpperCase();
        }
        else
        {

        }


        myViewHolder.subtitle.setText(summary);
        //Log.i("Timestamp", tc.getEvents().last().getUpdated()+"");
        //myViewHolder.lblTime.setText(tc.getEvents().last().getTime().toUpperCase() + " " +  getCurrentTimeStamp(tc.getUpdated()).toUpperCase());
        //myViewHolder.lblTime.setText(getCurrentTimeStamp(tc.getUpdated()).toUpperCase());
        myViewHolder.lblTime.setText(lblTime);


        if (tc.getFavorite()) {
            myViewHolder.btnFavorites.setImageResource(R.drawable.ic_favorite);
        } else {
            myViewHolder.btnFavorites.setImageResource(R.drawable.ic_not_favorite);
        }


        if (mDragStartListener != null) {
            myViewHolder.btnFavorites.setImageResource(R.drawable.ic_handle);

            myViewHolder.btnFavorites.setOnClickListener(null);

            myViewHolder.btnFavorites.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.i("DragStatus", event.toString());
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        if (mDragStartListener != null)
                            mDragStartListener.onStartDrag(myViewHolder);
                    }
                    return false;
                }
            });
        } else {
            myViewHolder.btnFavorites.setOnTouchListener(null);
            myViewHolder.btnFavorites.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.i("Counters", "Favorites icon clicked!");

                    try{
                        Realm realm = Realm.getDefaultInstance();
                        Number newPostion = 1;

                        RealmResults<Counter> favs = CountersDAO.getInstance().findFavorites(realm);
                        if(favs.size() > 0) {
                            newPostion = favs.max("Position").intValue();
                        }
                        else{
                            newPostion = 0;
                        }

                        realm.beginTransaction();

                        //String id = tc.getId();
                        Boolean fav = tc.getFavorite();

                        Counter cnt = realm.where(Counter.class).equalTo("Id",tcId).findFirst();

                        cnt.setFavorite(!fav);

                        int pos = newPostion.intValue() + 1;
                        pos = !fav == true ? pos : 0;
                        cnt.setPosition(pos);

                        Log.i("Max position", newPostion + ", new position " + pos);
                        realm.commitTransaction();
                        realm.close();
                    }
                    catch(Exception ex)
                    {
                        Log.e("Counters", ex.toString());
                    }
                }
            });
        }


        if (tc.getEvents().last() != null) {
            switch (tc.getEvents().last().getStatus()) {
                case -1: //no data
                    myViewHolder.icon.setImageResource(R.drawable.ic_level_none);
                    break;

                case 0:
                    myViewHolder.icon.setImageResource(R.drawable.ic_level0);
                    break;

                case 1:
                    myViewHolder.icon.setImageResource(R.drawable.ic_level1);
                    break;

                case 2:
                    myViewHolder.icon.setImageResource(R.drawable.ic_level2);
                    break;

                case 3:
                    myViewHolder.icon.setImageResource(R.drawable.ic_level3);
                    break;

                case 4:
                    myViewHolder.icon.setImageResource(R.drawable.ic_level4);
                    break;

                case 5:
                    myViewHolder.icon.setImageResource(R.drawable.ic_level5);
                    break;

                case 6:
                    myViewHolder.icon.setImageResource(R.drawable.ic_level0);
                    break;

                default:
                    myViewHolder.icon.setImageResource(R.drawable.ic_level_none);
                    break;


            }
        }
        else
        {
            myViewHolder.icon.setImageResource(R.drawable.ic_level_none);
        }


        if (position > mPreviousPosition) {
            //AnimationUtils.animateSunblind(myViewHolder, true);
//            AnimationUtils.animateSunblind(holder, true);
            //AnimationUtils.animate1(myViewHolder, true);
            //AnimationUtils.animate(myViewHolder,true);
        } else {
            //AnimationUtils.animateSunblind(myViewHolder, false);
//            AnimationUtils.animateSunblind(holder, false);
            //AnimationUtils.animate1(myViewHolder, false);
            //AnimationUtils.animate(myViewHolder, false);
        }
        mPreviousPosition = position;

    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (origPosition == -1) origPosition = fromPosition;
        endPosition = toPosition;
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        //mItems.remove(position);
        //notifyItemRemoved(position);
        Log.i("Dismiss", "From position " + position);

    }


    // Object which will hold data displayed on screen; this data will be
    // changed in corelation with current item from the list displayed on screen
    // Class holds references to widgets in xml file
    class MyViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        TextView title;
        TextView lblTime;
        ImageView icon;
        TextView subtitle;
        ImageButton btnFavorites;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.lblCounterName);
            subtitle = (TextView) itemView.findViewById(R.id.lblCounterDetail1);
            icon = (ImageView) itemView.findViewById(R.id.iwStatus);
            btnFavorites = (ImageButton) itemView.findViewById(R.id.btnFavorite);
            lblTime = (TextView) itemView.findViewById(R.id.lblCounterTime);
        }

        @Override
        public void onItemSelected() {
            //itemView.setBackgroundColor(Color.LTGRAY);
            itemView.setAlpha((float) 0.7);

        }

        @Override
        public void onItemClear() {
            //itemView.setBackgroundColor(Color.BLUE);
            itemView.setAlpha((float) 1);

            if (origPosition == -1 || endPosition == -1 || (origPosition == endPosition)) return;

            Realm realm = Realm.getDefaultInstance();

            List<Counter> list = new ArrayList<>();
            list.addAll(getData());

            Log.i("SWAP", "Swaping position " + origPosition + " to position " + endPosition);

            Counter orig = list.remove(origPosition);
            list.add(endPosition, orig);


            //--------------------------
            realm.beginTransaction();

            int position = 0;
            for (int i = 0; i < list.size(); i++) {
                Counter c = list.get(i);
                position = list.size() - i;

                if (c.getPosition() != position) {
                    Log.i("SAVE", "changing position");
                    c.setPosition(position);
                }
            }
            realm.commitTransaction();
            realm.close();
            //---------------------

            if (mDragStartListener != null)
                mDragStartListener.onStopDrag(null);

            origPosition = -1;
            endPosition = -1;

        }
    }
}
