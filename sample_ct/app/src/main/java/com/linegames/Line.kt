package com.linegames;

//import com.linecorp.common.android.growthy.GrowthyManager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.facebook.FacebookSdk.getApplicationContext
import com.linecorp.linesdk.BuildConfig.VERSION_NAME
import com.linecorp.linesdk.LineApiResponseCode
import com.linecorp.linesdk.Scope
import com.linecorp.linesdk.api.LineApiClient
import com.linecorp.linesdk.api.LineApiClientBuilder
import com.linecorp.linesdk.auth.LineAuthenticationParams
import com.linecorp.linesdk.auth.LineLoginApi
import com.linegames.base.NTBase
import com.linegames.base.NTLog
import org.json.JSONObject
import java.util.*

class Line {
    companion object {
        val TAG = "NTSDK"
        val LINE_LOGIN_REQUEST_CODE = 0x0FF
        var LOGIN_CB = 0L
        var LINE_CHANNEL_ID = ""

        enum class Status {
            SUCCESS,
            UNKNOWN,
            CANCEL,
            SERVER_ERROR,
            NETWORK_ERROR,
        }

        @JvmStatic
        val getInstance = Line()

        val lineApiClient: LineApiClient by lazy()
        {
            Log.d(TAG, "======================Line API =====================================" + VERSION_NAME)
            val apiClientBuilder = LineApiClientBuilder(NTBase.MainActivity, LINE_CHANNEL_ID)
            apiClientBuilder.build()
        }

        @JvmStatic
        fun Init(userCB: Long) {

        }

        @JvmStatic
        fun Login(channel_ID: String, userCB: Long) {
            if (channel_ID.isNullOrEmpty()) {
                Log.d(TAG, "Channel_ID is null.")

            }
            LINE_CHANNEL_ID = channel_ID
            LOGIN_CB = userCB
            Log.d(TAG, "Line.kt Login LineChannel : " + LINE_CHANNEL_ID + " VERSION_NAME : " + VERSION_NAME )
            ConnectPermission()

        }

        @JvmStatic
        fun Logout() {
            Log.d(TAG, "Line.kt Logout ")

        }

        @JvmStatic
        fun Refresh(userCB: Long) {
            Log.d(TAG, "Line.kt Refresh ")

        }

        @JvmStatic
        fun Verify(userCB: Long) {
            Log.d(TAG, "Line.kt Verify ")

        }

        @JvmStatic
        fun Profile(userCB: Long) {
            Log.d(TAG, "Line.kt Profile ")

        }


        fun OnResume() {
            Log.d(TAG, "Line.kt OnResume()")


        }

        fun OnStop() {
            Log.d(TAG, "Line.kt OnStop()")

        }

        fun ConnectPermission() {

            if (ActivityCompat.checkSelfPermission(
                    NTBase.MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    NTBase.MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
                Log.d("TAG","AndroidPermission ShowPermission() userCB : request")
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                    NTBase.MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                Log.d("TAG","AndroidPermission ShowPermission() userCB : shouldShowRequestPermissionRationale")
            } else {
                Log.d("TAG","AndroidPermission ShowPermission() userCB : GRANTED")
            }
        }

        fun LineOnActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            Log.d(
                TAG,
                "LineOnActivityResult " + " requestCode : " + requestCode + " resultCode : " + resultCode
            )
            if (requestCode == 0x0FF) {
                val result = LineLoginApi.getLoginResultFromIntent(data)
                when (result.responseCode) {
                    LineApiResponseCode.SUCCESS -> {   // Login successful
                        Log.d(TAG, "result : " + result.toString())

                    }

                    LineApiResponseCode.CANCEL -> {   // Login canceled by user
                        Log.d("NTSDK", "LineLogin Canceled by user. " + result.errorData.toString())

                    }

                    LineApiResponseCode.NETWORK_ERROR -> {   // Login canceled by user
                        Log.d("NTSDK", "LineLogin NETWORK_ERROR. " + result.errorData.toString())

                    }

                    LineApiResponseCode.SERVER_ERROR -> {   // Login canceled by user
                        Log.d("NTSDK", "LINE Login SERVER_ERROR. " + result.errorData.toString())

                    }

                    else -> {   // Login canceled due to other error
                        Log.e("NTSDK", "Login FAILED! " + result.errorData.toString())


                    }
                }
            }
        }

        private fun LineLogin(onResult: (Status, String) -> Unit) {
            if (IsInValidParameter(onResult)) return
            Thread(Runnable {
                lineApiClient.verifyToken().let { verify ->
                    if (verify.isSuccess) {
                        Log.d( TAG,"LineLogin Success. responseData : " + verify.responseData.toString() )

                        onResult(Status.SUCCESS, JSONObject().apply {
                            put("userID", lineApiClient.profile.responseData.userId)
                            put("accessToken",
                                lineApiClient.currentAccessToken.responseData.tokenString
                            )
                            put("displayName", lineApiClient.profile.responseData.displayName)
                            put("pictureUrl", lineApiClient.profile.responseData.pictureUrl)
                            put("statusMessage", lineApiClient.profile.responseData.statusMessage)
                            put("Message", "LineLogin Success!!!")
                        }.toString())
                    } else {
                        try {
                            @Suppress("SENSELESS_COMPARISON")
                            if (verify.responseData != null) //verify.reponseData가 있으면 Refresh
                            {
                                Line_AccessToken_Refresh(onResult)
                            }
                        } catch (e: Exception) {
                            FirstLogin(onResult)  //verify.reponseData 가없으면 FirstLogin
                        }
                    }
                }
            }).start()
        }

        fun FirstLogin(onResult: (Status, String) -> Unit) {
            Log.d(TAG, "FirstLogin LINE_CHANNEL_ID : " + LINE_CHANNEL_ID)
            if (IsInValidParameter(onResult)) return
            Thread(Runnable {
                try {
                    val loginIntent = LineLoginApi.getLoginIntent(
                        NTBase.MainActivity,
                        LINE_CHANNEL_ID,
                        LineAuthenticationParams.Builder()
                            .scopes(Arrays.asList(Scope.PROFILE))
                            .build()
                    )
                    ActivityCompat.startActivityForResult(
                        NTBase.MainActivity,
                        loginIntent,
                        LINE_LOGIN_REQUEST_CODE,
                        null
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "FirstLogin Exception error: " + e.toString())
                    onResult(
                        Status.UNKNOWN,
                        JSONObject().apply { put("Message", e.toString()) }.toString()
                    )
                }
            }).start()
        }

        private fun LineLogout(onResult: (Status, String) -> Unit) {
            if (IsInValidParameter(onResult)) return

            Thread(Runnable {
                try {
                    lineApiClient.logout().run {
                        if (isSuccess) {
                            Log.d(TAG, "LineLogout Success!")
                        } else {
                            Log.d(TAG, "LineLogout Fail erroData : " + errorData.toString())
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "LineLogout Exception error : " + e.toString())
                }
            }).start()
        }

        private fun LineRefresh(onResult: (Status, String) -> Unit) {
            Line_AccessToken_Refresh(onResult)
        }

        private fun Line_AccessToken_Refresh(onResult: (Status, String) -> Unit) {
            if (IsInValidParameter(onResult)) return
            Thread(Runnable {
                try {
                    lineApiClient.refreshAccessToken().let { refresh ->
                        if (refresh.isSuccess) {
                            Log.d(TAG, "LineRefresh Success!")
                            onResult(Status.SUCCESS, JSONObject().apply {
                                put("userID", lineApiClient.profile.responseData.userId)
                                put("accessToken", refresh.responseData.tokenString)
                                put("displayName", lineApiClient.profile.responseData.displayName)
                                put("pictureUrl", lineApiClient.profile.responseData.pictureUrl)
                                put(
                                    "statusMessage",
                                    lineApiClient.profile.responseData.statusMessage
                                )
                                put("Message", "Silent LineLogin Success!!")
                            }.toString())
                        } else {
                            Log.d(
                                TAG,
                                "LineRefresh Fail UnknownError : " + refresh.responseData.toString()
                            )
                            Log.d(TAG, "LineRefresh Fail message  : " + refresh.errorData.message)
                            onResult(
                                Status.UNKNOWN,
                                JSONObject().apply {
                                    put(
                                        "Message",
                                        refresh.responseData.toString()
                                    )
                                }.toString()
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "LineRefresh Exception Error : " + e.toString())
                    onResult(
                        Status.UNKNOWN,
                        JSONObject().apply { put("Message", e.toString()) }.toString()
                    )
                }
            }).start()
        }

        private fun LineVerify(onResult: (Status, String) -> Unit) {
            Log.d(TAG, "LineVerify ")
            Thread(Runnable {
                try {
                    lineApiClient.verifyToken().let { verify ->
                        if (verify.isSuccess) {
                            Log.d(
                                TAG,
                                "LineVerify Success. responseData : " + verify.responseData.toString()
                            )
                            onResult(Status.SUCCESS, JSONObject().apply {
                                put("userID", GetLineUserID())
                                put("accessToken", verify.responseData.accessToken.tokenString)
                                put("displayName", lineApiClient.profile.responseData.displayName)
                                put("pictureUrl", lineApiClient.profile.responseData.pictureUrl)
                                put(
                                    "statusMessage",
                                    lineApiClient.profile.responseData.statusMessage
                                )
                                put("Message", "Silent LineLogin Success!")
                            }.toString())
                        } else {
                            Log.d(
                                TAG,
                                "LineVerify Fail. errorData : " + verify.errorData.toString()
                            )
                            onResult(Status.UNKNOWN,
                                JSONObject().apply { put("Message", verify.errorData.toString()) }
                                    .toString()
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "LineVerify Exception Error : " + e.toString())
                    onResult(
                        Status.UNKNOWN,
                        JSONObject().apply { put("Message", e.toString()) }.toString()
                    )
                }
            }).start()
        }

        private fun LineProfile(onResult: (Status, String) -> Unit) {
            Log.d(TAG, "LineProfile ")
            if (IsInValidParameter(onResult)) return

            Thread(Runnable {
                try {
                    lineApiClient.profile.apply {
                        if (isSuccess) {
                            val profile = responseData
                            Log.d(TAG, "LineProfile Success")
                            Log.d(TAG, "LineProfile responseCode : " + responseCode)
                            Log.d(TAG, "LineProfile displayName : " + profile.displayName)
                            Log.d(TAG, "LineProfile picuturUrl : " + profile.pictureUrl)
                            Log.d(TAG, "LineProfile statusMessage : " + profile.statusMessage)
                            Log.d(TAG, "LineProfile userID : " + profile.userId)
                            onResult(
                                Status.SUCCESS,
                                JSONObject().apply
                                {
                                    put("DisplayName", profile.displayName)
                                    put("PictureUrl", profile.pictureUrl)
                                    put("StatusMessage", profile.statusMessage)
                                    put("userID", profile.userId)
                                    put("Message", "LINE Profile Success")
                                    put("responseCode", responseCode)
                                }.toString()
                            )
                        } else {
                            Log.d(TAG, "LineProfile Fail responseCode : " + responseCode)
                            onResult(Status.UNKNOWN,
                                JSONObject().apply { put("Message", "Line Profile Fail.") }
                                    .toString()
                            )
                        }

                    }
                } catch (e: Exception) {
                    Log.d(TAG, "LineProfile Exception Error : " + e.toString())
                    onResult(
                        Status.UNKNOWN,
                        JSONObject().apply { put("Message", e.toString()) }.toString()
                    )
                }
            }).start()
        }

        fun IsInValidParameter(onResult: (Status, String) -> Unit): Boolean {
            if (LINE_CHANNEL_ID.isNullOrEmpty()) {
                Log.e(TAG, "")
                onResult(
                    Status.UNKNOWN,
                    JSONObject().apply { put("Message", "Please Login First.") }.toString()
                )
                return true
            }
            return false
        }

        fun GetLineUserID(): String? {
            if (LINE_CHANNEL_ID.isNullOrEmpty()) {
                Log.e(TAG, "GetLineUserID() is null. Check the ChannelID in ProjectSetting.")
                return null
            }

            try {
                return lineApiClient.profile.responseData.userId
            } catch (e: Exception) {
                Log.e(TAG, "GetLineUserID() Exception error : " + e.toString())
                return null
            }
        }
    }
}

