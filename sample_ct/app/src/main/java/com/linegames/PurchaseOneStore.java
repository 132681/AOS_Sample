package com.linegames;

import android.annotation.TargetApi;
import android.app.Activity;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import android.util.Log;

import com.linegames.base.LGBase;
import com.linegames.base.LGLog;
import com.linegames.ct2.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.content.Intent;

public class PurchaseOneStore
{
    private static String LOG_TAG = "NTSDK";
    private static String PURCHASE_TAG = "PurchaseAPI";
    private static String result_msg= "Connect Success.";
    private static String result_status = "NT_SUCCESS";
    private static String PURCHASE_ID = "purchaseId";
    private static String PAYMENT_ID = "PaymentID";
    private static String PRODUCT_ID = "productId";
    private static String MICROPRICE = "microPrice";
    private static String CURRENCY_CODE = "currencyCode";
    private static String ORIGINJSON = "originalJson";
    private static String DEVELOPER_PAYLOAD = "developerPayload";

    private static int result_ResponseCode = 0;
    private static long noCB = 777L;
    private static long PURCHASE_GALAXY_REQUEST_CODE = 0x0FA;

    private static Activity mMainActivity;
    private static ArrayList<String> productIdArr;
    private static JSONArray mProductsJsonArray;
    private static String mProductMode;

    private static PurchaseOneStore getInstance = null;
    public native void nativeCB( String status, String msg, long userCB );

    public static synchronized PurchaseOneStore GetInstance() {
        if (getInstance == null) {
            synchronized ( PurchaseOneStore.class ) {
                if ( getInstance == null )
                    getInstance = new PurchaseOneStore();
            }
        }
        return getInstance;
    }

    public static void Test()
    {
        Debug("test  ----");
    }

    public static void Connect( final String jProductMode, final long userCB )
    {
        Debug( "Connect() OneStore userCB  (OneStore) :" + userCB + " jProductMode : " + jProductMode);


        GetInstance().Invoke( result_status, new JSONArray(), result_msg, result_ResponseCode , userCB );
    }

    private void DisConnect()
    {
        if ( IsConnected() )
            Debug( "Connection DisConnect (OneStore) " );
    }

    public static void RegisterProduct( String jProductID )
    {
        Debug( "RegisterProduct pID : " +jProductID);
        if (jProductID.isEmpty())
        {
            DebugError("RegisterProduct pID is null. (OneStore) ");
            return;
        }

        if (productIdArr == null)
            productIdArr = new ArrayList<>();

        if (productIdArr.contains(jProductID))
            return;

       // GetInstance().productIdArr.add(jProductID);
        Debug( "RegisterProduct pID : " +jProductID + "productIdArr.size() : " + productIdArr.size());
    }

    public static void RefreshProductInfo( final long userCB )
    {
        if ( !CheckInitlized( userCB ) ) return;

//        final int inputProductsCount = productIdArr.size();
//        String              mConsumablePurchaseIDs = "";
//        Intent intent = mMainActivity.getIntent();
//        String mProductIds = "";
//
//        final JSONArray productsJsonArray = new JSONArray();
//        final ArrayList<String> invalidProductsId = productIdArr;
//
//        if ( intent != null &&
//                intent.getExtras() != null &&
//                intent.getExtras().containsKey( "ProductIds" ))
//        {
//            Debug("----------------------------");
//            Bundle extras = intent.getExtras();
//            mProductIds    = extras.getString( "ProductIds" );
//        }


    }

    public static void BuyProduct( String jProductID, String developerPayload, long lUserCB )
    {
        Debug("BuyProduct productID : " + jProductID + " DeveloperPayload : " + developerPayload );
        final String productID = jProductID;
        final long userCB = lUserCB;
        result_msg = "";
        if ( !CheckInitlized( userCB ) ) return;

        if ( productID.isEmpty() ) {
            result_msg = "BuyProduct() productID is null. (OneStore) ";
            DebugError(result_msg);
            GetInstance().Invoke( result_status, new JSONArray(), result_msg, result_ResponseCode , userCB );
            return;
        }

    }

