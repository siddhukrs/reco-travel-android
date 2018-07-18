package com.travel.reco;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionStateChange;

public class FeedActivity extends AppCompatActivity {

    private RecyclerView.LayoutManager lManager;
    private PhotoAdapter adapter;
    private Pusher pusher;
    private static final String prependEvent = "prependEvent";
    private static final String appendEvent = "appendEvent";
    private static final String CHANNEL_NAME = "my-channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        setupPusher();
        setupRecyclerView();
    }

    // Get the RecyclerView, use LinearLayout as the layout manager, and set custom adapter
    private void setupRecyclerView() {
        RecyclerView recycler = findViewById(R.id.recycler_view);
        lManager = new LinearLayoutManager(this);
        adapter = new PhotoAdapter(this, new ArrayList<Photo>());
        recycler.setLayoutManager(lManager);
        recycler.setAdapter(adapter);
    }

    private void setupPusher() {
        PusherOptions options = new PusherOptions().setCluster("us2");
        pusher = new Pusher(Configuration.pusherApiKey, options);
        Channel channel = pusher.subscribe(CHANNEL_NAME);
        channel.bind(appendEvent, appendAndScrollEventListener);
        channel.bind(prependEvent, prependAndScrollMessageListener);
        pusher.connect(new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                System.out.println("State changed to " + change.getCurrentState() + " from " + change.getPreviousState());
            }

            @Override
            public void onError(String message, String code, Exception e) {
                System.out.println("There was a problem connecting!");
                e.printStackTrace();
            }
        });
    }

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
    public void onDestroy() {
        super.onDestroy();
        pusher.disconnect();
    }
}
