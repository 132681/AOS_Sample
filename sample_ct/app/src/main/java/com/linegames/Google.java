package com.linegames;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.games.Games;
//import com.google.android.gms.games.GamesSignInClient;
//import com.google.android.gms.games.PlayGames;
//import com.google.android.gms.games.PlayGamesSdk;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.linegames.base.NTLog;


import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
//import com.google.android.gms.games.Games;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.linegames.base.NTBase;
import com.linegames.ct2.MainActivity;

import java.util.Objects;
import java.util.concurrent.Executor;

public class Google
{
    private static String LOG_TAG = "NTSDK";
    private static String PURCHASE_TAG = "PurchaeAPI";

    private static String result_msg= "Connect Success.";
    private static String result_status = "NT_SUCCESS";
    private static int result_ResponseCode = 0;
    private static long purchaseCB = 0L;
    private static long noCB = 777;

    public static GoogleSignInClient mGoogleSignInClient;
    public static GoogleSignInClient mGooglePlayServiceClient;

    private static Activity mMainActivity;

    private static Google getInstance = null;
    public native void nativeCB( String status, String msg, long userCB );

    @SuppressLint("SuspiciousIndentation")
    public static synchronized Google GetInstance() {
        if (getInstance == null) {
            synchronized ( Google.class ) {
                if ( getInstance == null )
                    getInstance = new Google();
                    mMainActivity = NTBase.getMainActivity();

            }
        }
        return getInstance;
    }

    public void StartGoogleSign()
    {
        NTLog.d("", "lss =================== StartGoogleSign ==================");
        GoogleSignInOptions gs = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestId()
                .requestEmail()
                .build();

        GoogleSignInOptions gps = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                .requestId()
                .requestEmail()
                .build();

        if (mGoogleSignInClient == null)
            mMainActivity = NTBase.getMainActivity();

        mGoogleSignInClient = GoogleSignIn.getClient(mMainActivity, gs);

        mGooglePlayServiceClient = GoogleSignIn.getClient(mMainActivity, gps);

        if (mGoogleSignInClient == null)
            NTLog.d("lss ================= mGoogleSignInClient is null ===================");
        else
            NTLog.d("lss =================  mGoogleSignInClient is not null ===================");
    }

    public void StartGooglePlayGamesSdk()
    {
//        PlayGamesSdk.initialize(mMainActivity);
    }

    public void Sign( boolean isGoogleSign )
    {
        if (isGoogleSign)
        {
            GoogleSign();
        }
        else
        {
            GooglePlayServiceSign();
        }
    }

