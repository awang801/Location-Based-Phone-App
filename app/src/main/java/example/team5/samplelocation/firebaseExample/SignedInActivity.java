package example.team5.samplelocation.firebaseExample;

/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import example.team5.samplelocation.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import example.team5.samplelocation.databaseupdate.dbString;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignedInActivity extends AppCompatActivity {
    final String TAG = "SignedInActivity";

    @BindView(android.R.id.content)
    View mRootView;

    @BindView(R.id.user_profile_picture)
    ImageView mUserProfilePicture;

    @BindView(R.id.user_email)
    TextView mUserEmail;

    @BindView(R.id.user_display_name)
    TextView mUserDisplayName;

    @BindView(R.id.user_enabled_providers)
    TextView mEnabledProviders;

    private IdpResponse mIdpResponse;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(AuthUiActivity.createIntent(this));
            finish();
            return;
        }

        mIdpResponse = IdpResponse.fromResultIntent(getIntent());

        setContentView(R.layout.signed_in_layout);
        ButterKnife.bind(this);
        populateProfile();
        populateIdpToken();
    }

    @OnClick(R.id.sign_out)
    public void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(AuthUiActivity.createIntent(SignedInActivity.this));
                            finish();
                        } else {
                            showSnackbar(R.string.sign_out_failed);
                        }
                    }
                });
    }

    @OnClick(R.id.delete_account)
    public void deleteAccountClicked() {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete this account?")
                .setPositiveButton("Yes, nuke it!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteAccount();
                    }
                })
                .setNegativeButton("No", null)
                .create();

        dialog.show();
    }

    private void deleteAccount() {
        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(AuthUiActivity.createIntent(SignedInActivity.this));
                            finish();
                        } else {
                            showSnackbar(R.string.delete_account_failed);
                        }
                    }
                });
    }

    @MainThread
    private void populateProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .fitCenter()
                    .into(mUserProfilePicture);
        }

        mUserEmail.setText(
                TextUtils.isEmpty(user.getEmail()) ? "No email" : user.getEmail());
        mUserDisplayName.setText(
                TextUtils.isEmpty(user.getDisplayName()) ? "No display name" : user.getDisplayName());

        StringBuilder providerList = new StringBuilder(100);
        providerList.append("Providers used: ");

        if (user.getProviders() == null || user.getProviders().isEmpty()) {
            providerList.append("none");
        } else {
            Iterator<String> providerIter = user.getProviders().iterator();
            while (providerIter.hasNext()) {
                String provider = providerIter.next();
                if (GoogleAuthProvider.PROVIDER_ID.equals(provider)) {
                    providerList.append("Google");
                } else if (FacebookAuthProvider.PROVIDER_ID.equals(provider)) {
                    providerList.append("Facebook");
                } else if (EmailAuthProvider.PROVIDER_ID.equals(provider)) {
                    providerList.append("Password");
                } else {
                    providerList.append(provider);
                }

                if (providerIter.hasNext()) {
                    providerList.append(", ");
                }
            }
        }

        mEnabledProviders.setText(providerList);

        //Http Request to Send the UserId to the backend
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            // Store the instance id for use in the okhttp request later
                            String instanceID = FirebaseInstanceId.getInstance().getToken();
                            String idToken = task.getResult().getToken();

                            Log.d(TAG, "UserToken: " + idToken);
                            Log.d(TAG, "InstanceID: " + instanceID);
                            // Send token to your backend via HTTPS
                            OkHttpClient client = new OkHttpClient();
                            RequestBody body = new FormBody.Builder()
                                    .add("acc_id", idToken)
                                    .add("instance", instanceID)
                                    .build();

                            Request request = new Request.Builder()
                                    .url(dbString.php_root+"addUser.php")
                                    .post(body)
                                    .build();

                            //try {
                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    e.printStackTrace();
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    if (!response.isSuccessful()) {
                                        throw new IOException("Unexpected code " + response);
                                    }
                                    else {
                                        Log.d(TAG, "Response: " + response.body().string());
                                    }
                                }
                            });
                        } else {
                            // Handle error -> task.getException();
                        }
                    }
                });
    }

    private void populateIdpToken() {
        String token = null;
        String secret = null;
        if (mIdpResponse != null) {
            token = mIdpResponse.getIdpToken();
            secret = mIdpResponse.getIdpSecret();
        }
        if (token == null) {
            findViewById(R.id.idp_token_layout).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.idp_token)).setText(token);
        }
        if (secret == null) {
            findViewById(R.id.idp_secret_layout).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.idp_secret)).setText(secret);
        }
    }

    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG)
                .show();
    }

    public static Intent createIntent(Context context, IdpResponse idpResponse) {
        Intent in = IdpResponse.getIntent(idpResponse);
        in.setClass(context, SignedInActivity.class);
        return in;
    }
}