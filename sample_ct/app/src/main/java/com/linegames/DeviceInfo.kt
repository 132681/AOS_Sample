package com.linegames

import android.app.Activity
import android.os.AsyncTask
import android.provider.Settings
import android.util.Log
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.linegames.base.NTBase
import java.util.*

class DeviceInfo
{
    public constructor()
    {
//        Log.d(TAG, "-------------------------------- DeviceInfo constructor----------------------------- : ")
    }

    companion object
    {
        var GPS_ADID: String? = null
        var AndroidID: String? = null
        var WidevineID: String? = null

        private const val TAG = "NTSDK"
        @JvmStatic val getInstance = DeviceInfo()

        @JvmStatic fun AOSDeviceInfo()
        {
            getInstance.GetDeviceInfo()
        }
    }

    fun GetDeviceInfo( )
    {
        generationAdId();
        getAndroidID();
    }

    @Synchronized
    fun generationAdId()
    {
        AsyncTask.execute {
            try {
                GPS_ADID = AdvertisingIdClient.getAdvertisingIdInfo(NTBase.MainActivity.getApplicationContext()).id
                Log.d(TAG, "---------------- DeviceInfo generationAdId GPS_ADID ---------------- : " + GPS_ADID)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getAndroidID(): String? {

        AndroidID = Settings.Secure.getString(NTBase.MainActivity.getApplicationContext().contentResolver, Settings.Secure.ANDROID_ID)
        Log.d(TAG, "---------------- DeviceInfo getAndroidID AndroidID ---------------- : " + AndroidID)
        return AndroidID
    }

}