package com.linegames.ct2;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.linegames.NotiFCMService;
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

import android.app.AlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity
{
    private static ViewPager2 viewPager;
    private static MyPagerAdapter pagerAdapter;
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

    private static String email_receipt = "email_receipt";
    private static String email_signature = "email_signature";

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

        Log.d(TAG, "lss stringFromJNI : " + stringFromJNI());

        LGBase.getInstance().onCreate( this );

        Google.GetInstance().StartGoogleSign();

        NotiFCMService.Companion.start("icon","1:764920947053:android:3a48ce8f849f332794ba64");

        int targetSdkVersion = getApplicationContext().getApplicationInfo().targetSdkVersion;
        Log.d(TAG, "lss targetSdkVersion : " + targetSdkVersion);

        titles = new ArrayList<>();
        titles.add(UserAction.AndroidFunction.GOOGLE.name());
        titles.add(UserAction.AndroidFunction.PURCHASE.name());
        titles.add(UserAction.AndroidFunction.NOTIFICATION.name());
//        titles.add("Adjust");
//        titles.add("UMG");
//        titles.add("ETC1_");
//        titles.add("ETC2_");

        viewPager = findViewById(R.id.viewPager);
        if (viewPager == null)
        {
            LGLog.d("lss viewPager is null");
            return;
        }

        pagerAdapter = new MyPagerAdapter(titles, viewPager);
        if (pagerAdapter == null)
        {
            LGLog.d("lss pagerAdapter is null");
            return;
        }
        viewPager.setAdapter(pagerAdapter);

        viewPager.setCurrentItem(titles.size() * 100, true);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                //LGLog.d("lss onPageScrollStateChanged : " + state);
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

    // InfoUpdateListener 인터페이스 정의
    public interface InfoUpdateListener {
        void onUpdateInfoText(String infoText);
    }

    // onUpdateInfoText 메서드 구현
    private InfoUpdateListener infoUpdateListener = new InfoUpdateListener() {
        @Override
        public void onUpdateInfoText(String infoText) {
            // 이곳에서 infoTextView의 텍스트를 업데이트합니다.
            // 예: infoTextView.setText(infoText);
        }
    };

    public static void UpdateInfoText(String status, String sInfoData)
    {
        //LGLog.d("lss UpdateInfoText titles : " + pagerAdapter.titles);
        try {
            JSONObject receiptObj = new JSONObject(sInfoData);

            if (receiptObj.has("products"))
                email_receipt = receiptObj.getString("products");

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        infoTextView = pagerAdapter.getInfoTextView( pagerAdapter.viewPager.getCurrentItem());
        if (infoTextView != null) {
            infoTextView.setText(status + "" + sInfoData);
        } else {
            LGLog.d("lss pagerAdapter: " + (pagerAdapter != null ? "Not null" : "Null"));
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

        public MyPagerAdapter(List<String> titles, ViewPager2 viewPager) {
            this.titles = titles;
            this.viewPager = viewPager;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // 기타 페이지인 경우 기존의 item_page.xml을 사용
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_page, parent, false);
            //Log.d(TAG, "lss onCreateViewHolder Etc");
            return new ViewHolder(view);
        }

        // onBindViewHolder 메서드 수정
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            int virtualPosition = position % titles.size();
            String title = titles.get(virtualPosition);
            holder.titleTextView.setText(title);

//            // Set up spinner for Purchase page only
//            if (title.equals("Purchase")) {
//                holder.spinner.setVisibility(View.VISIBLE);
//            } else {
//                holder.spinner.setVisibility(View.GONE);
//            }

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
                case "Noti":
                    holder.infoTextView.setText("Notification information");
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
                int tempi = i;
                String buttonText = UserAction.getActionByIndex(title, tempi + 1); // Generate button text based on page title and button index
                //LGLog.d("", "lss buttonText : " + buttonText);
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
                            String actionText = UserAction.getActionByIndex(title, tempi + 1); // Generate button text based on page title and button index
                            //LGLog.d("", "lss onClick actionText : " + actionText);

                            UserAction.Action action = UserAction.fromString(title + " " + (tempi + 1));

                            if (action != null) {
                                switch (action) {
                                    case GOOGLE_SIGN:
                                        Google.GetInstance().GoogleSign();
                                        holder.infoTextView.setText("GoogleSign");
                                        break;
                                    case GOOGLE_SIGN_OUT:
                                        Google.GetInstance().GoogleSignOut();
                                        holder.infoTextView.setText("GoogleSignOut");
                                        break;
                                    case GOOGLE_PLAY_SERVICE_SIGN:
                                        Google.GetInstance().GooglePlayServiceSign();
//                                            holder.infoTextView.setText(resultStr);
                                        break;
                                    case GOOGLE_CLOUD_LIST:
  //                                      Google.GetInstance().signInSilently();
                                        holder.infoTextView.setText("GoogleCloud List");
                                        break;
//                                    case GOOGLE_CLOUD_SAVE:
//                                        Google.GetInstance().writeSnapshot(v.getContext().getString(R.string.SaveLoadName), v.getContext().getString(R.string.SaveData), v.getContext().getString(R.string.Desc))
//                                                .addOnCompleteListener(new OnCompleteListener<SnapshotMetadata>() {
//                                                    @Override
//                                                    public void onComplete(@NonNull Task<SnapshotMetadata> task) {
//                                                        if (task.isSuccessful()) {
//                                                            LGLog.d("", "lss GoogleCloudSave Success : " + task.toString());
//                                                            holder.infoTextView.setText("Success GoogleCloudSave");
//                                                        } else {
//                                                            Exception e = task.getException();
//                                                            LGLog.d("", "lss GoogleCloudSave Error : " + e.toString());
//                                                            holder.infoTextView.setText(" GoogleCloudSave Error : " + e.toString());
//                                                        }
//                                                    }
//                                                });
//                                        break;
//                                    case GOOGLE_CLOUD_LOAD:
//                                        Google.GetInstance().loadSnapshot(v.getContext().getString(R.string.SaveLoadName)).addOnCompleteListener(new OnCompleteListener<String>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<String> task) {
//                                                if (task.isSuccessful()) {
//                                                    String snapshotData = task.getResult();
//                                                    Log.d(TAG, "lss GoogleCloudLoad Success : " + snapshotData);
//                                                    holder.infoTextView.setText("Success GoogleCloudLoad : " + snapshotData);
//                                                } else {
//                                                    Exception e = task.getException();
//                                                    Log.d(TAG, "lss GoogleCloudLoad Error e : " + e.toString());
//                                                    holder.infoTextView.setText("GoogleCloudLoad Error : " + e.toString());
//                                                }
//                                            }
//                                        });
//                                        break;
                                    case PURCHASE_CONNECT:
                                        Purchase.GetInstance().Connect("", 111);
                                        break;
                                    case PURCHASE_REFRESHPRODUCTINFO:
                                        List<String> spinnerItems = getSpinnerItems();
                                        for (String item : spinnerItems) {
                                            Log.d(TAG, "Purchase 2 Spinner Item: " + item);
                                            Purchase.GetInstance().RegisterProduct(item);
                                        }
                                        Purchase.GetInstance().RefreshProductInfo(111);
                                        break;
                                    case PURCHASE_BUYPURCHASE:
                                        Purchase.GetInstance().BuyProduct(getSelectedProductId(), "", 111);
                                        break;
                                    case PURCHASE_CONSUME:
                                        try {
                                            Purchase.GetInstance().Consume(getSelectedProductId(), 111);
                                        } catch (Exception e) {
                                            // Handle exception
                                        }
                                        break;
                                    case PURCHASE_RESTORE:
                                        Purchase.GetInstance().RestoreProduct(111);
                                        break;
                                    case PURCHASE_CONSUMEALL:
                                        Purchase.GetInstance().ConsumeAll(111);
                                        break;
                                    case PURCHASE_SENDEMAILRECEIPTINFO:
                                        sendEmailToAdmin();
                                        break;
                                    case FCM_NOTI_REGISTE:
                                        NotiFCMService.Companion.start("","1:764920947053:android:3a48ce8f849f332794ba64");
                                        break;
                                    case FCM_NOTI_SHOW:
                                        break;
                                    case FCM_NOTI_SETTIMER:
                                        break;
                                    case FCM_NOTI_LOCAL:
                                        break;
                                    case ADJUST_EVENT1:
                                        // Handle ADJUST_EVENT1
                                        break;
                                    case ADJUST_EVENT2:
                                        // Handle ADJUST_EVENT2
                                        break;
                                    case UMG_EVENT1:
                                        // Handle UMG_EVENT1
                                        break;
                                    case UMG_EVENT2:
                                        // Handle UMG_EVENT2
                                        break;
                                    case ETC1_EVENT1:
                                        // Handle ETC1_EVENT1
                                        break;
                                    case ETC1_EVENT2:
                                        // Handle ETC1_EVENT2
                                        break;
                                    case ETC2_EVENT1:
                                        // Handle ETC2_EVENT1
                                        break;
                                    case ETC2_EVENT2:
                                        // Handle ETC2_EVENT2
                                        break;
                                    default:
                                        // Handle other actions
                                        Log.d(TAG, "Action :  " + action.toString());
                                        break;
                                }
                            }
                            else {
                                Log.d(TAG, "Action is null ");
                            }
                        }
                    });
                }
            }
        }


