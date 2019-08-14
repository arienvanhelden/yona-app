/*
 * Copyright (c) 2018 Stichting Yona Foundation
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.yona.plugin.services.api.model;

import android.content.ContentValues;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class AppActivity extends BaseEntity
{
    @SerializedName("deviceDateTime")
    @Expose
    private String deviceDateTime;
    @SerializedName("activities")
    @Expose
    private List<Activity> activities = new ArrayList<Activity>();

    /**
     * Gets device date time.
     *
     * @return The deviceDateTime
     */
    public String getDeviceDateTime()
    {
        return deviceDateTime;
    }

    /**
     * Sets device date time.
     *
     * @param deviceDateTime The deviceDateTime
     */
    public void setDeviceDateTime(String deviceDateTime)
    {
        this.deviceDateTime = deviceDateTime;
    }

    /**
     * Gets activities.
     *
     * @return The activities
     */
    public List<Activity> getActivities()
    {
        return activities;
    }

    /**
     * Sets activities.
     *
     * @param activities The activities
     */
    public void setActivities(List<Activity> activities)
    {
        this.activities = activities;
    }

    @Override
    public ContentValues getDbContentValues()
    {
        return null;
    }
}
