package com.linegames;

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences

//import android.os.AsyncTask

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.ads.identifier.AdvertisingIdClient.isAdvertisingIdProviderAvailable
import androidx.ads.identifier.AdvertisingIdInfo
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.AdjustEvent
import com.adjust.sdk.LogLevel
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures.addCallback

import com.linegames.base.NTBase
import com.linegames.base.NTLog
import org.checkerframework.checker.nullness.compatqual.NullableDecl
import java.util.concurrent.Executors

@Suppress("DEPRECATION")
class NTAdjust
{
    var GPS_ADID: String? = null
    private var sharedPref : SharedPreferences

    public constructor()
    {
        NTLog.d("=============================== constructor constructor =============================== 1" )
        generationAdId();
        sharedPref = NTBase.MainActivity.getApplicationContext().getSharedPreferences("adjust",Context.MODE_PRIVATE);
    }

    companion object
    {
        val instance = NTAdjust()

        @JvmStatic external fun InitReciever( sAdid : String?, AdjustAdid : String? )
        @JvmStatic fun Init( sAppToken :String, sTrackerToken :String?, sEnvironment :String , sSecreteId :Long , sInfo1 :Long , sInfo2 :Long , sInfo3 :Long , sInfo4 :Long )
        {
            instance.AdjustInit(sAppToken, sTrackerToken,  sEnvironment , sSecreteId, sInfo1, sInfo2, sInfo3, sInfo4)
        }

        @JvmStatic fun TrackEvent( sEventToken : String, sNid : String, sGNid : String, sGameServerID : String, sOrderId : String?, sCurrencyType : String?, Amount : Double )
        {
            instance.AdjustTrackEvent(sEventToken, sNid, sGNid, sGameServerID, sOrderId, sCurrencyType, Amount)
        }

        @JvmStatic fun GetAdjustAdid() : String?
        {
            var sAdjustata: String? = null
            sAdjustata = instance.LoadAdjustData("sAdjust")
            NTLog.d("LoadAdjustData : " + sAdjustata )
            return instance.GetAdjust_Adid()
        }

        @JvmStatic fun GetAdid() : String?
        {
            instance.SaveAdjustData("sAdjust","sAdjustData1")
            NTLog.d("SaveAdjustData sAdjust : sAdjust " + "sAdjustData : sAdjustData1" )
            return instance.GetGPSAdid()
        }

        @JvmStatic fun IsInitAdjust() : Boolean = instance.IsInit_Adjust()
    }

//    private fun determineAdvertisingInfo() {
//        if (isAdvertisingIdProviderAvailable(NTBase.MainActivity)) {
//            val advertisingIdInfoListenableFuture =
//                androidx.ads.identifier.AdvertisingIdClient.getAdvertisingIdInfo(NTBase.MainActivity)
//            addCallback(advertisingIdInfoListenableFuture,
//                object : FutureCallback<AdvertisingIdInfo> {
//                    fun onSuccess(@NullableDecl result: AdvertisingIdInfo) {
//                        val myAdvertisementId = result.id
//                        Log.d(
//                            "MY_APP_TAG",
//                            "myAdvertisementId :$myAdvertisementId"
//                        )
//                    }
//
//                    override fun onFailure(t: Throwable) {
//                        Log.e(
//                            "MY_APP_TAG",
//                            "Failed to connect to Advertising ID provider."
//                        )
//                    }
//                }, Executors.newSingleThreadExecutor()
//            )
//        } else {
//            Log.d(
//                "MY_APP_TAG",
//                "AdvertisingIdClient.isAdvertisingIdProviderAvailable is false"
//            )
//        }
//    }



    fun AdjustInit(appToken: String, sTrackerToken: String?, environment: String, secretId: Long, info1: Long, info2: Long, info3: Long, info4: Long)
    {
        NTLog.i("AdjustSdkVersion() : " + Adjust.getSdkVersion());
        NTLog.d("AdjustInit appToken : " + appToken + " sTrackerToken : " + sTrackerToken + " environment : " + environment + " secretId : " + secretId+ " info1 : " + info1+ " info2 : " + info2+ " info3 : " + info3+ "info4 : " + info4);

        val setAdjustLogOFF : Boolean = false

        val config = AdjustConfig(NTBase.MainActivity, appToken, environment, setAdjustLogOFF)

        if (!sTrackerToken.isNullOrEmpty())
            config.setDefaultTracker(sTrackerToken)

        if(setAdjustLogOFF){
            config.setLogLevel(LogLevel.SUPRESS)//log off
        } else {
            config.setLogLevel(LogLevel.VERBOSE)//log on
        }

        config.setOnAttributionChangedListener {
            NTLog.d("--------------- Attribution callback called! ---------------- : " + it.adid)
            InitReciever( GPS_ADID, it.adid )
        }

        // Set session success tracking delegate.
        config.setOnSessionTrackingSucceededListener { sessionSuccessResponseData ->
            NTLog.d("---------------- sessionSuccessResponseData.adid ---------------- : " + sessionSuccessResponseData.adid)
            NTLog.d("---------------- GPS_ADID ---------------- : " + GPS_ADID)
            InitReciever( GPS_ADID, sessionSuccessResponseData.adid )
        }

        // Set session failure tracking delegate.
        config.setOnSessionTrackingFailedListener {
            NTLog.d("---------------- Session failure callback called! ---------------- : " + it.adid)
        }
        // Allow to send in the background.
        config.setSendInBackground(true)

        if(secretId > 0 && info1 > 0 && info2 > 0 && info3 > 0 && info4 > 0)
        {
            config.setAppSecret(secretId, info1, info2, info3, info4)
        }

        Adjust.onCreate(config)

        AdjustLifecycleCallbacks()

        Adjust.onResume()

    }

