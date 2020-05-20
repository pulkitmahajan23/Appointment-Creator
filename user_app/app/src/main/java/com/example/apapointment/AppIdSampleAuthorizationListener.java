package com.example.apapointment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import com.ibm.cloud.appid.android.api.AppIDAuthorizationManager;
import com.ibm.cloud.appid.android.api.AuthorizationException;
import com.ibm.cloud.appid.android.api.AuthorizationListener;
import com.ibm.cloud.appid.android.api.tokens.AccessToken;
import com.ibm.cloud.appid.android.api.tokens.IdentityToken;
import com.ibm.cloud.appid.android.api.tokens.RefreshToken;

/**
 * This listener provides the callback methods that are called at the end of App ID
 * authorization process when using the {@link com.ibm.cloud.appid.android.api.AppID} login APIs
 */
public class AppIdSampleAuthorizationListener implements AuthorizationListener {
    private NoticeHelper noticeHelper;
    private TokensPersistenceManager tokensPersistenceManager;
    private boolean isAnonymous;
    private Activity activity;
    static private boolean shouldShowChromeIssueDialog = true;
    static private int clicks = 0;

    public AppIdSampleAuthorizationListener(Activity activity, AppIDAuthorizationManager authorizationManager, boolean isAnonymous) {
        tokensPersistenceManager = new TokensPersistenceManager(activity, authorizationManager);
        noticeHelper = new NoticeHelper(activity, authorizationManager, tokensPersistenceManager);
        this.isAnonymous = isAnonymous;
        this.activity = activity;
        clicks += 1;
    }

    @Override
    public void onAuthorizationFailure(AuthorizationException exception) {
        Log.e(logTag("onAuthorizationFailure"),"Authorization failed", exception);
    }

    @Override
    public void onAuthorizationCanceled() {
        Log.w(logTag("onAuthorizationCanceled"),"Authorization canceled");
        if (shouldShowChromeIssueDialog && clicks % 3 == 0) {
            // in here we assume that the app isn't able to open Chrome
            // show dialog suggesting Peter may have to open chrome and accept the terms of service.
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage("Please open Chrome and accept their terms of service.")
                    .setTitle("Could Not Open Chrome Tab")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            shouldShowChromeIssueDialog = false; // dialog was shown, we don't want to show it again
        }
    }

    @Override
    public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken, RefreshToken refreshToken) {
        Log.i(logTag("onAuthorizationSuccess"),"Authorization succeeded");
        shouldShowChromeIssueDialog = false; // if authorization was successful then there's no problem with chrome
        if (accessToken == null && identityToken == null) {
            Log.i(logTag("onAuthorizationSuccess"),"Finish done flow");

        } else {
            Intent intent = new Intent(activity, home.class);
            intent.putExtra("auth-state", noticeHelper.determineAuthState(isAnonymous));
            //storing the new token
            tokensPersistenceManager.persistTokensOnDevice();
            activity.startActivity(intent);
            activity.finish();
        }
    }

    private String logTag(String methodName){
        return this.getClass().getCanonicalName() + "." + methodName;
    }
}