    public void GoogleSign()
    {
        NTLog.d("","lss GoogleSign");

         mGoogleSignInClient.silentSignIn().addOnCompleteListener( mMainActivity,
                new OnCompleteListener<GoogleSignInAccount>()
                {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> taskAuth )
                    {
                        if( taskAuth.isSuccessful() )
                        {
                            NTLog.d("","lss Google: success" + taskAuth.getResult().toString() );
                            String userid = taskAuth.getResult().getId();
                            String token = taskAuth.getResult().getIdToken();
                            NTLog.d("","lss userid : " + userid );
                            NTLog.d("","lss token : " + token );
                        }
                        else
                        {
                            NTLog.d("","lss Google: signInSilently(): failure" );
                            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                            mMainActivity.startActivityForResult(signInIntent, MainActivity.RC_SIGN_IN_GOOGLE_SIGN_IN);
                        }
                    }
                });

    }

    public void GooglePlayServiceSign()
    {
        NTLog.d("","lss GooglePlayServiceSign");

        mGooglePlayServiceClient.silentSignIn().addOnCompleteListener( mMainActivity,
                new OnCompleteListener<GoogleSignInAccount>()
                {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> taskAuth )
                    {
                        if( taskAuth.isSuccessful() )
                        {
                            NTLog.d("","lss GooglePlayServiceSign: success" + taskAuth.getResult().toString() );
                            String userid = taskAuth.getResult().getId();
                            String token = taskAuth.getResult().getIdToken();
                            NTLog.d("","lss userid : " + userid );
                            NTLog.d("","lss token : " + token );

                            if (userid == null)
                            {
                                NTLog.d("", "lss userid is null");
                            }
                            else {
                                PlayersClient playersClient = Games.getPlayersClient( mMainActivity, taskAuth.getResult() );
                                playersClient.getCurrentPlayer().addOnCompleteListener(new OnCompleteListener<Player>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Player> task) {

                                        NTLog.d("lssplayersClient.getCurrentPlayer() isSuccessful(): " + task.isSuccessful() );

                                        if ( task.isSuccessful() ) {
                                            // String displayName = task.getResult().getDisplayName();
                                            String m_sPlayerID = Objects.requireNonNull(task.getResult()).getPlayerId();
                                            NTLog.d("lss m_sPlayerID: " + m_sPlayerID );
                                        }
                                        else {
                                            Exception e = task.getException();
                                        }
                                    }
                                });
                            }
                        }
                        else
                        {
                            NTLog.d("","lss GooglePlayServiceSign: failure" );
                            Intent signInIntent = mGooglePlayServiceClient.getSignInIntent();
                            mMainActivity.startActivityForResult(signInIntent, MainActivity.RC_SIGN_IN_GOOGLE_PLAY_SERVICES_SIGN_IN);
                        }
                    }
                });

    }

    public void GoogleSignOut()
    {
        NTLog.d("","lss GoogleSignOut");

        mGoogleSignInClient.signOut()
                .addOnCompleteListener(mMainActivity, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Sign out completed
                        //Toast.makeText(mMainActivity, "Signed out", Toast.LENGTH_SHORT).show();
                        NTLog.d("","lss GoogleSignOut : " + task.isSuccessful());
                    }
                });
    }

    public void GooglePlayServiceSignOut()
    {
        NTLog.d("","lss GooglePlayServiceSignOut");

        mGooglePlayServiceClient.signOut()
                .addOnCompleteListener(mMainActivity, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Sign out completed
                        NTLog.d("","lss GooglePlayServiceSignOut : " + task.isSuccessful());
                    }
                });
    }

    public void GoogleSilentLogin()
    {
        NTLog.d("","GoogleSilentLogin");

    }

    public void GoogleSignSilentLogin()
    {
        NTLog.d("","GoogleSignSilentLogin");
    }

    public void GooglePlayServiceSilentLogin()
    {
        NTLog.d("","GooglePlayServiceSilentLogin");

//        Games.getGamesClient(mMainActivity, GoogleSignIn.getLastSignedInAccount(mMainActivity))
//                .getActivationHint()
//                .addOnCompleteListener(new OnCompleteListener<Bundle>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Bundle> task) {
//                        if (task.isSuccessful()) {
//                            // Signed in successfully
//                            NTLog.d("GooglePlayServicesSignIn", "Silent Signed in successfully");
//                            // Get AccessToken
//                            // 여기에서는 AccessToken을 직접 가져오는 API가 제공되지 않으므로 처리 방법은 다를 수 있습니다.
//                            // Google Play 서비스에 대한 AccessToken은 GoogleSignInAccount에 직접적으로 노출되지 않습니다.
//                            // 따라서 Google Play 서비스의 Silent 로그인 시 AccessToken을 직접 가져오는 방법에 대해서는 구글 공식 문서를 참고하시기 바랍니다.
//                        } else {
//                            // Sign in failed
//                            NTLog.w("GooglePlayServicesSignIn", "Silent signInResult:failed");
//                        }
//                    }
//                });
    }

    /*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else if (requestCode == RC_SIGN_IN_GOOGLE_PLAY_SERVICES_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGooglePlayServicesSignInResult(task);
        }
    }
    */

    public void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully
           // Toast.makeText(this, "Signed in as: " + account.getEmail(), Toast.LENGTH_SHORT).show();
            NTLog.d("GoogleSignIn", "lss Signed in as: " + account.getEmail());

            // Get AccessToken
            String accessToken = account.getIdToken();
            NTLog.d("AccessToken", "lss Google Sign-In AccessToken: " + accessToken);

        } catch (ApiException e) {
            // Sign in failed
            //Log.w("GoogleSignIn", "signInResult:failed code=" + e.getStatusCode());
            //Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void handleGooglePlayServicesSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully
            //Toast.makeText(this, "Signed in to Google Play Services as: " + account.getEmail(), Toast.LENGTH_SHORT).show();
            NTLog.d("GooglePlayServicesSignIn", "lss Signed in as: " + account.getEmail());

            String accessToken = account.getIdToken();
            // Get AccessToken
            NTLog.d("AccessToken", "lss Google Play Services AccessToken: " + accessToken);

            // Additional handling for Google Play Services sign-in
            // ...

        } catch (ApiException e) {
            // Sign in failed
            //Log.w("GooglePlayServicesSignIn", "signInResult:failed code=" + e.getStatusCode());
            //Toast.makeText(this, "Sign in to Google Play Services failed", Toast.LENGTH_SHORT).show();
        }
    }
}
