package com.linegames.ct2;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.billingclient.api.SkuDetails;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.SnapshotsClient;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.linecorp.linesdk.auth.LineLoginApi;
import com.linecorp.linesdk.auth.LineLoginResult;
import com.linegames.AndroidPermission;
import com.linegames.DeviceInfo;
import com.linegames.Line;
import com.linegames.NTAdjust;
import com.linegames.Purchase;
import com.linegames.PurchaseSub;
import com.linegames.PurchaseGalaxy;
import com.linegames.auth.Facebook;
import com.linegames.base.NTBase;
import com.linegames.base.NTLog;
import com.linegames.DFPurchase;
import com.linegames.Google;

import org.json.JSONArray;
import org.json.JSONException;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

public class MainActivity extends Activity
{
    private static final String TAG = "NTSDK";
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
    //permission

    private static String GalaxyPid = "ct_samsung_apps_gem100"; //ct_samsung_apps_gem300 ct_samsung_apps_gem500

    static {
        System.loadLibrary("native-lib");
    }
    public native String stringFromJNI();
    //public native String LoginReciever(int status, String jsAccessToken, String jsMsg);
    //@JvmStatic external fun LoginReciever( status : Int, jsFBID : String, jsAccessToken : String, jsMsg : String )
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView( R.layout.activity_main );

        Log.d(TAG, "stringFromJNI : " + stringFromJNI());

        NTBase.getInstance().onCreate( this );

//        Google.GetInstance().StartGoogleSign();

        int targetSdkVersion = getApplicationContext().getApplicationInfo().targetSdkVersion;
        Log.d(TAG, "targetSdkVersion : " + targetSdkVersion);

        Button btn1 = (Button) this.findViewById(R.id.button1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "adjustinit");
                NTAdjust.Companion.Init("kz6rk49tznr4", "", "sandbox",1,665017812,845755228,363896731,1903830666);
               // testFunc();
            }
        });

        int orderIdData = 0;
        Button btn2 = (Button) this.findViewById(R.id.button2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "adjustevent1");
                String jToken1 = "xm779b";
                String jNull = "";    //1593404419905006435
                String jNid = "ct_nid";
                String jGnid = "ct_gnid";
                String jGameServerID = "CT-KR-01";
                String jOrderId = jNull;
                String jCurrencyType = jNull;
                int amount = 0;
                NTAdjust.Companion.TrackEvent(jToken1, jNid, jGnid, jGameServerID, jOrderId, jCurrencyType, amount);
            }
        });

        Button btn3 = (Button) this.findViewById(R.id.button3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "adjustevent2");
                String jToken2 = "27n0xy";
                String jNull = "";    //1593404419905006435
                String jNid = jNull;
                String jGnid = jNull;
                String jGameServerID = "CT-KR-01";
                String jOrderId = "orderId2";
                String jCurrencyType = "JPY";
                int amount = 1000;
                NTAdjust.Companion.TrackEvent(jToken2, jNid, jGnid, jGameServerID, jOrderId, jCurrencyType, amount);
            }
        });

        Button btn4 = (Button) this.findViewById(R.id.button4);
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "AdjustAdid");
                NTAdjust.Companion.GetAdjustAdid();
            }
        });
        Button btn5 = (Button) this.findViewById(R.id.button5);
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "GPS_Adid");
                NTAdjust.Companion.GetAdid();
                DeviceInfo.Companion.AOSDeviceInfo ();
            }
        });

        Button btn6 = (Button) this.findViewById(R.id.button6);
        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "btn6 facebook init");
            }
        });
        Button btn7 = (Button) this.findViewById(R.id.button7);
        btn7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "btn7 facebook login");
                String[] s_array = {"public_profile","email"};
                Facebook.Companion.getInstance().Login(s_array);

            }
        });
        Button btn8 = (Button) this.findViewById(R.id.button8);
        btn8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "btn8");
                Log.d(TAG, "btn7 facebook logout");
                Facebook.Companion.getInstance().Logout();
              //  Purchase.Companion.getInstance().AdjustInit("gxi7w15gal8g","sandbox",0,0,0,0,0);

                //                NTFacebook.Companion.getGetInstance().GetMe();
//                Purchase.Companion.getInstance().AdjustTrackEvent("xm779b","11111111","222222222","ASIA","","",0);
//                Purchase.Companion.getInstance().AdjustTrackEvent("27n0xy","11111111","222222222","ASIA","","",0);

            }
        });
        Button btn9 = (Button) this.findViewById(R.id.button9);
        btn9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "btn9");
