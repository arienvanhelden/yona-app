
/*
 * Copyright (c) 2018 Stichting Yona Foundation
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.yona.plugin.services.listener;

import android.os.AsyncTask;

public abstract class DataLoader extends AsyncTask<Void, Void, Object>
{

    @Override
    protected Object doInBackground(Void... params)
    {
        return doDBCall();
    }

    /**
     * Do db call object.
     *
     * @return the object
     */
    public abstract Object doDBCall();

    /**
     * Execute async.
     */
    public void executeAsync()
    {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}

