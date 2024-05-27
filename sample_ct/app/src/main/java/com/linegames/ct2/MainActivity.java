package com.linegames.ct2;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.SnapshotsClient;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.linecorp.linesdk.auth.LineLoginApi;
import com.linecorp.linesdk.auth.LineLoginResult;
import com.linegames.Line;
import com.linegames.Purchase;
import com.linegames.PurchaseGalaxy;
import com.linegames.auth.Facebook;
import com.linegames.base.LGBase;
import com.linegames.Google;
import com.linegames.base.LGLog;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import org.json.JSONException;

public class MainActivity extends Activity
{
    private ViewPager2 viewPager;
    private MyPagerAdapter pagerAdapter;
    private List<String> titles;

    private static final String TAG = "NTSDK";
    public static String PurchaseConsumeProductId = "cointop2_google_play_gem100";
    public static String ConsumeProductId1 = "cointop2_google_play_gem100";
//    public static String ConsumeProductId1 = "cointop2_google_play_gem300";
    public static String ConsumeProductId1Sub = "cointop2_google_play_gem100_sub";
    public static String ConsumeProductId2 = "cointop2_google_play_gem500";
    public static String ConsumeProductId3 = "cointop2_google_play_gem100000";
    private static final int RC_SAVED_GAMES = 9009;
    //permission
    private int nCurrentPermission = 0;
    private static final int PERMISSIONS_REQUEST = 0x0000001;
    public static final int RC_SIGN_IN_GOOGLE_SIGN_IN = 1001;
    public static final int RC_SIGN_IN_GOOGLE_PLAY_SERVICES_SIGN_IN = 1002;

    public static TextView infoTextView;

    private static String GalaxyPid = "ct_samsung_apps_gem100"; //ct_samsung_apps_gem300 ct_samsung_apps_gem500

    static {
        System.loadLibrary("native-lib");
//        System.loadLibrary("LGSDK");
    }

    public String getSelectedProductId() {
        return selectedProductId;
    }

    public void setSelectedProductId(String selectedProductId) {
        this.selectedProductId = selectedProductId;
    }

    private String selectedProductId = "cointop2_google_play_gem100";


    public native String stringFromJNI();
    //public native String LoginReciever(int status, String jsAccessToken, String jsMsg);
    //@JvmStatic external fun LoginReciever( status : Int, jsFBID : String, jsAccessToken : String, jsMsg : String )
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Log.d(TAG, "stringFromJNI : " + stringFromJNI());

        LGBase.getInstance().onCreate( this );

        Google.GetInstance().StartGoogleSign();

        int targetSdkVersion = getApplicationContext().getApplicationInfo().targetSdkVersion;
        Log.d(TAG, "targetSdkVersion : " + targetSdkVersion);

        titles = new ArrayList<>();
        titles.add("Google");
        titles.add("Purchase");
        titles.add("Adjust");
        titles.add("UMG");
        titles.add("ETC1_");
        titles.add("ETC2_");

        viewPager = findViewById(R.id.viewPager);

        pagerAdapter = new MyPagerAdapter(titles, viewPager);
        viewPager.setAdapter(pagerAdapter);