//        private UserAction.Action generateButtonText(String title, int index) {
//            // Generate button text based on page title and button index
//            return UserAction.getActionByIndex(title, index);
//            //return title + " " + index;
//        }

        private void generateButtonText(String title, int index) {
            String buttonText = title + " " + index;
            UserAction.Action action = UserAction.fromString(buttonText);

            if (action != null) {
                switch (action) {
                    case GOOGLE_SIGN:
                        Google.GetInstance().GoogleSign();
                        infoTextView.setText("GoogleSign");
                        break;
                    case GOOGLE_SIGN_OUT:
                        Google.GetInstance().GoogleSignOut();
                        infoTextView.setText("GoogleSignOut");
                        break;
                    case GOOGLE_PLAY_SERVICE_SIGN:
                        // 람다 표현식으로 GooglePlayServiceSign 사용
                        Google.GetInstance().GooglePlayServiceSign();
                        break;
                    case GOOGLE_CLOUD_LIST:
//                        Google.GetInstance().signInSilently();
//                        infoTextView.setText("GoogleCloud List");
                        break;
                    case GOOGLE_CLOUD_SAVE:
//                        Google.GetInstance().writeSnapshot(getString(R.string.SaveLoadName), getString(R.string.SaveData), getString(R.string.Desc))
//                                .addOnCompleteListener(new OnCompleteListener<SnapshotMetadata>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<SnapshotMetadata> task) {
//                                        if (task.isSuccessful()) {
//                                            // 게임 콘텐츠가 성공적으로 저장되었을 때의 처리
//                                            // 예를 들어, 사용자에게 저장 완료 메시지를 표시하거나 UI를 업데이트할 수 있습니다.
//                                            LGLog.d("", "lss GoogleCloudSave Success : " + task.toString());
//                                            infoTextView.setText("Success GoogleCloudSave");
//                                        } else {
//                                            // 저장이 실패한 경우 처리
//                                            Exception e = task.getException();
//                                            LGLog.d("", "lss GoogleCloudSave Error : " + e.toString());
//                                            // 에러 메시지를 표시하거나 적절한 오류 처리를 수행합니다.
//                                            infoTextView.setText(" GoogleCloudSave Error : " + e.toString());
//                                        }
//                                    }
//                                });
                        break;
                    case GOOGLE_CLOUD_LOAD:
//                        Google.GetInstance().loadSnapshot(getString(R.string.SaveLoadName)).addOnCompleteListener(new OnCompleteListener<String>() {
//                            @Override
//                            public void onComplete(@NonNull Task<String> task) {
//                                if (task.isSuccessful()) {
//                                    // 작업이 성공적으로 완료됐을 때
//                                    String snapshotData = task.getResult();
//                                    Log.d(TAG, "lss GoogleCloudLoad Success : " + snapshotData);
//                                    // snapshotData를 사용하여 필요한 작업을 수행합니다.
//                                    infoTextView.setText("Success GoogleCloudLoad : " + snapshotData);
//                                } else {
//                                    // 작업이 실패했을 때
//                                    Exception e = task.getException();
//                                    // 실패 이유를 처리합니다.
//                                    Log.d(TAG, "lss GoogleCloudLoad Error e : " + e.toString());
//                                    infoTextView.setText("GoogleCloudLoad Error : " + e.toString());
//                                }
//                            }
//                        });
                        break;
                    case PURCHASE_CONNECT:
                        Purchase.GetInstance().Connect("", 111);
                        //infoTextView.setText("Google information");
                        break;
                    case PURCHASE_REFRESHPRODUCTINFO:
                        List<String> spinnerItems = getSpinnerItems();
                        for (String item : spinnerItems) {
                            Log.d(TAG, "Purchase 2 Spinner Item: " + item);
                            Purchase.GetInstance().RegisterProduct(item);
                        }
                        Purchase.GetInstance().RefreshProductInfo(111);
                        break;
                    case PURCHASE_BUYPURCHASE:
                        Purchase.GetInstance().BuyProduct(getSelectedProductId(), "", 111);
                        break;
                    case PURCHASE_CONSUME:
                        try {
                            Purchase.GetInstance().Consume(getSelectedProductId(), 111);
                        } catch (Exception e) {

                        }
                        break;
                    case PURCHASE_RESTORE:
                        Purchase.GetInstance().RestoreProduct(111);
                        break;
                    case PURCHASE_CONSUMEALL:
                        Purchase.GetInstance().ConsumeAll(111);
                        break;
                    case FCM_NOTI_REGISTE:
                        break;
                    case FCM_NOTI_SHOW:
                        break;
                    case FCM_NOTI_SETTIMER:
                        break;
                    case FCM_NOTI_LOCAL:
                        break;
                    case ADJUST_EVENT1:
                        break;
                    case ADJUST_EVENT2:
                        break;
//                case "UMG 1":
//                    break;
//                case "UMG 2":
//                    break;
                    default:
                        // Login canceled due to other error
                }
            } else {
                Log.d(TAG, "action is null  " + action);
            }
        }


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
    }

    private List<String> emailList = new ArrayList<>();
    public void sendEmailToAdmin()
    {
        String title = "Android Test Receipt";
//        String[] receivers = new String[]{"god0@line.games"};
        String[] receivers = null;
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_SUBJECT, title);
        email.putExtra(Intent.EXTRA_EMAIL, receivers);
        email.putExtra(Intent.EXTRA_TEXT, String.format("receipt : %s ", email_receipt));
        email.setType("message/rfc822");
        LGBase.getMainActivity().startActivity(email);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intentData)
    {
        super.onActivityResult(requestCode, resultCode, intentData);
        Log.d( "NTSDK", "lss @@@@@@@@@@@@  onActivityResult @@@@@@@@@@@@@ " + " resultCode : " + resultCode  );

        if(requestCode == 0XFF)
        {
            LineLoginResult result = LineLoginApi.getLoginResultFromIntent(intentData);
            Log.d( "NTSDK", "lss @@@@@@@@@@@@" + " result.getResponseCode() : " + result.getResponseCode() );

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
            Log.d("NTSDK", "lss Google Login onActivityResult ");
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intentData);
            Google.GetInstance().handleSignInResult(task);
        }
        else if (requestCode == RC_SIGN_IN_GOOGLE_PLAY_SERVICES_SIGN_IN)
        {
            Log.d("NTSDK", "lss GooglePlay Login onActivityResult ");
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
