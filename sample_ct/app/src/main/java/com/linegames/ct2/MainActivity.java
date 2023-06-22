package com.linegames.ct2;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.billingclient.api.SkuDetails;
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
import com.linegames.UMG;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import androidx.core.app.ActivityCompat;

public class MainActivity extends Activity
{
    private static final String TAG = "NTSDK";
    public static String ConsumeProductId1 = "cointop2_google_play_gem300";
    public static String ConsumeProductId1Sub = "cointop2_google_play_gem100_sub";
    public static String ConsumeProductId2 = "cointop2_google_play_gem500";
    public static String ConsumeProductId3 = "cointop2_google_play_gem100000";

    //permission
    private int nCurrentPermission = 0;
    private static final int PERMISSIONS_REQUEST = 0x0000001;
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

        int targetSdkVersion = getApplicationContext().getApplicationInfo().targetSdkVersion;
        Log.d(TAG, "targetSdkVersion : " + targetSdkVersion);

        Button btn1 = (Button) this.findViewById(R.id.button1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "adjustinit");
                //NTAdjust.Companion.Init("kz6rk49tznr4", "", "sandbox",1,665017812,845755228,363896731,1903830666);
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
//                Purchase.GetInstance().Connect("",111);
                PurchaseSub.GetInstance().Connect("",111);
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
                //.add("cointop2_google_play_gem100");
//                skuList.add("cointop2_google_play_gem300");
//                skuList.add("cointop2_google_play_gem500");
//                skuList.add("cointop2_google_play_gem100000");
                skuList.add("cointop2_google_play_gem100_sub");
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

//                Purchase.GetInstance().RefreshProductInfo (111);
                PurchaseSub.GetInstance().RefreshProductInfo (111);
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
                PurchaseSub.GetInstance().BuyProduct(ConsumeProductId1Sub, "cointop2_google_play_gem100Sub.414132124.uwo_kr_server1", 11);
            }
        });

        Button btn20 = (Button) this.findViewById(R.id.button20);
        btn20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "Consume");
                try {
//                    Purchase.GetInstance().Consume(ConsumeProductId1,111);
                    PurchaseSub.GetInstance().Consume(ConsumeProductId1Sub,111);
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
//                Purchase.GetInstance().RestoreProduct(111);
                PurchaseSub.GetInstance().RestoreProduct(111);
           }
        });
        Button btn22 = (Button) this.findViewById(R.id.button22);
        btn22.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "ConsumeAll");
//                Purchase.GetInstance().ConsumeAll(111);
                PurchaseSub.GetInstance().ConsumeAll(111);
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
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "Android Permission 12");
                AndroidPermission.GetInstance().ShowRequestPermission();

                Log.d(TAG, "Android Permission 13");
            }
        });

        Button btn32 = (Button) this.findViewById(R.id.button32);
        btn32.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "PermissionView");

                String[] permissionTitle = new String[]{"Kim","Lee","Park"};
                String[] permissionDesc = new String[]{"Desc1","Desc2","Desc3"};
                String[] permissionType = new String[]{"Type1","Type2","Type3"};
                Log.d(TAG, "PermissionView 2");
                UMG.Companion.ShowApplicationPermission(permissionTitle,permissionDesc,permissionType,111);

            }
        });

        Button btn33 = (Button) this.findViewById(R.id.button33);
        btn33.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "BacktraceCrash1");
 //               NTBacktrace.Companion.BacktraceCrash1();
                //write
                SharedPreferences sharedPref = NTBase.getMainActivity ().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("aoskeyt","testdatat");
                editor.apply();
            }
        });

        Button btn34 = (Button) this.findViewById(R.id.button34);
        btn34.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "BacktraceCrash2");
 //               NTBacktrace.Companion.BacktraceCrash2();
                //read
                SharedPreferences sharedPref = NTBase.getMainActivity ().getPreferences(Context.MODE_PRIVATE);
                String LoadData = sharedPref.getString("aoskeyt", "");
                Log.d(TAG, "LoadData : " + LoadData);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d( "NTSDK", "@@@@@@@@@@@@  onActivityResult @@@@@@@@@@@@@ " + " resultcode : " + requestCode  );

        if(requestCode == 0XFF)
        {
            LineLoginResult result = LineLoginApi.getLoginResultFromIntent(data);
            Log.d( "NTSDK", "@@@@@@@@@@@@" + " result.getResponseCode() : " + result.getResponseCode() );

            Line.Companion.LineOnActivityResult( requestCode, resultCode, data );

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
        } else if ( requestCode == 64206 )
        {
            Log.d("NTSDK", "Facebook Login onActivityResult ");
            Facebook.Companion.getInstance ().onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.e( "NTSDK", "@@@@@@@@@@@@  onResume @@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        Purchase.GetInstance().OnResume();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    protected void onStop()
    {
        Log.e( "NTSDK", "@@@@@@@@@@@@  Start @@@@@@@@@@@@@@@@@@@@@@@@@@@@");
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
