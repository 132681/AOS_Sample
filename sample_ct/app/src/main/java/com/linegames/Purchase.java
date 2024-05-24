package com.linegames;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.Nullable;

import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.common.collect.ImmutableList;
import com.linegames.base.NTLog;

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
import com.linegames.base.NTBase;
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

    private static JSONArray email_productsJsonArray = new JSONArray();
    private static String email_receipt = "_SUCCESS";

    private static String email_signature = "NT_SUCCESS";

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
        result_status = "UNKNOWN";
        result_ResponseCode = -1;

        if ( IsConnected() ) {
            result_msg = "PurchaseAPI Already connected.";
            NTLog.e(result_msg);
            result_status = "NT_SUCCESS";
            result_ResponseCode = 0;
            GetInstance().Invoke( result_status, new JSONArray(), result_msg, result_ResponseCode , userCB );
            return;
        }

        mMainActivity = NTBase.getMainActivity();
        if (mMainActivity == null)
        {
            result_msg = "Connect() mMainActivity is null.";
            NTLog.e(result_msg);
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
                GetInstance().Invoke( "PURCHASE_DISCONNEDTED", new JSONArray(), "Purchase DisConnected.", -1 , userCB );
            }
        });
    }

    private void DisConnect()
    {
        if ( IsConnected() )
        {
            mBillingClient.endConnection();
            mBillingClient = null;
            NTLog.i("Connection DisConnect" );
        }
    }

    public static void RegisterProduct( String jProductID )
    {
        if (jProductID.isEmpty())
        {
            NTLog.e("RegisterProduct pID is null");
            return;
        }

        if (productIdArr == null)
            productIdArr = new ArrayList<>();

        if (productIdArr.contains(jProductID))
            return;

        GetInstance().productIdArr.add(jProductID);
        NTLog.d("RegisterProduct pID : " +jProductID );
    }