    fun AdjustTrackEvent(sEventToken : String, sNid : String, sGNid : String, sGameServerID : String, sOrderID : String?, sCurrencyType : String?, sAmount : Double)
    {
        NTLog.d("NTAdjust.kt AdjustTrackEvent eventToken : " + sEventToken + " sNid : " + sNid+ " sGNid : " + sGNid  + " sGameServerID : " + sGameServerID + " sOrderID : " + sOrderID  + " sCurrencyType : " + sCurrencyType  + " sAmount : " + sAmount )

        val event = AdjustEvent(sEventToken)
        if(!TextUtils.isEmpty(sNid)) event.setCallbackId(sNid)
        if(!TextUtils.isEmpty(sNid)) event.addCallbackParameter("nid", sNid )
        if(!TextUtils.isEmpty(sNid)) event.addPartnerParameter("nid", sNid )
        if(!TextUtils.isEmpty(sGNid)) event.addCallbackParameter("gnid", sGNid )
        if(!TextUtils.isEmpty(sGNid)) event.addPartnerParameter("gnid", sGNid )
        if(!TextUtils.isEmpty(sGameServerID)) event.addCallbackParameter("server_id", sGameServerID )
        if(!TextUtils.isEmpty(sGameServerID)) event.addPartnerParameter("server_id", sGameServerID )
        if(!TextUtils.isEmpty(sOrderID)) event.setOrderId(sOrderID)
        if(!TextUtils.isEmpty(sCurrencyType)) event.setRevenue(sAmount, sCurrencyType)
        Adjust.trackEvent(event)
    }

    /** Retrieve the Android Advertising Id
     *
     * The device must be KitKat (4.4)+
     * This method must be invoked from a background thread.
     *
     */
    @Synchronized
    fun generationAdId()
    {
        @Suppress("DEPRECATION")
        android.os.AsyncTask.execute {
            try {
                GPS_ADID = AdvertisingIdClient.getAdvertisingIdInfo(NTBase.MainActivity.getApplicationContext()).id
                NTLog.d("=============================== constructor constructor =============================== 1 GPS_ADID" + GPS_ADID )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun GetGPSAdid() : String?
    {
        NTLog.d("=============================== constructor constructor =============================== 2" )
        return GPS_ADID
    }

    fun GetAdjust_Adid() : String?
    {
        var AdjustAdid = Adjust.getAdid()
        return AdjustAdid
    }

    fun LoadAdjustData(sAdjustKey : String) : String?
    {
        NTLog.d("NTAdjust.kt LoadAdjustData sAdjustKey : " + sAdjustKey )
        return sharedPref.getString(sAdjustKey, "")
    }

    fun SaveAdjustData(sAdjustKey : String, sAdjustData : String)
    {
        NTLog.d("NTAdjust.kt SaveAdjustData sAdjustData : " + sAdjustData + "sAdjustData : " + sAdjustData )
        var editor = sharedPref.edit()
        editor.putString(sAdjustKey, sAdjustData)
        editor.commit()
    }

    fun IsInit_Adjust() : Boolean
    {
        if (GetAdjust_Adid() == null)
            return false;
        else
            return true;
    }

    fun SetOfflineMode(offLineModeEnable : Boolean)
    {
        Adjust.setOfflineMode(offLineModeEnable)
    }

    fun SetEnableDisableSDK(enableDisableSDK : Boolean)
    {
        Adjust.setEnabled(enableDisableSDK)
    }

    // You can use this class if your app is for Android 4.0 or higher
    private class AdjustLifecycleCallbacks : Application.ActivityLifecycleCallbacks
    {
        override fun onActivityResumed(activity: Activity) { Adjust.onResume() }
        override fun onActivityPaused(activity: Activity) { Adjust.onPause() }
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}
    }

}