package com.travel.reco;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.SubscriptionEventListener;

public class PusherConfiguration {
    private static Pusher pusher;

    public static Channel privateChannel;
    private static String privateChannelName;
    public static final String prependEntryEvent = "prependEntryEvent";
    public static final String appendEntryEvent = "appendEntryEvent";

    public static Channel publicChannel;
    private static final String publicChannelName = "publicChannel";
    private static final String sendKeyEvent = "keyEvent";

    public static void subscribeToPublicChannel() {
        publicChannel = pusher.subscribe(publicChannelName);
    }

    public static void subscribeToPrivateChannel(String userName, ChannelEventListener subscriptionSuccessListener) {
        privateChannelName = userName;
        privateChannel = pusher.subscribe(privateChannelName, subscriptionSuccessListener);
    }

    public static void subscribeToEventOnPrivateChannel (String eventName, SubscriptionEventListener listener) {
        privateChannel.bind(eventName, listener);
    }

    public static void sendKeyOnLogin (String key, String userName) {
    }

    public static void setupPusher() {
        PusherOptions options = new PusherOptions().setCluster("us2");
        pusher = new Pusher(Configuration.pusherApiKey, options);
        pusher.connect();
    }

    public static void disconnect() {
        pusher.disconnect();
    }
}
