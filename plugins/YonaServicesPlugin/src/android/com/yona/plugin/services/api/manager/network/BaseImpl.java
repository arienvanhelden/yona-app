/*
 * Copyright (c) 2018 Stichting Yona Foundation
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.yona.plugin.services.api.manager.network;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import com.yona.plugin.services.utils.AppConstant;
import com.yona.plugin.services.api.model.ErrorMessage;

import com.yona.plugin.services.listener.DataLoadListener;
import com.yona.plugin.services.listener.DataLoadListenerImpl;
import com.yona.plugin.services.api.utils.NetworkUtils;
import com.yona.plugin.services.api.utils.ServerErrorCode;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by kinnarvasa on 28/03/16.
 */

// TODO: Revisit
public class BaseImpl
{
    private final int maxStale = 60 * 60 * 24 * 28; // keep cache for 28 days.
    private Context appContext;
    private SharedPreferences sharedPreferences;

    protected BaseImpl(Context context)
    {
        appContext = context;
        sharedPreferences = context.getSharedPreferences(AppConstant.USER_PREFERENCE_KEY, android.app.Activity.MODE_PRIVATE);

    }

    private final Interceptor getInterceptor = new Interceptor()
    {
        @Override
        public Response intercept(Chain chain) throws IOException
        {
            Response response = null;
            Request request = chain.request();
            chain.request().newBuilder().addHeader(NetworkConstant.CONTENT_TYPE, "application/json");
            if (NetworkUtils.isOnline(appContext))
            {
                chain.request().newBuilder().addHeader("Cache-Control", "only-if-cached").build();
            }
            else
            {
                throw new UnknownHostException();
            }

            response = chain.proceed(request);
            if (response.priorResponse() != null &&
                    response.priorResponse().code() ==
                            HttpURLConnection.HTTP_MOVED_PERM)
            {

                throw new UnknownHostException();
            }
            else
            {
                request = request.newBuilder().build();
            }

            return response.newBuilder()
                    .header("Cache-Control", "public, max-age=" + maxStale)
                    .build();
        }
    };

    // Made both variables  below as class variables to make sure all network impl classes are using same host environment serverURL.
    // If needed in future instead of using class variables we need to store all the impl instances into an array and iterate and update t
    // host serverURL in all instances when environment switch happens.

    private static Retrofit retrofit;
    private static RestApi restApi;

    /**
     * Gets retrofit.
     *
     * @return the retrofit
     */
    Retrofit getRetrofit()
    {
        if (retrofit == null && sharedPreferences.contains("BaseUrl"))
        {

            String serverUrl = sharedPreferences.getString("BaseUrl", "");

            retrofit = new Retrofit.Builder()
                    .baseUrl(serverUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(gethttpClient())
                    .build();
        }
        return retrofit;
    }

    /**
     * Reinitialize retrofit.
     */
    protected void reinitializeAPI()
    {
        retrofit = null; // this method is require when user do signout and want to change environment, it should update with new environemnt.
        restApi = null;
    }

    /**
     * Reinitialize retrofit.
     */
    protected void reinitializeRetrofit()
    {
        retrofit = null; // this method is require when user do signout and want to change environment, it should update with new environemnt.
    }

    private OkHttpClient gethttpClient()
    {
        return new OkHttpClient.Builder()
                .connectTimeout(NetworkConstant.API_CONNECT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(NetworkConstant.API_WRITE_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .readTimeout(NetworkConstant.API_READ_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(getInterceptor)
                .build();
    }

    /**
     * Gets rest api.
     *
     * @return the rest api
     */
    RestApi getRestApi()
    {
        if (restApi == null)
        {
            restApi = getRetrofit().create(RestApi.class);
        }
        return restApi;
    }

    /**
     * Gets call.
     *
     * @param listener the listener
     * @return the call
     */
    Callback getCall(final DataLoadListener listener)
    {
        return new Callback()
        {
            @Override
            public void onResponse(retrofit2.Call call, retrofit2.Response response)
            {
                if (response.code() < NetworkConstant.RESPONSE_STATUS && listener != null)
                {
                    listener.onDataLoad(response.body());
                }
                else
                {
                    onError(response, listener);
                }
            }

            @Override
            public void onFailure(retrofit2.Call call, Throwable t)
            {
                onError(t, listener);
            }
        };
    }

    /**
     * Gets call.
     *
     * @param listener the listener
     * @return the call
     */
    Callback createCallBack(final DataLoadListenerImpl listener)
    {
        return new Callback()
        {
            @Override
            public void onResponse(retrofit2.Call call, retrofit2.Response response)
            {
                if (response.code() < NetworkConstant.RESPONSE_STATUS && listener != null)
                {
                    listener.onDataLoad(response.body());
                }
                else
                {
                    onError(response, listener);
                }
            }

            @Override
            public void onFailure(retrofit2.Call call, Throwable t)
            {
                onError(t, listener);
            }
        };
    }

    /**
     * On error.
     *
     * @param t        the t
     * @param listener the listener
     */
    void onError(Throwable t, DataLoadListener listener)
    {
        if (listener != null)
        {

            if (t instanceof ConnectException || t instanceof SocketTimeoutException || t instanceof UnknownHostException)
            {
                // If client causing problem.
                if (!NetworkUtils.isOnline(appContext))
                {
                    listener.onError(new ErrorMessage("OOPS! No Internet Connection Available, please connect and try again."));
                }
                else
                {
                    listener.onError(new ErrorMessage("Server is not accessible. Try again."));
                }
            }
            else
            {
                listener.onError(t.getMessage());
            }
        }
    }

    /**
     * On error.
     *
     * @param response the response
     * @param listener the listener
     */
    void onError(retrofit2.Response response, DataLoadListener listener)
    {
        if (listener != null)
        {
            try
            {
                Converter<ResponseBody, ErrorMessage> errorConverter =
                        getRetrofit().responseBodyConverter(ErrorMessage.class, new Annotation[0]);
                ErrorMessage errorMessage = errorConverter.convert(response.errorBody());
                if (ServerErrorCode.USER_NOT_FOUND.equals(errorMessage.getCode()))
                {
                    reinitializeRetrofit();
                    // Todo: handle user not exists
                    //YonaApplication.getEventChangeManager().notifyChange(EventChangeManager.EVENT_USER_NOT_EXIST, errorMessage);
                }
                else
                {
                    listener.onError(errorMessage);
                }
            }
            catch (IOException e)
            {
                listener.onError(new ErrorMessage(e.getMessage()));
            }
        }
    }

}
