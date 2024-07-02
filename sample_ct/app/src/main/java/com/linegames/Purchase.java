package com.linegames;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;

import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.common.collect.ImmutableList;

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
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.linegames.base.LGLog;
import com.linegames.base.LGBase;
import com.linegames.ct2.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Purchase implements PurchasesUpdatedListener
{
    private static String LOG_TAG = "NTSDK";
    private static String PURCHASE_TAG = "PurchaeAPI";

    private static String result_msg= "Connect Success.";
    private static String result_status = "NT_SUCCESS";
    private static int result_ResponseCode = 0;
    private static long purchaseCB = 0L;
    private static long noCB = 777;

    private static Activity mMainActivity;
    private static BillingClient mBillingClient;
    private static ArrayList<String> productIdArr;
    private static List<ProductDetails> mProductDetailsList;
    private static List<com.android.billingclient.api.Purchase> mPurchases;
    private static HashMap<String, ProductDetails> mProductDetailsListMap;

    private static Purchase getInstance = null;
    public native void nativeCB( String status, String msg, long userCB );

    public static synchronized Purchase GetInstance() {
        if (getInstance == null) {
            synchronized ( Purchase.class ) {
                if ( getInstance == null )
                    getInstance = new Purchase();
            }
        }
        return getInstance;
    }

    public static void Connect( final String jProductMode, final long userCB )
    {
        LGLog.d("Connect ========================" );
        result_status = "UNKNOWN";
        result_ResponseCode = -1;

        if ( IsConnected() ) {
            result_msg = "PurchaseAPI Already connected.";
            LGLog.e(result_msg);
            result_status = "NT_SUCCESS";
            result_ResponseCode = 0;
            GetInstance().Invoke( result_status, new JSONArray(), result_msg, result_ResponseCode , userCB );
            return;
        }

        mMainActivity = LGBase.getMainActivity();
        if (mMainActivity == null)
        {
            result_msg = "Connect() mMainActivity is null.";
            LGLog.e(result_msg);
            result_status = "UNKNOWN";
            result_ResponseCode = -1;
            GetInstance().Invoke( result_status, new JSONArray(), result_msg, result_ResponseCode , userCB );
            return;
        }

        final JSONObject jsonResult = new JSONObject();

        mBillingClient = BillingClient.newBuilder(mMainActivity)
                .enablePendingPurchases()
                .setListener(Purchase.GetInstance())
                .build();

        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {

                result_msg = billingResult.getDebugMessage();
                if ( billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK )
                {
                    // The BillingClient is ready. You can query purchases here.
                    result_status = "NT_SUCCESS";
                    result_msg = "Connect Success.";
                }
                else
                {
                    result_status = "UNKNOWN";
                    if ( billingResult.getResponseCode() == 3 )
                        result_status = "NE_PURCHASE_UPDATE_GOOGLEPLAY";
                }

                try {
                    jsonResult.put( "Message", billingResult.getDebugMessage() );
                    jsonResult.put( "ResponseCode", billingResult.getResponseCode() );
                } catch (Exception e) {
                    e.printStackTrace();
                }
                GetInstance().Invoke( result_status, new JSONArray(), result_msg, billingResult.getResponseCode() , userCB );
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                try {
                    jsonResult.put( "Message", "Purchase DisConnected." );
                } catch (Exception e) {
                    e.printStackTrace();
                }
                GetInstance().Invoke( "PURCHASE_DISCONNECTED", new JSONArray(), "Purchase DisConnected.", -1 , userCB );
            }
        });
    }

    private void DisConnect()
    {
        if ( IsConnected() )
        {
            mBillingClient.endConnection();
            mBillingClient = null;
            LGLog.i("Connection DisConnect" );
        }
    }

    public static void RegisterProduct( String jProductID )
    {
        if (jProductID.isEmpty())
        {
            LGLog.e("RegisterProduct pID is null");
            return;
        }

        if (productIdArr == null)
            productIdArr = new ArrayList<>();

        if (productIdArr.contains(jProductID))
            return;

        GetInstance().productIdArr.add(jProductID);
        LGLog.d("RegisterProduct pID : " +jProductID );
    }

    public static void RefreshProductInfo( final long userCB )
    {
        final JSONArray productsJsonArray = new JSONArray();
        String resultMsg;
        if (productIdArr == null)
        {
            result_status = "UNKNOWN";
            resultMsg = "Check the RefreshProductInfo() Input Parameter. ProductId count is 0.";
            GetInstance().Invoke( result_status, productsJsonArray, resultMsg, -1 , userCB );
            return;
        }

        final ArrayList<String> tempProductsId = productIdArr;
        final int inputProductsCount = tempProductsId.size();

        LGLog.d("RefreshProductInfo() Input ProductId : " + tempProductsId.toString());

        mProductDetailsList = new ArrayList<ProductDetails>();
        final ArrayList<String> invalidProductsId = tempProductsId;

        List<QueryProductDetailsParams.Product> productsInfoParams = new ArrayList<>();

        for (String pid : productIdArr) {

            if (pid == null || pid.isEmpty())
                continue;

            productsInfoParams.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(pid)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build());
        }

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productsInfoParams)
                .build();

        mProductDetailsListMap = new HashMap<String, ProductDetails>();

        if ( !CheckInitlized( userCB ) ) return;

        mBillingClient.queryProductDetailsAsync( params, new ProductDetailsResponseListener()
        {
            @Override
            public void onProductDetailsResponse(BillingResult billingResult,
                                                 List<ProductDetails> ProductDetailsList)
            {
                result_status = "UNKNOWN";
                int resultCode = billingResult.getResponseCode();
                String resultMsg = "Fail.";

                if ( billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK )
                {
                    mProductDetailsList.addAll(ProductDetailsList);
                    try {
                        for (ProductDetails product : ProductDetailsList) {
                            mProductDetailsListMap.put( product.getProductId(), product );
                            productsJsonArray.put(new JSONObject()
                                    .put("productId", product.getProductId())
                                    .put("title", product.getTitle())
                                    .put("description", product.getDescription())
                                    .put("price", product.getOneTimePurchaseOfferDetails().getFormattedPrice())
                                    .put("microPrice", Long.toString(product.getOneTimePurchaseOfferDetails().getPriceAmountMicros()))
                                    .put("currencyCode", product.getOneTimePurchaseOfferDetails().getPriceCurrencyCode())
                            );

                            if ( invalidProductsId.contains( product.getProductId() ) )
                                invalidProductsId.remove( product.getProductId() );

                        }
                        result_status = "NT_SUCCESS";

                        if ( productsJsonArray.length() != inputProductsCount )
                            resultMsg = "Invalid ProductID List  => " + invalidProductsId.toString();

                    } catch (Exception e) {
                        e.printStackTrace();
                        LGLog.e(e.getMessage());
                        resultMsg = e.getMessage();
                        resultCode = billingResult.getResponseCode();
                    }
                }
                else {
                        resultMsg = billingResult.getDebugMessage() + "(" + resultCode + ")";
                        resultCode = billingResult.getResponseCode();
                }


                GetInstance().Invoke( result_status, productsJsonArray, resultMsg, resultCode , userCB );
            }
        });


    }

    public static void BuyProduct( String jProductID, String jDeveloperPayload, long lUserCB )
    {

        LGLog.d("BuyProduct productID : " + jProductID + " DeveloperPayload : " + jDeveloperPayload );
        final String productID = jProductID;
        final String developerPayload = jDeveloperPayload;
        final long userCB = lUserCB;

        if ( !CheckInitlized( userCB ) ) return;

        final JSONArray productsJsonArray = new JSONArray();

        if ( productID.isEmpty() ) {
            result_msg = "BuyProduct() productID is null.";
            LGLog.e(result_msg);
            GetInstance().Invoke( result_status, new JSONArray(), result_msg, result_ResponseCode , userCB );
            return;
        }
        purchaseCB = userCB;
        mMainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                ProductDetails buyProductSkuDetail = null;
                if ( mProductDetailsList != null )
                {
                    for ( ProductDetails detail : mProductDetailsList )
                    {
                        if ( detail.getProductId().equals(productID) )
                        {
                            LGLog.d("find productID SkuDetail : " + productID);
                            buyProductSkuDetail = detail;
                            break;
                        }
                        else
                        {
                            LGLog.d("Dont find productID SkuDetail : " + productID);
                        }
                    }

                    if ( buyProductSkuDetail == null || buyProductSkuDetail.getProductId().isEmpty() )
                    {
                        result_msg = "ProductID does no match. Please Check productID ( " + productID + " ) ";
                        LGLog.d(result_msg);
                        GetInstance().Invoke( "UNKNOWN", new JSONArray(), result_msg, result_ResponseCode , userCB );
                        return;
                    }

                    ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                            ImmutableList.of(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                            .setProductDetails(buyProductSkuDetail)
                                            .build()
                            );

                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .setObfuscatedAccountId(developerPayload)
                            .build();

                    mBillingClient.launchBillingFlow(mMainActivity, billingFlowParams);
                    return;
                }
                else
                {
                    result_msg = "Purchase is not ready. Call RefreshProductInfo() API.";
                    LGLog.e(result_msg);
                }

                GetInstance().Invoke( result_status, productsJsonArray, result_msg, result_ResponseCode , userCB );
            }
        });
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<com.android.billingclient.api.Purchase> purchases)
    {
        result_status = "UNKNOWN";
        result_msg = billingResult.getDebugMessage();

        int responseCode = billingResult.getResponseCode();
        final JSONArray productsJsonArray = new JSONArray();

        LGLog.i("onPurchasesUpdated: $responseCode $debugMessage responseCode " + responseCode);
        switch (responseCode) {
            case BillingClient.BillingResponseCode.OK:
                if (purchases == null) {
                    LGLog.e("onPurchasesUpdated: null purchase list");
                }
                else
                {
                    JSONObject jsonObj = null;
                    mPurchases = purchases;
                    try {
                        for (com.android.billingclient.api.Purchase product : mPurchases)
                        {
                            productsJsonArray.put(new JSONObject()
                                    .put("productId", GetInstance().GetProductIdFromReceipt(product.getOriginalJson()) )
                                    .put("microPrice", Long.toString(mProductDetailsListMap.get( GetInstance().GetProductIdFromReceipt(product.getOriginalJson()) ).getOneTimePurchaseOfferDetails().getPriceAmountMicros()) )
                                    .put("currencyCode", mProductDetailsListMap.get( GetInstance().GetProductIdFromReceipt(product.getOriginalJson()) ).getOneTimePurchaseOfferDetails().getPriceCurrencyCode() )
                                    .put("originalJson", product.getOriginalJson() )
                                    .put("developerPayload", product.getDeveloperPayload() )
                                    .put("signature", product.getSignature() )
                            );

                            result_status = "NT_SUCCESS";
                            if(product.getPurchaseState() == com.android.billingclient.api.Purchase.PurchaseState.PENDING)
                            {
                                result_status = "PURCHASE_PENDING_TRANSACTIONS";
                                GetInstance().SetSharedPreferencesData(product.getOrderId(), "");
                            }
                            else
                            {
                                if (product.getOrderId().equals( GetInstance().GetSharedPreferencesData(product.getOrderId())) )
                                {
                                    LGLog.d("Purchase Pending Return. (" + GetInstance().GetProductIdFromReceipt(product.getOriginalJson()) + ")");
                                    GetInstance().RemoveSharedPreferencesData(product.getOrderId());
                                    GetInstance().Invoke( result_status, productsJsonArray, billingResult.getDebugMessage(), billingResult.getResponseCode() , purchaseCB );
                                    return;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        result_msg = e.getMessage();
                        LGLog.e(result_msg);
                    }
                }

                GetInstance().Invoke( result_status, productsJsonArray, billingResult.getDebugMessage(), billingResult.getResponseCode() , purchaseCB );
                return;

            case BillingClient.BillingResponseCode.USER_CANCELED:
                result_msg = "onPurchasesUpdated: User canceled the purchase.";
                result_status = "USER_CANCELED";
                LGLog.i(result_msg);
                break;
            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                result_status = "ITEM_ALREADY_OWNED";
                result_msg = "onPurchasesUpdated: The user already owns this item";
                LGLog.i(result_msg);
                //Need Consume Process
                break;
            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                result_msg = "onPurchasesUpdated: Developer error means that Google Play " +
                        "does not recognize the configuration. If you are just getting started, " +
                        "make sure you have configured the application correctly in the " +
                        "Google Play Console. The SKU product ID must match and the APK you " +
                        "are using must be signed with release keys.";
                LGLog.i(result_msg);
                break;
            case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
                result_msg = "onPurchasesUpdated: Need Goolge Account Login!!!";
                LGLog.i(result_msg);
                //Need Google Account login
                break;
            case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                result_msg = "Need Test URL confirm.";
                LGLog.i(result_msg);
                //Need Test URL confrim
                break;
            case BillingClient.BillingResponseCode.ERROR:
                result_msg = "This card cannot be paid.";
                result_status = "CREDITCARD_UNAVAILABLE";
                LGLog.i(result_msg);
                //This creditcard cannot be paid.
                break;
        }
        GetInstance().Invoke( result_status, productsJsonArray, result_msg, billingResult.getResponseCode() , purchaseCB );
    }

    public static void Consume( final String productID, final long userCB ) throws JSONException {
        LGLog.d("Consume() productID : " + productID );
        if ( !CheckInitlized( userCB ) ) return;

        result_status = "UNKNOWN";
        if ( productID.isEmpty() )
        {
            result_msg = "Consume() productID is NULL.";
            LGLog.e(result_msg);
            GetInstance().Invoke( result_status, new JSONArray(), result_msg, result_ResponseCode , userCB );
        }
        else
        {
            if ( mPurchases != null )
            {
                ConsumeParams consumeParams = null;
                for (com.android.billingclient.api.Purchase purchaseData : mPurchases )
                {
                    if ( productID.equals(GetInstance().GetProductIdFromReceipt(purchaseData.getOriginalJson())) )
                    {
                        consumeParams = ConsumeParams.newBuilder()
                                .setPurchaseToken(purchaseData.getPurchaseToken())
                                .build();
                        LGLog.d("Consume() consumeParams productID : " + GetInstance().GetProductIdFromReceipt(purchaseData.getOriginalJson()) );
                        break;
                    }
                }

                if ( consumeParams != null )
                {
                    final JSONArray productsJsonArray = new JSONArray();
                    mBillingClient.consumeAsync( consumeParams, new ConsumeResponseListener() {
                        @Override
                        public void onConsumeResponse(BillingResult billingResult, String s)
                        {
                            if ( billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK )
                            {
                                result_status = "NT_SUCCESS";
                                result_ResponseCode = 0;
                                result_msg = " Consume Success ( " + productID + " )";
                            }
                            else
                            {
                                result_msg = productID + " Purchase.java Consume() fail ";
                                result_ResponseCode = billingResult.getResponseCode();
                            }
                            LGLog.d(result_msg );
                            GetInstance().Invoke( result_status, productsJsonArray , result_msg, result_ResponseCode , userCB );
                        }
                    });
                }
                else
                {
                    result_msg = productID + " Consume() consumeParams is null. Check the productID ";
                    LGLog.d(result_msg);
                    GetInstance().Invoke( result_status, new JSONArray(), result_msg, result_ResponseCode , userCB );
                }
            }
            else
            {
                result_msg = productID + " Consume() mPurchases is null.";
                LGLog.d(result_msg);
                GetInstance().Invoke( result_status, new JSONArray(), result_msg, result_ResponseCode , userCB );
            }
        }
    }

    public static void RestoreProduct( long userCB )
    {
        LGLog.d("RestoreProduct() start" );

        if ( !CheckInitlized( userCB ) ) return;
        final JSONArray productsJsonArray = new JSONArray();
        int loopCnt = 0;

        mBillingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(), new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(BillingResult billingResult, List<com.android.billingclient.api.Purchase> list) {
                LGLog.d("queryPurchasesAsync billingResult.toString() : " + billingResult.toString());
                mPurchases = list;
                for ( com.android.billingclient.api.Purchase PurchasesResult : list ) {

                    try {
                        JSONObject productInfo = new JSONObject();

                        productInfo.put("productId", GetInstance().GetProductIdFromReceipt(PurchasesResult.getOriginalJson()) );
                        productInfo.put("originalJson", PurchasesResult.getOriginalJson() );
                        productInfo.put("signature", PurchasesResult.getSignature() );

                        if ( mProductDetailsListMap != null )
                            productInfo.put("microPrice", Long.toString( mProductDetailsListMap.get(GetInstance().GetProductIdFromReceipt(PurchasesResult.getOriginalJson())).getOneTimePurchaseOfferDetails().getPriceAmountMicros()));

                        if ( mProductDetailsListMap != null )
                            productInfo.put("currencyCode", Objects.requireNonNull( mProductDetailsListMap.get(GetInstance().GetProductIdFromReceipt(PurchasesResult.getOriginalJson())), "mProductDetailsListMap is null").getOneTimePurchaseOfferDetails().getPriceCurrencyCode());

                        productsJsonArray.put(productInfo);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        LGLog.e("RestoreProduct catch!!!!");
                    }
                }
                LGLog.d("queryPurchasesAsync list.toString() : 2");
                GetInstance().Invoke( "NT_SUCCESS" , productsJsonArray, billingResult.getDebugMessage (), billingResult.getResponseCode() , userCB );
            }
        });
    }

    public static void ConsumeAll( long userCB )
    {
        LGLog.i("ConsumeAll()");

        if ( !CheckInitlized( userCB ) ) return;

        mBillingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(), new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(BillingResult billingResult, List<com.android.billingclient.api.Purchase> list) {
                LGLog.d("queryPurchasesAsync billingResult.toString() : " + billingResult.toString());

                for ( com.android.billingclient.api.Purchase PurchasesResult : list ) {

                    LGLog.d("Consume PurchasesResult.toString() : " + PurchasesResult.toString());
                    ConsumeParams consumeParams = ConsumeParams.newBuilder()
                            .setPurchaseToken(PurchasesResult.getPurchaseToken())
                            .build();

                    mBillingClient.consumeAsync( consumeParams, new ConsumeResponseListener() {
                        @Override
                        public void onConsumeResponse(BillingResult billingResult, String s) {

                            if ( billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK )
                            {
                                Debug( PurchasesResult.getSkus() + " Consume() Success. Consume s : " + s );
                            }
                            else
                            {
                                Debug( PurchasesResult.getSkus() + " Consume() Fail. Consume s : " + s + "getResponseCode : " + billingResult.getResponseCode() );
                            }
                        }
                    });
                }
            }
        });
    }

    public static void SaveLocalData( String jKey, String jData )
    {
        LGLog.d("Purchase.java SaveLocalData jKey : " + jKey + " jData : " + jData);
        GetInstance().SetSharedPreferencesData(jKey, jData);
    }

    public static String LoadLocalData( String jKey )
    {
        LGLog.d("Purchase.java LocalData jKey : " + jKey);
        return GetInstance().GetSharedPreferencesData(jKey);
    }

    public static void RemoveLocalData( String jKey )
    {
        LGLog.d("Purchase.java RemoveLocalData jKey : " + jKey);
        GetInstance().RemoveSharedPreferencesData(jKey);
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
            LGLog.e(result_msg);
        }

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

    private static boolean CheckInitlized( long userCB )
    {
        if (mMainActivity == null)
        {
            result_msg = "CheckInitlized() mMainActivity is null.";
            LGLog.e(result_msg);
            result_status = "UNKNOWN";
            result_ResponseCode = -1;
            GetInstance().Invoke( result_status, new JSONArray(), result_msg, result_ResponseCode , userCB );
            return false;
        }

        if ( !IsConnected() )
        {
            result_msg = "Purchase is not ready. First Call Connect() API.";
            LGLog.e(result_msg);
            GetInstance().Invoke( result_status, new JSONArray(), result_msg, result_ResponseCode , userCB );
            return false;
        }
        return true;
    }

    private static boolean IsConnected()
    {
        return mBillingClient != null && mBillingClient.isReady();
    }

    private String GetProductIdFromReceipt(String strReceipt) throws JSONException {

        String resultProductId = "";

        JSONObject jsonObj = new JSONObject(strReceipt);
        if (jsonObj != null)
            resultProductId = jsonObj.getString("productId");

        return resultProductId;
    }

    private void SetSharedPreferencesData(String jKey, String jData)
    {
        SharedPreferences.Editor editor = mMainActivity.getPreferences(Context.MODE_PRIVATE).edit();
        editor.putString(jKey, jData);
        editor.commit();
    }

    private String GetSharedPreferencesData(String jKey)
    {
        SharedPreferences prefs = mMainActivity.getPreferences(Context.MODE_PRIVATE);
        String jsLoadData = prefs.getString(jKey, "");
        LGLog.d("Purchase.java jKey = " + jKey + " jsLoadData : " + jsLoadData);
        return prefs.getString(jKey, "");
    }

    private void RemoveSharedPreferencesData(String sKey)
    {
        SharedPreferences prefs = mMainActivity.getPreferences(Context.MODE_PRIVATE);
        prefs.edit().remove(sKey).commit();
    }

    public void OnResume()
    {
    }

    public void OnStop()
    {
    }

    public void OnDestroy()
    {
        DisConnect();
    }
    //=======================================//    Log    //========================================//
    private static void Debug( String logMessage )
    {
        //GetInstance().LogLineNTLog.d("[" + PURCHASE_TAG + "]: " + logMessage );
    }

    private void LogLineDebug(String str) {

        int limitedline = 3000;
        String temp_json = str;
        int log_index = 1;
        try {
            while (temp_json.length() > 0) {

                if (temp_json.length() > limitedline) {
                    LGLog.d("log - " + log_index + " : "
                            + temp_json.substring(0, limitedline));
                    temp_json = temp_json.substring(limitedline);
                    log_index++;
                } else {
                    LGLog.d(temp_json);
                    break;
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            LGLog.e(result_msg);
        }
    }

    //========================================//    Log    //=========================================//

    //    Not Use 2020/08/06 LSS    //
    public void GetPurchaseHistory()
    {
        LGLog.d("GetPurchaseHistory() start" );

        if ( !CheckInitlized( noCB ) ) return;

        mBillingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, new PurchaseHistoryResponseListener() {
            @Override
            public void onPurchaseHistoryResponse(BillingResult billingResult, List<PurchaseHistoryRecord> list) {

                for (PurchaseHistoryRecord historyPurchase : list)
                {
                    LGLog.d("History purchase getOriginalJson : " + historyPurchase.getOriginalJson());
                    LGLog.d("History purchase getSignature : " + historyPurchase.getSignature());
                }
            }
        });
    }
}
