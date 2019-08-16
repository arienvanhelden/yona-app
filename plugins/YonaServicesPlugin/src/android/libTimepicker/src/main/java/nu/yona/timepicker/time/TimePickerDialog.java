/*
 * Copyright (c) 2016 Stichting Yona Foundation
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nu.yona.timepicker.time;

import android.animation.ObjectAnimator;
import android.app.ActionBar.LayoutParams;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import nu.yona.timepicker.AccessibleTextView;
import nu.yona.timepicker.HapticFeedbackController;
import nu.yona.timepicker.R;
import nu.yona.timepicker.TypefaceHelper;
import nu.yona.timepicker.Utils;
import nu.yona.timepicker.time.RadialPickerLayout.OnValueSelectedListener;

/**
 * Dialog to set a time.
 */
public class TimePickerDialog extends DialogFragment implements
        OnValueSelectedListener, TimePickerController, TabHost.OnTabChangeListener {

    private static final String TAG = "TimePickerDialog";

    private static final String KEY_INITIAL_TIME = "initial_time";
    private static final String KEY_IS_24_HOUR_VIEW = "is_24_hour_view";
    private static final String KEY_TITLE = "dialog_title";
    private static final String KEY_CURRENT_ITEM_SHOWING = "current_item_showing";
    private static final String KEY_IN_KB_MODE = "in_kb_mode";
    private static final String KEY_TYPED_TIMES = "typed_times";
    private static final String KEY_THEME_DARK = "theme_dark";
    private static final String KEY_THEME_DARK_CHANGED = "theme_dark_changed";
    private static final String KEY_ACCENT = "accent";
    private static final String KEY_VIBRATE = "vibrate";
    private static final String KEY_DISMISS = "dismiss";
    private static final String KEY_SELECTABLE_TIMES = "selectable_times";
    private static final String KEY_MIN_TIME = "min_time";
    private static final String KEY_MAX_TIME = "max_time";
    private static final String KEY_ENABLE_SECONDS = "enable_seconds";
    private static final String KEY_OK_RESID = "ok_resid";
    private static final String KEY_OK_STRING = "ok_string";
    private static final String KEY_CANCEL_RESID = "cancel_resid";
    private static final String KEY_CANCEL_STRING = "cancel_string";

    public static final int HOUR_INDEX = 0;
    public static final int MINUTE_INDEX = 1;
    public static final int SECOND_INDEX = 2;
    public static final int AM = 0;
    public static final int PM = 1;

    // Delay before starting the pulse animation, in ms.
    private static final int PULSE_ANIMATOR_DELAY = 300;

    private OnTimeSetListener mCallback;
    private OnTimeSelected mTimeSelectedCallback;
    private DialogInterface.OnCancelListener mOnCancelListener;
    private DialogInterface.OnDismissListener mOnDismissListener;

    private HapticFeedbackController mHapticFeedbackController;

    private Button mCancelButton;
    private Button mNextButton;
    private Button mPreviousButton;
    private Button mOkButton;
    private TextView mHourView;
    private TextView mHourViewEnd;
    private TextView mHourSpaceView;
    private TextView mHourSpaceViewEnd;
    private TextView mMinuteView;
    private TextView mMinuteViewEnd;
    private TextView mMinuteSpaceView;
    private TextView mMinuteSpaceViewEnd;
    private TextView mSecondView;
    private TextView mSecondSpaceView;
    private TextView mAmPmTextView;
    private TextView mErrorMsg;
    private View mAmPmHitspace;
    private RadialPickerLayout mTimePicker;
    private RadialPickerLayout mTimePickerEnd;
    private View nextBackgroundView;
    private View doneBackgroudnView;

    private int mSelectedColor;
    private int mUnselectedColor;
    private String mAmText;
    private String mPmText;

    private boolean mIsDualScreenMode;
    private boolean mAllowAutoAdvance;
    private Timepoint mInitialTime;
    private boolean mIs24HourMode;
    private String mTitle;
    private boolean mThemeDark;
    private boolean mThemeDarkChanged;
    private boolean mVibrate;
    private int mAccentColor = -1;
    private boolean mDismissOnPause;
    private Timepoint[] mSelectableTimes;
    private Timepoint mMinTime;
    private Timepoint mMaxTime;
    private Timepoint mFirstTime;
    private Timepoint mSecondTime;
    private boolean mEnableSeconds;
    private int mOkResid;
    private String mOkString;
    private int mCancelResid;
    private String mCancelString;

    // For hardware IME input.
    private char mPlaceholderText;
    private String mDoublePlaceholderText;
    private String mDeletedKeyFormat;
    private boolean mInKbMode;
    private ArrayList<Integer> mTypedTimes;
    private Node mLegalTimesTree;
    private int mAmKeyCode;
    private int mPmKeyCode;

    // Accessibility strings.
    private String mHourPickerDescription;
    private String mSelectHours;
    private String mMinutePickerDescription;
    private String mSelectMinutes;
    private String mSecondPickerDescription;
    private String mSelectSeconds;
    private TabHost tabHost;

    @Override
    public void onTabChanged(String tabId) {
        Log.i(TimePickerDialog.class.getName(), "tabId..." + tabId);
        if (tabId.equals(getString(R.string.from))) {
            callPrevious();
        } else if (tabId.equals(getString(R.string.to))) {
            callNext();
        }
        updateTab(tabId);
    }

    private void updateTab(String tabId) {
        if (tabId.equals(getString(R.string.from))) {
            if (tabHost.getTabWidget().getChildCount() > 1) {
                ImageView iv = (ImageView) tabHost.getTabWidget().getChildAt(0).findViewById(R.id.tabIndicator);
                iv.setVisibility(View.VISIBLE);
                ImageView iv1 = (ImageView) tabHost.getTabWidget().getChildAt(1).findViewById(R.id.tabIndicatorEnd);
                iv1.setVisibility(View.GONE);
            } else {
                ImageView iv = (ImageView) tabHost.getTabWidget().getChildAt(0).findViewById(R.id.tabIndicator);
                iv.setVisibility(View.VISIBLE);
            }
        } else if (tabId.equals(getString(R.string.to))) {
            ImageView iv = (ImageView) tabHost.getTabWidget().getChildAt(0).findViewById(R.id.tabIndicator);
            iv.setVisibility(View.GONE);
            ImageView iv1 = (ImageView) tabHost.getTabWidget().getChildAt(1).findViewById(R.id.tabIndicatorEnd);
            iv1.setVisibility(View.VISIBLE);
        }
    }

    /**
     * The callback interface used to indicate the user is done filling in
     * the time (they clicked on the 'Set' button).
     */
    public interface OnTimeSetListener {

        /**
         * @param view      The view associated with this listener.
         * @param hourOfDay The hour that was set.
         * @param minute    The minute that was set.
         * @param second    The second that was set
         */
        void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second);
    }

    /**
     * The interface On time set listener.
     */
    public interface OnTimeSelected {
        /**
         * Sets time.
         *
         * @param firstTime, secondTime
         */
        void setTime(Timepoint firstTime, Timepoint secondTime);
    }


    private void callNext() {
        Timepoint minTimePoint = new Timepoint(mTimePicker.getHours(), mTimePicker.getMinutes(), mTimePicker.getSeconds());
        if (setFirstTime(minTimePoint)) {
            updateTOTTimePicker(mSecondTime);
            onValueSelected(mSecondTime);
            nextBackgroundView.setVisibility(View.GONE);
            doneBackgroudnView.setVisibility(View.VISIBLE);
            //tabHost.setCurrentTab(1);
        }
    }

    private void callPrevious() {
        //todo goto previous screen with previous selected time and update on that screen
        if (mTimePickerEnd != null) {
            Timepoint maxTimePoint = new Timepoint(mTimePickerEnd.getHours(), mTimePickerEnd.getMinutes(), mTimePickerEnd.getSeconds());
            if (setSecondTime(maxTimePoint)) {
                updateVANTimePicker(mFirstTime);
                onValueSelected(mFirstTime);
                doneBackgroudnView.setVisibility(View.GONE);
                nextBackgroundView.setVisibility(View.VISIBLE);
                //tabHost.setCurrentTab(0);
            }
        }
    }

    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int i = v.getId();
            if (i == R.id.ok) {
                if (mIsDualScreenMode) {
                    Timepoint maxTimePoint = new Timepoint(mTimePickerEnd.getHours(), mTimePickerEnd.getMinutes(), mTimePickerEnd.getSeconds());
                    if (setSecondTime(maxTimePoint) && checkValidTimeSelection() && mTimeSelectedCallback != null) {
                        if (mTimeSelectedCallback != null) {
                            mTimeSelectedCallback.setTime(mFirstTime, mSecondTime);
                        }
                        tryVibrate();
                        if (getDialog() != null) getDialog().cancel();
                    }
                } else {
                    Timepoint minTimePoint = new Timepoint(mTimePicker.getHours(), mTimePicker.getMinutes(), mTimePicker.getSeconds());
                    if (setFirstTime(minTimePoint)) {
                        if (mTimeSelectedCallback != null) {
                            mTimeSelectedCallback.setTime(mFirstTime, mSecondTime);
                        }
                        tryVibrate();
                        if (getDialog() != null) getDialog().cancel();
                    }
                }

            } else if (i == R.id.next) {
                tabHost.setCurrentTab(1);
            } else if (i == R.id.cancel) {
                tryVibrate();
                if (getDialog() != null) getDialog().cancel();
                return;
            } else if (i == R.id.previous) {
                tabHost.setCurrentTab(0);
            }
        }
    };

    public TimePickerDialog() {
        // Empty constructor required for dialog fragment.
    }

    /**
     * @param callback
     * @param hourOfDay
     * @param minute
     * @param second
     * @param is24HourMode
     * @return
     */
    public static TimePickerDialog newInstance(OnTimeSetListener callback,
                                               int hourOfDay, int minute, int second, boolean is24HourMode) {
        TimePickerDialog ret = new TimePickerDialog();
        ret.initialize(callback, hourOfDay, minute, second, is24HourMode);
        return ret;
    }

    /**
     * @param callback
     * @param fTimePoint
     * @param sTimePoint
     * @param mIs24HourMode
     * @param mIsDualScreenMode
     * @return
     */
    public static TimePickerDialog newInstance(OnTimeSelected callback, Timepoint fTimePoint, Timepoint sTimePoint, boolean mIs24HourMode, boolean mIsDualScreenMode) {
        TimePickerDialog timePickerDialog = new TimePickerDialog();
        timePickerDialog.initialize(callback, fTimePoint, sTimePoint, mIs24HourMode, mIsDualScreenMode);
        return timePickerDialog;
    }

    public static TimePickerDialog newInstance(OnTimeSetListener callback,
                                               int hourOfDay, int minute, boolean is24HourMode) {
        return TimePickerDialog.newInstance(callback, hourOfDay, minute, 0, is24HourMode);
    }

    /**
     * @param callback
     * @param tPointFirst
     * @param tPointSecond
     * @param is24HourMode
     * @param isDualScreenMode
     */
    public void initialize(OnTimeSelected callback, Timepoint tPointFirst, Timepoint tPointSecond, boolean is24HourMode, boolean isDualScreenMode) {
        mIsDualScreenMode = isDualScreenMode;
        mTimeSelectedCallback = callback;
        if (tPointFirst != null) {
            mInitialTime = tPointFirst;
            setFirstTime(tPointFirst);
        } else {
            mInitialTime = new Timepoint(0, 0, 0);
            setFirstTime(mInitialTime);
        }
        if (tPointSecond != null) {
            setSecondTime(tPointSecond);
        } else {
            mInitialTime = new Timepoint(0, 0, 0);
            setSecondTime(mInitialTime);
        }
        mIs24HourMode = is24HourMode;
        mInKbMode = false;
        mTitle = "";
        mThemeDark = false;
        mThemeDarkChanged = false;
        mAccentColor = -1;
        mVibrate = true;
        mDismissOnPause = false;
        mEnableSeconds = false;
        mOkResid = R.string.mdtp_ok;
        mCancelResid = R.string.mdtp_cancel;
    }

    public void initialize(OnTimeSetListener callback,
                           int hourOfDay, int minute, int second, boolean is24HourMode) {
        mCallback = callback;

        mInitialTime = new Timepoint(hourOfDay, minute, second);
        mIs24HourMode = is24HourMode;
        mInKbMode = false;
        mTitle = "";
        mThemeDark = false;
        mThemeDarkChanged = false;
        mAccentColor = -1;
        mVibrate = true;
        mDismissOnPause = false;
        mEnableSeconds = false;
        mOkResid = R.string.mdtp_ok;
        mCancelResid = R.string.mdtp_cancel;

    }

    /**
     * Set a title. NOTE: this will only take effect with the next onCreateView
     */
    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    /**
     * Set a dark or light theme. NOTE: this will only take effect for the next onCreateView.
     */
    public void setThemeDark(boolean dark) {
        mThemeDark = dark;
        mThemeDarkChanged = true;
    }

    /**
     * Set the accent color of this dialog
     *
     * @param color the accent color you want
     */
    public void setAccentColor(String color) {
        try {
            mAccentColor = Color.parseColor(color);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /**
     * Set the accent color of this dialog
     *
     * @param color the accent color you want
     */
    public void setAccentColor(@ColorInt int color) {
        mAccentColor = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));
        ;
    }

    @Override
    public boolean isThemeDark() {
        return mThemeDark;
    }

    @Override
    public boolean is24HourMode() {
        return mIs24HourMode;
    }

    @Override
    public int getAccentColor() {
        return mAccentColor;
    }


    /**
     * Set whether the device should vibrate when touching fields
     *
     * @param vibrate true if the device should vibrate when touching a field
     */
    public void vibrate(boolean vibrate) {
        mVibrate = vibrate;
    }

    /**
     * Set whether the picker should dismiss itself when it's pausing or whether it should try to survive an orientation change
     *
     * @param dismissOnPause true if the picker should dismiss itself
     */
    public void dismissOnPause(boolean dismissOnPause) {
        mDismissOnPause = dismissOnPause;
    }

    /**
     * Set whether an additional picker for seconds should be shown
     *
     * @param enableSeconds true if the seconds picker should be shown
     */
    public void enableSeconds(boolean enableSeconds) {
        mEnableSeconds = enableSeconds;
    }

    @SuppressWarnings("unused")
    public void setMinTime(int hour, int minute, int second) {
        setMinTime(new Timepoint(hour, minute, second));
    }

    public void setMinTime(Timepoint minTime) {
        if (mMaxTime != null && minTime.compareTo(mMaxTime) > 0)
            throw new IllegalArgumentException("Minimum time must be smaller than the maximum time");
        mMinTime = minTime;
    }

    public boolean setFirstTime(Timepoint firstTime) {
       /* if (mSecondTime != null && (mSecondTime.getHour() != 0 && mSecondTime.getMinute() != 0 && mSecondTime.getSecond() != 0) && firstTime.compareTo(mSecondTime) > 0) {
            if (mErrorMsg != null) {
                mErrorMsg.setText("Minimum time must be smaller than the maximum time");
                mErrorMsg.setVisibility(View.VISIBLE);
            }
            //throw new IllegalArgumentException("Minimum time must be smaller than the maximum time");
            return false;
        } else {*/
        mFirstTime = firstTime;
        return true;
        //}
    }

    public boolean setSecondTime(Timepoint secondTime) {
        /*if (mFirstTime != null && secondTime.compareTo(mFirstTime) < 0) {
            return false;
        } else {
            //throw new IllegalArgumentException("Maximum time must be greater than the minimum time");*/
        mSecondTime = secondTime;
        return true;
        /*}*/
    }

    /**
     * Check selected time of from and to wheather its valid or not
     *
     * @return
     */
    public boolean checkValidTimeSelection() {
        if (mFirstTime != null && mSecondTime != null && mSecondTime.compareTo(mFirstTime) <= 0) {
            if (mErrorMsg != null) {
                mErrorMsg.setText(getString(R.string.error_msg_dual_screen));
                mErrorMsg.setVisibility(View.VISIBLE);
            }
            return false;
        }
        return true;
    }

    @SuppressWarnings("unused")
    public void setMaxTime(int hour, int minute, int second) {
        setMaxTime(new Timepoint(hour, minute, second));
    }

    public void setMaxTime(Timepoint maxTime) {
        if (mMinTime != null && maxTime.compareTo(mMinTime) < 0)
            throw new IllegalArgumentException("Maximum time must be greater than the minimum time");
        mMaxTime = maxTime;
    }

    @SuppressWarnings("unused")
    public void setSelectableTimes(Timepoint[] selectableTimes) {
        mSelectableTimes = selectableTimes;
        Arrays.sort(mSelectableTimes);
    }

    /**
     * Set the interval for selectable times in the TimePickerDialog
     * This is a convenience wrapper around setSelectableTimes
     * The interval for all three time components can be set independently
     *
     * @param hourInterval   The interval between 2 selectable hours ([1,24])
     * @param minuteInterval The interval between 2 selectable minutes ([1,60])
     * @param secondInterval The interval between 2 selectable seconds ([1,60])
     */
    public void setTimeInterval(@IntRange(from = 1, to = 24) int hourInterval,
                                @IntRange(from = 1, to = 60) int minuteInterval,
                                @IntRange(from = 1, to = 60) int secondInterval) {
        List<Timepoint> timepoints = new ArrayList<>();

        int hour = 0;
        while (hour < 24) {
            int minute = 0;
            while (minute < 60) {
                int second = 0;
                while (second < 60) {
                    timepoints.add(new Timepoint(hour, minute, second));
                    second += secondInterval;
                }
                minute += minuteInterval;
            }
            hour += hourInterval;
        }
        setSelectableTimes(timepoints.toArray(new Timepoint[timepoints.size()]));
    }

    /**
     * Set the interval for selectable times in the TimePickerDialog
     * This is a convenience wrapper around setSelectableTimes
     * The interval for all three time components can be set independently
     *
     * @param hourInterval   The interval between 2 selectable hours ([1,24])
     * @param minuteInterval The interval between 2 selectable minutes ([1,60])
     */
    public void setTimeInterval(@IntRange(from = 1, to = 24) int hourInterval,
                                @IntRange(from = 1, to = 60) int minuteInterval) {
        setTimeInterval(hourInterval, minuteInterval, 1);
    }

    /**
     * Set the interval for selectable times in the TimePickerDialog
     * This is a convenience wrapper around setSelectableTimes
     * The interval for all three time components can be set independently
     *
     * @param hourInterval The interval between 2 selectable hours ([1,24])
     */
    @SuppressWarnings("unused")
    public void setTimeInterval(@IntRange(from = 1, to = 24) int hourInterval) {
        setTimeInterval(hourInterval, 1);
    }

    public void setOnTimeSetListener(OnTimeSetListener callback) {
        mCallback = callback;
    }

    public void setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        mOnCancelListener = onCancelListener;
    }

    @SuppressWarnings("unused")
    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    public void setStartTime(int hourOfDay, int minute, int second) {
        mInitialTime = roundToNearest(new Timepoint(hourOfDay, minute, second));
        mInKbMode = false;
    }

    @SuppressWarnings("unused")
    public void setStartTime(int hourOfDay, int minute) {
        setStartTime(hourOfDay, minute, 0);
    }

    /**
     * Set the label for the Ok button (max 12 characters)
     *
     * @param okString A literal String to be used as the Ok button label
     */
    @SuppressWarnings("unused")
    public void setOkText(String okString) {
        mOkString = okString;
    }

    /**
     * Set the label for the Ok button (max 12 characters)
     *
     * @param okResid A resource ID to be used as the Ok button label
     */
    @SuppressWarnings("unused")
    public void setOkText(@StringRes int okResid) {
        mOkString = null;
        mOkResid = okResid;
    }

    /**
     * Set the label for the Cancel button (max 12 characters)
     *
     * @param cancelString A literal String to be used as the Cancel button label
     */
    @SuppressWarnings("unused")
    public void setCancelText(String cancelString) {
        mCancelString = cancelString;
    }

    /**
     * Set the label for the Cancel button (max 12 characters)
     *
     * @param cancelResid A resource ID to be used as the Cancel button label
     */
    @SuppressWarnings("unused")
    public void setCancelText(@StringRes int cancelResid) {
        mCancelString = null;
        mCancelResid = cancelResid;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_INITIAL_TIME)
                && savedInstanceState.containsKey(KEY_IS_24_HOUR_VIEW)) {
            mInitialTime = savedInstanceState.getParcelable(KEY_INITIAL_TIME);
            mIs24HourMode = savedInstanceState.getBoolean(KEY_IS_24_HOUR_VIEW);
            mInKbMode = savedInstanceState.getBoolean(KEY_IN_KB_MODE);
            mTitle = savedInstanceState.getString(KEY_TITLE);
            mThemeDark = savedInstanceState.getBoolean(KEY_THEME_DARK);
            mThemeDarkChanged = savedInstanceState.getBoolean(KEY_THEME_DARK_CHANGED);
            mAccentColor = savedInstanceState.getInt(KEY_ACCENT);
            mVibrate = savedInstanceState.getBoolean(KEY_VIBRATE);
            mDismissOnPause = savedInstanceState.getBoolean(KEY_DISMISS);
            mSelectableTimes = (Timepoint[]) savedInstanceState.getParcelableArray(KEY_SELECTABLE_TIMES);
            mMinTime = savedInstanceState.getParcelable(KEY_MIN_TIME);
            mMaxTime = savedInstanceState.getParcelable(KEY_MAX_TIME);
            mEnableSeconds = savedInstanceState.getBoolean(KEY_ENABLE_SECONDS);
            mOkResid = savedInstanceState.getInt(KEY_OK_RESID);
            mOkString = savedInstanceState.getString(KEY_OK_STRING);
            mCancelResid = savedInstanceState.getInt(KEY_CANCEL_RESID);
            mCancelString = savedInstanceState.getString(KEY_CANCEL_STRING);
        }
    }

    private void setNewTab(TabHost tabHost, String tag, int title, int contentID, int index) {
        TabHost.TabSpec tabSpec = tabHost.newTabSpec(tag);
        tabSpec.setIndicator(getTabIndicator(tabHost.getContext(), title, index)); // new function to inject our own tab layout
        tabSpec.setContent(contentID);
        tabHost.addTab(tabSpec);
    }

    private View getTabIndicator(Context context, int title, int index) {
        View view = null;
        if (index == 0) {
            view = LayoutInflater.from(context).inflate(R.layout.mdtp_dual_time_header_label, null);
            ((AccessibleTextView) view.findViewById(R.id.txtTitleTab)).setText(title);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.mdtp_dual_time_header_label_end, null);
            ((AccessibleTextView) view.findViewById(R.id.txtTitleTab2)).setText(title);
        }
        return view;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.mdtp_dual_time_picker_dialog, container, false);
        KeyboardListener keyboardListener = new KeyboardListener();
        view.findViewById(R.id.time_picker_dialog).setOnKeyListener(keyboardListener);

        // If an accent color has not been set manually, get it from the context
        if (mAccentColor == -1) {
            mAccentColor = Utils.getAccentColorFromThemeIfAvailable(getActivity());
        }

        // if theme mode has not been set by java code, check if it is specified in Style.xml
        if (!mThemeDarkChanged) {
            mThemeDark = Utils.isDarkTheme(getActivity(), mThemeDark);
        }

        Resources res = getResources();
        Context context = getActivity();
        mHourPickerDescription = res.getString(R.string.mdtp_hour_picker_description);
        mSelectHours = res.getString(R.string.mdtp_select_hours);
        mMinutePickerDescription = res.getString(R.string.mdtp_minute_picker_description);
        mSelectMinutes = res.getString(R.string.mdtp_select_minutes);
        mSecondPickerDescription = res.getString(R.string.mdtp_second_picker_description);
        mSelectSeconds = res.getString(R.string.mdtp_select_seconds);
        mSelectedColor = ContextCompat.getColor(context, R.color.mdtp_white);
        mUnselectedColor = ContextCompat.getColor(context, R.color.mdtp_accent_color_focused);
        if (mIsDualScreenMode) {
            view.findViewById(R.id.next_backgroud).setVisibility(View.VISIBLE);

            tabHost = (TabHost) view.findViewById(R.id.tabHost);
            tabHost.findViewById(R.id.tabHost);
            tabHost.setOnTabChangedListener(this);
            tabHost.setup();
            setNewTab(tabHost, getString(R.string.from), R.string.from, R.id.time_picker, 0);
            setNewTab(tabHost, getString(R.string.to), R.string.to, R.id.time_picker_end, 1);
            tabHost.setCurrentTab(0);
        } else {
            ((TextView) view.findViewById(R.id.previous)).setText(getString(R.string.mdtp_cancel));
            view.findViewById(R.id.done_background).setVisibility(View.VISIBLE);
        }

        doneBackgroudnView = view.findViewById(R.id.done_background);
        nextBackgroundView = view.findViewById(R.id.next_backgroud);
        mHourView = (TextView) view.findViewById(R.id.hours);
        mHourView.setOnKeyListener(keyboardListener);
        mHourView.setTypeface(TypefaceHelper.get(context, Utils.OSWALD_LIGHT));
        mHourSpaceView = (TextView) view.findViewById(R.id.hour_space);
        mHourSpaceViewEnd = (TextView) view.findViewById(R.id.hour_space_end);
        mHourViewEnd = (TextView) view.findViewById(R.id.hours_end);
        mHourViewEnd.setOnKeyListener(keyboardListener);
        mHourViewEnd.setTypeface(TypefaceHelper.get(context, Utils.OSWALD_LIGHT));
        mMinuteSpaceView = (TextView) view.findViewById(R.id.minutes_space);
        mMinuteSpaceViewEnd = (TextView) view.findViewById(R.id.minutes_space_end);
        mMinuteView = (TextView) view.findViewById(R.id.minutes);
        mMinuteView.setOnKeyListener(keyboardListener);
        mMinuteView.setTypeface(TypefaceHelper.get(context, Utils.OSWALD_LIGHT));
        mMinuteViewEnd = (TextView) view.findViewById(R.id.minutes_end);
        mMinuteViewEnd.setOnKeyListener(keyboardListener);
        mMinuteViewEnd.setTypeface(TypefaceHelper.get(context, Utils.OSWALD_LIGHT));
        mSecondSpaceView = (TextView) view.findViewById(R.id.seconds_space);
        mSecondView = (TextView) view.findViewById(R.id.seconds);
        mSecondView.setOnKeyListener(keyboardListener);
        mAmPmTextView = (TextView) view.findViewById(R.id.ampm_label);
        mAmPmTextView.setOnKeyListener(keyboardListener);
        String[] amPmTexts = new DateFormatSymbols().getAmPmStrings();
        mAmText = amPmTexts[0];
        mPmText = amPmTexts[1];

        mHapticFeedbackController = new HapticFeedbackController(getActivity());
        mInitialTime = roundToNearest(mInitialTime);

        mTimePicker = (RadialPickerLayout) view.findViewById(R.id.time_picker);
        mTimePicker.setOnValueSelectedListener(this);
        mTimePicker.setOnKeyListener(keyboardListener);
        mTimePicker.initialize(getActivity(), this, mInitialTime, mIs24HourMode);

        mTimePickerEnd = (RadialPickerLayout) view.findViewById(R.id.time_picker_end);
        mTimePickerEnd.setOnValueSelectedListener(this);
        mTimePickerEnd.setOnKeyListener(keyboardListener);
        //mTimePickerEnd.setVisibility(View.GONE);
        if (mSecondTime != null) {
            mTimePickerEnd.initialize(getActivity(), this, mSecondTime, mIs24HourMode);
        }

        int currentItemShowing = HOUR_INDEX;
        int currentItemShowingEnd = HOUR_INDEX;
        if (savedInstanceState != null &&
                savedInstanceState.containsKey(KEY_CURRENT_ITEM_SHOWING)) {
            currentItemShowing = savedInstanceState.getInt(KEY_CURRENT_ITEM_SHOWING);
        }
        /*if (savedInstanceState != null &&
                savedInstanceState.containsKey(KEY_CURRENT_ITEM_SHOWING_END)) {
            currentItemShowingEnd = savedInstanceState.getInt(KEY_CURRENT_ITEM_SHOWING_END);
        }*/
        setCurrentItemShowing(currentItemShowing, false, true, true);
        setCurrentItemShowing(currentItemShowingEnd, false, true, true);
        mTimePicker.invalidate();
        mTimePickerEnd.invalidate();

        mHourView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tabHost.getCurrentTab() == 1) {
                    tabHost.setCurrentTab(0);
                } else {
                    setCurrentItemShowing(HOUR_INDEX, true, false, true);
                }
                tryVibrate();
            }
        });
        mMinuteView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tabHost.getCurrentTab() == 1) {
                    tabHost.setCurrentTab(0);
                } else {
                    setCurrentItemShowing(MINUTE_INDEX, true, false, true);
                    tryVibrate();
                }
            }
        });
        mSecondView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setCurrentItemShowing(SECOND_INDEX, true, false, true);
                tryVibrate();
            }
        });

        mHourViewEnd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tabHost.getCurrentTab() == 0) {
                    tabHost.setCurrentTab(1);
                } else {
                    setCurrentItemShowing(HOUR_INDEX, true, false, true);
                    tryVibrate();
                }
            }
        });
        mMinuteViewEnd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tabHost.getCurrentTab() == 0) {
                    tabHost.setCurrentTab(1);
                } else {
                    setCurrentItemShowing(MINUTE_INDEX, true, false, true);
                    tryVibrate();
                }
            }
        });
        mSecondView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setCurrentItemShowing(SECOND_INDEX, true, false, true);
                tryVibrate();
            }
        });

        mOkButton = (Button) view.findViewById(R.id.ok);
        mOkButton.setOnClickListener(clickListener);
        mOkButton.setOnKeyListener(keyboardListener);
        mOkButton.setTypeface(TypefaceHelper.get(context, Utils.ROBOTO_MEDIUM));
        if (mOkString != null) mOkButton.setText(mOkString);
        else mOkButton.setText(mOkResid);

        mErrorMsg = (TextView) view.findViewById(R.id.time_picker_error_msg);

        mPreviousButton = (Button) view.findViewById(R.id.previous);
        mPreviousButton.setOnClickListener(clickListener);

        mNextButton = (Button) view.findViewById(R.id.next);
        mNextButton.setOnClickListener(clickListener);

        mCancelButton = (Button) view.findViewById(R.id.cancel);
        mCancelButton.setOnClickListener(clickListener);
        mCancelButton.setTypeface(TypefaceHelper.get(context, Utils.ROBOTO_MEDIUM));
        if (mCancelString != null) mCancelButton.setText(mCancelString);
        else mCancelButton.setText(mCancelResid);
        mCancelButton.setVisibility(isCancelable() ? View.VISIBLE : View.GONE);

        // Enable or disable the AM/PM view.
        mAmPmHitspace = view.findViewById(R.id.ampm_hitspace);
        if (mIs24HourMode) {
            mAmPmTextView.setVisibility(View.GONE);
        } else {
            mAmPmTextView.setVisibility(View.VISIBLE);
            updateAmPmDisplay(mInitialTime.isAM() ? AM : PM);
            mAmPmHitspace.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Don't do anything if either AM or PM are disabled
                    if (isAmDisabled() || isPmDisabled()) return;

                    tryVibrate();
                    int amOrPm = mTimePicker.getIsCurrentlyAmOrPm();
                    if (amOrPm == AM) {
                        amOrPm = PM;
                    } else if (amOrPm == PM) {
                        amOrPm = AM;
                    }
                    mTimePicker.setAmOrPm(amOrPm);
                }
            });
        }

        // Disable seconds picker
        if (!mEnableSeconds) {
            mSecondSpaceView.setVisibility(View.GONE);
            view.findViewById(R.id.separator_seconds).setVisibility(View.GONE);
        }

        // Center stuff depending on what's visible
        if (mIs24HourMode && !mEnableSeconds) {
            // center first separator
            RelativeLayout.LayoutParams paramsSeparator = new RelativeLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            paramsSeparator.addRule(RelativeLayout.CENTER_IN_PARENT);
            TextView separatorView = (TextView) view.findViewById(R.id.separator);
            separatorView.setLayoutParams(paramsSeparator);
        } else if (mEnableSeconds) {
            // link separator to minutes
            final View separator = view.findViewById(R.id.separator);
            RelativeLayout.LayoutParams paramsSeparator = new RelativeLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            paramsSeparator.addRule(RelativeLayout.LEFT_OF, R.id.minutes_space);
            paramsSeparator.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            separator.setLayoutParams(paramsSeparator);

            if (!mIs24HourMode) {
                // center minutes
                RelativeLayout.LayoutParams paramsMinutes = new RelativeLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                paramsMinutes.addRule(RelativeLayout.CENTER_IN_PARENT);
                mMinuteSpaceView.setLayoutParams(paramsMinutes);
            } else {
                // move minutes to right of center
                RelativeLayout.LayoutParams paramsMinutes = new RelativeLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                paramsMinutes.addRule(RelativeLayout.RIGHT_OF, R.id.center_view);
                mMinuteSpaceView.setLayoutParams(paramsMinutes);
            }
        }

        mAllowAutoAdvance = true;
        setHour(mFirstTime.getHour(), true);
        setMinute(mFirstTime.getMinute());
        setSecond(mFirstTime.getSecond());

        setHourEnd(mSecondTime.getHour(), true);
        setMinuteEnd(mSecondTime.getMinute());
        setSecondEnd(mSecondTime.getSecond());

        // Set up for keyboard mode.
        mDoublePlaceholderText = res.getString(R.string.mdtp_time_placeholder);
        mDeletedKeyFormat = res.getString(R.string.mdtp_deleted_key);
        mPlaceholderText = mDoublePlaceholderText.charAt(0);
        mAmKeyCode = mPmKeyCode = -1;
        generateLegalTimesTree();
        if (mInKbMode) {
            mTypedTimes = savedInstanceState.getIntegerArrayList(KEY_TYPED_TIMES);
            tryStartingKbMode(-1);
            mHourView.invalidate();
        } else if (mTypedTimes == null) {
            mTypedTimes = new ArrayList<>();
        }

       /* // Set the title (if any)
        if (!mTitle.isEmpty()) {
            timePickerHeader.setVisibility(TextView.VISIBLE);
            timePickerHeader.setText(mTitle.toUpperCase(Locale.getDefault()));
        }*/

        // Set the theme at the end so that the initialize()s above don't counteract the theme.
        mOkButton.setTextColor(mAccentColor);
        mCancelButton.setTextColor(mAccentColor);
        //timePickerHeader.setBackgroundColor(Utils.darkenColor(mAccentColor));
        view.findViewById(R.id.time_display_background).setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.mdtp_white));
        view.findViewById(R.id.time_display).setBackgroundColor(mAccentColor);
        view.findViewById(R.id.time_display_end).setBackgroundColor(mAccentColor);

        if (getDialog() == null) {
            view.findViewById(R.id.done_background).setVisibility(View.GONE);
        }

        int circleBackground = ContextCompat.getColor(context, R.color.mdtp_circle_background);
        int backgroundColor = ContextCompat.getColor(context, R.color.mdtp_background_color);
        int darkBackgroundColor = ContextCompat.getColor(context, R.color.mdtp_light_gray);
        int lightGray = ContextCompat.getColor(context, R.color.mdtp_light_gray);

        mTimePicker.setBackgroundColor(mThemeDark ? lightGray : circleBackground);
        mTimePickerEnd.setBackgroundColor(mThemeDark ? lightGray : circleBackground);
        view.findViewById(R.id.time_picker_dialog).setBackgroundColor(mThemeDark ? darkBackgroundColor : backgroundColor);
        return view;
    }

    private void updateTOTTimePicker(Timepoint timePoint) {
        mTimePickerEnd.clearFocus();
        mTimePickerEnd.invalidate();
        if (timePoint != null) {
            mTimePickerEnd.initialize(getActivity(), this, timePoint, mIs24HourMode);
            setHourEnd(timePoint.getHour(), true);
            setMinuteEnd(timePoint.getMinute());
            setSecondEnd(timePoint.getSecond());
            setCurrentItemShowing(HOUR_INDEX, true, false, true);
            tryVibrate();
            mTimePickerEnd.invalidate();
        } else {
            if (mTimePickerEnd.getTime() != null) {
                mTimePickerEnd.initialize(getActivity(), this, mTimePickerEnd.getTime(), mIs24HourMode);
            } else {
                mTimePickerEnd.initialize(getActivity(), this, mInitialTime, mIs24HourMode);
            }
            setHourEnd(mInitialTime.getHour(), true);
            setMinuteEnd(mInitialTime.getMinute());
            setSecondEnd(mInitialTime.getSecond());
            setCurrentItemShowing(HOUR_INDEX, true, false, true);
            tryVibrate();
            mTimePickerEnd.invalidate();
        }
        mHapticFeedbackController.start();
    }

    private void updateVANTimePicker(Timepoint timePoint) {
        mTimePicker.clearFocus();
        mTimePicker.invalidate();
        if (timePoint != null) {
            mTimePicker.initialize(getActivity(), this, timePoint, mIs24HourMode);
            setHour(timePoint.getHour(), true);
            setMinute(timePoint.getMinute());
            setSecond(timePoint.getSecond());
            setCurrentItemShowing(HOUR_INDEX, true, false, true);
            tryVibrate();
            mTimePicker.invalidate();
        } else {
            mTimePicker.initialize(getActivity(), this, mTimePicker.getTime(), mIs24HourMode);
            setHour(mInitialTime.getHour(), true);
            setMinute(mInitialTime.getMinute());
            setSecond(mInitialTime.getSecond());
            setCurrentItemShowing(HOUR_INDEX, true, false, true);
            tryVibrate();
            mTimePicker.invalidate();
        }
        mHapticFeedbackController.start();
    }

    /**
     * Update Timepicker with new value
     *//*
    private void updateTimePicker(Timepoint timePoint) {
        //Todo update according the selection time picker

        if (isSecondScreenVisible) {

        } else {

        }

    }*/
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        mHapticFeedbackController.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mHapticFeedbackController.stop();
        if (mDismissOnPause) dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (mOnCancelListener != null) mOnCancelListener.onCancel(dialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mOnDismissListener != null) mOnDismissListener.onDismiss(dialog);
    }

    @Override
    public void tryVibrate() {
        if (mVibrate) mHapticFeedbackController.tryVibrate();
    }

    private void updateAmPmDisplay(int amOrPm) {
        if (amOrPm == AM) {
            mAmPmTextView.setText(mAmText);
            Utils.tryAccessibilityAnnounce(mTimePicker, mAmText);
            mAmPmHitspace.setContentDescription(mAmText);
        } else if (amOrPm == PM) {
            mAmPmTextView.setText(mPmText);
            Utils.tryAccessibilityAnnounce(mTimePicker, mPmText);
            mAmPmHitspace.setContentDescription(mPmText);
        } else {
            mAmPmTextView.setText(mDoublePlaceholderText);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (mTimePicker != null) {
            outState.putParcelable(KEY_INITIAL_TIME, mTimePicker.getTime());
            outState.putBoolean(KEY_IS_24_HOUR_VIEW, mIs24HourMode);
            outState.putInt(KEY_CURRENT_ITEM_SHOWING, mTimePicker.getCurrentItemShowing());
            outState.putBoolean(KEY_IN_KB_MODE, mInKbMode);
            if (mInKbMode) {
                outState.putIntegerArrayList(KEY_TYPED_TIMES, mTypedTimes);
            }
            outState.putString(KEY_TITLE, mTitle);
            outState.putBoolean(KEY_THEME_DARK, mThemeDark);
            outState.putBoolean(KEY_THEME_DARK_CHANGED, mThemeDarkChanged);
            outState.putInt(KEY_ACCENT, mAccentColor);
            outState.putBoolean(KEY_VIBRATE, mVibrate);
            outState.putBoolean(KEY_DISMISS, mDismissOnPause);
            outState.putParcelableArray(KEY_SELECTABLE_TIMES, mSelectableTimes);
            outState.putParcelable(KEY_MIN_TIME, mMinTime);
            outState.putParcelable(KEY_MAX_TIME, mMaxTime);
            outState.putBoolean(KEY_ENABLE_SECONDS, mEnableSeconds);
            outState.putInt(KEY_OK_RESID, mOkResid);
            outState.putString(KEY_OK_STRING, mOkString);
            outState.putInt(KEY_CANCEL_RESID, mCancelResid);
            outState.putString(KEY_CANCEL_STRING, mCancelString);
        }

        if (mTimePickerEnd != null) {
            //Todo for second time picker when needed
        }
    }

    private void updateValueSelectedTOT(Timepoint newValue) {
        if (newValue != null) {
            setHourEnd(newValue.getHour(), false);
            mTimePickerEnd.setContentDescription(mHourPickerDescription + ": " + newValue.getHour());
            setMinuteEnd(newValue.getMinute());
            mTimePickerEnd.setContentDescription(mMinutePickerDescription + ": " + newValue.getMinute());
            setSecondEnd(newValue.getSecond());
            mTimePickerEnd.setContentDescription(mSecondPickerDescription + ": " + newValue.getSecond());
        } else {
            setHourEnd(mInitialTime.getHour(), false);
            mTimePickerEnd.setContentDescription(mHourPickerDescription + ": " + mInitialTime.getHour());
            setMinuteEnd(mInitialTime.getMinute());
            mTimePickerEnd.setContentDescription(mMinutePickerDescription + ": " + mInitialTime.getMinute());
            setSecondEnd(mInitialTime.getSecond());
            mTimePickerEnd.setContentDescription(mSecondPickerDescription + ": " + mInitialTime.getSecond());
        }
    }

    private void updateValueSelectedVAN(Timepoint newValue) {
        setHour(newValue.getHour(), false);
        mTimePicker.setContentDescription(mHourPickerDescription + ": " + newValue.getHour());
        setMinute(newValue.getMinute());
        mTimePicker.setContentDescription(mMinutePickerDescription + ": " + newValue.getMinute());
        setSecond(newValue.getSecond());
        mTimePicker.setContentDescription(mSecondPickerDescription + ": " + newValue.getSecond());
    }

    /**
     * Called by the picker for updating the header display.
     */
    @Override
    public void onValueSelected(Timepoint newValue) {
        mErrorMsg.setVisibility(View.GONE);
        Log.i(TimePickerDialog.class.getName(), "onvalueSelected,,..update curret tab..." + tabHost.getCurrentTab());
        if (tabHost.getCurrentTab() == 0) {
            updateValueSelectedVAN(newValue);
        } else {
            updateValueSelectedTOT(newValue);
        }
        if (!mIs24HourMode) updateAmPmDisplay(newValue.isAM() ? AM : PM);
    }

    @Override
    public void advancePicker(int index) {
        if (!mAllowAutoAdvance) return;
        if (index == HOUR_INDEX) {
            setCurrentItemShowing(MINUTE_INDEX, true, true, false);
            if (tabHost.getCurrentTab() == 0) {
                String announcement = mSelectHours + ". " + mTimePicker.getMinutes();
                Utils.tryAccessibilityAnnounce(mTimePicker, announcement);
            } else {
                String announcement = mSelectHours + ". " + mTimePickerEnd.getMinutes();
                Utils.tryAccessibilityAnnounce(mTimePickerEnd, announcement);
            }
        } else if (index == MINUTE_INDEX && mEnableSeconds) {
            setCurrentItemShowing(SECOND_INDEX, true, true, false);
            if (tabHost.getCurrentTab() == 0) {
                String announcement = mSelectMinutes + ". " + mTimePicker.getSeconds();
                Utils.tryAccessibilityAnnounce(mTimePicker, announcement);
            } else {
                String announcement = mSelectMinutes + ". " + mTimePickerEnd.getSeconds();
                Utils.tryAccessibilityAnnounce(mTimePickerEnd, announcement);
            }
        }
    }

    @Override
    public void enablePicker() {
        if (!isTypedTimeFullyLegal()) mTypedTimes.clear();
        finishKbMode(true);
    }

    public boolean isOutOfRange(Timepoint current) {
        if (mMinTime != null && mMinTime.compareTo(current) > 0) return true;

        if (mMaxTime != null && mMaxTime.compareTo(current) < 0) return true;

        if (mSelectableTimes != null) return !Arrays.asList(mSelectableTimes).contains(current);

        return false;
    }

    @Override
    public boolean isOutOfRange(Timepoint current, int index) {
        if (current == null) return false;

        if (index == HOUR_INDEX) {
            if (mMinTime != null && mMinTime.getHour() > current.getHour()) return true;

            if (mMaxTime != null && mMaxTime.getHour() + 1 <= current.getHour()) return true;

            if (mSelectableTimes != null) {
                for (Timepoint t : mSelectableTimes) {
                    if (t.getHour() == current.getHour()) return false;
                }
                return true;
            }

            return false;
        } else if (index == MINUTE_INDEX) {
            if (mMinTime != null) {
                Timepoint roundedMin = new Timepoint(mMinTime.getHour(), mMinTime.getMinute());
                if (roundedMin.compareTo(current) > 0) return true;
            }

            if (mMaxTime != null) {
                Timepoint roundedMax = new Timepoint(mMaxTime.getHour(), mMaxTime.getMinute(), 59);
                if (roundedMax.compareTo(current) < 0) return true;
            }

            if (mSelectableTimes != null) {
                for (Timepoint t : mSelectableTimes) {
                    if (t.getHour() == current.getHour() && t.getMinute() == current.getMinute())
                        return false;
                }
                return true;
            }

            return false;
        } else return isOutOfRange(current);
    }

    @Override
    public boolean isAmDisabled() {
        Timepoint midday = new Timepoint(12);

        if (mMinTime != null && mMinTime.compareTo(midday) > 0) return true;

        if (mSelectableTimes != null) {
            for (Timepoint t : mSelectableTimes) if (t.compareTo(midday) < 0) return false;
            return true;
        }

        return false;
    }

    @Override
    public boolean isPmDisabled() {
        Timepoint midday = new Timepoint(12);

        if (mMaxTime != null && mMaxTime.compareTo(midday) < 0) return true;

        if (mSelectableTimes != null) {
            for (Timepoint t : mSelectableTimes) if (t.compareTo(midday) >= 0) return false;
            return true;
        }

        return false;
    }

    /**
     * Round a given Timepoint to the nearest valid Timepoint
     *
     * @param time Timepoint - The timepoint to round
     * @return Timepoint - The nearest valid Timepoint
     */
    private Timepoint roundToNearest(Timepoint time) {
        return roundToNearest(time, Timepoint.TYPE.HOUR);
    }

    @Override
    public Timepoint roundToNearest(Timepoint time, Timepoint.TYPE type) {

        if (mMinTime != null && mMinTime.compareTo(time) > 0) return mMinTime;

        if (mMaxTime != null && mMaxTime.compareTo(time) < 0) return mMaxTime;
        if (mSelectableTimes != null) {
            int currentDistance = Integer.MAX_VALUE;
            Timepoint output = time;
            for (Timepoint t : mSelectableTimes) {
                if (type == Timepoint.TYPE.MINUTE && t.getHour() != time.getHour()) continue;
                if (type == Timepoint.TYPE.SECOND && t.getHour() != time.getHour() && t.getMinute() != time.getMinute())
                    continue;
                int newDistance = Math.abs(t.compareTo(time));
                if (newDistance < currentDistance) {
                    currentDistance = newDistance;
                    output = t;
                } else break;
            }
            return output;
        }

        return time;
    }

    private void setHour(int value, boolean announce) {
        String format;
        if (mIs24HourMode) {
            format = "%02d";
        } else {
            format = "%d";
            value = value % 12;
            if (value == 0) {
                value = 12;
            }
        }

        CharSequence text = String.format(format, value);
        mHourView.setText(text);
        mHourSpaceView.setText(text);

        if (announce) {
            Utils.tryAccessibilityAnnounce(mTimePicker, text);
        }

    }

    private void setHourEnd(int value, boolean announce) {
        String format;
        if (mIs24HourMode) {
            format = "%02d";
        } else {
            format = "%d";
            value = value % 12;
            if (value == 0) {
                value = 12;
            }
        }

        CharSequence text = String.format(format, value);
        mHourViewEnd.setText(text);
        mHourSpaceViewEnd.setText(text);

        if (announce) {
            Utils.tryAccessibilityAnnounce(mTimePickerEnd, text);
        }
    }

    private void setMinute(int value) {
        if (value == 60) {
            value = 0;
        }
        CharSequence text = String.format(Locale.getDefault(), "%02d", value);
        Utils.tryAccessibilityAnnounce(mTimePicker, text);
        mMinuteView.setText(text);
        mMinuteSpaceView.setText(text);
    }

    private void setMinuteEnd(int value) {
        if (value == 60) {
            value = 0;
        }
        CharSequence text = String.format(Locale.getDefault(), "%02d", value);
        Utils.tryAccessibilityAnnounce(mTimePickerEnd, text);
        mMinuteViewEnd.setText(text);
        mMinuteSpaceViewEnd.setText(text);
    }

    private void setSecond(int value) {
        if (value == 60) {
            value = 0;
        }
        CharSequence text = String.format(Locale.getDefault(), "%02d", value);
        if (tabHost.getCurrentTab() == 0) {
            Utils.tryAccessibilityAnnounce(mTimePicker, text);
        } else {
            Utils.tryAccessibilityAnnounce(mTimePickerEnd, text);
        }
        mSecondView.setText(text);
        mSecondSpaceView.setText(text);
    }

    private void setSecondEnd(int value) {
        if (value == 60) {
            value = 0;
        }
        CharSequence text = String.format(Locale.getDefault(), "%02d", value);
        if (tabHost.getCurrentTab() == 0) {
            Utils.tryAccessibilityAnnounce(mTimePicker, text);
        } else {
            Utils.tryAccessibilityAnnounce(mTimePickerEnd, text);
        }
        mSecondView.setText(text);
        mSecondSpaceView.setText(text);
    }

    // Show either Hours or Minutes.
    private void setCurrentItemShowing(int index, boolean animateCircle, boolean delayLabelAnimate,
                                       boolean announce) {
        TextView labelToAnimate;
        if (tabHost.getCurrentTab() == 0) {
            mTimePicker.setCurrentItemShowing(index, animateCircle);

            switch (index) {
                case HOUR_INDEX:
                    int hours = mTimePicker.getHours();
                    if (!mIs24HourMode) {
                        hours = hours % 12;
                    }
                    mTimePicker.setContentDescription(mHourPickerDescription + ": " + hours);
                    if (announce) {
                        Utils.tryAccessibilityAnnounce(mTimePicker, mSelectHours);
                    }
                    labelToAnimate = mHourView;
                    break;
                case MINUTE_INDEX:
                    int minutes = mTimePicker.getMinutes();
                    mTimePicker.setContentDescription(mMinutePickerDescription + ": " + minutes);
                    if (announce) {
                        Utils.tryAccessibilityAnnounce(mTimePicker, mSelectMinutes);
                    }
                    labelToAnimate = mMinuteView;
                    break;
                default:
                    int seconds = mTimePicker.getSeconds();
                    mTimePicker.setContentDescription(mSecondPickerDescription + ": " + seconds);
                    if (announce) {
                        Utils.tryAccessibilityAnnounce(mTimePicker, mSelectSeconds);
                    }
                    labelToAnimate = mSecondView;
            }

            int hourColor = (index == HOUR_INDEX) ? mSelectedColor : mUnselectedColor;
            int minuteColor = (index == MINUTE_INDEX) ? mSelectedColor : mUnselectedColor;
            int secondColor = (index == SECOND_INDEX) ? mSelectedColor : mUnselectedColor;
            mHourView.setTextColor(hourColor);
            mMinuteView.setTextColor(minuteColor);
            mSecondView.setTextColor(secondColor);

            ObjectAnimator pulseAnimator = Utils.getPulseAnimator(labelToAnimate, 0.85f, 1.1f);
            if (delayLabelAnimate) {
                pulseAnimator.setStartDelay(PULSE_ANIMATOR_DELAY);
            }
            pulseAnimator.start();
        } else {
            mTimePickerEnd.setCurrentItemShowing(index, animateCircle);

            switch (index) {
                case HOUR_INDEX:
                    int hours = mTimePickerEnd.getHours();
                    if (!mIs24HourMode) {
                        hours = hours % 12;
                    }
                    mTimePickerEnd.setContentDescription(mHourPickerDescription + ": " + hours);
                    if (announce) {
                        Utils.tryAccessibilityAnnounce(mTimePickerEnd, mSelectHours);
                    }
                    labelToAnimate = mHourViewEnd;
                    break;
                case MINUTE_INDEX:
                    int minutes = mTimePickerEnd.getMinutes();
                    mTimePickerEnd.setContentDescription(mMinutePickerDescription + ": " + minutes);
                    if (announce) {
                        Utils.tryAccessibilityAnnounce(mTimePickerEnd, mSelectMinutes);
                    }
                    labelToAnimate = mMinuteViewEnd;
                    break;
                default:
                    int seconds = mTimePickerEnd.getSeconds();
                    mTimePickerEnd.setContentDescription(mSecondPickerDescription + ": " + seconds);
                    if (announce) {
                        Utils.tryAccessibilityAnnounce(mTimePickerEnd, mSelectSeconds);
                    }
                    labelToAnimate = mSecondView;
            }
            int hourColor = (index == HOUR_INDEX) ? mSelectedColor : mUnselectedColor;
            int minuteColor = (index == MINUTE_INDEX) ? mSelectedColor : mUnselectedColor;
            int secondColor = (index == SECOND_INDEX) ? mSelectedColor : mUnselectedColor;
            mHourViewEnd.setTextColor(hourColor);
            mMinuteViewEnd.setTextColor(minuteColor);
            mSecondView.setTextColor(secondColor);

            ObjectAnimator pulseAnimator = Utils.getPulseAnimator(labelToAnimate, 0.85f, 1.1f);
            if (delayLabelAnimate) {
                pulseAnimator.setStartDelay(PULSE_ANIMATOR_DELAY);
            }
            pulseAnimator.start();
        }
    }

    /**
     * For keyboard mode, processes key events.
     *
     * @param keyCode the pressed key.
     * @return true if the key was successfully processed, false otherwise.
     */
    private boolean processKeyUp(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK) {
            if (isCancelable()) dismiss();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_TAB) {
            if (mInKbMode) {
                if (isTypedTimeFullyLegal()) {
                    finishKbMode(true);
                }
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
            if (mInKbMode) {
                if (!isTypedTimeFullyLegal()) {
                    return true;
                }
                finishKbMode(false);
            }
            if (mCallback != null) {
                mCallback.onTimeSet(mTimePicker,
                        mTimePicker.getHours(), mTimePicker.getMinutes(), mTimePicker.getSeconds());
            }
            dismiss();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DEL) {
            if (mInKbMode) {
                if (!mTypedTimes.isEmpty()) {
                    int deleted = deleteLastTypedKey();
                    String deletedKeyStr;
                    if (deleted == getAmOrPmKeyCode(AM)) {
                        deletedKeyStr = mAmText;

                    } else if (deleted == getAmOrPmKeyCode(PM)) {
                        deletedKeyStr = mPmText;
                    } else {
                        deletedKeyStr = String.format("%d", getValFromKeyCode(deleted));
                    }
                    Utils.tryAccessibilityAnnounce(mTimePicker,
                            String.format(mDeletedKeyFormat, deletedKeyStr));
                    updateDisplay(true);
                }
            }
        } else if (keyCode == KeyEvent.KEYCODE_0 || keyCode == KeyEvent.KEYCODE_1
                || keyCode == KeyEvent.KEYCODE_2 || keyCode == KeyEvent.KEYCODE_3
                || keyCode == KeyEvent.KEYCODE_4 || keyCode == KeyEvent.KEYCODE_5
                || keyCode == KeyEvent.KEYCODE_6 || keyCode == KeyEvent.KEYCODE_7
                || keyCode == KeyEvent.KEYCODE_8 || keyCode == KeyEvent.KEYCODE_9
                || (!mIs24HourMode &&
                (keyCode == getAmOrPmKeyCode(AM) || keyCode == getAmOrPmKeyCode(PM)))) {
            if (!mInKbMode) {
                if (mTimePicker == null) {
                    // Something's wrong, because time picker should definitely not be null.
                    Log.e(TAG, "Unable to initiate keyboard mode, TimePicker was null.");
                    return true;
                }
                mTypedTimes.clear();
                tryStartingKbMode(keyCode);
                return true;
            }
            // We're already in keyboard mode.
            if (addKeyIfLegal(keyCode)) {
                updateDisplay(false);
            }
            return true;
        }
        return false;
    }

    /**
     * Try to start keyboard mode with the specified key, as long as the timepicker is not in the
     * middle of a touch-event.
     *
     * @param keyCode The key to use as the first press. Keyboard mode will not be started if the
     *                key is not legal to start with. Or, pass in -1 to get into keyboard mode without a starting
     *                key.
     */
    private void tryStartingKbMode(int keyCode) {
        if (mTimePicker.trySettingInputEnabled(false) &&
                (keyCode == -1 || addKeyIfLegal(keyCode))) {
            mInKbMode = true;
            mOkButton.setEnabled(false);
            updateDisplay(false);
        }
    }

    private boolean addKeyIfLegal(int keyCode) {
        // If we're in 24hour mode, we'll need to check if the input is full. If in AM/PM mode,
        // we'll need to see if AM/PM have been typed.
        if ((mIs24HourMode && mTypedTimes.size() == (mEnableSeconds ? 6 : 4)) ||
                (!mIs24HourMode && isTypedTimeFullyLegal())) {
            return false;
        }

        mTypedTimes.add(keyCode);
        if (!isTypedTimeLegalSoFar()) {
            deleteLastTypedKey();
            return false;
        }

        int val = getValFromKeyCode(keyCode);
        Utils.tryAccessibilityAnnounce(mTimePicker, String.format("%d", val));
        // Automatically fill in 0's if AM or PM was legally entered.
        if (isTypedTimeFullyLegal()) {
            if (!mIs24HourMode && mTypedTimes.size() <= (mEnableSeconds ? 5 : 3)) {
                mTypedTimes.add(mTypedTimes.size() - 1, KeyEvent.KEYCODE_0);
                mTypedTimes.add(mTypedTimes.size() - 1, KeyEvent.KEYCODE_0);
            }
            mOkButton.setEnabled(true);
        }

        return true;
    }

    /**
     * Traverse the tree to see if the keys that have been typed so far are legal as is,
     * or may become legal as more keys are typed (excluding backspace).
     */
    private boolean isTypedTimeLegalSoFar() {
        Node node = mLegalTimesTree;
        for (int keyCode : mTypedTimes) {
            node = node.canReach(keyCode);
            if (node == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the time that has been typed so far is completely legal, as is.
     */
    private boolean isTypedTimeFullyLegal() {
        if (mIs24HourMode) {
            // For 24-hour mode, the time is legal if the hours and minutes are each legal. Note:
            // getEnteredTime() will ONLY call isTypedTimeFullyLegal() when NOT in 24hour mode.
            int[] values = getEnteredTime(null);
            return (values[0] >= 0 && values[1] >= 0 && values[1] < 60 && values[2] >= 0 && values[2] < 60);
        } else {
            // For AM/PM mode, the time is legal if it contains an AM or PM, as those can only be
            // legally added at specific times based on the tree's algorithm.
            return (mTypedTimes.contains(getAmOrPmKeyCode(AM)) ||
                    mTypedTimes.contains(getAmOrPmKeyCode(PM)));
        }
    }

    private int deleteLastTypedKey() {
        int deleted = mTypedTimes.remove(mTypedTimes.size() - 1);
        if (!isTypedTimeFullyLegal()) {
            mOkButton.setEnabled(false);
        }
        return deleted;
    }

    /**
     * Get out of keyboard mode. If there is nothing in typedTimes, revert to TimePicker's time.
     *
     * @param updateDisplays If true, update the displays with the relevant time.
     */
    private void finishKbMode(boolean updateDisplays) {
        if (tabHost.getCurrentTab() == 0) {
            mInKbMode = false;
            if (!mTypedTimes.isEmpty()) {
                int values[] = getEnteredTime(null);
                mTimePickerEnd.setTime(new Timepoint(values[0], values[1], values[2]));
                if (!mIs24HourMode) {
                    mTimePickerEnd.setAmOrPm(values[3]);
                }
                mTypedTimes.clear();
            }
            if (updateDisplays) {
                updateDisplay(false);
                mTimePickerEnd.trySettingInputEnabled(true);
            }
        } else {
            mInKbMode = false;
            if (!mTypedTimes.isEmpty()) {
                int values[] = getEnteredTime(null);
                mTimePicker.setTime(new Timepoint(values[0], values[1], values[2]));
                if (!mIs24HourMode) {
                    mTimePicker.setAmOrPm(values[3]);
                }
                mTypedTimes.clear();
            }
            if (updateDisplays) {
                updateDisplay(false);
                mTimePicker.trySettingInputEnabled(true);
            }
        }
    }

    /**
     * Update the hours, minutes, seconds and AM/PM displays with the typed times. If the typedTimes
     * is empty, either show an empty display (filled with the placeholder text), or update from the
     * timepicker's values.
     *
     * @param allowEmptyDisplay if true, then if the typedTimes is empty, use the placeholder text.
     *                          Otherwise, revert to the timepicker's values.
     */
    private void updateDisplay(boolean allowEmptyDisplay) {
        if (!allowEmptyDisplay && mTypedTimes.isEmpty()) {
            if (tabHost.getCurrentTab() == 0) {
                int hour = mTimePicker.getHours();
                int minute = mTimePicker.getMinutes();
                int second = mTimePicker.getSeconds();
                setHour(hour, true);
                setMinute(minute);
                setSecond(second);
                if (!mIs24HourMode) {
                    updateAmPmDisplay(hour < 12 ? AM : PM);
                }
                setCurrentItemShowing(mTimePicker.getCurrentItemShowing(), true, true, true);
                mOkButton.setEnabled(true);
            } else {
                int hour = mTimePickerEnd.getHours();
                int minute = mTimePickerEnd.getMinutes();
                int second = mTimePickerEnd.getSeconds();
                setHourEnd(hour, true);
                setMinuteEnd(minute);
                setSecondEnd(second);
                if (!mIs24HourMode) {
                    updateAmPmDisplay(hour < 12 ? AM : PM);
                }
                setCurrentItemShowing(mTimePickerEnd.getCurrentItemShowing(), true, true, true);
                mOkButton.setEnabled(true);
            }
        } else {
            Boolean[] enteredZeros = {false, false, false};
            int[] values = getEnteredTime(enteredZeros);
            String hourFormat = enteredZeros[0] ? "%02d" : "%2d";
            String minuteFormat = (enteredZeros[1]) ? "%02d" : "%2d";
            String secondFormat = (enteredZeros[1]) ? "%02d" : "%2d";
            String hourStr = (values[0] == -1) ? mDoublePlaceholderText :
                    String.format(hourFormat, values[0]).replace(' ', mPlaceholderText);
            String minuteStr = (values[1] == -1) ? mDoublePlaceholderText :
                    String.format(minuteFormat, values[1]).replace(' ', mPlaceholderText);
            String secondStr = (values[2] == -1) ? mDoublePlaceholderText :
                    String.format(secondFormat, values[1]).replace(' ', mPlaceholderText);

            if (tabHost.getCurrentTab() == 0) {
                mHourView.setText(hourStr);
                mHourSpaceView.setText(hourStr);
                mHourView.setTextColor(mUnselectedColor);
                mMinuteView.setText(minuteStr);
                mMinuteSpaceView.setText(minuteStr);
                mMinuteView.setTextColor(mUnselectedColor);
            } else {
                mHourViewEnd.setText(hourStr);
                mHourSpaceViewEnd.setText(hourStr);
                mHourViewEnd.setTextColor(mUnselectedColor);
                mMinuteViewEnd.setText(minuteStr);
                mMinuteSpaceViewEnd.setText(minuteStr);
                mMinuteViewEnd.setTextColor(mUnselectedColor);
            }
            if (!mIs24HourMode) {
                updateAmPmDisplay(values[3]);
            }
        }
    }

    private static int getValFromKeyCode(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_0:
                return 0;
            case KeyEvent.KEYCODE_1:
                return 1;
            case KeyEvent.KEYCODE_2:
                return 2;
            case KeyEvent.KEYCODE_3:
                return 3;
            case KeyEvent.KEYCODE_4:
                return 4;
            case KeyEvent.KEYCODE_5:
                return 5;
            case KeyEvent.KEYCODE_6:
                return 6;
            case KeyEvent.KEYCODE_7:
                return 7;
            case KeyEvent.KEYCODE_8:
                return 8;
            case KeyEvent.KEYCODE_9:
                return 9;
            default:
                return -1;
        }
    }

    /**
     * Get the currently-entered time, as integer values of the hours, minutes and seconds typed.
     *
     * @param enteredZeros A size-2 boolean array, which the caller should initialize, and which
     *                     may then be used for the caller to know whether zeros had been explicitly entered as either
     *                     hours of minutes. This is helpful for deciding whether to show the dashes, or actual 0's.
     * @return A size-3 int array. The first value will be the hours, the second value will be the
     * minutes, and the third will be either TimePickerDialog.AM or TimePickerDialog.PM.
     */
    private int[] getEnteredTime(Boolean[] enteredZeros) {
        int amOrPm = -1;
        int startIndex = 1;
        if (!mIs24HourMode && isTypedTimeFullyLegal()) {
            int keyCode = mTypedTimes.get(mTypedTimes.size() - 1);
            if (keyCode == getAmOrPmKeyCode(AM)) {
                amOrPm = AM;
            } else if (keyCode == getAmOrPmKeyCode(PM)) {
                amOrPm = PM;
            }
            startIndex = 2;
        }
        int minute = -1;
        int hour = -1;
        int second = 0;
        int shift = mEnableSeconds ? 2 : 0;
        for (int i = startIndex; i <= mTypedTimes.size(); i++) {
            int val = getValFromKeyCode(mTypedTimes.get(mTypedTimes.size() - i));
            if (mEnableSeconds) {
                if (i == startIndex) {
                    second = val;
                } else if (i == startIndex + 1) {
                    second += 10 * val;
                    if (enteredZeros != null && val == 0) {
                        enteredZeros[2] = true;
                    }
                }
            }
            if (i == startIndex + shift) {
                minute = val;
            } else if (i == startIndex + shift + 1) {
                minute += 10 * val;
                if (enteredZeros != null && val == 0) {
                    enteredZeros[1] = true;
                }
            } else if (i == startIndex + shift + 2) {
                hour = val;
            } else if (i == startIndex + shift + 3) {
                hour += 10 * val;
                if (enteredZeros != null && val == 0) {
                    enteredZeros[0] = true;
                }
            }
        }

        return new int[]{hour, minute, second, amOrPm};
    }

    /**
     * Get the keycode value for AM and PM in the current language.
     */
    private int getAmOrPmKeyCode(int amOrPm) {
        // Cache the codes.
        if (mAmKeyCode == -1 || mPmKeyCode == -1) {
            // Find the first character in the AM/PM text that is unique.
            KeyCharacterMap kcm = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
            char amChar;
            char pmChar;
            for (int i = 0; i < Math.max(mAmText.length(), mPmText.length()); i++) {
                amChar = mAmText.toLowerCase(Locale.getDefault()).charAt(i);
                pmChar = mPmText.toLowerCase(Locale.getDefault()).charAt(i);
                if (amChar != pmChar) {
                    KeyEvent[] events = kcm.getEvents(new char[]{amChar, pmChar});
                    // There should be 4 events: a down and up for both AM and PM.
                    if (events != null && events.length == 4) {
                        mAmKeyCode = events[0].getKeyCode();
                        mPmKeyCode = events[2].getKeyCode();
                    } else {
                        Log.e(TAG, "Unable to find keycodes for AM and PM.");
                    }
                    break;
                }
            }
        }
        if (amOrPm == AM) {
            return mAmKeyCode;
        } else if (amOrPm == PM) {
            return mPmKeyCode;
        }

        return -1;
    }

    /**
     * Create a tree for deciding what keys can legally be typed.
     */
    private void generateLegalTimesTree() {
        // Create a quick cache of numbers to their keycodes.
        int k0 = KeyEvent.KEYCODE_0;
        int k1 = KeyEvent.KEYCODE_1;
        int k2 = KeyEvent.KEYCODE_2;
        int k3 = KeyEvent.KEYCODE_3;
        int k4 = KeyEvent.KEYCODE_4;
        int k5 = KeyEvent.KEYCODE_5;
        int k6 = KeyEvent.KEYCODE_6;
        int k7 = KeyEvent.KEYCODE_7;
        int k8 = KeyEvent.KEYCODE_8;
        int k9 = KeyEvent.KEYCODE_9;

        // The root of the tree doesn't contain any numbers.
        mLegalTimesTree = new Node();
        if (mIs24HourMode) {
            // We'll be re-using these nodes, so we'll save them.
            Node minuteFirstDigit = new Node(k0, k1, k2, k3, k4, k5);
            Node minuteSecondDigit = new Node(k0, k1, k2, k3, k4, k5, k6, k7, k8, k9);
            // The first digit must be followed by the second digit.
            minuteFirstDigit.addChild(minuteSecondDigit);

            if (mEnableSeconds) {
                Node secondsFirstDigit = new Node(k0, k1, k2, k3, k4, k5);
                Node secondsSecondDigit = new Node(k0, k1, k2, k3, k4, k5, k6, k7, k8, k9);
                secondsFirstDigit.addChild(secondsSecondDigit);

                // Minutes can be followed by seconds.
                minuteSecondDigit.addChild(secondsFirstDigit);
            }

            // The first digit may be 0-1.
            Node firstDigit = new Node(k0, k1);
            mLegalTimesTree.addChild(firstDigit);

            // When the first digit is 0-1, the second digit may be 0-5.
            Node secondDigit = new Node(k0, k1, k2, k3, k4, k5);
            firstDigit.addChild(secondDigit);
            // We may now be followed by the first minute digit. E.g. 00:09, 15:58.
            secondDigit.addChild(minuteFirstDigit);

            // When the first digit is 0-1, and the second digit is 0-5, the third digit may be 6-9.
            Node thirdDigit = new Node(k6, k7, k8, k9);
            // The time must now be finished. E.g. 0:55, 1:08.
            secondDigit.addChild(thirdDigit);

            // When the first digit is 0-1, the second digit may be 6-9.
            secondDigit = new Node(k6, k7, k8, k9);
            firstDigit.addChild(secondDigit);
            // We must now be followed by the first minute digit. E.g. 06:50, 18:20.
            secondDigit.addChild(minuteFirstDigit);

            // The first digit may be 2.
            firstDigit = new Node(k2);
            mLegalTimesTree.addChild(firstDigit);

            // When the first digit is 2, the second digit may be 0-3.
            secondDigit = new Node(k0, k1, k2, k3);
            firstDigit.addChild(secondDigit);
            // We must now be followed by the first minute digit. E.g. 20:50, 23:09.
            secondDigit.addChild(minuteFirstDigit);

            // When the first digit is 2, the second digit may be 4-5.
            secondDigit = new Node(k4, k5);
            firstDigit.addChild(secondDigit);
            // We must now be followd by the last minute digit. E.g. 2:40, 2:53.
            secondDigit.addChild(minuteSecondDigit);

            // The first digit may be 3-9.
            firstDigit = new Node(k3, k4, k5, k6, k7, k8, k9);
            mLegalTimesTree.addChild(firstDigit);
            // We must now be followed by the first minute digit. E.g. 3:57, 8:12.
            firstDigit.addChild(minuteFirstDigit);
        } else {
            // We'll need to use the AM/PM node a lot.
            // Set up AM and PM to respond to "a" and "p".
            Node ampm = new Node(getAmOrPmKeyCode(AM), getAmOrPmKeyCode(PM));

            // Seconds will be used a few times as well, if enabled.
            Node secondsFirstDigit = new Node(k0, k1, k2, k3, k4, k5);
            Node secondsSecondDigit = new Node(k0, k1, k2, k3, k4, k5, k6, k7, k8, k9);
            secondsSecondDigit.addChild(ampm);
            secondsFirstDigit.addChild(secondsSecondDigit);

            // The first hour digit may be 1.
            Node firstDigit = new Node(k1);
            mLegalTimesTree.addChild(firstDigit);
            // We'll allow quick input of on-the-hour times. E.g. 1pm.
            firstDigit.addChild(ampm);

            // When the first digit is 1, the second digit may be 0-2.
            Node secondDigit = new Node(k0, k1, k2);
            firstDigit.addChild(secondDigit);
            // Also for quick input of on-the-hour times. E.g. 10pm, 12am.
            secondDigit.addChild(ampm);

            // When the first digit is 1, and the second digit is 0-2, the third digit may be 0-5.
            Node thirdDigit = new Node(k0, k1, k2, k3, k4, k5);
            secondDigit.addChild(thirdDigit);
            // The time may be finished now. E.g. 1:02pm, 1:25am.
            thirdDigit.addChild(ampm);

            // When the first digit is 1, the second digit is 0-2, and the third digit is 0-5,
            // the fourth digit may be 0-9.
            Node fourthDigit = new Node(k0, k1, k2, k3, k4, k5, k6, k7, k8, k9);
            thirdDigit.addChild(fourthDigit);
            // The time must be finished now, when seconds are disabled. E.g. 10:49am, 12:40pm.
            fourthDigit.addChild(ampm);

            // When the first digit is 1, the second digit is 0-2, and the third digit is 0-5,
            // and fourth digit is 0-9, we may add seconds if enabled.
            if (mEnableSeconds) {
                // The time must be finished now. E.g. 10:49:01am, 12:40:59pm.
                fourthDigit.addChild(secondsFirstDigit);
            }

            // When the first digit is 1, and the second digit is 0-2, the third digit may be 6-9.
            thirdDigit = new Node(k6, k7, k8, k9);
            secondDigit.addChild(thirdDigit);
            // The time must be finished now. E.g. 1:08am, 1:26pm.
            thirdDigit.addChild(ampm);

            // When the first digit is 1, and the second digit is 0-2, and the third digit is 6-9,
            // we may add seconds is enabled.
            if (mEnableSeconds) {
                // The time must be finished now. E.g. 1:08:01am, 1:26:59pm.
                thirdDigit.addChild(secondsFirstDigit);
            }

            // When the first digit is 1, the second digit may be 3-5.
            secondDigit = new Node(k3, k4, k5);
            firstDigit.addChild(secondDigit);

            // When the first digit is 1, and the second digit is 3-5, the third digit may be 0-9.
            thirdDigit = new Node(k0, k1, k2, k3, k4, k5, k6, k7, k8, k9);
            secondDigit.addChild(thirdDigit);
            // The time must be finished now if seconds are disabled. E.g. 1:39am, 1:50pm.
            thirdDigit.addChild(ampm);

            // When the first digit is 1, and the second digit is 3-5, and the third digit is 0-9,
            // we may add seconds if enabled.
            if (mEnableSeconds) {
                // The time must be finished now. E.g. 1:39:01am, 1:50:59pm.
                thirdDigit.addChild(secondsFirstDigit);
            }

            // The hour digit may be 2-9.
            firstDigit = new Node(k2, k3, k4, k5, k6, k7, k8, k9);
            mLegalTimesTree.addChild(firstDigit);
            // We'll allow quick input of on-the-hour-times. E.g. 2am, 5pm.
            firstDigit.addChild(ampm);

            // When the first digit is 2-9, the second digit may be 0-5.
            secondDigit = new Node(k0, k1, k2, k3, k4, k5);
            firstDigit.addChild(secondDigit);

            // When the first digit is 2-9, and the second digit is 0-5, the third digit may be 0-9.
            thirdDigit = new Node(k0, k1, k2, k3, k4, k5, k6, k7, k8, k9);
            secondDigit.addChild(thirdDigit);
            // The time must be finished now. E.g. 2:57am, 9:30pm.
            thirdDigit.addChild(ampm);

            // When the first digit is 2-9, and the second digit is 0-5, and third digit is 0-9, we
            // may add seconds if enabled.
            if (mEnableSeconds) {
                // The time must be finished now. E.g. 2:57:01am, 9:30:59pm.
                thirdDigit.addChild(secondsFirstDigit);
            }
        }
    }

    /**
     * Simple node class to be used for traversal to check for legal times.
     * mLegalKeys represents the keys that can be typed to get to the node.
     * mChildren are the children that can be reached from this node.
     */
    private static class Node {
        private int[] mLegalKeys;
        private ArrayList<Node> mChildren;

        public Node(int... legalKeys) {
            mLegalKeys = legalKeys;
            mChildren = new ArrayList<>();
        }

        public void addChild(Node child) {
            mChildren.add(child);
        }

        public boolean containsKey(int key) {
            for (int legalKey : mLegalKeys) {
                if (legalKey == key) return true;
            }
            return false;
        }

        public Node canReach(int key) {
            if (mChildren == null) {
                return null;
            }
            for (Node child : mChildren) {
                if (child.containsKey(key)) {
                    return child;
                }
            }
            return null;
        }
    }

    private class KeyboardListener implements OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                return processKeyUp(keyCode);
            }
            return false;
        }

    }

    public void notifyOnDateListener() {
        if (mCallback != null) {
            mCallback.onTimeSet(mTimePicker, mTimePicker.getHours(), mTimePicker.getMinutes(), mTimePicker.getSeconds());
        }
    }
}
