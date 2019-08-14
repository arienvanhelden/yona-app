/*
 *  Copyright (c) 2016 Stichting Yona Foundation
 *
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 *
 */

package de.blinkt.openvpn.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4n.view.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import de.blinkt.openvpn.R;
import de.blinkt.openvpn.fragments.GeneralSettings;
import de.blinkt.openvpn.fragments.LogFragment;
import de.blinkt.openvpn.fragments.SendDumpFragment;
import de.blinkt.openvpn.fragments.VPNProfileList;
import de.blinkt.openvpn.views.ScreenSlidePagerAdapter;
import de.blinkt.openvpn.views.SlidingTabLayout;
import de.blinkt.openvpn.views.TabBarView;


public class MainActivity extends BaseActivity {

    private static final String FEATURE_TELEVISION = "android.hardware.type.television";
    private static final String FEATURE_LEANBACK = "android.software.leanback";
    private ViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;
    private SlidingTabLayout mSlidingTabLayout;
    private Toolbar mToolBar;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager(), this);

        /* Toolbar and slider should have the same elevation */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            disableToolbarElevation();
        }


        mPagerAdapter.addTab(R.string.vpn_list_title, VPNProfileList.class);

        mPagerAdapter.addTab(R.string.generalsettings, GeneralSettings.class);

        if (SendDumpFragment.getLastestDump(this) != null) {
            mPagerAdapter.addTab(R.string.crashdump, SendDumpFragment.class);
        }

        if (isDirectToTV())
            mPagerAdapter.addTab(R.string.openvpn_log, LogFragment.class);

        TabBarView tabs = (TabBarView) findViewById(R.id.sliding_tabs);
        mPager.setAdapter(mPagerAdapter);
        tabs.setViewPager(mPager);

        // requestDozeDisable();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestDozeDisable() {
        Intent intent = new Intent();
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm.isIgnoringBatteryOptimizations(packageName))
            intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        else {
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
        }
        startActivity(intent);
    }

    private boolean isDirectToTV() {
        return (getPackageManager().hasSystemFeature(FEATURE_TELEVISION)
                || getPackageManager().hasSystemFeature(FEATURE_LEANBACK));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void disableToolbarElevation() {
        mToolBar = (Toolbar) findViewById(R.id.toolbar_layout);
        mToolBar.setElevation(0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.show_log) {
//            Intent showLog = new Intent(this, LogWindow.class);
//            startActivity(showLog);
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        System.out.println(data);


    }

    public Toolbar getmToolBar() {
        return this.mToolBar;
    }
}