//                NTFacebook.Companion.getGetInstance().GetFriendList();
              //  Purchase.Companion.getInstance().AdjustTrackEvent("jzygrh","11111111","222222222","ASIA","","",0);
            }
        });
        Button btn10 = (Button) this.findViewById(R.id.button10);
        btn10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
            Log.d(TAG, "btn10");
            }
        });
        Button btn11 = (Button) this.findViewById(R.id.button11);
        btn11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "Init");
                Line.Companion.Init(111);
            }
        });
        Button btn12 = (Button) this.findViewById(R.id.button12);
        btn12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "Login");
                //cointop 1653386472
                //samples 1636568237
                Line.Companion.Login("1653386472", 111);
//                Line.Companion.Login("1653386472", 111);
            }
        });
        Button btn13 = (Button) this.findViewById(R.id.button13);
        btn13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "Logout");
                Line.Companion.Logout();
            }
        });
        Button btn14 = (Button) this.findViewById(R.id.button14);
        btn14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "Profile");
                Line.Companion.Profile(111);
            }
        });
        Button btn15 = (Button) this.findViewById(R.id.button15);
        btn15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "Verify");
                Line.Companion.Verify(111);
            }
        });
        Button btn16 = (Button) this.findViewById(R.id.button16);
        btn16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "Refresh");
                Line.Companion.Refresh(111);
            }
        });
        Button btn17 = (Button) this.findViewById(R.id.button17);
        btn17.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "Connect");
                Purchase.GetInstance().Connect("",111);
//                PurchaseSub.GetInstance().Connect("",111);
            }
        });
        Button btn18 = (Button) this.findViewById(R.id.button18);
        btn18.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "GetProductList");

                List<String> skuList = new ArrayList<String>();
                //skuList.add("ct_floor_store_gem500");
                     //        cointop2_google_play_gem300
                skuList.add("cointop2_google_play_gem100");
                skuList.add("cointop2_google_play_gem300");
                skuList.add("cointop2_google_play_gem500");
//                skuList.add("cointop2_google_play_gem100000");
//                skuList.add("cointop2_google_play_gem100_sub");
//                skuList.add("cointop2_google_play_gem10000044");
//                for (int i = 0; i < 10; i++)
//                {
//                    String pid = "cointop_google_play_gem10" + i;
//                    skuList.add(pid);
//                }
//                for (int i = 0; i < 10; i++)
//                {
//                    String pid = "cointop_google_play_gem30" + i;
//                    skuList.add(pid);
//                }
//                for (int i = 0; i < 3; i++)
//                {
//                    String pid = "cointop_google_play_gem501" + i;
//                    skuList.add(pid);
//                }
                String[] pidArray = skuList.toArray(new String[skuList.size()]);

                for (String pid : pidArray)
                {
                    Purchase.GetInstance().RegisterProduct (pid);
                }

                Purchase.GetInstance().RefreshProductInfo (111);
//                PurchaseSub.GetInstance().RefreshProductInfo (111);
            }
        });
        Button btn19 = (Button) this.findViewById(R.id.button19);
        btn19.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "BuyProduct");
//                String payloadTest = "abcdefghijklmnopqrstuvwxyz_abcdefghijklmnopqrstuvwxyz_abcdefghij_";
                Purchase.GetInstance().BuyProduct(ConsumeProductId1, "cointop2_google_play_gem100.414132124.uwo_kr_server1", 11);
            }
        });

        Button btn35 = (Button) this.findViewById(R.id.button35);
        btn35.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "BuyProductSub");
//                String payloadTest = "abcdefghijklmnopqrstuvwxyz_abcdefghijklmnopqrstuvwxyz_abcdefghij_";
                try {
                    Purchase.GetInstance().sendEmailToAdmin();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Button btn20 = (Button) this.findViewById(R.id.button20);
        btn20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "Consume");
                try {
                    Purchase.GetInstance().Consume(ConsumeProductId1,111);
//                    PurchaseSub.GetInstance().Consume(ConsumeProductId1Sub,111);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //                Purchase.GetInstance().Consume("cointop_google_play_gem100",111);
            }
        });
        Button btn21 = (Button) this.findViewById(R.id.button21);
        btn21.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "ProductRestore");
                Purchase.GetInstance().RestoreProduct(111);
//                PurchaseSub.GetInstance().RestoreProduct(111);
           }
        });
        Button btn22 = (Button) this.findViewById(R.id.button22);
        btn22.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "ConsumeAll");
                Purchase.GetInstance().ConsumeAll(111);
//                PurchaseSub.GetInstance().ConsumeAll(111);
//                Purchase.GetInstance().IsExistUnconsumedList();
            }
        });
        Button btn23 = (Button) this.findViewById(R.id.button23);
        btn23.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
