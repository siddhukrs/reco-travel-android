package com.travel.reco;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.londatiga.android.instagram.*;

public class LoginActivity extends AppCompatActivity {

    private Instagram instagram;
    private InstagramSession instagramSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instagram = new Instagram(this, Configuration.clientId, Configuration.clientSecret, Configuration.redirectURI);
        instagramSession = instagram.getSession();

        String actionReason = getIntent().getStringExtra("action");
        if(actionReason != null && actionReason.equals("logout")) {
            instagramSession.reset();
            showToast("Logged out");
            finish();
            startActivity(new Intent(LoginActivity.this, LoginActivity.class));
        }

        if (instagramSession.isActive()) {
            InstagramUser instagramUser = instagramSession.getUser();
            showToast("Logged in as: " + instagramUser.username);
            finish();
            startActivity(new Intent(LoginActivity.this, FeedActivity.class));
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

    private Instagram.InstagramAuthListener mAuthListener = new Instagram.InstagramAuthListener() {
        @Override
        public void onSuccess(InstagramUser user) {
            finish();
            startActivity(new Intent(LoginActivity.this, FeedActivity.class));
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
}