    public static void Consume( final String productID, final long userCB ) throws JSONException {
        Debug( "Consume() productID : " + productID );
        if ( !CheckInitlized( userCB ) ) return;

        result_status = "UNKNOWN";

        String jConsumePurchaseId = null;

        if (mProductsJsonArray == null)
        {
            result_msg = "mProductsJsonArray is null.";
            DebugError(result_msg);
            GetInstance().Invoke( result_status, new JSONArray(), result_msg, result_ResponseCode , userCB );
            return;
        }

        //Find PurchaseId by productId
        for (int i = 0; i < mProductsJsonArray.length(); i++) {
            JSONObject jPurhcaseInfo = mProductsJsonArray.getJSONObject(i);
            if (jPurhcaseInfo.getString("productId").equals(productID))
            {
                String jObjStr = jPurhcaseInfo.getString("originalJson");
                JSONObject pJson = new JSONObject(jObjStr);
                jConsumePurchaseId = pJson.getString(PURCHASE_ID);
                break;
            }
        }

        if ( jConsumePurchaseId != null && !jConsumePurchaseId.isEmpty() )
        {
            String finalJConsumePurchaseId = jConsumePurchaseId;

        }
        else
        {
            result_msg = "Consume Pid is null. : " + productID;
            DebugError(result_msg);
            GetInstance().Invoke( result_status, new JSONArray(), result_msg, result_ResponseCode , userCB );
        }
    }

    public static void RestoreProduct( long userCB )
    {
        Debug( "RestoreProduct() start (OneStore) " );
        if ( !CheckInitlized( userCB ) ) return;
        final JSONArray productsJsonArray = new JSONArray();


    }

    public static void ConsumeAll( long userCB ) throws JSONException {
        Debug( "ConsumeAll() (OneStore) " );

        if ( !CheckInitlized( userCB ) ) return;

    }

    private void Invoke( String result_status, JSONArray productJsonArr, String msg, int resultCode, long userCB )
    {
        final JSONObject jsonResult = new JSONObject();
        try
        {
            jsonResult.put( "products", productJsonArr );
            jsonResult.put( "Message", msg );
            jsonResult.put( "ResponseCode", resultCode);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            result_msg = e.getMessage();
            DebugError(result_msg);
        }

        if (userCB != noCB)
        {
            Debug( "Invoke() status : " + result_status + " msg : " + jsonResult.toString() + " userCB : " + userCB  );
            if (userCB != noCB)
            {
                LGLog.d("Invoke() status : " + result_status + " msg : " + jsonResult.toString() + " userCB : " + userCB  );
                nativeCB( result_status, jsonResult.toString(), userCB );
                if (MainActivity.infoTextView == null)
                {
                    LGLog.d("lss MainActivity.infoTextView == null"  );
                }
                MainActivity.UpdateInfoText( result_status, jsonResult.toString());//("Invoke() status : " + result_status + " msg : " + jsonResult.toString());
            }

        }
    }

    private static boolean CheckInitlized( long userCB )
    {
        if ( mMainActivity == null )
            mMainActivity = LGBase.getMainActivity();

        result_status = "UNKNOWN";
        result_ResponseCode = -1;

        if ( !IsConnected() )
        {
            result_msg = "Purchase is not ready. First Call Connect() API. (OneStore) ";
            DebugError(result_msg);
            GetInstance().Invoke( result_status, new JSONArray(), result_msg, result_ResponseCode , userCB );
            return false;
        }
        return true;
    }

    private static boolean IsConnected()
    {
           return true;
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static JSONObject CreateReceipt(String paymentId, String purchaseId, String purchaseDate, String developerPayload) throws JSONException {
        if (purchaseDate.isEmpty())
            DebugError("CreateReceipt() purchaseDate is null.");

        if (paymentId.isEmpty())
            DebugError("CreateReceipt() paymentId is null.");

        if (purchaseId.isEmpty())
            DebugError("CreateReceipt() purchaseId is null.");

        if (developerPayload.isEmpty())
            DebugError("CreateReceipt() developerPayload is null.");

        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        Date trans_date = new Date();
        try {
            trans_date = formatter.parse(purchaseDate);
        } catch (ParseException e) {}

        String[] splitPayload = developerPayload.split("\\.");
        String productId = null;
        if (splitPayload.length > 1)
            productId = splitPayload[1];

        if (productId.isEmpty())
            DebugError("CreateReceipt() productId is null. Check the developerPayload : " + developerPayload );

        JSONObject receiptObj = new JSONObject();
        receiptObj.put("paymentId", paymentId);
        receiptObj.put( PURCHASE_ID, purchaseId);
        receiptObj.put("orderId", paymentId);
        receiptObj.put("purchaseTime",  trans_date.getTime());
        receiptObj.put("productId", productId);
        receiptObj.put("obfuscatedAccountId", developerPayload);

        return receiptObj;
    }

    public void OnResume()
    {
        Debug("NTSDK OnResume (OneStore) ");
        if ( mMainActivity != null )
        {
            Connect( mProductMode, noCB );
        }
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
    //========================================//    Log    //=========================================//

    public void OnStop()
    {
    }

    public void OnDestory()
    {

    }
}