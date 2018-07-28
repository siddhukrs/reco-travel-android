package com.travel.reco;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.ArrayList;

import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.SubscriptionEventListener;

import com.pusher.pushnotifications.PushNotificationReceivedListener;
import com.pusher.pushnotifications.PushNotifications;

public class FeedActivity extends AppCompatActivity {

    private String userId;
    private String username;
    private String accessToken;
    private String groupId;

    private RecyclerView.LayoutManager lManager;
    private PhotoAdapter adapter;
    private boolean logoutCommandRecieved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userId = getIntent().getStringExtra("userId");
        username = getIntent().getStringExtra("username");
        accessToken = getIntent().getStringExtra("accessToken");
        groupId = getIntent().getStringExtra("groupId");

        setContentView(R.layout.activity_feed);
        setupRecyclerView();
        setSupportActionBar((Toolbar)findViewById(R.id.top_toolbar));
        setupSwipeToRefresh();

        setupPusherSubscriptions();
    }

    private void setupPusherSubscriptions() {

        PushNotificationReceivedListener logoutPushNotificationListener = new PushNotificationReceivedListener() {
            @Override
            public void onMessageReceived(RemoteMessage remoteMessage) {
                logoutCommandRecieved = true;
                logout();
            }
        };
        PushNotifications.start(getApplicationContext(), Configuration.pushNotificationsClientId);
        PushNotifications.subscribe("relogin");
        PushNotifications.setOnMessageReceivedListenerForVisibleActivity(this, logoutPushNotificationListener);

        SubscriptionEventListener appendAndScrollEventListener = new SubscriptionEventListener() {
            @Override
            public void onEvent(String channel, final String event, final String data) {
                addImageToView(data, false, true);
            }
        };

        SubscriptionEventListener prependAndScrollMessageListener = new SubscriptionEventListener() {
            @Override
            public void onEvent(String channel, final String event, final String data) {
                addImageToView(data, true, true);
            }
        };

        ChannelEventListener subscriptionSuccessListener = new ChannelEventListener() {
            @Override
            public void onSubscriptionSucceeded(String channelName) {
                System.out.println("Subscribed!");
                HTTPCaller tokenCaller = new HTTPCaller();
                tokenCaller.execute(Configuration.tokenEndpoint,
                        "{\"user_id\":\"" + userId + "\", \"access_token\":\"" + accessToken + "\", \"group_id\": " + groupId + "}");
            }
            @Override
            public void onEvent(String channelName, String eventName, String data) {
            }
        };

        PusherConfiguration.subscribeToPrivateChannel(username, subscriptionSuccessListener);
        PusherConfiguration.subscribeToEventOnPrivateChannel(PusherConfiguration.appendEntryEvent, appendAndScrollEventListener);
        PusherConfiguration.subscribeToEventOnPrivateChannel(PusherConfiguration.prependEntryEvent, prependAndScrollMessageListener);
    }

    private void setupSwipeToRefresh() {
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullToRefresh.setRefreshing(false);
                HTTPCaller refreshCaller = new HTTPCaller();
                refreshCaller.execute(Configuration.recoEndpoint, "{}");
            }
        });
    }

    // Get the RecyclerView, use LinearLayout as the layout manager, and set custom adapter
    private void setupRecyclerView() {
        RecyclerView recycler = findViewById(R.id.recycler_view);
        lManager = new LinearLayoutManager(this);
        adapter = new PhotoAdapter(this, new ArrayList<Photo>(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.united.com");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        recycler.setLayoutManager(lManager);
        recycler.setAdapter(adapter);
    }

    private void addImageToView(final String jsonUrlData, final boolean prepend, final boolean scrollToImage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Received event with data: " + jsonUrlData);
                Gson gson = new Gson();
                Photo photo = gson.fromJson(jsonUrlData, Photo.class);
                if(prepend == true) {
                    adapter.prependPhoto(photo);
                    if(scrollToImage) {
                        ((LinearLayoutManager) lManager).scrollToPositionWithOffset(0, 0);
                    }
                }
                else {
                    adapter.appendPhoto(photo);
                    if(scrollToImage) {
                        ((LinearLayoutManager) lManager).scrollToPositionWithOffset(adapter.getItemCount() - 1, 0);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(logoutCommandRecieved)
        {
            logout();
        }
    }

    private void logout() {
        Intent intent = new Intent(FeedActivity.this, LoginActivity.class);
        intent.putExtra("action", "logout");
        finish();
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feed, menu);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PusherConfiguration.disconnect();
    }
}
