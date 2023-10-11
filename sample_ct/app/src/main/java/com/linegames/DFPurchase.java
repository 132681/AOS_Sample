package com.linegames;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
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
import com.linegames.base.NTBase;
import com.linegames.base.NTLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class DFPurchase
{
    // Billing
    public static boolean m_isPurchasingComplete1;
    public static boolean m_isPurchasingComplete2;
    public static boolean m_isPurchasingSentToServer;
    public static int m_isPurchasingResponseCode;
    public static boolean m_isPurchasingSucceed;
    private static HashSet<String> m_registeredSkuList;
//    private static HashMap<String, JSONObject> m_skuInfo;
    private static HashMap<String, ProductDetails> m_skuDetailInfo;
    private static BillingClient m_billingClient;
    private static List<com.android.billingclient.api.Purchase> m_restoreList;

    private static final String TAG = "ModeratoJava";

    private static com.linegames.DFPurchase getInstance = null;
    public static synchronized com.linegames.DFPurchase GetInstance() {
        if (getInstance == null) {
            synchronized ( com.linegames.DFPurchase.class ) {
                if ( getInstance == null )
                    getInstance = new com.linegames.DFPurchase();
            }
        }
        return getInstance;
    }

    public static void Connect()
    {
        m_registeredSkuList = new HashSet<>();
        m_registeredSkuList.clear();
        m_skuDetailInfo = new HashMap<String, ProductDetails>();
        m_skuDetailInfo.clear();

        // Google Play Billing
        PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener()
        {
            @Override
            public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<com.android.billingclient.api.Purchase> list) {

                if( billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK )
                {
                    m_isPurchasingComplete1 = true;
                    m_isPurchasingComplete2 = true;
                    m_isPurchasingSucceed = true;
                    Log.i(TAG, "Billing Updated Success 111");
                    LoadRestoreInfo();
                    return;
                }
                else if( billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED )
                {
                    Log.i(TAG, "Billing Cancel");
                }
                else
                {
                    Log.i(TAG, "Billing Code" + billingResult.getResponseCode() );
                }

                m_isPurchasingComplete1 = true;
                m_isPurchasingComplete2 = true;
                m_isPurchasingSentToServer = false;
                m_isPurchasingResponseCode = -1;
                m_isPurchasingSucceed = false;
            }

        };

        m_billingClient = BillingClient.newBuilder(NTBase.getMainActivity())
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        if( m_billingClient != null )
        {
            Log.i(TAG, "BillingClient Build");
        }
        else
        {
            Log.i(TAG, "BillingClient failed");
        }

        m_billingClient.startConnection(new BillingClientStateListener()
        {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult)
            {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK)
                {
                    Log.i(TAG, "Billing Connect Success ");
                    LoadRestoreInfo();
                }
                else
                {
                    Log.i(TAG, "Billing Connect Failed");
                }
            }

            @Override
            public void onBillingServiceDisconnected()
            {
                Log.i(TAG, "Billing Disconnected");
            }
        });
    }

    static public float getProductDecimalPrice(String productName)
    {
        //Log.i(TAG, "getProductDecimalPrice " + productName);
        ProductDetails cachedProductInfo = m_skuDetailInfo.get(productName);
        if (cachedProductInfo != null)
        {
            try {
                //Log.i(TAG, "ProductDecimalPrice is already cached " + productName);
                String priceMicro = Long.toString(cachedProductInfo.getOneTimePurchaseOfferDetails().getPriceAmountMicros());
                String currency = cachedProductInfo.getOneTimePurchaseOfferDetails().getPriceCurrencyCode();
                float priceMicroFloat = Float.parseFloat(priceMicro);
                float price = priceMicroFloat / 1000000;
                return price;
            }
            catch (Exception e)
            {
            }
        }
        return 0.0f;
    }

    static public String getProductPrice(String productName)
    {
        //Log.i(TAG, "getProductCurrency " + productName);
        ProductDetails cachedProductInfo = m_skuDetailInfo.get(productName);
        if (cachedProductInfo != null)
        {
            try {
                //Log.i(TAG, "getProductPrice is already cached " + productName);
                String price = cachedProductInfo.getOneTimePurchaseOfferDetails().getFormattedPrice();
                return price;
            }
            catch (Exception e)
            {
                return "Error";
            }
        }

        return "Error";
    }

    static public String getProductCurrency(String productName)
    {
        //Log.i(TAG, "getProductCurrency " + productName);
        ProductDetails cachedProductInfo = m_skuDetailInfo.get(productName);
        if (cachedProductInfo != null)
        {
            try
            {
                String currency = cachedProductInfo.getOneTimePurchaseOfferDetails().getPriceCurrencyCode();
                return currency;
            }
            catch (Exception e)
            {
                return "Error";
            }
        }

        return "Error";
    }

    static public void registerStoreProductID(String productName)
    {
        Log.i(TAG, "registerStoreProductID " + productName);
        m_registeredSkuList.add(productName);
    }

    static public void refreshStoreProductInfo()
    {
        if (m_registeredSkuList.size() > 0)
        {
            if (m_registeredSkuList.size() == m_skuDetailInfo.size())
            {
                return;
            }
        }

        Log.i(TAG, "refreshStoreProductInfo do query!!");

        ArrayList<String> invalidProductsId = new ArrayList<String>();
        for (String s : m_registeredSkuList)
            invalidProductsId.add(s);

        Log.i(TAG, "refreshStoreProductInfo do query!! :" + invalidProductsId.size());

        List<QueryProductDetailsParams.Product> productsInfoParams = new ArrayList<>();
        for (String pid : invalidProductsId) {
            productsInfoParams.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(pid)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build());
            Log.i(TAG, "pid :" + pid);
        }

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productsInfoParams)
                .build();

        Log.i(TAG, "refreshStoreProductInfo queryProductDetailsAsync ");

        m_billingClient.queryProductDetailsAsync( params, new ProductDetailsResponseListener() {
            @Override
            public void onProductDetailsResponse(BillingResult billingResult,
                                                 List<ProductDetails> ProductDetailsList) {


                Log.i(TAG, "refreshStoreProductInfo onProductDetailsResponse " + billingResult.getResponseCode());
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK)
                {
                    Log.i(TAG, "refreshStoreProductInfo onProductDetailsResponse Success product size : "+ ProductDetailsList.size());
                }
                else
                {
                    Log.i(TAG, "refreshStoreProductInfo onProductDetailsResponse Fail");
                }

                for (ProductDetails product: ProductDetailsList) {
                    try
                    {
//                        JSONObject object = new JSONObject();
//                        m_skuInfo.put(productId,object);
                        String productId   = product.getProductId();
                        m_skuDetailInfo.put(productId, product);
                        Log.i(TAG, "refreshStoreProductInfo getSkuDetails : " + product.toString());

                    }
                    catch (Exception e)
                    {
                        Log.i(TAG, "refreshStoreProductInfo getSkuDetails failed : " + e.toString());
                        return;
                    }

                }
            }
        });
    }

    static public void requestPurchaseToGooglePlay(String productName)
    {
        //Log.i(TAG, "requestPurchaseToGooglePlay : " + productName);

        m_isPurchasingComplete1 = false;
        m_isPurchasingComplete2 = false;
        m_isPurchasingSentToServer = false;
        m_isPurchasingResponseCode = -1;
        m_isPurchasingSucceed = false;

        //BillingFlowParams

        ProductDetails skuDetails = m_skuDetailInfo.get(productName);
        if( skuDetails != null )
        {

            ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                    ImmutableList.of(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(skuDetails)
                                    .build()
                    );

            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build();


            BillingResult responseCode = m_billingClient.launchBillingFlow(NTBase.getMainActivity(), billingFlowParams);
            if( responseCode.getResponseCode() != BillingClient.BillingResponseCode.OK )
            {
                m_isPurchasingComplete1 = true;
                m_isPurchasingComplete2 = true;
                m_isPurchasingSentToServer = false;
                m_isPurchasingResponseCode = -1;
                m_isPurchasingSucceed = false;
                return;
            }
        }
        else
        {
            Log.i(TAG, "Billing Not Find Item " + productName);
        }

    }


    static public int isPurchasingSentToServer()
    {
        if (m_isPurchasingSentToServer)
            return 1;
        return 0;
    }

    static public int isCompletedToPurchaseFromGooglePlay()
    {
        if (m_isPurchasingComplete1 && m_isPurchasingComplete2)
            return 1;
        return 0;
    }

    static public int isSucceedToPurchaseFromGooglePlay()
    {
        if (m_isPurchasingComplete1)
        {
            if (m_isPurchasingComplete2)
            {
                if (m_isPurchasingSucceed)
                {
                    return 1;
                }
            }
        }
        return 0;
    }

    static public int getPurchaseResponseCodeFromGooglePlay()
    {
        return m_isPurchasingResponseCode;
    }

    static public int IsDisconnectedStore()
    {
        return 0;
    }

    public static void IAP_TrackPurchaseWithStoreProductName(String productId,float price,String currency)
    {
        //Log.i(TAG,"IAP_TrackPurchaseWithStoreProductName " + productId + price + currency);
        //AppEventsLogger logger = AppEventsLogger.newLogger(MainActivity.m_this);
        //
        //Bundle parameters = new Bundle();
        //parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currency);
        //parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product");
        //parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, productId);
        //
        //logger.logEvent(AppEventsConstants.EVENT_NAME_PURCHASED, price, parameters);
    }

    public static String IAP_GetProductName() throws JSONException {

        for(int i=0; i<m_restoreList.size(); ++i )
        {
            String resultProductId = "";

            JSONObject jsonObj = new JSONObject(m_restoreList.get(i).getOriginalJson());
            if (jsonObj != null)
                resultProductId = jsonObj.getString("productId");

            return resultProductId;
        }

        return "";
    }

    public static String IAP_GetPurchaseData()
    {
        for(int i=0; i<m_restoreList.size(); ++i )
        {
            return m_restoreList.get(i).getOriginalJson();
        }

        return "";
    }

    public static String IAP_GetSignature()
    {
        for(int i=0; i<m_restoreList.size(); ++i )
        {
            return m_restoreList.get(i).getSignature();
        }

        return "";
    }

    public static int IAP_IsExistReceipt()
    {
        if( m_restoreList.size() > 0 )
            return 1;
        else
            return 0;
    }

    public static void IAP_RemoveReceipt()
    {
        m_billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(), new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(BillingResult billingResult, List<com.android.billingclient.api.Purchase> list) {
                NTLog.d("queryPurchasesAsync billingResult.toString() : " + billingResult.toString());
                if( billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK )
                {
                    return;
                }

                for ( com.android.billingclient.api.Purchase PurchasesResult : list ) {

                    NTLog.d("Consume PurchasesResult.toString() : " + PurchasesResult.toString());
                    ConsumeParams consumeParams = ConsumeParams.newBuilder()
                            .setPurchaseToken(PurchasesResult.getPurchaseToken())
                            .build();

                    m_billingClient.consumeAsync( consumeParams, new ConsumeResponseListener() {
                        @Override
                        public void onConsumeResponse(BillingResult billingResult, String s) {

                            if ( billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK )
                            {
                                Log.i(TAG, "Successfully consumed token: " + s);
                                LoadRestoreInfo();
                            }
                            else
                            {
                                Log.i(TAG, "failed consumed token: " + s);
                            }
                        }
                    });
                }

            }
        });
    }

    public static void LoadRestoreInfo()
    {
        m_billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(), new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(BillingResult billingResult, List<com.android.billingclient.api.Purchase> list) {
                m_restoreList = list;
                NTLog.d("LoadRestoreInfo queryPurchasesAsync count : " + m_restoreList.size());
            }
        });
    }

    private void DisConnect()
    {
        if ( m_billingClient != null && m_billingClient.isReady() )
        {
            m_billingClient.endConnection();
            m_billingClient = null;
            NTLog.i("Connection DisConnect" );
        }
    }

}
