package com.kvajpoj.activities;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.kvajpoj.R;
import com.kvajpoj.service.ServiceConstants;

import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PreferenceActivity extends AppCompatActivity  {

    Toolbar mToolbar;

    private SettingsFragment mSettingsFragment;
    private String mBuildTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //mBuildTime = GetBuildTime();
        mToolbar = findViewById(R.id.toolbar);
        initToolbar();
        initPreferences();

        // hide actions in notification
        Intent hideIntent = new Intent(ServiceConstants.ACTION.HIDE_ACTION);
        sendBroadcast(hideIntent);
    }

    private void initPreferences() {

        mSettingsFragment = new SettingsFragment();
        mSettingsFragment.setBuildTime(mBuildTime);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, mSettingsFragment)
                .commit();
    }

    void initToolbar() {
        mToolbar.setSubtitle(getString(R.string.action_settings));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        Intent showIntent = new Intent(ServiceConstants.ACTION.SHOW_ACTION);
        sendBroadcast(showIntent);
        super.onPause();
    }

    /*private String GetBuildTime() {
        String s = "";

        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();
            s = SimpleDateFormat.getInstance().format(new java.util.Date(time));
            zf.close();
        } catch (Exception e) {
            Log.e("PreferenceActivity", e.getMessage());
        } finally {
            return s;
        }
    }*/

    public static class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

        Preference mRefreshOnBackgroundPreference;
        Preference mVersionPreference;
        String mBuildTime = "";

        public void setBuildTime(String buildTime) {
            mBuildTime = buildTime;
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.preferences);

            mVersionPreference = (Preference) findPreference("prefAppVersion");
            mVersionPreference.setSummary(R.string.appVersion);

            mRefreshOnBackgroundPreference = findPreference("prefRefreshOnBackground");
            mRefreshOnBackgroundPreference.setOnPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            if (preference.getKey().equals("prefRefreshOnBackground") && (Boolean) newValue) {
                getActivity().sendStickyBroadcast(new Intent(ServiceConstants.ACTION.HIDE_ACTION));
            }
            return true;
        }

    }

}