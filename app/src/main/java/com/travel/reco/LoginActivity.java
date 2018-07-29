package com.travel.reco;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private boolean logoutCommandReceived = false;
    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PushNotifications.start(getApplicationContext(), Configuration.pushNotificationsClientId);
        PushNotifications.subscribe("relogin");
        PushNotifications.setOnMessageReceivedListenerForVisibleActivity(this, new PushNotificationReceivedListener() {
            @Override
            public void onMessageReceived(RemoteMessage remoteMessage) {
                logoutCommandReceived = true;
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
            loadData();
            login();
        }
        else {
            setContentView(R.layout.activity_login);
            ImageView instagramIcon = findViewById(R.id.ig_icon);
            instagramIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    groupId = ((TextView)findViewById(R.id.groupId)).getText().toString();
                    saveData();
                    instagram.authorize(mAuthListener);
                }
            });
        }
    }

    private void login() {
        InstagramUser instagramUser = instagramSession.getUser();
        String accessToken = instagramSession.getAccessToken();
        String username = instagramUser.username;
        String userId = instagramUser.id;

        PusherConfiguration.setupPusher();

        showToast("Logged in as " + username);
        finish();
        Intent intent = new Intent(LoginActivity.this, FeedActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("userId", userId);
        intent.putExtra("accessToken", accessToken);
        intent.putExtra("groupId", groupId);
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
        loadData();
        if(logoutCommandReceived) {
            logout();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveData();
    }

    private void saveData() {
        SharedPreferences sp =
                getSharedPreferences("GroupPreferences",
                        Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("groupId", groupId);
        editor.commit();
    }

    private void loadData() {
        SharedPreferences sp =
                getSharedPreferences("GroupPreferences",
                        Context.MODE_PRIVATE);
        groupId = sp.getString("groupId", groupId);
    }
}
