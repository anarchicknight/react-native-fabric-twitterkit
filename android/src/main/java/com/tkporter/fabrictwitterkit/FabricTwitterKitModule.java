// -------------------------------------------------------------------------------------------
// Modifications:
// Copyright (C) 2016 Sony Interactive Entertainment Inc.
// Licensed under the MIT License. See the LICENSE file in the project root for license information.
// --------------------------------------------------------------------------------------------
package com.tkporter.fabrictwitterkit;

import android.app.Activity;
import android.content.Intent;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.google.gson.Gson;

import com.twitter.sdk.android.tweetcomposer.TweetComposer;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.User;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


public class FabricTwitterKitModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    public TwitterLoginButton loginButton;
    private final ReactApplicationContext reactContext;
    private Callback callback = null;
    //112 is the average ascii value for every letter in 'twitter'
    private static final int REQUEST_CODE = 112112;

    public FabricTwitterKitModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }


    @Override
    public String getName() {
        return "FabricTwitterKit";
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        onActivityResult(requestCode, resultCode, data);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                sendCallback(true, false, false);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                sendCallback(false, true, false);
            }
        }
        if (loginButton != null) {
            loginButton.onActivityResult(requestCode, resultCode, data);
            loginButton = null;
        }
    }

    public void onNewIntent(Intent intent) {
    }

    public void sendCallback(Boolean completed, Boolean cancelled, Boolean error) {
        if (callback != null) {
            callback.invoke(completed, cancelled, error);
            callback = null;
        }
    }

    @ReactMethod
    public void getUserName(final Callback callback) {
        String name = TwitterCore.getInstance().getSessionManager().getActiveSession().getUserName();
        callback.invoke(null, name);
    }

    @ReactMethod
    public void login(final Callback callback) {

        loginButton = new TwitterLoginButton(getCurrentActivity());
        loginButton.setCallback(new com.twitter.sdk.android.core.Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> sessionResult) {
                WritableMap result = new WritableNativeMap();
                result.putString("authToken", sessionResult.data.getAuthToken().token);
                result.putString("authTokenSecret",sessionResult.data.getAuthToken().secret);
                result.putString("userID", sessionResult.data.getUserId()+"");
                result.putString("userName", sessionResult.data.getUserName());
                callback.invoke(null, result);
            }

            @Override
            public void failure(TwitterException exception) {
                exception.printStackTrace();
                callback.invoke(exception.getMessage());
            }
        });

        loginButton.performClick();
    }

    @ReactMethod
    public void fetchProfile(final Callback callback) {

        try {
            ReactNativeFabricApiClient client = new ReactNativeFabricApiClient(TwitterCore.getInstance().getSessionManager().getActiveSession());
            client.getCustomService().show(TwitterCore.getInstance().getSessionManager().getActiveSession().getUserId()).enqueue(new com.twitter.sdk.android.core.Callback<User>() {
                @Override
                public void success(Result<User> result) {
                    Gson gson = new Gson();
                    try {
                        WritableMap map = (WritableMap) FabricTwitterKitUtils.jsonToWritableMap(gson.toJson(result.data));
                        callback.invoke(null, map);
                    } catch (Exception exception) {
                        callback.invoke(exception.getMessage());
                    }
                }

                @Override
                public void failure(TwitterException exception) {
                    exception.printStackTrace();
                    //TwitterCore.getInstance().getSessionManager().clearActiveSession();
                    callback.invoke(exception.getMessage());
                }
            });
        } catch (Exception ex) {
            callback.invoke(ex.getMessage());
        }
    }

    @ReactMethod
    public void createList(ReadableMap options, final Callback callback){
        try {
            ReactNativeFabricApiClient client = new ReactNativeFabricApiClient(TwitterCore.getInstance().getSessionManager().getActiveSession());
            // Not bothering with checking here if the key exists as will make sure in RN JS side that the values are present
            String name = options.getString("name");
            String description = options.getString("description");
            String mode = options.getString("mode");

            client.getCustomService().createList(name, description, mode).enqueue(new com.twitter.sdk.android.core.Callback<Object>() {
                @Override
                public void success(Result<Object> result) {
                    Gson gson = new Gson();
                    try {
                        WritableMap map = (WritableMap) FabricTwitterKitUtils.jsonToWritableMap(gson.toJson(result.data));
                        callback.invoke(null, map);
                    } catch (Exception exception) {
                        callback.invoke(exception.getMessage());
                    }
                }

                @Override
                public void failure(TwitterException exception) {
                    exception.printStackTrace();
                    callback.invoke(exception.getMessage());
                }
            });
        } catch(Exception ex) {
            callback.invoke(ex.getMessage());
        }
    }

    @ReactMethod
    public void deleteList(ReadableMap options, final Callback callback) {
        try {
            ReactNativeFabricApiClient client = new ReactNativeFabricApiClient(TwitterCore.getInstance().getSessionManager().getActiveSession());
            long listId = Long.parseLong(options.getString("listId"));
            client.getCustomService().deleteList(listId).enqueue(new com.twitter.sdk.android.core.Callback<Object>() {
                @Override
                public void success(Result<Object> result) {
                    Gson gson = new Gson();
                    try {
                        WritableMap map = (WritableMap) FabricTwitterKitUtils.jsonToWritableMap(gson.toJson(result.data));
                        callback.invoke(null, map);
                    } catch (Exception exception) {
                        callback.invoke(exception.getMessage());
                    }
                }

                @Override
                public void failure(TwitterException exception) {
                    exception.printStackTrace();
                    callback.invoke(exception.getMessage());
                }
            });
        } catch(Exception ex) {
            callback.invoke(ex.getMessage());
        }
    }

    @ReactMethod
    public void getMyLists(ReadableMap options, final Callback callback) {

        try {
            ReactNativeFabricApiClient client = new ReactNativeFabricApiClient(TwitterCore.getInstance().getSessionManager().getActiveSession());
            int count = options.hasKey("count") ? Integer.parseInt(options.getString("count")) : 50;
            client.getCustomService().myLists(TwitterCore.getInstance().getSessionManager().getActiveSession().getUserId(), count).enqueue(new com.twitter.sdk.android.core.Callback<Object>() {
                @Override
                public void success(Result<Object> result) {
                    Gson gson = new Gson();
                    try {
                        WritableMap map = (WritableMap) FabricTwitterKitUtils.jsonToWritableMap(gson.toJson(result.data));
                        callback.invoke(null, map);
                    } catch (Exception exception) {
                        callback.invoke(exception.getMessage());
                    }
                }

                @Override
                public void failure(TwitterException exception) {
                    exception.printStackTrace();
                    //TwitterCore.getInstance().getSessionManager().clearActiveSession();
                    callback.invoke(exception.getMessage());
                }
            });
        } catch (Exception ex) {
            callback.invoke(ex.getMessage());
        }
    }

    @ReactMethod
    public void getSubscribedLists(ReadableMap options, final Callback callback) {

        try {
            ReactNativeFabricApiClient client = new ReactNativeFabricApiClient(TwitterCore.getInstance().getSessionManager().getActiveSession());
            int count = options.hasKey("count") ? Integer.parseInt(options.getString("count")) : 50;
            client.getCustomService().subscribedLists(TwitterCore.getInstance().getSessionManager().getActiveSession().getUserId(), count).enqueue(new com.twitter.sdk.android.core.Callback<Object>() {
                @Override
                public void success(Result<Object> result) {
                    Gson gson = new Gson();
                    try {
                        WritableMap map = (WritableMap) FabricTwitterKitUtils.jsonToWritableMap(gson.toJson(result.data));
                        callback.invoke(null, map);
                    } catch (Exception exception) {
                        callback.invoke(exception.getMessage());
                    }
                }

                @Override
                public void failure(TwitterException exception) {
                    exception.printStackTrace();
                    //TwitterCore.getInstance().getSessionManager().clearActiveSession();
                    callback.invoke(exception.getMessage());
                }
            });
        } catch (Exception ex) {
            callback.invoke(ex.getMessage());
        }
    }

    @ReactMethod
    public void getMemberLists(ReadableMap options, final Callback callback) {

        try {
            ReactNativeFabricApiClient client = new ReactNativeFabricApiClient(TwitterCore.getInstance().getSessionManager().getActiveSession());
            int count = options.hasKey("count") ? Integer.parseInt(options.getString("count")) : 50;
            client.getCustomService().memberLists(TwitterCore.getInstance().getSessionManager().getActiveSession().getUserId(), count).enqueue(new com.twitter.sdk.android.core.Callback<Object>() {
                @Override
                public void success(Result<Object> result) {
                    Gson gson = new Gson();
                    try {
                        WritableMap map = (WritableMap) FabricTwitterKitUtils.jsonToWritableMap(gson.toJson(result.data));
                        callback.invoke(null, map);
                    } catch (Exception exception) {
                        callback.invoke(exception.getMessage());
                    }
                }

                @Override
                public void failure(TwitterException exception) {
                    exception.printStackTrace();
                    //TwitterCore.getInstance().getSessionManager().clearActiveSession();
                    callback.invoke(exception.getMessage());
                }
            });
        } catch (Exception ex) {
            callback.invoke(ex.getMessage());
        }
    }

    @ReactMethod
    public void logOut() {
        TwitterCore.getInstance().getSessionManager().clearActiveSession();
        TwitterCore.getInstance().logOut();
    }

    private boolean hasValidKey(String key, ReadableMap options) {
        return options.hasKey(key) && !options.isNull(key);
    }



    class ReactNativeFabricApiClient extends TwitterApiClient {
        public ReactNativeFabricApiClient(TwitterSession session) {
            super(session);
        }

        /**
         * Provide CustomService with defined endpoints
         */
        public CustomService getCustomService() {
            return getService(CustomService.class);
        }
    }

    // example users/show service endpoint
    interface CustomService {
        @GET("/1.1/users/show.json")
        Call<User> show(@Query("user_id") long id);

        @GET("/1.1/lists/ownerships.json")
        Call<Object> myLists(@Query("user_id") long id, @Query("count") int count);

        @GET("/1.1/lists/subscriptions.json")
        Call<Object> subscribedLists(@Query("user_id") long id, @Query("count") int count);

        @GET("/1.1/lists/memberships.json")
        Call<Object> memberLists(@Query("user_id") long id, @Query("count") int count);

        @POST("/1.1/lists/create.json")
        Call<Object> createList(@Query("name") String name, @Query("description") String description, @Query("mode") String mode);

        @POST("/1.1/lists/destroy.json")
        Call<Object> deleteList(@Query("list_id") long listId);
    }

}
