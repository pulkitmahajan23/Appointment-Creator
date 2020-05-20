package com.example.apapointment;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.ibm.cloud.appid.android.api.AppID;
import com.ibm.cloud.appid.android.api.AppIDAuthorizationManager;
import com.ibm.cloud.appid.android.api.LoginWidget;
public class MainActivity extends AppCompatActivity {
    private final static String region = AppID.REGION_UK;
    private final static String authTenantId = "2233da77-828a-4ab9-9638-3da709626bdd";

    private AppID appId;
    private AppIDAuthorizationManager appIDAuthorizationManager;
    private TokensPersistenceManager tokensPersistenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        appId = AppID.getInstance();

        appId.initialize(this, authTenantId, region);

        appIDAuthorizationManager = new AppIDAuthorizationManager(this.appId);
        tokensPersistenceManager = new TokensPersistenceManager(this, appIDAuthorizationManager);

        String storedRefreshToken = tokensPersistenceManager.getStoredRefreshToken();
        if (storedRefreshToken != null && !storedRefreshToken.isEmpty()) {
            refreshTokens(storedRefreshToken);
        }
    }

    private void refreshTokens(String refreshToken) {
        Log.d(logTag("refreshTokens"), "Trying to refresh tokens using a refresh token");
        boolean storedTokenAnonymous = tokensPersistenceManager.isStoredTokenAnonymous();
        AppIdSampleAuthorizationListener appIdSampleAuthorizationListener =
                new AppIdSampleAuthorizationListener(this, appIDAuthorizationManager, storedTokenAnonymous);
        appId.signinWithRefreshToken(this, refreshToken, appIdSampleAuthorizationListener);
    }

    public void loginClick(View v) {
        Log.d(logTag("onLoginClick"),"Attempting identified authorization");
        LoginWidget loginWidget = appId.getLoginWidget();
        final String storedAccessToken;
        storedAccessToken = tokensPersistenceManager.getStoredAccessToken();

        AppIdSampleAuthorizationListener appIdSampleAuthorizationListener =
                new AppIdSampleAuthorizationListener(this, appIDAuthorizationManager, false);

        loginWidget.launch(this, appIdSampleAuthorizationListener, storedAccessToken);
    }

    private String logTag(String methodName){
        return getClass().getCanonicalName() + "." + methodName;
    }
}

