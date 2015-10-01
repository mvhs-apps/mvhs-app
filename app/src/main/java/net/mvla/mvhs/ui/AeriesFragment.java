package net.mvla.mvhs.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;

import net.mvla.mvhs.R;
import net.mvla.mvhs.Utils;

public class AeriesFragment extends Fragment {

    public static final String SAVE_STATE_CREDENTIAL = "save_state_credential";

    public static final int RC_SAVE = 0;
    public static final int RC_READ = 1;
    public static final int RC_HINT = 2;

    private Credential mCurrentCredential;

    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private WebView mWebView;

    private Handler mHandler;

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

        mHandler = new Handler(getActivity().getMainLooper());

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
                .addOnConnectionFailedListener(connectionResult -> {
                    mConnected = false;
                    mConnectionFailed = true;
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
        mWebView.addJavascriptInterface(this, "android");

        mLoginLayout = view.findViewById(R.id.fragment_aeries_login_linear);
        mUsernameEditText = (EditText) view.findViewById(R.id.fragment_aeries_login_username);
        mPasswordEditText = (EditText) view.findViewById(R.id.fragment_aeries_login_password);
        mPasswordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                mLoginButton.callOnClick();
                return true;
            }
            return false;
        });
        mLoginButton = (Button) view.findViewById(R.id.fragment_aeries_login_button);
        mLoginButton.setOnClickListener(v -> {
            mAttemptRequestCredentials = false;
            login(mUsernameEditText.getText().toString(), mPasswordEditText.getText().toString());
            Utils.hideSoftKeyBoard(getActivity());
        });

        ViewCompat.setBackgroundTintList(mLoginButton, ContextCompat.getColorStateList(getActivity(), R.color.button_color_list));

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
        Utils.executeJavascript(mWebView, "document.getElementById(\"portalAccountUsername\").value=\"" + username + "\"");
        Utils.executeJavascript(mWebView, "document.getElementById(\"portalAccountPassword\").value=\"" + password + "\"");
        Utils.executeJavascript(mWebView, "document.getElementById(\"LoginButton\").click()");
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

        new Thread(() -> {
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
                    credentialRequestResult -> {
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
                    });
        }).start();

    }

    private void showIndeterminateProgressBar() {
        if (getActivity() != null) {
            ((AeriesActivity) getActivity()).showIndeterminateProgressBar();
        }
    }

    private void finishAllLoading() {
        if (!isAdded()) {
            return;
        }

        mHandler.post(() -> {
            if (getActivity() != null) {
                ((AeriesActivity) getActivity()).hideIndeterminateProgressBar();
            }

            if (mSavingSnackbar != null) {
                mSavingSnackbar.dismiss();
            }

            mLoginButton.setEnabled(true);
            mLoginButton.setText(getString(R.string.login));
        });
    }

    private void resolveResult(Status status, int requestCode) {
        //noinspection StatementWithEmptyBody
        if (status != null && status.hasResolution()) {
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

            new Thread(() -> {
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
                        status -> {
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
                                        finishAllLoading();
                                        onCredentialsSaveFailed();
                                    }
                                } else {
                                    finishAllLoading();
                                    onCredentialsSaveFailed();
                                }
                            }
                        }

                );
            }).start();
        }
    }

    @JavascriptInterface
    public void onFind(String value) {
        if (!value.equalsIgnoreCase("null")) {
            Snackbar.make(mWebView, R.string.incorrect_login, Snackbar.LENGTH_LONG).show();
        }
    }

    private class AeriesWebViewClient extends WebViewClient {

        @SuppressWarnings("deprecation")
        @Override
        public void onReceivedError(final WebView view, int errorCode, String description, final String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

            onError(view, errorCode, description, failingUrl);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);

            onError(view, error.getErrorCode(), error.getDescription().toString(), request.getUrl().toString());
        }

        private void onError(WebView view, int errorCode, String description, String failingUrl) {
            if (view != null && view.getContext() != null) {
                mErrorSnackbar = Snackbar.make(view, "Error " + errorCode + ": " + description, Snackbar.LENGTH_INDEFINITE)
                        .setAction("Try Again", v -> {
                            mErrorSnackbar.dismiss();
                            view.loadUrl(failingUrl);
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

            if (!isAdded()) {
                return;
            }

            //Login page
            if (url.equalsIgnoreCase("https://mvla.asp.aeries.net/student/LoginParent.aspx")) {

                if (mAttemptRequestCredentials) {
                    showIndeterminateProgressBar();
                    attemptRequestCredentials();
                } else {
                    finishAllLoading();
                }

                for (String error : new String[]{"errorMessage_password", "errorMessage_username"}) {
                    Utils.executeJavascript(mWebView, "android.onFind(document.getElementById(\"" + error + "\").innerText)");
                }
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            if (!isAdded()) {
                return;
            }

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