//                Log.d(TAG, "Galaxy Connect");
            }
        });
        Button btn24 = (Button) this.findViewById(R.id.button24);
        btn24.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "Galaxy Connect");
                String productMode = "TEST";   //PRODUCT     FAILURE
                PurchaseGalaxy.Connect(productMode, 111);
            }
        });
        Button btn25 = (Button) this.findViewById(R.id.button25);
        btn25.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "Galaxy GetRefreshProduct");

                List<String> skuList = new ArrayList<String>();
                skuList.add("ct_samsung_apps_gem100");
                skuList.add("ct_samsung_apps_gem300");
                skuList.add("ct_samsung_apps_gem500");
                String[] pidArray = skuList.toArray(new String[skuList.size()]);

                for (String pid : pidArray)
                {
                    PurchaseGalaxy.RegisterProduct (pid);
                }

                PurchaseGalaxy.RefreshProductInfo(111);
            }
        });
        Button btn26 = (Button) this.findViewById(R.id.button26);
        btn26.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "Galaxy BuyProduct");
                StringBuilder orderId = new StringBuilder();
                orderId.append(GalaxyPid);
                orderId.append(".");
                orderId.append(GalaxyPid);

                PurchaseGalaxy.BuyProduct (GalaxyPid, orderId.toString(), 111);
            }
        });
        Button btn27 = (Button) this.findViewById(R.id.button27);
        btn27.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "Galaxy Consume");
                try {
                    PurchaseGalaxy.Consume(GalaxyPid,111);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Button btn28 = (Button) this.findViewById(R.id.button28);
        btn28.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "Galaxy ConsumeAll");
                try {
                    PurchaseGalaxy.ConsumeAll(111);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Button btn29 = (Button) this.findViewById(R.id.button29);
        btn29.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "Galaxy RestoreProduct");
                PurchaseGalaxy.RestoreProduct(111);
            }
        });
        Button btn30 = (Button) this.findViewById(R.id.button30);
        btn30.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "Galaxy 00");
                //PurchaseGalaxy.GetInstance().GetPurchaseHistory();
            }
        });

        Button btn31 = (Button) this.findViewById(R.id.button31);
        btn31.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.S)
            @Override
            public void onClick(View view)
            {
                int targetSdkVersion = getApplicationContext().getApplicationInfo().targetSdkVersion;
                int minSdkVersion = getApplicationContext().getApplicationInfo().minSdkVersion;
                Log.d(TAG, "Android targetSdkVersion : " + targetSdkVersion);
                Log.d(TAG, "Android minSdkVersion : " + minSdkVersion);

                AndroidPermission.GetInstance().ShowRequestPermission();

                Log.d(TAG, "AndroidPermission.GetInstance().ShowRequestPermission()");
            }
        });

        Button btn32 = (Button) this.findViewById(R.id.button32);
        btn32.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "GoogleSign");
                Google.GetInstance().GooglePlayServiceSign();
                Log.d(TAG, "Google.GetInstance().GooglePlayServiceSign()========================");
            }
        });

        Button btn33 = (Button) this.findViewById(R.id.button33);
        btn33.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "Google.GetInstance().signInSilently()========================");
                Google.GetInstance().signInSilently();
            }
        });

        String SAVE_GAMENAME_1 = "SaveGame1";
        String SAVE_GAMENAME_2 = "SaveGame2";
        Button btn34 = (Button) this.findViewById(R.id.button34);
        btn34.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "Google.GetInstance().writeSnapshot()========================");

                String gameData = "Your game data string"; // 저장할 게임 데이터
                Google.GetInstance().writeSnapshot(SAVE_GAMENAME_1, gameData, SAVE_GAMENAME_1)
                        .addOnCompleteListener(new OnCompleteListener<SnapshotMetadata>() {
                            @Override
                            public void onComplete(@NonNull Task<SnapshotMetadata> task) {
                                if (task.isSuccessful()) {
                                    // 게임 콘텐츠가 성공적으로 저장되었을 때의 처리
                                    // 예를 들어, 사용자에게 저장 완료 메시지를 표시하거나 UI를 업데이트할 수 있습니다.
                                    NTLog.d("","lss Google.GetInstance().writeSnapshot Success : " + task.toString());
                                } else {
                                    // 저장이 실패한 경우 처리
                                    Exception e = task.getException();
                                    NTLog.d("","lss Google.GetInstance().writeSnapshot Error : " + e.toString());
                                    // 에러 메시지를 표시하거나 적절한 오류 처리를 수행합니다.
                                }
                            }
                        });

            }
        });

        Button btn36 = (Button) this.findViewById(R.id.button36);
        btn36.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "Google.GetInstance().loadSnapshot()========================");
                Google.GetInstance().loadSnapshot(SAVE_GAMENAME_1).addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful()) {
                            // 작업이 성공적으로 완료됐을 때
                            String snapshotData = task.getResult();
                            Log.d(TAG, "lss loadSnapshot : " + snapshotData);
                            // snapshotData를 사용하여 필요한 작업을 수행합니다.
                        } else {
                            // 작업이 실패했을 때
                            Exception e = task.getException();
                            // 실패 이유를 처리합니다.
                            Log.d(TAG, "lss loadSnapshot e : " + e.toString());
                        }
                    }
                });
            }
        });

        Button btn37 = (Button) this.findViewById(R.id.button37);
        btn37.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "DF_Connect");
                DFPurchase.Connect();
            }
        });

        Button btn38 = (Button) this.findViewById(R.id.button38);
        btn38.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "DF_RefreshProduct");
