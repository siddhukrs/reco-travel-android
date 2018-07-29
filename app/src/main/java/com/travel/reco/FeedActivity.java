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
import android.widget.TextView;

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
    private boolean modelReadyCommandRecieved = false;
    private Configuration configuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userId = getIntent().getStringExtra("userId");
        username = getIntent().getStringExtra("username");
        accessToken = getIntent().getStringExtra("accessToken");
        groupId = getIntent().getStringExtra("groupId");
        configuration = new Configuration();
        setContentView(R.layout.activity_feed);
        setupRecyclerView();
        setSupportActionBar((Toolbar)findViewById(R.id.top_toolbar));
        setupSwipeToRefresh();

        setupPusherSubscriptions();
    }

    private void setupPusherSubscriptions() {

        PushNotificationReceivedListener modelReadyPushNotificationListener = new PushNotificationReceivedListener() {
            @Override
            public void onMessageReceived(RemoteMessage remoteMessage) {
                modelReadyCommandRecieved = true;
            }
        };
        PushNotifications.start(getApplicationContext(), Configuration.pushNotificationsClientId);
        PushNotifications.subscribe("model-ready");
        PushNotifications.setOnMessageReceivedListenerForVisibleActivity(this, modelReadyPushNotificationListener);

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
                        "{\"user_id\":\"" + userId + "\", \"access_token\":\"" + accessToken + "\", \"group_id\": \"" + groupId + "\"}");
            }
            @Override
            public void onEvent(String channelName, String eventName, String data) {
            }
        };

        PusherConfiguration.subscribeToPrivateChannel(userId, subscriptionSuccessListener);
        PusherConfiguration.subscribeToEventOnPrivateChannel(PusherConfiguration.appendEntryEvent, appendAndScrollEventListener);
        PusherConfiguration.subscribeToEventOnPrivateChannel(PusherConfiguration.prependEntryEvent, prependAndScrollMessageListener);
    }

    private void setupSwipeToRefresh() {
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                clearAllImages();
                pullToRefresh.setRefreshing(false);
                HTTPCaller refreshCaller = new HTTPCaller();
                refreshCaller.execute(Configuration.recoEndpoint,
                        "{\"user_id\":\"" + userId + "\", \"group_id\":\"" + groupId + "\"}");
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
                View parentView = (View)v.getParent();
                TextView locationTextView = parentView.findViewById(R.id.location);
                String location = locationTextView.getText().toString();
                Uri uri = Uri.parse("https://www.united.com/ual/en/us/flight-search/book-a-flight/results/rev?f=AUS&t="
                        + configuration.getAirportCode(location)
                        + "&d=2018-09-21&r=2018-09-23&px=1&taxng=1&idx=1");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        recycler.setLayoutManager(lManager);
        recycler.setAdapter(adapter);
    }

    private void clearAllImages() {
        adapter.clearPhotos();
        adapter.notifyDataSetChanged();
    }

    private void addImageToView(final String jsonData, final boolean prepend, final boolean scrollToImage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Received event with data: " + jsonData);
                Gson gson = new Gson();
                Photo photo = gson.fromJson(jsonData, Photo.class);
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