        viewPager.setCurrentItem(titles.size() * 1000, false);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    int currentPosition = viewPager.getCurrentItem();
                    int lastReal = pagerAdapter.getItemCount() - 1;
                    if (currentPosition == 0) {
                        viewPager.setCurrentItem(lastReal - 1, false);
                    } else if (currentPosition == lastReal) {
                        viewPager.setCurrentItem(1, false);
                    }
                }
            }
        });
        infoTextView = pagerAdapter.getInfoTextView(0);
        if (infoTextView != null) {
            infoTextView.setText("Updated Info Text");
        }
    }

    // getSpinnerItems 메소드를 static으로 변경
    public List<String> getSpinnerItems() {
        List<String> items = new ArrayList<>();
        items.add("cointop2_google_play_gem100");
        items.add("cointop2_google_play_gem300");
        items.add("cointop2_google_play_gem500");
        items.add("cointop2_google_play_gem800");
        return items;
    }

    public class MyPagerAdapter extends RecyclerView.Adapter<MyPagerAdapter.ViewHolder> {
        private List<String> titles;
        private ViewPager2 viewPager;
        public MyPagerAdapter(List<String> titles, ViewPager2  viewPager) {
            this.titles = titles;
            this.viewPager = viewPager;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_page, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            int virtualPosition = position % titles.size();
            String title = titles.get(virtualPosition);
            holder.titleTextView.setText(title);

            // Set up spinner for Purchase page only
            if (title.equals("Purchase")) {
                holder.spinner.setVisibility(View.VISIBLE);
            } else {
                holder.spinner.setVisibility(View.GONE);
            }

            // Set infotextview based on page title
            holder.infoTextView.setText(title);
            // Set text information for each page
            switch (title) {
                case "Google":
                    holder.infoTextView.setText("Google information");
                    break;
                case "Purchase":
                    holder.infoTextView.setText("Purchase information");
                    break;
                case "Adjust":
                    holder.infoTextView.setText("Adjust information");
                    break;
                case "UMG":
                    holder.infoTextView.setText("UMG information");
                    break;
                case "ETC1":
                    holder.infoTextView.setText("ETC1 information");
                    break;
                case "ETC2":
                    holder.infoTextView.setText("ETC2 information");
                    break;
            }

            // Set OnClickListener for each button
            for (int i = 0; i < 10; i++) {
                String buttonText = generateButtonText(title, i + 1); // Generate button text based on page title and button index

                Button button = null;
                switch (i) {
                    case 0:
                        button = holder.addButton1;
                        break;
                    case 1:
                        button = holder.addButton2;
                        break;
                    case 2:
                        button = holder.addButton3;
                        break;
                    case 3:
                        button = holder.addButton4;
                        break;
                    case 4:
                        button = holder.addButton5;
                        break;
                    case 5:
                        button = holder.addButton6;
                        break;
                    case 6:
                        button = holder.addButton7;
                        break;
                    // Add cases for other buttons as needed
                }

                if (button != null) {
                    button.setText(buttonText);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Call corresponding class's function based on button text
                            switch (title) {
                                case "Google":
                                    generateButtonText(buttonText, holder.infoTextView);
                                    break;
                                case "Purchase":
                                    generateButtonText(buttonText, holder.infoTextView);
                                    break;
                                case "Adjust":
                                    generateButtonText(buttonText, holder.infoTextView);
                                    break;
                                case "UMG":
                                    generateButtonText(buttonText, holder.infoTextView);
                                    break;
                                case "ETC1":
                                    generateButtonText(buttonText, holder.infoTextView);
                                    break;
                                case "ETC2":
                                    generateButtonText(buttonText, holder.infoTextView);
                                    break;
                                // Add cases for other titles if needed
                            }
                        }
                    });
                }
            }
        }

        private String generateButtonText(String title, int index) {
            // Generate button text based on page title and button index
            return title + " " + index;
        }

        private void generateButtonText(String buttonText, TextView infoTextView)  {
            Log.d(TAG, "Google button clicked: " + buttonText );
            switch (buttonText) {
                case "Google 1":
                    Google.GetInstance().GoogleSign();
                    infoTextView.setText("GoogleSign");
                    break;
                case "Google 2":
                    Google.GetInstance().GoogleSignOut();
                    infoTextView.setText("GoogleSignOut");
                    break;
                case "Google 3":
                    // 람다 표현식으로 GooglePlayServiceSign 사용
                    Google.GetInstance().GooglePlayServiceSign( (success, playerId) -> {
                        String resultStr = "Sign in failed.";
                        if (success) {
                            LGLog.d("", "Sign in successful! Player ID: " + playerId);
                            resultStr = "Sign in successful! Player ID: " + playerId;
                        } else {
                            LGLog.d("", "Sign in failed.");
                        }
                        infoTextView.setText("GooglePlayServiceSign");
                    });

                    break;
                case "Google 4":
                    Google.GetInstance().signInSilently();
                    infoTextView.setText("GoogleCloud List");
                    break;
                case "Google 5":
                    Google.GetInstance().writeSnapshot(getString(R.string.SaveLoadName), getString(R.string.SaveData), getString(R.string.Desc))
                            .addOnCompleteListener(new OnCompleteListener<SnapshotMetadata>() {
                                @Override
                                public void onComplete(@NonNull Task<SnapshotMetadata> task) {
                                    if (task.isSuccessful()) {
                                        // 게임 콘텐츠가 성공적으로 저장되었을 때의 처리
                                        // 예를 들어, 사용자에게 저장 완료 메시지를 표시하거나 UI를 업데이트할 수 있습니다.
                                        LGLog.d("","lss GoogleCloudSave Success : " + task.toString());
                                        infoTextView.setText("Success GoogleCloudSave");
                                    } else {
                                        // 저장이 실패한 경우 처리
                                        Exception e = task.getException();
                                        LGLog.d("","lss GoogleCloudSave Error : " + e.toString());
                                        // 에러 메시지를 표시하거나 적절한 오류 처리를 수행합니다.
                                        infoTextView.setText(" GoogleCloudSave Error : " + e.toString());
                                    }
                                }});
                            break;
                case "Google 6":
                        Google.GetInstance().loadSnapshot(getString(R.string.SaveLoadName)).addOnCompleteListener(new OnCompleteListener<String>() {
                                    @Override
                                    public void onComplete(@NonNull Task<String> task) {
                                        if (task.isSuccessful()) {
                                            // 작업이 성공적으로 완료됐을 때
                                            String snapshotData = task.getResult();
                                            Log.d(TAG, "lss GoogleCloudLoad Success : " + snapshotData);
                                            // snapshotData를 사용하여 필요한 작업을 수행합니다.
                                            infoTextView.setText("Success GoogleCloudLoad : " + snapshotData);
                                        } else {
                                            // 작업이 실패했을 때
                                            Exception e = task.getException();
                                            // 실패 이유를 처리합니다.
                                            Log.d(TAG, "lss GoogleCloudLoad Error e : " + e.toString());
                                            infoTextView.setText("GoogleCloudLoad Error : " + e.toString());
                                        }
                                    }
                                });
                            break;
                case "Purchase 1":
                    Purchase.GetInstance().Connect("", 111);
                    //infoTextView.setText("Google information");
                    break;
                case "Purchase 2":
                    List<String> spinnerItems = getSpinnerItems();
                    for (String item : spinnerItems) {
                        Log.d(TAG, "Purchase 2 Spinner Item: " + item);
                        Purchase.GetInstance().RegisterProduct(item);
                    }
                    Purchase.GetInstance().RefreshProductInfo( 111);
                    break;
                case "Purchase 3":
                    Purchase.GetInstance().BuyProduct(getSelectedProductId(), "", 111);
                    break;
                case "Purchase 4":
                    try {
                        Purchase.GetInstance().Consume(getSelectedProductId(),111);
                    } catch (Exception e)
                    {

                    }
                    break;
                case "Purchase 5":
                    Purchase.GetInstance().RestoreProduct (111);
                    break;
                case "Purchase 6":
                    Purchase.GetInstance().ConsumeAll(111);
                    break;
                case "Adjust 1":
                    break;
                case "Adjust 2":
                    break;
                case "Adjust 3":
                    break;
                case "Adjust 4":
                    break;
                case "Adjust 5":
                    break;
                case "Adjust 6":
                    break;
                case "UMG 1":
                    break;
                case "UMG 2":
                    break;
                case "UMG 3":
                    break;
                case "UMG 4":
                    break;
                case "UMG 5":
                    break;
                case "UMG 6":
                    break;
                default:
                    // Login canceled due to other error
            }
        }

        //        private void handleGoogleButtonClick(String buttonText, TextView infoTextView) {
