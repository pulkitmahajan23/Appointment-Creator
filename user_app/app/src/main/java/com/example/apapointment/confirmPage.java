package com.example.apapointment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ibm.cloud.appid.android.api.AppID;
import com.ibm.cloud.appid.android.api.AppIDAuthorizationManager;
import com.ibm.cloud.appid.android.api.AuthorizationException;
import com.ibm.cloud.appid.android.api.AuthorizationListener;
import com.ibm.cloud.appid.android.api.LoginWidget;
import com.ibm.cloud.appid.android.api.tokens.AccessToken;
import com.ibm.cloud.appid.android.api.tokens.IdentityToken;
import com.ibm.cloud.appid.android.api.tokens.RefreshToken;
import com.ibm.cloud.appid.android.api.userprofile.UserProfileException;

import java.net.URL;
import java.util.List;

public class confirmPage extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    private AppID appID;

    private AppIDAuthorizationManager appIDAuthorizationManager;
    private TokensPersistenceManager tokensPersistenceManager;
    private NoticeHelper noticeHelper;

    private NoticeHelper.AuthState authState;

    private ProgressManager progressManager;
    private boolean isCloudDirectory = false, isAnonymous = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_page);

        progressManager = new ProgressManager(this);

        getSupportActionBar().hide();

        appID = AppID.getInstance();

        appIDAuthorizationManager = new AppIDAuthorizationManager(appID);
        tokensPersistenceManager = new TokensPersistenceManager(this, appIDAuthorizationManager);

        noticeHelper = new NoticeHelper(this, appIDAuthorizationManager, tokensPersistenceManager);
        IdentityToken idt = appIDAuthorizationManager.getIdentityToken();

        //Getting information from identity token. This is information that is coming from the identity provider.
        List<String> authenticationMethods = idt.getAuthenticationMethods();
        if (authenticationMethods != null
                && authenticationMethods.size() >= 1
                && authenticationMethods.get(0).equals("cloud_directory")) {
            isCloudDirectory = true;
        }
        String profilePhotoUrl = idt.getPicture();
        setProfilePhoto(profilePhotoUrl);

        String userName = idt.getEmail() != null ? idt.getEmail().split("@")[0] : "Guest";
        if(idt.getName() != null)
            userName = idt.getName();
        ((TextView) findViewById(R.id.details_name_set)).setText(userName);
        if(medicalActivity.med) {
            ((TextView) findViewById(R.id.details_type_set)).setText(medicalActivity.getType());
            ((TextView) findViewById(R.id.details_vendor_set)).setText(medicalActivity.getVendor());
            ((TextView) findViewById(R.id.details_slot_set)).setText(medicalActivity.getSlot());
            Toast.makeText(this,"Medical",Toast.LENGTH_SHORT).show();
        }
        else if(localVendor.local_vendor)
        {
            ((TextView) findViewById(R.id.details_type_set)).setText(localVendor.getType());
            ((TextView) findViewById(R.id.details_vendor_set)).setText(localVendor.getVendor());
            ((TextView) findViewById(R.id.details_slot_set)).setText(localVendor.getSlot());
            Toast.makeText(this,"Local Vendor",Toast.LENGTH_SHORT).show();
        }
        else if(pharmacyActivity.pharmacy)
        {
            ((TextView) findViewById(R.id.details_type_set)).setText(pharmacyActivity.getType());
            ((TextView) findViewById(R.id.details_vendor_set)).setText(pharmacyActivity.getVendor());
            ((TextView) findViewById(R.id.details_slot_set)).setText(pharmacyActivity.getSlot());
            Toast.makeText(this,"Pharmacy",Toast.LENGTH_SHORT).show();
        }
        else if(supermarketActivity.superm)
        {
            ((TextView) findViewById(R.id.details_type_set)).setText(supermarketActivity.getType());
            ((TextView) findViewById(R.id.details_vendor_set)).setText(supermarketActivity.getVendor());
            ((TextView) findViewById(R.id.details_slot_set)).setText(supermarketActivity.getSlot());
            Toast.makeText(this,"Supermarket",Toast.LENGTH_SHORT).show();
        }
        authState = (NoticeHelper.AuthState)getIntent().getSerializableExtra("auth-state");
    }

    private void setProfilePhoto(final String photoUrl) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final Bitmap bmp = photoUrl == null || photoUrl.length() == 0 ? null :
                            BitmapFactory.decodeStream(new URL(photoUrl).openConnection().getInputStream());
                    //run on main thread
                    if (bmp != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ImageButton profilePicture = (ImageButton) findViewById(R.id.imageButton);
                                profilePicture.setImageBitmap(Utils.getRoundedCornerBitmap(bmp,40));
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void handleAppIdError(UserProfileException e) {
        switch(e.getError()){
            case FAILED_TO_CONNECT:
                throw new RuntimeException("Failed to connect to App ID to access profile attributes", e);
            case UNAUTHORIZED:
                throw new RuntimeException("Not authorized to access profile attributes at App ID", e);
        }
    }

    private String logTag(String methodName){
        return getClass().getCanonicalName() + "." + methodName;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        AuthorizationListener listener =
                new AuthorizationListener() {
                    @Override
                    public void onAuthorizationFailure(AuthorizationException e) {
                        Log.e(logTag("onAuthorizationFailure"),"Authorization failed", e);

                        progressManager.hideProgress();
                        String errorMsg = e.getMessage();
                        if (errorMsg != null && errorMsg.contains("selfServiceEnabled is OFF")) {
                            progressManager.showAlert("Oops...", "You can not perform this action");
                        } else {
                            progressManager.showAlert("Oops...", e.getMessage());
                        }
                    }

                    @Override
                    public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken, RefreshToken refreshToken) {
                        progressManager.hideProgress();
                        if (accessToken != null && identityToken != null) {
                            Intent intent = new Intent(confirmPage.this, confirmPage.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            confirmPage.this.startActivity(intent);
                        }
                    }

                    @Override
                    public void onAuthorizationCanceled() {
                        progressManager.hideProgress();
                    }
                };
        progressManager.showProgress();
        switch (item.getItemId()) {
            case R.id.accountDetails:
                appID.getLoginWidget().launchChangeDetails(this, listener);
                return true;
            case R.id.changePassword:
                appID.getLoginWidget().launchChangePassword(this, listener);
                return true;
            case R.id.logOut:
                onLogoutClick(null);
                return true;
            default:
                progressManager.hideProgress();
                return false;
        }
    }
    public void onLogoutClick(View view) {
        if (isAnonymous) { // in case of anonymous, the "logout" button is actually "login"...
            launchLoginWidget();
            return;
        }
        appID.logout();
        tokensPersistenceManager.clearStoredTokens();
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
        this.finish();
    }

    private void launchLoginWidget() {
        LoginWidget loginWidget = appID.getLoginWidget();
        AuthorizationListener loginAuthorization = new AppIdSampleAuthorizationListener(this,appIDAuthorizationManager,false);
        loginWidget.launch(this,loginAuthorization);
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.user_menu);
        if (!isCloudDirectory) {
            popup.getMenu().findItem(R.id.changePassword).setVisible(false);
            popup.getMenu().findItem(R.id.accountDetails).setVisible(false);
        }
        if (isAnonymous) {
            popup.getMenu().findItem(R.id.logOut).setTitle(R.string.login);
        }
        popup.show();
    }
}