//    public static void RefreshProductInfo( final long userCB )
//    {
//
//        final ArrayList<String> tempProductsId = productIdArr;
//        final int inputProductsCount = tempProductsId.size();
//        final int defaultCount = 20;
//
//        NTLog.d("RefreshProductInfo() Input ProductId : " + tempProductsId.toString());
//
//        final JSONArray productsJsonArray = new JSONArray();
//        mProductDetailsList = new ArrayList<ProductDetails>();
//        final ArrayList<String> invalidProductsId = tempProductsId;
//        final int[] queryCompleteCounter = new int[] { 0 };
//        final int loopCnt = inputProductsCount / defaultCount;
//        NTLog.d("RefreshProductInfo() loopCnt  " + loopCnt + "= inputProductsCount /" + inputProductsCount + " defaultCount " + defaultCount);
//
//        for(int i = 0 ; i < loopCnt + 1; i++)
//        {
//            int fromIndex = i * defaultCount;
//            int toIndex = Math.min(inputProductsCount, ( i + 1 ) * defaultCount);
//            if( toIndex > inputProductsCount )
//                continue;
//
//            List<String> productsInfo = tempProductsId.subList( fromIndex , toIndex );
//
//            NTLog.d(" ================ i : " + i + "================");
//
//
//            NTLog.d(" fromIndex : " + fromIndex + " toIndex : " + toIndex );
//            for (int j = 0; j < productsInfo.size(); j++){
//                NTLog.d(" productsInfo.size() : " + productsInfo.size() + " j :  " + j + " " + productsInfo.get(j));
//            }
//
////            List<QueryProductDetailsParams.Product> productsInfoParams = new ArrayList<>();
////
////            if (productsInfo.size() < 1)
////                continue;
////
////            for (String pid : productsInfo) {
////                NTLog.d("lss RefreshProductInfo() 1 : ");
////                if (pid == null || pid.isEmpty())
////                    continue;
////
////            }
//
//
//        }
//    }

    public static void RefreshProductInfo( final long userCB )
    {

        final ArrayList<String> tempProductsId = productIdArr;
        final int inputProductsCount = tempProductsId.size();
        final int defaultCount = 20;

        NTLog.d("RefreshProductInfo() Input ProductId : " + tempProductsId.toString());

        final JSONArray productsJsonArray = new JSONArray();
        mProductDetailsList = new ArrayList<ProductDetails>();
        final ArrayList<String> invalidProductsId = tempProductsId;
        final int[] queryCompleteCounter = new int[] { 0 };
        final int loopCnt = inputProductsCount / defaultCount;

        for(int i = 0 ; i < loopCnt + 1; i++)
        {

            int fromIndex = i * defaultCount;
            int toIndex = Math.min(inputProductsCount, ( i + 1 ) * defaultCount);
            if( toIndex > inputProductsCount )
                continue;

            List<String> productsInfo = tempProductsId.subList( fromIndex , toIndex );
            List<QueryProductDetailsParams.Product> productsInfoParams = new ArrayList<>();

            if (productsInfo.size() < 1)
                continue;
            NTLog.d("lss RefreshProductInfo() 0 : ");

            for (String pid : productsInfo) {
                NTLog.d("lss RefreshProductInfo() 1 : ");
                if (pid == null || pid.isEmpty())
                    continue;

                productsInfoParams.add(QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(pid)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build());
            }
            NTLog.d("lss RefreshProductInfo() 3 : ");

            if (productsInfoParams.isEmpty()) {
                NTLog.e("제품 리스트가 비어 있습니다.");
                continue; // 또는 예외 처리 코드를 추가할 수 있습니다.
            }

            QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                    .setProductList(productsInfoParams)
                    .build();

            int endcnt = i;
            mProductDetailsListMap = new HashMap<String, ProductDetails>();

            if ( !CheckInitlized( userCB ) ) return;

            mBillingClient.queryProductDetailsAsync( params, new ProductDetailsResponseListener()
            {
                @Override
                public void onProductDetailsResponse(BillingResult billingResult,
                                                     List<ProductDetails> ProductDetailsList)
                {

                    NTLog.d("billingResult.toString() : " + billingResult.toString() );
                    if ( billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK )
                    {
                        mProductDetailsList.addAll(ProductDetailsList);
                        try {
                            for (ProductDetails product : ProductDetailsList) {
                                //NTLog.d("billingResult.getProductType() : " + product.getProductType() );

                                mProductDetailsListMap.put( product.getProductId(), product );
                                productsJsonArray.put(new JSONObject()
                                        .put("productId", product.getProductId())
                                        .put("title", product.getTitle())
                                        .put("description", product.getDescription())
                                        .put("price", product.getOneTimePurchaseOfferDetails().getFormattedPrice())
                                        .put("microPrice", Long.toString(product.getOneTimePurchaseOfferDetails().getPriceAmountMicros()))
                                        .put("currencyCode", product.getOneTimePurchaseOfferDetails().getPriceCurrencyCode())
                                        .put("type", product.getProductType())
                                );

                                if ( invalidProductsId.contains( product.getProductId() ) )
                                {
                                    invalidProductsId.remove( product.getProductId() );
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            NTLog.e(e.getMessage());
                        }
                    }
                    else
                    {
                        NTLog.d("billingResult.getDebugMessage() : " + billingResult.getDebugMessage() );
                        NTLog.d("billingResult.getResponseCode() : " + billingResult.getResponseCode() );
                        NTLog.d("billingResult.toString() : " + billingResult.toString() );
                    }
                    String resultMsg;
                    int resultCode;

                    synchronized (queryCompleteCounter)
                    {
                        resultCode = billingResult.getResponseCode();
                        if ( queryCompleteCounter[0] == loopCnt )
                        {
                            resultMsg = "Success. productsId Count : " + productsJsonArray.length();
                            if ( productsJsonArray.length() != inputProductsCount )
                            {
                                if (resultCode == 2)
                                    resultMsg = billingResult.getDebugMessage() + "(" + resultCode + ")";
                                else
                                    resultMsg = "Invalid ProductID List (" + resultCode + ") => " + invalidProductsId.toString();

                                NTLog.d("billingResult.getDebugMessage() : " + billingResult.getDebugMessage() );
                                NTLog.d("billingResult.getResponseCode() : " + billingResult.getResponseCode() );

                                result_status = "UNKNOWN";
                                resultCode = billingResult.getResponseCode();
                            }
                            else
                            {
                                result_status = "NT_SUCCESS";
                                resultCode = 0;
                            }

                            GetInstance().Invoke( result_status, productsJsonArray, resultMsg, resultCode , userCB );
                        }
                        queryCompleteCounter[0]++;
                    }
                }
            });
        }
    }

    public static void BuyProduct( String jProductID, String jDeveloperPayload, long lUserCB )
    {

        NTLog.d("BuyProduct productID : " + jProductID + " DeveloperPayload : " + jDeveloperPayload );
        final String productID = jProductID;
        final String developerPayload = jDeveloperPayload;
        final long userCB = lUserCB;

        if ( !CheckInitlized( userCB ) ) return;

        final JSONArray productsJsonArray = new JSONArray();

        if ( productID.isEmpty() ) {
            result_msg = "BuyProduct() productID is null.";
            NTLog.e(result_msg);
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
                            NTLog.d("find productID SkuDetail : " + productID);
                            buyProductSkuDetail = detail;
                            break;
                        }
                        else
                        {
                            NTLog.d("Dont find productID SkuDetail : " + productID);
                        }
                    }
                    if ( buyProductSkuDetail == null || buyProductSkuDetail.getProductId().isEmpty() )
                    {

                        result_msg = "ProductID does no match. Please Check productID ( " + productID + " ) ";
                        NTLog.d(result_msg);
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
                    NTLog.e(result_msg);
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

        NTLog.i("onPurchasesUpdated: $responseCode $debugMessage responseCode " + responseCode);
        switch (responseCode) {
            case BillingClient.BillingResponseCode.OK:
                if (purchases == null) {
                    NTLog.e("onPurchasesUpdated: null purchase list");
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
                            email_receipt = product.getOriginalJson();
                            email_signature = product.getSignature();
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
                                    NTLog.d("Purchase Pending Return. (" + GetInstance().GetProductIdFromReceipt(product.getOriginalJson()) + ")");
                                    GetInstance().RemoveSharedPreferencesData(product.getOrderId());
                                    return;
                                }
                            }
                        }

//                        sendEmailToAdmin();

                    } catch (Exception e) {
                        e.printStackTrace();
                        result_msg = e.getMessage();
                        NTLog.e(result_msg);
                    }
                }

                GetInstance().Invoke( result_status, productsJsonArray, billingResult.getDebugMessage(), billingResult.getResponseCode() , purchaseCB );
                return;

            case BillingClient.BillingResponseCode.USER_CANCELED:
                result_msg = "onPurchasesUpdated: User canceled the purchase.";
                result_status = "USER_CANCELED";
                NTLog.i(result_msg);
                break;
            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                result_status = "ITEM_ALREADY_OWNED";
                result_msg = "onPurchasesUpdated: The user already owns this item";
                NTLog.i(result_msg);
                //Need Consume Process
                break;
            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                result_msg = "onPurchasesUpdated: Developer error means that Google Play " +
                        "does not recognize the configuration. If you are just getting started, " +
                        "make sure you have configured the application correctly in the " +
                        "Google Play Console. The SKU product ID must match and the APK you " +
                        "are using must be signed with release keys.";
                NTLog.i(result_msg);
                break;
            case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
                result_msg = "onPurchasesUpdated: Need Goolge Account Login!!!";
                NTLog.i(result_msg);
                //Need Google Account login
                break;
            case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                result_msg = "Need Test URL confirm.";
                NTLog.i(result_msg);
                //Need Test URL confrim
                break;
            case BillingClient.BillingResponseCode.ERROR:
                result_msg = "This card cannot be paid.";
                result_status = "CREDITCARD_UNAVAILABLE";
                NTLog.i(result_msg);
                //This creditcard cannot be paid.
                break;
        }
        GetInstance().Invoke( result_status, productsJsonArray, result_msg, billingResult.getResponseCode() , purchaseCB );
    }

    public static void sendEmailToAdmin() throws JSONException {
        String title = "Receipt";
//        String[] receivers = new String[]{"god0@line.games"};
        String[] receivers = null;
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_SUBJECT, title);
        email.putExtra(Intent.EXTRA_EMAIL, receivers);
        email.putExtra(Intent.EXTRA_TEXT, String.format("receipt : %s signature : %s ", email_receipt, email_signature ));
        email.setType("message/rfc822");
        NTBase.getMainActivity().startActivity(email);
    }

    public static void Consume( final String productID, final long userCB ) throws JSONException {
        NTLog.d("Consume() productID 1: " + productID );


        if ( !CheckInitlized( userCB ) ) return;

        if ( productID.isEmpty() )
        {
            result_msg = "Consume() productID is NULL.";
            NTLog.e(result_msg);
            GetInstance().Invoke( result_status, new JSONArray(), result_msg, result_ResponseCode , userCB );
        }
        else
        {
            if ( mPurchases != null )
            {
                NTLog.d("Consume() productID 2: " + productID );

                ConsumeParams consumeParams = null;
                for (com.android.billingclient.api.Purchase purchaseData : mPurchases )
                {
                    NTLog.d("Consume() productID 3: " + productID );
                    if ( productID.equals(GetInstance().GetProductIdFromReceipt(purchaseData.getOriginalJson())) )
                    {
                        NTLog.d("Consume() productID getOriginalJson 4: " + purchaseData.getOriginalJson() );
                        NTLog.d("Consume() productID getPurchaseToken 4: " + purchaseData.getPurchaseToken() );
                        NTLog.d("Consume() productID 4: " + productID );
                        consumeParams = ConsumeParams.newBuilder()
                                .setPurchaseToken(purchaseData.getPurchaseToken())
                                .build();
                        NTLog.d("Consume() consumeParams productID : " + GetInstance().GetProductIdFromReceipt(purchaseData.getOriginalJson()) );
                        break;
                    }
                    else {
                        NTLog.d("Consume() consumeParams productID not ");
                    }
                }

                NTLog.d("Consume() productID 5: " + productID );

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
                            NTLog.d(result_msg );
                            GetInstance().Invoke( result_status, productsJsonArray , result_msg, result_ResponseCode , userCB );
                        }
                    });
                }
                else
                {
                    result_msg = productID + " Consume() consumeParams is null. Check the productID ";
                    NTLog.d(result_msg);
                    GetInstance().Invoke( result_status, new JSONArray(), result_msg, result_ResponseCode , userCB );
                }
            }
            else
            {
                result_msg = productID + " Consume() mPurchases is null.";
                NTLog.d(result_msg);
                GetInstance().Invoke( result_status, new JSONArray(), result_msg, result_ResponseCode , userCB );
            }
        }
    }

    public static void RestoreProduct( long userCB )
    {
        NTLog.d("RestoreProduct() start" );

        if ( !CheckInitlized( userCB ) ) return;
        final JSONArray productsJsonArray = new JSONArray();
        int loopCnt = 0;

        if (mProductDetailsListMap == null || mProductDetailsListMap.size() < 1)
        {
            NTLog.e("RestoreProduct() mProductDetailsListMap is null. ");
            GetInstance().Invoke( "UNKNOWN", productsJsonArray, "RestoreProduct is not ready. Call RefreshProductInfo() API.", -1 , userCB );
            return;
        }

        mBillingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(), new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(BillingResult billingResult, List<com.android.billingclient.api.Purchase> list) {
                NTLog.d("queryPurchasesAsync billingResult.toString() : " + billingResult.toString());
                mPurchases = list;
                for ( com.android.billingclient.api.Purchase PurchasesResult : list ) {

                    NTLog.d("RestoreProduct PurchasesResult.toString() : " + PurchasesResult.toString());

                    try {
                        productsJsonArray.put(new JSONObject()
                                .put("productId", GetInstance().GetProductIdFromReceipt(PurchasesResult.getOriginalJson()) )
                                .put("originalJson", PurchasesResult.getOriginalJson() )
                                .put("microPrice", Long.toString(Objects.requireNonNull( mProductDetailsListMap.get(GetInstance().GetProductIdFromReceipt(PurchasesResult.getOriginalJson())), "mProductDetailsListMap is null").getOneTimePurchaseOfferDetails().getPriceAmountMicros()))
                                .put("currencyCode", Objects.requireNonNull( mProductDetailsListMap.get(GetInstance().GetProductIdFromReceipt(PurchasesResult.getOriginalJson())), "mProductDetailsListMap is null").getOneTimePurchaseOfferDetails().getPriceCurrencyCode() )
                                .put("signature", PurchasesResult.getSignature() )
                        );
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        NTLog.e("RestoreProduct catch!!!!");
                    }
                }

                GetInstance().Invoke( "NT_SUCCESS" , productsJsonArray, billingResult.getDebugMessage (), billingResult.getResponseCode() , userCB );
            }
        });
    }

    public static void ConsumeAll( long userCB )
    {
        NTLog.i("ConsumeAll()");

        if ( !CheckInitlized( userCB ) ) return;

        mBillingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(), new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(BillingResult billingResult, List<com.android.billingclient.api.Purchase> list) {
                NTLog.d("queryPurchasesAsync billingResult.toString() : " + billingResult.toString());

                for ( com.android.billingclient.api.Purchase PurchasesResult : list ) {

                    NTLog.d("Consume PurchasesResult.toString() : " + PurchasesResult.toString());
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
        NTLog.d("Purchase.java SaveLocalData jKey : " + jKey + " jData : " + jData);
        GetInstance().SetSharedPreferencesData(jKey, jData);
    }

    public static String LoadLocalData( String jKey )
    {
        NTLog.d("Purchase.java LocalData jKey : " + jKey);
        return GetInstance().GetSharedPreferencesData(jKey);
    }

    public static void RemoveLocalData( String jKey )
    {
        NTLog.d("Purchase.java RemoveLocalData jKey : " + jKey);
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
            NTLog.e(result_msg);
        }

        if (userCB != noCB)
        {
            NTLog.d("Invoke() status : " + result_status + " msg : " + jsonResult.toString() + " userCB : " + userCB  );
            nativeCB( result_status, jsonResult.toString(), userCB );
        }
    }

    private static boolean CheckInitlized( long userCB )
    {
        if (mMainActivity == null)
        {
            result_msg = "CheckInitlized() mMainActivity is null.";
            NTLog.e(result_msg);
            result_status = "UNKNOWN";
            result_ResponseCode = -1;
            GetInstance().Invoke( result_status, new JSONArray(), result_msg, result_ResponseCode , userCB );
            return false;
        }

        if ( !IsConnected() )
        {
            result_msg = "Purchase is not ready. First Call Connect() API.";
            NTLog.e(result_msg);
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
        NTLog.d("Purchase.java jKey = " + jKey + " jsLoadData : " + jsLoadData);
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

    public void OnDestory()
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
                    NTLog.d("log - " + log_index + " : "
                            + temp_json.substring(0, limitedline));
                    temp_json = temp_json.substring(limitedline);
                    log_index++;
                } else {
                    NTLog.d(temp_json);
                    break;
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            NTLog.e(result_msg);
        }
    }

    //========================================//    Log    //=========================================//

    //    Not Use 2020/08/06 LSS    //
    public void GetPurchaseHistory()
    {
        NTLog.d("GetPurchaseHistory() start" );

        if ( !CheckInitlized( noCB ) ) return;

        mBillingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, new PurchaseHistoryResponseListener() {
            @Override
            public void onPurchaseHistoryResponse(BillingResult billingResult, List<PurchaseHistoryRecord> list) {

                for (PurchaseHistoryRecord historyPurchase : list)
                {
                    NTLog.d("History purchase getOriginalJson : " + historyPurchase.getOriginalJson());
                    NTLog.d("History purchase getSignature : " + historyPurchase.getSignature());
                }
            }
        });
    }
}
