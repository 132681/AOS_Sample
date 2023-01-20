package com.linegames;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.pm.PackageManager;
import android.os.Debug;
import android.util.Log;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.linegames.base.NTBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class AndroidPermission
{
    private static String LOG_TAG = "NTSDK";
    private static String PURCHASE_TAG = "AndroidPermissionAPI";

    private static String result_msg= "Connect Success.";
    private static String result_status = "NT_SUCCESS";
    private static int result_ResponseCode = 0;
    private static long purchaseCB = 0L;
    private static long noCB = 777;

    private static Activity mMainActivity;

    private static String[] permissions = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private List permissionList;

    private static AndroidPermission getInstance = null;
    public native void nativeCB( String status, String msg, long userCB );

    int nCurrentPermission = 0;
    static final int PERMISSIONS_REQUEST = 0x0000001;

    public static synchronized AndroidPermission GetInstance() {
        if (getInstance == null) {
            synchronized ( AndroidPermission.class ) {
                if ( getInstance == null )
                    getInstance = new AndroidPermission();
            }
            mMainActivity = NTBase.getMainActivity();
        }
        return getInstance;
    }

    public static void ShowPermission()
    {
        GetInstance().OnCheckPermission();
    }

    public boolean checkPermission()
    {
        permissionList = new ArrayList();
        int result;
        for (String pm : permissions)
        {
            result = ContextCompat.checkSelfPermission(mMainActivity, pm);
            if (result != PackageManager.PERMISSION_GRANTED){
                permissionList.add(pm);
            }
        }

        if (!permissionList.isEmpty()){
            return false;
        }
        return true;
    }

    public void ShowRequestPermission()
    {
        if (permissions == null)
        {
            Debug("permissions is null");

        }
        else
        {
            ActivityCompat.requestPermissions(mMainActivity, permissions , PERMISSIONS_REQUEST);

        }
//        String[] permissionArr = permissionList.toArray(new String[permissionList.size()]);
    }

    public void OnCheckPermission() {

            if ( ContextCompat.checkSelfPermission(mMainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED )
            {
                ActivityCompat.requestPermissions(mMainActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST);
                Debug( "AndroidPermission ShowPermission() userCB : request" );
            }
            else if (ActivityCompat.shouldShowRequestPermissionRationale(mMainActivity,Manifest.permission.ACCESS_FINE_LOCATION)){

                Debug( "AndroidPermission ShowPermission() userCB : shouldShowRequestPermissionRationale" );
            }
            else
            {
                Debug( "AndroidPermission ShowPermission() userCB : GRANTED" );
//                ActivityCompat.requestPermissions(mMainActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST);
            }
//        if (ContextCompat.checkSelfPermission(mMainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//
//                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//            if (ContextCompat.shouldShowRequestPermissionRationale(mMainActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
//
//                Toast.makeText(mMainActivity, "앱 실행을 위해서는 권한을 설정해야 합니다", Toast.LENGTH_LONG).show();
//                ContextCompat.requestPermissions(mMainActivity,
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST);
//
//            } else {
//
//                ContextCompat.requestPermissions(mMainActivity,
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST);
//            }
//        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        Debug("AndroidPermission requestCode : " + requestCode);
//
//        switch (requestCode) {
//PERMISSIONS_REQUEST
//            case :
//
//                ActivityCompat.shouldShowRequestPermissionRationale(mMainActivity,Manifest.permission.ACCESS_FINE_LOCATION);
//
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(mMainActivity, "앱 실행을 위한 권한이 설정 되었습니다", Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(mMainActivity, "앱 실행을 위한 권한이 취소 되었습니다", Toast.LENGTH_LONG).show();
//                }
//                break;
//        }
 //   }
    private void DisConnect()
    {
    }

    public void OnResume()
    {
        Debug("NTSDK OnResume");
    }

    public void OnStop()
    {
        Debug("NTSDK OnStop");
    }

    //=======================================//    Log    //========================================//
    private static void Debug( String logMessage )
    {
        GetInstance().LogLineDebug( "[" + PURCHASE_TAG + "]: " + logMessage );
    }

    private void LogLineDebug(String str) {

        int limitedline = 3000;
        String temp_json = str;
        int log_index = 1;
        try {
            while (temp_json.length() > 0) {

                if (temp_json.length() > limitedline) {
                    Log.d(LOG_TAG, "log - " + log_index + " : "
                            + temp_json.substring(0, limitedline));
                    temp_json = temp_json.substring(limitedline);
                    log_index++;
                } else {
                    Log.d(LOG_TAG, temp_json);
                    break;
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            DebugError(result_msg);
        }
    }

    private static void DebugError( String logMessage )
    {
        Log.e( LOG_TAG, "[" + PURCHASE_TAG + "]: " + logMessage );
    }

}