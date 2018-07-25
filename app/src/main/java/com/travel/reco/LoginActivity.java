package com.travel.reco;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.RemoteMessage;
import com.pusher.client.Pusher;
import com.pusher.pushnotifications.PushNotificationReceivedListener;
import com.pusher.pushnotifications.PushNotifications;

import net.londatiga.android.instagram.*;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private Instagram instagram;
    private InstagramSession instagramSession;
    private boolean logoutCommandRecieved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PushNotifications.start(getApplicationContext(), Configuration.pushNotificationsClientId);
        PushNotifications.subscribe("relogin");
        PushNotifications.setOnMessageReceivedListenerForVisibleActivity(this, new PushNotificationReceivedListener() {
            @Override
            public void onMessageReceived(RemoteMessage remoteMessage) {
                logoutCommandRecieved = true;
                logout();
            }
        });
        instagram = new Instagram(this, Configuration.clientId, Configuration.clientSecret, Configuration.redirectURI);
        instagramSession = instagram.getSession();

        String actionReason = getIntent().getStringExtra("action");
        if(actionReason != null && actionReason.equals("logout")) {
            logout();
        }

        if (instagramSession.isActive()) {
            login();
        }
        else {
            setContentView(R.layout.activity_login);
            ImageView instagramIcon = findViewById(R.id.ig_icon);
            instagramIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    instagram.authorize(mAuthListener);
                }
            });
        }
    }

    private void login() {
        InstagramUser instagramUser = instagramSession.getUser();
        String accessToken = instagramSession.getAccessToken();
        String username = instagramUser.username;

        PusherConfiguration.setupPusher();
        PusherConfiguration.subscribeToPublicChannel();
        PusherConfiguration.sendKeyOnLogin(accessToken, username);

        showToast("Logged in as " + username);
        finish();
        Intent intent = new Intent(LoginActivity.this, FeedActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    private void logout() {
        instagramSession.reset();
        showToast("Logged out");
        finish();
        startActivity(new Intent(LoginActivity.this, LoginActivity.class));
    }

    private Instagram.InstagramAuthListener mAuthListener = new Instagram.InstagramAuthListener() {
        @Override
        public void onSuccess(InstagramUser user) {
            login();
        }

        @Override
        public void onError(String error) {
            showToast(error);
        }

        @Override
        public void onCancel() {
        }
    };

    private void showToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(logoutCommandRecieved) {
            logout();
        }
    }
}