//            Log.d(TAG, "Google button clicked: " + buttonText);
//        }

        @Override
        public int getItemCount() {
            return titles.size() * 1000; // Infinite scrolling effect
        }

        public TextView getInfoTextView(int position) {
            RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
            if (viewHolder != null) {
                return ((ViewHolder) viewHolder).infoTextView;
            } else {
                return null;
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView;
            TextView infoTextView;
            Spinner spinner;
            Button addButton1, addButton2, addButton3, addButton4, addButton5, addButton6, addButton7; // Add more buttons if needed

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.titleTextView);
                infoTextView = itemView.findViewById(R.id.infoTextView); // Initialize infoTextView
                addButton1 = itemView.findViewById(R.id.addButton1);
                addButton2 = itemView.findViewById(R.id.addButton2);
                addButton3 = itemView.findViewById(R.id.addButton3);
                addButton4 = itemView.findViewById(R.id.addButton4);
                addButton5 = itemView.findViewById(R.id.addButton5);
                addButton6 = itemView.findViewById(R.id.addButton6);
                addButton7 = itemView.findViewById(R.id.addButton7);

                spinner = itemView.findViewById(R.id.spinner); // 스피너 초기화

                if (spinner != null) {
                    // Spinner에 어댑터 설정
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(itemView.getContext(),
                            android.R.layout.simple_spinner_item, getSpinnerItems());
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);

                    // 새로운 코드 추가
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (parent != null) {
                                setSelectedProductId(parent.getItemAtPosition(position).toString());
                                Toast.makeText(itemView.getContext(), parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            // 아무 것도 하지 않음
                        }
                    });
                } else {
                    Log.e("ViewHolder", "Spinner not found");
                }

            }
        }
