package com.linegames;

import android.util.Log;

import com.linegames.base.LGLog;
import com.linegames.ct2.MainActivity;
import com.linegames.ct2.UserAction;

import org.json.JSONException;

import java.util.List;

public class PurchaseManager {
    private static String LOG_TAG = "NTSDK";
    private static String PURCHASE_TAG = "PurchaeAPI";
    private static PurchaseManager getInstance = null;
    public static synchronized PurchaseManager GetInstance() {
        if (getInstance == null) {
            synchronized ( PurchaseManager.class ) {
                if ( getInstance == null )
                    getInstance = new PurchaseManager();
            }
        }
        return getInstance;
    }

    public static UserAction.storeList storeType;
    public UserAction.storeList getStoreType() {
        return storeType;
    }

    // setter 메서드
    public void setStoreType(UserAction.storeList storeType) {
        this.storeType = storeType;
    }

    public void Connect( final String jProductMode, final long userCB )
    {
        LGLog.d("Connect ========================" );

        switch (storeType) {
            case GOOGLE:
                Purchase.GetInstance().Connect("", userCB);
                break;
            case ONESTORE:
                PurchaseOneStore.GetInstance().Connect("", userCB);
                break;
            case GALAXY:
                PurchaseGalaxy.GetInstance().Connect("", userCB);
                break;
            default:
                System.out.println("PurchaseManager Connect Unknown store ( " + storeType.toString() + " )" );
                break;
        }

    }

    private void DisConnect()
    {
    }

    public void RegisterProduct( String jProductID )
    {
        switch (storeType) {
            case GOOGLE:
                Purchase.GetInstance().RegisterProduct(jProductID);
                break;
            case ONESTORE:
                PurchaseOneStore.GetInstance().RegisterProduct(jProductID);
                break;
            case GALAXY:
                PurchaseGalaxy.GetInstance().RegisterProduct(jProductID);
                break;
            default:
                System.out.println("PurchaseManager RegisterProduct Unknown store ( " + storeType.toString() + " )" );
                break;
        }
    }

    public void RefreshProductInfo( final long userCB )
    {
        switch (storeType) {
            case GOOGLE:
                Purchase.GetInstance().RefreshProductInfo(userCB);
                break;
            case ONESTORE:
                PurchaseOneStore.GetInstance().RefreshProductInfo(userCB);
                break;
            case GALAXY:
                PurchaseGalaxy.GetInstance().RefreshProductInfo(userCB);
                break;
            default:
                System.out.println("PurchaseManager RefreshProductInfo Unknown store ( " + storeType.toString() + " )" );
                break;
        }
    }

    public void BuyProduct( String jProductID, String jDeveloperPayload, long userCB )
    {
        switch (storeType) {
            case GOOGLE:
                Purchase.GetInstance().BuyProduct(jProductID, jProductID, userCB);
                break;
            case ONESTORE:
                PurchaseOneStore.GetInstance().BuyProduct(jProductID, jProductID, userCB);
                break;
            case GALAXY:
                PurchaseGalaxy.GetInstance().BuyProduct(jProductID, jProductID, userCB);
                break;
            default:
                System.out.println("PurchaseManager BuyProduct Unknown store ( " + storeType.toString() + " )" );
                break;
        }
    }


    public void Consume( final String productID, final long userCB ) throws JSONException {

        switch (storeType) {
            case GOOGLE:
                Purchase.GetInstance().Consume(productID, userCB);
                break;
            case ONESTORE:
                PurchaseOneStore.GetInstance().Consume(productID, userCB);
                break;
            case GALAXY:
                PurchaseGalaxy.GetInstance().Consume(productID, userCB);
                break;
            default:
                System.out.println("PurchaseManager BuyProduct Unknown store ( " + storeType.toString() + " )" );
                break;
        }
    }

    public void RestoreProduct( long userCB )
    {
        switch (storeType) {
            case GOOGLE:
                Purchase.GetInstance().RestoreProduct(userCB);
                break;
            case ONESTORE:
                PurchaseOneStore.GetInstance().RestoreProduct(userCB);
                break;
            case GALAXY:
                PurchaseGalaxy.GetInstance().RestoreProduct(userCB);
                break;
            default:
                System.out.println("PurchaseManager BuyProduct Unknown store ( " + storeType.toString() + " )" );
                break;
        }

    }

    public void ConsumeAll( long userCB ) throws JSONException {
        switch (storeType) {
            case GOOGLE:
                Purchase.GetInstance().ConsumeAll(userCB);
                break;
            case ONESTORE:
                PurchaseOneStore.GetInstance().ConsumeAll(userCB);
                break;
            case GALAXY:
                PurchaseGalaxy.GetInstance().ConsumeAll(userCB);
                break;
            default:
                System.out.println("PurchaseManager BuyProduct Unknown store ( " + storeType.toString() + " )" );
                break;
        }
    }
}
