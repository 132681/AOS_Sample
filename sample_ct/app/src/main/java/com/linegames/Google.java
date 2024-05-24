package com.linegames;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.facebook.FacebookException;
import com.facebook.internal.WebDialog;
import com.google.android.gms.games.AuthenticationResult;
import com.google.android.gms.games.Games;
//import com.google.android.gms.games.GamesSignInClient;
//import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.PlayGamesSdk;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.SnapshotsClient;
import com.google.android.gms.games.snapshot.SnapshotContents;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.linegames.base.NTLog;


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

//import com.google.android.gms.games.snapshot.SnapshotCoordinator;
//import com.google.android.gms.games.snapshot.SnapshotsClient;
import com.google.android.gms.drive.Drive;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Google extends Activity
{
    private static String LOG_TAG = "NTSDK";
    private static String PURCHASE_TAG = "PurchaeAPI";

    private static String result_msg= "Connect Success.";
    private static String result_status = "NT_SUCCESS";
    private static int result_ResponseCode = 0;
    private static long purchaseCB = 0L;
    private static long noCB = 777;

    private static String webClientId = "764920947053-te91v114127brn1mdkolmuscn3iuugma.apps.googleusercontent.com";
//    private static String webClientId = "764920947053-te91v114127brn1mdkolmuscn3iuugma.apps.googleusercontent.commm";

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
                    //PlayGamesSdk.initialize(mMainActivity);

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

        if (mGoogleSignInClient == null)
            mMainActivity = NTBase.getMainActivity();

        mGoogleSignInClient = GoogleSignIn.getClient(mMainActivity, gs);

        if (mGoogleSignInClient == null)
            NTLog.d("lss ================= mGoogleSignInClient is null ===================");
        else
            NTLog.d("lss =================  mGoogleSignInClient is not null ===================");
    }

    public void StartGooglePlayGamesSdk()
    {
        NTLog.d("lss ================= StartGooglePlayGamesSdk ===================");
        PlayGamesSdk.initialize(mMainActivity);
    }

    public void Sign( boolean isGoogleSign )
    {
        if (isGoogleSign)
        {
            Google.GetInstance().StartGoogleSign();

            GoogleSign();
        }
        else
        {
           // StartGooglePlayGamesSdk();
            GooglePlayServiceSign();
        }
    }

    public void GoogleSign()
    {
        NTLog.d("","lss GoogleSign");
        mGoogleSignInClient.silentSignIn().addOnCompleteListener( mMainActivity,new OnCompleteListener<GoogleSignInAccount>()
                {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> taskAuth )
                    {
                        if( taskAuth.isSuccessful() )
                        {
                            NTLog.d("","lss Google: SilentSignIn success " + taskAuth.getResult().toString() );
                            String userid = taskAuth.getResult().getId();
                            String token = taskAuth.getResult().getIdToken();
                            NTLog.d("","lss userid : " + userid );
                            NTLog.d("","lss token : " + token );
                        }
                        else
                        {
                            NTLog.d("","lss Google: SilentSignIn Fail" );

                            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                            mMainActivity.startActivityForResult(signInIntent, MainActivity.RC_SIGN_IN_GOOGLE_SIGN_IN);
                        }
                    }
                });

    }

    public void GooglePlayServiceSign()
    {
        NTLog.d("","lss GooglePlayServiceSign");

        GamesSignInClient gamesSignInClient = PlayGames.getGamesSignInClient(mMainActivity);

        gamesSignInClient.isAuthenticated().addOnCompleteListener(isAuthenticatedTask -> {

            boolean isAuthenticated = (isAuthenticatedTask.isSuccessful() && isAuthenticatedTask.getResult().isAuthenticated());
            if (isAuthenticated) {
                // Continue with Play Games Services
                NTLog.d("","lss GooglePlayServiceSign Continue with Play Games Services");
                OnGooglePlayServiceResult(gamesSignInClient);

            } else {
                NTLog.d("","lss GooglePlayServiceSign.signIn() Not Logined");
                gamesSignInClient.signIn().addOnCompleteListener(mMainActivity, new OnCompleteListener<AuthenticationResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthenticationResult> task) {
                        gamesSignInClient.isAuthenticated().addOnCompleteListener(isAuthenticatedTask -> {
                            if ( isAuthenticatedTask.isSuccessful() && isAuthenticatedTask.getResult().isAuthenticated() )
                            {
                                NTLog.d("","lss Google Play Service Login success");
                                OnGooglePlayServiceResult(gamesSignInClient);
                            }
                            else
                            {
                                NTLog.d("","lss Google Play Service Login Cancel");
                            }
                        });
                    }
                });
                //                gamesSignInClient.signIn();
            }
        });
    }

    public void OnGooglePlayServiceResult(GamesSignInClient gamesSignInClient)
    {
        gamesSignInClient.requestServerSideAccess(webClientId, /*forceRefreshToken=*/ false)
                .addOnCompleteListener( task -> {
                    if (task.isSuccessful())
                    {
                        PlayGames.getPlayersClient(mMainActivity).getCurrentPlayer().addOnCompleteListener(mTask -> {
                            // Get PlayerID with mTask.getResult().getPlayerId()
                            NTLog.d("","lss call GamesSignInClient.signIn()nnn");
                            //NTLog.d("","lss Email : " +  task.getResult().);
                            NTLog.d("","lss id : " +  "");
                            NTLog.d("","lss displayName : " +  mTask.getResult().getDisplayName());
                            NTLog.d("","lss idToken : " +  "");
                            NTLog.d("","lss familyName : " +  "");
                            NTLog.d("","lss givenName : " +  "");
                            NTLog.d("","lss photoUrl : " +  mTask.getResult().getIconImageUri());
                            NTLog.d("","lss getserverAuthCode : " +  task.getResult());
                            NTLog.d("","lss playerId : " +  mTask.getResult().getPlayerId());
                            NTLog.d("","lss mTask getResult : " +  mTask.getResult().toString());
                        });
                    } else {
                        // Failed to retrieve authentication code.
                        NTLog.d("","lss call requestServerSideAccess fail");
                    }
                });
    }
    public void GoogleSignOut()
    {
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

            String userid = completedTask.getResult().getId();
            String token = completedTask.getResult().getIdToken();
            NTLog.d("","lss userid : " + userid );
//            NTLog.d("","lss token : " + token );

            // Get AccessToken
//            String accessToken = account.getIdToken();
//            NTLog.d("AccessToken", "lss Google Sign-In AccessToken: " + accessToken);

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

            String userid = completedTask.getResult().getId();
            String token = completedTask.getResult().getIdToken();
            NTLog.d("","lss userid : " + userid );
            //NTLog.d("","lss token : " + token );

//            Games.getGamesClient(NTBase.getMainActivity(), completedTask.getResult()).setViewForPopups( mMainActivity.getWindow().getDecorView() );

            String accessToken = account.getIdToken();
            // Get AccessToken
            //NTLog.d("AccessToken", "lss Google Play Services AccessToken: " + accessToken);

            // Additional handling for Google Play Services sign-in
            // ...

        } catch (ApiException e) {
            // Sign in failed
            //Log.w("GooglePlayServicesSignIn", "signInResult:failed code=" + e.getStatusCode());
            //Toast.makeText(this, "Sign in to Google Play Services failed", Toast.LENGTH_SHORT).show();
        }
    }

    private static final int RC_SAVED_GAMES = 9009;
    public void signInSilently()
    {
        SnapshotsClient snapshotsClient =
                PlayGames.getSnapshotsClient(this);
        int maxNumberOfSavedGamesToShow = 1;

        Task<Intent> intentTask = snapshotsClient.getSelectSnapshotIntent(
                "See My Saves", true, true, maxNumberOfSavedGamesToShow);

        Log.d("", "lss signInSilently");
        intentTask.addOnSuccessListener(new OnSuccessListener<Intent>() {
            @Override
            public void onSuccess(Intent intent) {
                NTBase.getMainActivity().startActivityForResult(intent, RC_SAVED_GAMES);
                Log.d("", "lss signInSilently onSuccess");
            }
        });
    }

    private static final String TEST_STRING = "This is a test string.";
    public Task<SnapshotMetadata> writeSnapshot(String sSaveName, String sSaveData, String sDesc) {

        if (sSaveName == null) {
            NTLog.d("", "sSaveName is null");
            return Tasks.forResult(null);
        }

        final byte[] byteData;
        if (sSaveData != null) {
            byteData = sSaveData.getBytes(StandardCharsets.UTF_8);
        } else {
            NTLog.d("", "sSaveData is null");
            return Tasks.forResult(null);
        }

        SnapshotsClient snapshotsClient = PlayGames.getSnapshotsClient(NTBase.getMainActivity());
        int conflictResolutionPolicy = SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED;

        return snapshotsClient.open(sSaveName, true, conflictResolutionPolicy)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        NTLog.d("" , e.toString());
                    }
                })
                .continueWithTask(new Continuation<SnapshotsClient.DataOrConflict<Snapshot>, Task<SnapshotMetadata>>() {
                    @Override
                    public Task<SnapshotMetadata> then(@NonNull Task<SnapshotsClient.DataOrConflict<Snapshot>> task) throws Exception {
                        Snapshot snapshot = task.getResult().getData();
                        SnapshotContents snapshotContents = snapshot.getSnapshotContents();

                        if (snapshotContents != null) {
                            snapshotContents.writeBytes(byteData);
                        }

                        SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                                .setDescription(sDesc) // Example description
                                .build();

                        if (snapshot != null) {
                            return snapshotsClient.commitAndClose(snapshot, metadataChange);

                        } else {
                            // Return a completed task with null result if snapshot is null
                            //return Tasks.forResult(null);
                            myToast = Toast.makeText(mMainActivity,"GoogleCloud Save Fail : " , Toast.LENGTH_SHORT);
                        }
                        myToast.show();
                        return Tasks.forResult(null);

                    }
                });
    }
    Toast myToast;
    public Task<String> loadSnapshot(String sLoadSaveName) {

        if (sLoadSaveName == null) {
            Log.e("", "sLoadSaveName is null");
            return Tasks.forResult("");
        }

        SnapshotsClient snapshotsClient = PlayGames.getSnapshotsClient(NTBase.getMainActivity());
        int conflictResolutionPolicy = SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED;
        return snapshotsClient.open(sLoadSaveName, true, conflictResolutionPolicy)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                         Log.e("", "Error while opening Snapshot.", e);
                    }
                }).continueWith(new Continuation<SnapshotsClient.DataOrConflict<Snapshot>, String>() {
                    @Override
                    public String then(@NonNull Task<SnapshotsClient.DataOrConflict<Snapshot>> task) throws Exception {
                        Snapshot snapshot = task.getResult().getData();

                        try {
                            byte[] rawData = snapshot.getSnapshotContents().readFully();
                            if (rawData != null) {
                                String resultStr = new String(rawData, StandardCharsets.UTF_8);
                                myToast = Toast.makeText(mMainActivity,"GoogleCloud Load Success : " + resultStr, Toast.LENGTH_SHORT);
//                                return Tasks.forResult(resultStr);
                                return resultStr;
                            } else {
//                                return "";
                                myToast = Toast.makeText(mMainActivity,"GoogleCloud Load Fail : " , Toast.LENGTH_SHORT);
                            }
                            myToast.show();

                        } catch (IOException e) {
                            Log.e("", "Error while reading Snapshot.", e);
                        }
                        return null;
                    }
                }).addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                    }
                });
    }

}
