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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.content.Intent;

import com.linegames.ct2.MainActivity;
import com.samsung.android.sdk.iap.lib.helper.HelperDefine.OperationMode;
import com.samsung.android.sdk.iap.lib.helper.IapHelper;
import com.samsung.android.sdk.iap.lib.listener.OnConsumePurchasedItemsListener;
import com.samsung.android.sdk.iap.lib.listener.OnGetOwnedListListener;
import com.samsung.android.sdk.iap.lib.listener.OnGetProductsDetailsListener;
import com.samsung.android.sdk.iap.lib.listener.OnPaymentListener;
import com.samsung.android.sdk.iap.lib.vo.ConsumeVo;
import com.samsung.android.sdk.iap.lib.vo.ErrorVo;
import com.samsung.android.sdk.iap.lib.vo.OwnedProductVo;
import com.samsung.android.sdk.iap.lib.vo.ProductVo;
import com.samsung.android.sdk.iap.lib.vo.PurchaseVo;

public class PurchaseGalaxy
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
    private static OperationMode IAP_MODE = OperationMode.OPERATION_MODE_TEST;

    private static IapHelper mIapHelper = null;
    private static Activity mMainActivity;
    private static ArrayList<String> productIdArr;
    private static JSONArray mProductsJsonArray;
    private static String mProductMode;

    private static PurchaseGalaxy getInstance = null;
    public native void nativeCB( String status, String msg, long userCB );

    public static synchronized PurchaseGalaxy GetInstance() {
        if (getInstance == null) {
            synchronized ( PurchaseGalaxy.class ) {
                if ( getInstance == null )
                    getInstance = new PurchaseGalaxy();
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
        Debug( "Connect() Galaxy userCB  (Galaxy) :" + userCB + " jProductMode : " + jProductMode);

        mProductMode = jProductMode;
        mMainActivity = LGBase.getMainActivity();

        IAP_MODE = OperationMode.OPERATION_MODE_PRODUCTION;
        if (jProductMode.equals("sandbox"))
            IAP_MODE = OperationMode.OPERATION_MODE_TEST;
        else if(jProductMode.equals("failure"))
            IAP_MODE = OperationMode.OPERATION_MODE_TEST_FAILURE;

        Debug("IAP_MODE : " + IAP_MODE.toString());
        result_status = "NT_SUCCESS";
        result_ResponseCode = 0;

        if (mIapHelper  == null)
        {
            mIapHelper = IapHelper.getInstance( mMainActivity.getApplicationContext() );
            mIapHelper.setOperationMode(IAP_MODE);
            result_msg = "PurchaseAPI Init Success (Galaxy) ";
        }
        else
        {
            result_msg = "PurchaseAPI Already connected. (Galaxy) ";
            mIapHelper.setOperationMode(IAP_MODE);
            DebugError(result_msg);
            result_status = "NT_SUCCESS";
        }
        GetInstance().Invoke( result_status, new JSONArray(), result_msg, result_ResponseCode , userCB );
    }

    private void DisConnect()
    {
        if ( IsConnected() )
            Debug( "Connection DisConnect (Galaxy) " );
    }

    public static void RegisterProduct( String jProductID )
    {
        Debug( "RegisterProduct pID : " +jProductID);
        if (jProductID.isEmpty())
        {
            DebugError("RegisterProduct pID is null. (Galaxy) ");
            return;
        }

        if (productIdArr == null)
            productIdArr = new ArrayList<>();

        if (productIdArr.contains(jProductID))
            return;

        GetInstance().productIdArr.add(jProductID);
        Debug( "RegisterProduct pID : " +jProductID + "productIdArr.size() : " + productIdArr.size());
    }

    public static void RefreshProductInfo( final long userCB )
    {
        if ( !CheckInitlized( userCB ) ) return;

        if (productIdArr == null)
        {
            GetInstance().Invoke( "UNKNOWN", null, "RefreshProductInfo() Success", 0 , userCB );
            return;
        }
        final int inputProductsCount = productIdArr.size();
        String              mConsumablePurchaseIDs = "";
        Intent intent = mMainActivity.getIntent();
        String mProductIds = "";

        final JSONArray productsJsonArray = new JSONArray();
        final ArrayList<String> invalidProductsId = productIdArr;

        if ( intent != null &&
                intent.getExtras() != null &&
                intent.getExtras().containsKey( "ProductIds" ))
        {
            Debug("----------------------------");
            Bundle extras = intent.getExtras();
            mProductIds    = extras.getString( "ProductIds" );
        }

        mIapHelper.getProductsDetails("", new OnGetProductsDetailsListener() {
            @Override
            public void onGetProducts(ErrorVo errorVo, ArrayList<ProductVo> arrayList)
            {
                result_status = "UNKNOWN";
                int resultCode = 0;
                String resultMsg;
                if( errorVo != null)
                {
                    if (errorVo.getErrorCode() == IapHelper.IAP_ERROR_NONE)
                    {
                        if (arrayList != null)
                        {
                            Debug("getProductsDetails arrayList.size(): " + arrayList.size());
                            for (int i = 0; i < arrayList.size(); i++)
                            {
                                ProductVo product = arrayList.get(i);
                                Debug("getItemId : " + product.getItemId() + " getCurrencyCode : " + product.getCurrencyCode() + " getIsConsumable : " + product.getIsConsumable() + "getItemPrice : " + product.getItemPrice() );
                                try {
                                    productsJsonArray.put(new JSONObject()
                                            .put("productId", product.getItemId() )
                                            .put("title", product.getItemName() )
                                            .put("description", product.getItemDesc() )
                                            .put("price", product.getItemPrice().toString() )
                                            .put("microPrice", Double.toString(product.getItemPrice() * 1000000))
                                            .put("currencyCode", product.getCurrencyCode())
                                    );
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    result_msg = e.getMessage();
                                }

                                if ( invalidProductsId.contains( product.getItemId() ) )
                                {
                                    invalidProductsId.remove( product.getItemId() );
                                }
                            }

                            if ( productsJsonArray.length() != inputProductsCount )
                            {
                                resultMsg = "Invalid ProductID List => " + invalidProductsId.toString();
                            }
                            else
                            {
                                result_status = "NT_SUCCESS";
                                resultMsg = "RefreshProductInfo() Success";
                                resultCode = 0;
                            }
                            GetInstance().Invoke( result_status, productsJsonArray, resultMsg, resultCode , userCB );
                        }
                    }
                    else
                    {
                        //-1007 Item Not Setting Real. check the IAP_MODE
                        //-1002 UNKNOWN
                        if (errorVo.getErrorCode() == IapHelper.IAP_ERROR_ALREADY_PURCHASED)
                        {
                            result_status = "ITEM_ALREADY_OWNED";
                            resultMsg = "The user already owns this item";
                        }
                        else
                        {
                            resultMsg = errorVo.getErrorDetailsString();
                        }
                        resultCode = errorVo.getErrorCode();
                        GetInstance().Invoke( result_status, productsJsonArray, resultMsg, resultCode , userCB );
                    }
                }
                else
                {
                    resultMsg = "RefreshProductInfo() errorVo is null.";
                    DebugError("errorVo.getErrorCode() : " + errorVo.getErrorCode());
                    DebugError("errorVo.getErrorDetailsString() : " + errorVo.getErrorDetailsString());
                    DebugError(resultMsg);
                    GetInstance().Invoke( result_status, productsJsonArray, resultMsg, resultCode , userCB );
                }
            }
        });
    }

    public static void BuyProduct( String jProductID, String developerPayload, long lUserCB )
    {
        Debug("BuyProduct productID : " + jProductID + " DeveloperPayload : " + developerPayload );
        final String productID = jProductID;
        final long userCB = lUserCB;
        result_msg = "";
        if ( !CheckInitlized( userCB ) ) return;

        if ( productID.isEmpty() ) {
            result_msg = "BuyProduct() productID is null. (Galaxy) ";
            DebugError(result_msg);
            GetInstance().Invoke( result_status, new JSONArray(), result_msg, result_ResponseCode , userCB );
            return;
        }
        mMainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProductsJsonArray = new JSONArray();
                mIapHelper.startPayment(productID, developerPayload, new OnPaymentListener()
                {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onPayment(ErrorVo _errorVo, PurchaseVo _purchaseVo) {
                        if (_errorVo != null)
                        {
                            Debug("_errorVo.getErrorString())" + _errorVo.getErrorString());
                            Debug("_errorVo.getErrorCode())" + _errorVo.getErrorCode());
                            if (_errorVo.getErrorCode() == IapHelper.IAP_ERROR_NONE)
                            {
                                if (_purchaseVo != null)
                                {
                                    if (developerPayload != null && _purchaseVo.getPassThroughParam() != null)
                                    {
                                        if (developerPayload.equals(_purchaseVo.getPassThroughParam()))
                                        {
                                            PurchaseVo pd = _purchaseVo;
                                            try {

                                                JSONObject pObj = new JSONObject(pd.getJsonString());
                                                pObj.put("obfuscatedAccountId", developerPayload );
                                                Debug("pObj : " + pObj.toString());

                                                mProductsJsonArray.put(new JSONObject()
                                                        .put("productId", pObj.getString("mItemId"))
                                                        .put("microPrice", Double.toString(pd.getItemPrice() * 1000000) )
                                                        .put("currencyCode", pd.getCurrencyCode() )
                                                        .put("originalJson", CreateReceipt(pd.getPaymentId(), pd.getPurchaseId(), pd.getPurchaseDate(), developerPayload).toString()  )
                                                        .put("obfuscatedAccountId", developerPayload )
                                                        .put("signature", pd.getJsonString() )
                                                        .put(PURCHASE_ID, pd.getPurchaseId() )
                                                        .put("PaymentID", pd.getPaymentId() )
                                                );
                                            }
                                            catch (Exception e)
                                            {
                                                e.printStackTrace();
                                                DebugError(e.toString());
                                                result_msg = e.toString();
                                            }

                                            if (_purchaseVo.getIsConsumable())
                                            {
                                                Debug("BuyProduct ItemId : " + _purchaseVo.getItemId() + " purchaseId : "  + _purchaseVo.getPurchaseId());
                                                result_status = "NT_SUCCESS";
                                            }
                                            else
                                            {
                                                result_msg = "NonConsumeAble Item : " + _purchaseVo.getItemId();
                                                DebugError(result_msg);
                                                result_status = "UNKNOWN";
                                            }
                                        } else
                                            DebugError("BuyPurchase passThroughParam is mismatched developerPayload : " + developerPayload +  "_purchaseVo.getPassThroughParam() : " + _purchaseVo.getPassThroughParam());
                                    }
                                }
                                else
                                    DebugError("BuyPurchase > _purchaseVo is null");
                            }
                            else if (_errorVo.getErrorCode() == IapHelper.IAP_PAYMENT_IS_CANCELED)
                            {
                                result_msg = "User canceled the purchase.";
                                Debug(result_msg);
                                result_status = "USER_CANCELED";
                            }
                            else if (_errorVo.getErrorCode() == IapHelper.IAP_ERROR_COMMON)
                            {
                                result_msg = "Item Already owned. :" + jProductID;
                                Debug(result_msg);
                                result_status = "ITEM_ALREADY_OWNED";
                            }
                            else
                            {
                                DebugError("BuyPurchase > ErrorCode [" + _errorVo.getErrorCode() + "]");
                                result_msg = "BuyPurchase > ErrorString[" + _errorVo.getErrorString() + "]";
                                DebugError(result_msg);
                            }
                        }
                        result_ResponseCode = _errorVo.getErrorCode();
                        GetInstance().Invoke( result_status, mProductsJsonArray, result_msg, result_ResponseCode , userCB );
                    }
                });
            }
        });
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
            mIapHelper.consumePurchasedItems(jConsumePurchaseId, new OnConsumePurchasedItemsListener()
            {
                @Override
                public void onConsumePurchasedItems(ErrorVo _errorVo, ArrayList<ConsumeVo> _consumeList) {
                    if (_errorVo != null)
                    {
                        if (_errorVo.getErrorCode() == IapHelper.IAP_ERROR_NONE)
                        {
                            try {
                                if (_consumeList != null)
                                {
                                    Debug("Consume _consumeList size() : " + _consumeList.size() );
                                    for (ConsumeVo consumeVo : _consumeList)
                                    {
                                        if (consumeVo.getStatusCode() == 0)
                                        {
                                            result_status = "NT_SUCCESS";
                                            result_msg = "Consume Success : " + productID + " (" + finalJConsumePurchaseId + ")";
                                            GetInstance().Invoke( result_status, new JSONArray(), result_msg, result_ResponseCode , userCB );
                                            return;
                                        }
                                    }
                                }
                            } catch (Exception e)
                            {
                                DebugError("onConsumePurchasedItems: Exception " + e);
                            }
                        } else {
                            DebugError("onConsumePurchasedItems > ErrorCode [" + _errorVo.getErrorCode() + "]");
                            if (_errorVo.getErrorString() != null)
                                DebugError("onConsumePurchasedItems > ErrorString[" + _errorVo.getErrorString() + "]");
                        }
                        result_msg = _errorVo.getErrorString();
                        result_ResponseCode = _errorVo.getErrorCode();
                    }
                    GetInstance().Invoke( result_status, new JSONArray(), result_msg, result_ResponseCode , userCB );
                }
            });
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
        Debug( "RestoreProduct() start (Galaxy) " );
        if ( !CheckInitlized( userCB ) ) return;
        final JSONArray productsJsonArray = new JSONArray();

        mIapHelper.getOwnedList("item", new OnGetOwnedListListener(){
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onGetOwnedProducts(ErrorVo _errorVo, ArrayList<OwnedProductVo> _ownedList) {

                Debug("RestoreProduct");
                mProductsJsonArray = new JSONArray();
                if( _errorVo != null) {
                    if (_errorVo.getErrorCode() == IapHelper.IAP_ERROR_NONE)
                    {
                        if (_ownedList != null)
                        {
                            Debug("RestoreProduct size(): "+_ownedList.size());
                            for (int i = 0; i < _ownedList.size(); i++) {
                                OwnedProductVo pd = _ownedList.get(i);
                                try {
                                    String oriJson = pd.getJsonString();
                                    JSONObject pJson = new JSONObject(oriJson);
                                    Debug("pJson : " + pJson.toString());
                                    long mPrice = (long)(pd.getItemPrice() * 1000000);
                                    mProductsJsonArray.put(new JSONObject()
                                            .put("productId", pJson.getString("mItemId")  )
                                            .put("microPrice", mPrice)
                                            .put("currencyCode", pd.getCurrencyCode() )
                                            .put("originalJson", CreateReceipt(pd.getPaymentId(), pd.getPurchaseId(), pd.getPurchaseDate(), pd.getPassThroughParam() ).toString()  )
                                            .put("developerPayload", pd.getPassThroughParam() )
                                            .put("signature", pd.getJsonString() )
                                            .put(PURCHASE_ID, pd.getPurchaseId() )
                                            .put("PaymentID", pd.getPaymentId() )
                                    );
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                    DebugError(e.toString());
                                }
                            }
                        }
                    }
                    else
                    {
                        Log.e(LOG_TAG, "onGetOwnedProducts ErrorCode [" + _errorVo.getErrorCode() +"]");
                        if(_errorVo.getErrorString()!=null)
                            Log.e(LOG_TAG, "onGetOwnedProducts ErrorString[" + _errorVo.getErrorString() + "]");
                    }
                }
                else
                    DebugError("getErrorString() : " + _errorVo.getErrorString());

                result_status = "NT_SUCCESS";
                result_msg = _errorVo.getErrorString();
                result_ResponseCode = _errorVo.getErrorCode();
                GetInstance().Invoke( result_status, mProductsJsonArray, result_msg, result_ResponseCode , userCB );
            }
        });
    }

    public static void ConsumeAll( long userCB ) throws JSONException {
        Debug( "ConsumeAll() (Galaxy) " );

        if ( !CheckInitlized( userCB ) ) return;
        mIapHelper.getOwnedList("item", new OnGetOwnedListListener(){
            @Override
            public void onGetOwnedProducts(ErrorVo _errorVo, ArrayList<OwnedProductVo> _ownedList)
            {
                mProductsJsonArray = new JSONArray();
                if( _errorVo != null)
                {
                    if (_errorVo.getErrorCode() == IapHelper.IAP_ERROR_NONE)
                    {
                        int gunLevel = 0;
                        boolean infiniteBullet = false;
                        if (_ownedList != null)
                        {
                            Debug("ConsumeAll size(): " + _ownedList.size());
                            for (int i = 0; i < _ownedList.size(); i++)
                            {
                                OwnedProductVo product = _ownedList.get(i);
                                Debug("purchaseId : " + product.getPurchaseId());
                                String jPurchaseId = product.getPurchaseId();
                                mIapHelper.consumePurchasedItems(jPurchaseId, new OnConsumePurchasedItemsListener()
                                {
                                    @Override
                                    public void onConsumePurchasedItems(ErrorVo _errorVo, ArrayList<ConsumeVo> _consumeList) {
                                        if (_errorVo != null)
                                        {
                                            if (_errorVo.getErrorCode() == IapHelper.IAP_ERROR_NONE)
                                            {
                                                try {
                                                    if (_consumeList != null)
                                                    {
                                                        for (ConsumeVo consumeVo : _consumeList)
                                                        {
                                                            if (consumeVo.getStatusCode() == 0)
                                                                Debug("ConsumeAll Consume Success : " + consumeVo.getPurchaseId() );
                                                            else
                                                                DebugError("ConsumeAll : getJsonString " + consumeVo.getJsonString());
                                                        }
                                                    }
                                                } catch (Exception e)
                                                {
                                                    DebugError("ConsumeAll : Exception " + e);
                                                }
                                            } else {
                                                DebugError("ConsumeAll  > ErrorCode [" + _errorVo.getErrorCode() + "]");
                                                if (_errorVo.getErrorString() != null)
                                                    DebugError("ConsumeAll  > ErrorString[" + _errorVo.getErrorString() + "]");
                                            }
                                        }
                                    }
                                });
                            }
                            mProductsJsonArray = new JSONArray();
                        }
                    }
                    else
                        Log.e(LOG_TAG, "ConsumeAll onGetOwnedProducts ErrorCode [" + _errorVo.getErrorCode() +"]");
                }
                else
                    DebugError("ConsumeAll getErrorString() : " + _errorVo.getErrorString());
            }
        });
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
            result_msg = "Purchase is not ready. First Call Connect() API. (Galaxy) ";
            DebugError(result_msg);
            GetInstance().Invoke( result_status, new JSONArray(), result_msg, result_ResponseCode , userCB );
            return false;
        }
        return true;
    }

    private static boolean IsConnected()
    {
        if (mIapHelper == null)
            return false;
        else
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
        Debug("NTSDK OnResume (Galaxy) ");
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
        if(mIapHelper != null)
        {
            mIapHelper.dispose();
        }
    }
}