//        private static List<String> getSpinnerItems() {
//            List<String> items = new ArrayList<>();
//            items.add("cointop2_google_play_gem100");
//            items.add("cointop2_google_play_gem300");
//            items.add("cointop2_google_play_gem500");
//            return items;
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intentData)
    {
        super.onActivityResult(requestCode, resultCode, intentData);
        Log.d( "NTSDK", "@@@@@@@@@@@@  onActivityResult @@@@@@@@@@@@@ " + " resultcode : " + requestCode  );

        if(requestCode == 0XFF)
        {
            LineLoginResult result = LineLoginApi.getLoginResultFromIntent(intentData);
            Log.d( "NTSDK", "@@@@@@@@@@@@" + " result.getResponseCode() : " + result.getResponseCode() );

            Line.Companion.LineOnActivityResult( requestCode, resultCode, intentData );

            switch (result.getResponseCode()) {
            case SUCCESS:
                // Login successful
                String accessToken = result.getLineCredential().getAccessToken().getTokenString();
                Log.d("NTSDK","LINE Login Success accessToken : " + accessToken);
                break;

            case CANCEL:
                // Login canceled by user
                Log.d("NTSDK", "LINE Login Canceled by user. " + result.getErrorData().toString());
                break;

            default:
                // Login canceled due to other error
                Log.e("NTSDK", "Login FAILED! " + result.getErrorData().toString());
            }
        }
        else if ( requestCode == 64206 )
        {
            Log.d("NTSDK", "Facebook Login onActivityResult ");
            Facebook.Companion.getInstance ().onActivityResult(requestCode, resultCode, intentData);
        }
        else if (requestCode == RC_SIGN_IN_GOOGLE_SIGN_IN)
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intentData);
            Google.GetInstance().handleSignInResult(task);
        }
        else if (requestCode == RC_SIGN_IN_GOOGLE_PLAY_SERVICES_SIGN_IN)
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intentData);
            Google.GetInstance().handleGooglePlayServicesSignInResult(task);
        }
        else if (requestCode == RC_SAVED_GAMES)
        {
            Log.d( "NTSDK", "@@@@@@@@@@@@  RC_SAVED_GAMES @@@@@@@@@@@@@ "  );
            if (intentData != null) {
                if (intentData.hasExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA)) {
                    // Load a snapshot.
                    SnapshotMetadata snapshotMetadata =
                            intentData.getParcelableExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA);
                    mCurrentSaveName = snapshotMetadata.getUniqueName();
                    long lTime = snapshotMetadata.getPlayedTime();
                    long lValue = snapshotMetadata.getProgressValue();
                    Log.d( "NTSDK", "lss mCurrentSaveName : " + mCurrentSaveName );
                    Log.d( "NTSDK", "lss lTime : " + lTime );
                    Log.d( "NTSDK", "lss lValue : " + lValue );

                    // Load the game data from the Snapshot
                    // ...
                } else if (intentData.hasExtra(SnapshotsClient.EXTRA_SNAPSHOT_NEW)) {
                    // Create a new snapshot named with a unique string
                    String unique = new BigInteger(281, new Random()).toString(13);
                    mCurrentSaveName = "snapshotTemp-" + unique;

                    // Create the new snapshot
                    // ...
                }
            }
        }

        if (intentData != null) {
            if (intentData.hasExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA)) {
                // Load a snapshot.
                SnapshotMetadata snapshotMetadata =
                        intentData.getParcelableExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA);
                mCurrentSaveName = snapshotMetadata.getUniqueName();
                Log.d( "NTSDK", "@@@@@@@@@@@@  EXTRA_SNAPSHOT_METADATA @@@@@@@@@@@@@ "  );

                // Load the game data from the Snapshot
                // ...
            } else if (intentData.hasExtra(SnapshotsClient.EXTRA_SNAPSHOT_NEW)) {
                // Create a new snapshot named with a unique string
                String unique = new BigInteger(281, new Random()).toString(13);
                mCurrentSaveName = "snapshotTemp-" + unique;
                Log.d( "NTSDK", "@@@@@@@@@@@@  EXTRA_SNAPSHOT_NEW @@@@@@@@@@@@@ "  );

                // Create the new snapshot
                // ...
            }
        }
    }
    private String mCurrentSaveName = "snapshotTemp";

    @Override
    protected void onResume()
    {
        super.onResume();
//        Log.e( "NTSDK", "@@@@@@@@@@@@  onResume @@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        Purchase.GetInstance().OnResume();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    protected void onStop()
    {
 //       Log.e( "NTSDK", "@@@@@@@@@@@@  Start @@@@@@@@@@@@@@@@@@@@@@@@@@@@");
//        new Handler().postDelayed(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                //딜레이 후 시작할 코드 작성
//                testFunc();
//            }
//        }, 600);
//        List<String> skuList = new ArrayList<String>();
//        skuList.add("ct_samsung_apps_gem100");
//        skuList.add("ct_samsung_apps_gem300");
//        skuList.add("ct_samsung_apps_gem500");
//        String[] pidArray = skuList.toArray(new String[skuList.size()]);
//
//        for (String pid : pidArray)
//        {
//            PurchaseGalaxy.RegisterProduct (pid);
//        }
//
//        Log.e( "NTSDK", "@@@@@@@@@@@@  Start 1 @@@@@@@@@@@@@@@@@@@@@@@@@@@@");
//        PurchaseGalaxy.RefreshProductInfo(111);

        if (LGBase.getMainActivity() == null)
            Log.i( "NTSDK", "1 Activity is null.");
        super.onStop();

        if (LGBase.getMainActivity() == null)
            Log.i( "NTSDK", "2 Activity is null.");

        Log.e( "NTSDK", "@@@@@@@@@@@@  onStop @@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        Purchase.GetInstance().OnStop();
        PurchaseGalaxy.GetInstance().OnStop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.e( "NTSDK", "@@@@@@@@@@@@  onDestroy @@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        PurchaseGalaxy.GetInstance().OnDestory();
    }

}