//                DFPurchase.registerStoreProductID("df_for_kakao_google_play_22_03");
//                DFPurchase.registerStoreProductID("df_for_kakao_google_play_20_14");
//                DFPurchase.registerStoreProductID("cointop2_google_play_gem300");
//                DFPurchase.registerStoreProductID("cointop2_google_play_gem500");
//                DFPurchase.refreshStoreProductInfo();
            }
        });

        Button btn39 = (Button) this.findViewById(R.id.button39);
        btn39.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "DF_Purchase");
                DFPurchase.requestPurchaseToGooglePlay("cointop2_google_play_gem300");
            }
        });

        Button btn40 = (Button) this.findViewById(R.id.button40);
        btn40.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "DF_Consume");
                DFPurchase.IAP_RemoveReceipt();
            }
        });

        Button btn41 = (Button) this.findViewById(R.id.button41);
        btn41.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "DF_Test1");
                DFPurchase.LoadRestoreInfo();
            }
        });

        Button btn42 = (Button) this.findViewById(R.id.button42);
        btn42.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "DF_Test2");
                try {
                    Log.d(TAG, "IAP_GetSignature : " + DFPurchase.IAP_GetProductName());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "IAP_GetSignature : " + DFPurchase.IAP_GetSignature());
                Log.d(TAG, "IAP_GetPurchaseData : " + DFPurchase.IAP_GetPurchaseData());
                Log.d(TAG, "IAP_IsExistReceipt : " + DFPurchase.IAP_IsExistReceipt());
            }
        });

    }

    public void testFunc()
    {
        for (int j = 20; j < 100; j++) {
            ArrayList<String> productIdArr = new ArrayList<>();
            String jProductID = "";
            int countNum = j;
            for (int i = 0; i < countNum; i++) {
                jProductID = "pId" + i;
                productIdArr.add(jProductID);
            }

            final int inputProductsCount = productIdArr.size();
            final int defaultCount = 20;

            NTLog.d("productIdArr.toString() : " + productIdArr.toString());
            NTLog.d("productIdArr.size() : " + productIdArr.size());

            final JSONArray productsJsonArray = new JSONArray();
            final ArrayList<String> invalidProductsId = productIdArr;
            final int[] queryCompleteCounter = new int[] { 0 };
            final int loopCnt = inputProductsCount / defaultCount;

            for(int i = 0 ; i < loopCnt + 1; i++)
            {
                int fromIndex = i * defaultCount;
                int toIndex = Math.min(inputProductsCount, ( i + 1 ) * defaultCount);

               if (toIndex > productIdArr.size())
                {
                    NTLog.d(" =========================================================================== ");
                    NTLog.d(" j : " + j + " toIndex : " + toIndex  + " productIdArr.size() : " + productIdArr.size() );
                    NTLog.d(" =========================================================================== ");
                    //continue;
                }

                List<String> productsInfo = productIdArr.subList(i * defaultCount, toIndex);
                NTLog.d(" fromIndex : " + fromIndex + " toIndex : " + toIndex );
                NTLog.d(" j : " + j + " i : " + i + " loopCnt : " + loopCnt +  " productsInfo.size() : " + productsInfo.size() );
                NTLog.d(" productsInfo.toString() : " + productsInfo.toString());
            }

        }
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

        if (NTBase.getMainActivity() == null)
            Log.i( "NTSDK", "1 Activity is null.");
        super.onStop();

        if (NTBase.getMainActivity() == null)
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
        Purchase.GetInstance().OnDestory();
        PurchaseGalaxy.GetInstance().OnDestory();
    }

}
