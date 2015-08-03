package com.mvhsapp.app.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.mvhsapp.app.R;
import com.mvhsapp.app.Utils;

public class AeriesFragment extends Fragment {

    public static final String SAVE_STATE_CREDENTIAL = "save_state_credential";

    public static final int RC_SAVE = 0;
    public static final int RC_READ = 1;
    public static final int RC_HINT = 2;

    private Credential mCurrentCredential;

    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private WebView mWebView;

    private boolean mLoggedIn;
    private GoogleApiClient mCredentialsApiClient;

    private View mLoginLayout;
    private Button mLoginButton;
    private Snackbar mErrorSnackbar;
    private Snackbar mSavingSnackbar;

    private boolean mConnected;
    private boolean mConnectionFailed;

    private boolean mAttemptRequestCredentials = true;

    @Override
    public void onStart() {
        super.onStart();
        mCredentialsApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mCredentialsApiClient.disconnect();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_STATE_CREDENTIAL, mCurrentCredential);
        mWebView.saveState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCredentialsApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        mConnected = true;
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        mCredentialsApiClient.reconnect();
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        mConnected = false;
                    }
                })
                .addApi(Auth.CREDENTIALS_API)
                .build();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_aeries, container, false);

        mWebView = (WebView) view.findViewById(R.id.fragment_aeries_webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new AeriesWebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (getActivity() != null) {
                    ((AeriesActivity) getActivity()).setProgressBarProgress(newProgress);
                }
            }
        });

        mLoginLayout = view.findViewById(R.id.fragment_aeries_login_linear);
        mUsernameEditText = (EditText) view.findViewById(R.id.fragment_aeries_login_username);
        mPasswordEditText = (EditText) view.findViewById(R.id.fragment_aeries_login_password);
        mPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    mLoginButton.callOnClick();
                    return true;
                }
                return false;
            }
        });
        mLoginButton = (Button) view.findViewById(R.id.fragment_aeries_login_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(mUsernameEditText.getText().toString(), mPasswordEditText.getText().toString());
                Utils.hideSoftKeyBoard(getActivity());
            }
        });

        ViewCompat.setBackgroundTintList(mLoginButton, getResources().getColorStateList(R.color.button_color_list));

        if (savedInstanceState == null) {
            mWebView.loadUrl("https://mvla.asp.aeries.net/student/LoginParent.aspx");
            mAttemptRequestCredentials = true;
        } else {
            mCurrentCredential = savedInstanceState.getParcelable(SAVE_STATE_CREDENTIAL);
            mWebView.restoreState(savedInstanceState);
        }

        return view;
    }

    private void login(String username, String password) {
        Utils.executeJavascript(mWebView, "document.getElementById(\"portalAccountUsername\").value=\"" + username + "\"", null);
        Utils.executeJavascript(mWebView, "document.getElementById(\"portalAccountPassword\").value=\"" + password + "\"", null);
        Utils.executeJavascript(mWebView, "document.getElementById(\"LoginButton\").click()", null);
    }

    /**
     * Returns whether took care of it
     */
    public boolean onBackPressed() {
        if (!mLoggedIn || !mWebView.canGoBack()) {
            return false;
        } else {
            mWebView.goBack();
            return true;
        }
    }


    private void onCredentialsSaved() {
        showSnackbarMessage(getString(R.string.credentials_saved));
    }

    private void onCredentialsSaveFailed() {
        showSnackbarMessage(getString(R.string.save_failed));
    }


    private void showSnackbarMessage(String string) {
        Snackbar.make(mWebView, string, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Request option 1: https://developers.google.com/identity/smartlock-passwords/android/overview
     */
    private void attemptRequestCredentials() {
        mLoginButton.setText(getString(R.string.retrieving_credentials));

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int attempt = 0; attempt < 10 && !mConnected && !mConnectionFailed; attempt++) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (!mConnected || mConnectionFailed) {
                    //Failed
                    finishAllLoading();
                    return;
                }

                CredentialRequest request = new CredentialRequest.Builder()
                        .setSupportsPasswordLogin(true)
                        .build();

                Auth.CredentialsApi.request(mCredentialsApiClient, request).setResultCallback(
                        new ResultCallback<CredentialRequestResult>() {
                            @Override
                            public void onResult(CredentialRequestResult credentialRequestResult) {
                                if (credentialRequestResult.getStatus().isSuccess()) {
                                    // Single credential and auto sign-in enabled.
                                    //Request 2
                                    processRetrievedCredential(credentialRequestResult.getCredential(), false);
                                    finishAllLoading();
                                } else {
                                    Status status = credentialRequestResult.getStatus();
                                    if (status.getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED) {
                                        // Needs to save credentials
                                        //Request 4
                                        resolveResult(status, RC_HINT);
                                    } else {
                                        // Multiple credentials - pick one
                                        //Request 3
                                        resolveResult(status, RC_READ);
                                    }
                                }
                            }
                        });
            }
        }).start();

    }

    private void showIndeterminateProgressBar() {
        if (getActivity() != null) {
            ((AeriesActivity) getActivity()).showIndeterminateProgressBar();
        }
    }

    private void finishAllLoading() {
        if (getActivity() != null) {
            ((AeriesActivity) getActivity()).hideIndeterminateProgressBar();
        }

        if (mSavingSnackbar != null) {
            mSavingSnackbar.dismiss();
        }
        mLoginButton.setEnabled(true);
        mLoginButton.setText(getString(R.string.login));
    }

    private void resolveResult(Status status, int requestCode) {
        //noinspection StatementWithEmptyBody
        if (status.hasResolution()) {
            try {
                // -> onActivityResult
                status.startResolutionForResult(getActivity(), requestCode);
            } catch (IntentSender.SendIntentException e) {
                //STATUS: Failed to send resolution.
                finishAllLoading();
            }
        } else {
            // The user must create an account or sign in manually.
            //STATUS: Unsuccessful credential request had no resolution.
            finishAllLoading();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        finishAllLoading();
        switch (requestCode) {
            case RC_HINT:
            case RC_READ:
                //Request 3/4
                if (resultCode == Activity.RESULT_OK) {
                    boolean isHint = (requestCode == RC_HINT);
                    Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                    processRetrievedCredential(credential, isHint);
                } /*else {
                    //Credential Read: NOT OK
                }*/
                break;
            case RC_SAVE:
                if (resultCode == Activity.RESULT_OK) {
                    onCredentialsSaved();
                }/* else {
                    //User chose not to save
                }*/
                break;
        }
    }

    private void processRetrievedCredential(Credential credential, boolean isHint) {
        if (!isHint) {
            //Request 2/3
            mUsernameEditText.setText(credential.getId());
            mPasswordEditText.setText(credential.getPassword());
            login(credential.getId(), credential.getPassword());
        } else {
            //Request 4
            mUsernameEditText.setText(credential.getId());
        }
    }

    private void attemptSaveCredentials() {
        //Username and password fields are not empty
        if (!mUsernameEditText.getText().toString().isEmpty()
                && !mPasswordEditText.getText().toString().isEmpty()) {

            final Credential credential = new Credential.Builder(mUsernameEditText.getText().toString())
                    .setPassword(mPasswordEditText.getText().toString())
                    .build();

            if (mCurrentCredential == null || mCurrentCredential.describeContents() != credential.describeContents()) {
                mSavingSnackbar = Snackbar.make(mWebView, R.string.saving_credentials, Snackbar.LENGTH_INDEFINITE);
                mSavingSnackbar.show();
            }

            mCurrentCredential = credential;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int attempt = 0; attempt < 10 && !mConnected && !mConnectionFailed; attempt++) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (!mConnected || mConnectionFailed) {
                        //Failed
                        finishAllLoading();
                        onCredentialsSaveFailed();
                        return;
                    }

                    Auth.CredentialsApi.save(mCredentialsApiClient, credential).setResultCallback(
                            new ResultCallback<Status>() {
                                @Override
                                public void onResult(Status status) {
                                    if (status.isSuccess()) {
                                        // Credentials were saved
                                        onCredentialsSaved();
                                        finishAllLoading();
                                    } else {
                                        if (status.hasResolution()) {
                                            // Try to resolve the save request. This will prompt the user if
                                            // the credential is new.
                                            try {
                                                status.startResolutionForResult(getActivity(), RC_SAVE);
                                            } catch (IntentSender.SendIntentException e) {
                                                // Could not resolve the request
                                                onCredentialsSaveFailed();
                                            }
                                        } else {
                                            onCredentialsSaveFailed();
                                        }
                                    }
                                }
                            }

                    );
                }
            }).start();
        }
    }


    private class AeriesWebViewClient extends WebViewClient {

        @Override
        public void onReceivedError(final WebView view, int errorCode, String description, final String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

            if (view != null) {
                mErrorSnackbar = Snackbar.make(view, "Error " + errorCode + ": " + description, Snackbar.LENGTH_INDEFINITE)
                        .setAction("Try Again", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mErrorSnackbar.dismiss();
                                view.loadUrl(failingUrl);
                            }
                        });
                mErrorSnackbar.show();

                finishAllLoading();
                mLoginButton.setEnabled(false);
                mLoginButton.setText(R.string.error);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("https://mvla.asp.aeries.net")) {
                return false;
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);

                return true;
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            //Login page
            if (url.equalsIgnoreCase("https://mvla.asp.aeries.net/student/LoginParent.aspx")) {

                if (mAttemptRequestCredentials) {
                    showIndeterminateProgressBar();
                    attemptRequestCredentials();
                } else {
                    finishAllLoading();
                }

                //TODO: Use WebView.findAll for compat
                Utils.executeJavascript(mWebView, "document.getElementById(\"errorMessage_password\").innerText", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        if (!value.equalsIgnoreCase("null")) {
                            Snackbar.make(mWebView, getString(R.string.incorrect_login), Snackbar.LENGTH_LONG);
                        }
                    }
                });

                Utils.executeJavascript(mWebView, "document.getElementById(\"errorMessage_username\").innerText", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        if (!value.equalsIgnoreCase("null")) {
                            Snackbar.make(mWebView, getString(R.string.incorrect_login), Snackbar.LENGTH_LONG);
                        }
                    }
                });
            } else if (url.startsWith("https://mvla.asp.aeries.net/student/m/loginparent.html")) {
                mAttemptRequestCredentials = false;
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            if (url.equalsIgnoreCase("https://mvla.asp.aeries.net/student/LoginParent.aspx")) {
                //Login page
                mLoggedIn = false;

                mWebView.setFocusable(false);
                mLoginButton.setText(getString(R.string.loading));
                mLoginButton.setEnabled(false);
                mLoginLayout.setVisibility(View.VISIBLE);

            } else if (url.equalsIgnoreCase("https://mvla.asp.aeries.net/student/m/loginparent.html")) {
                //Logged in
                mLoggedIn = true;
                mLoginLayout.setVisibility(View.GONE);
                mWebView.setFocusable(true);

                //If fields are not empty
                attemptSaveCredentials();
            }
        }


    }


}
