package com.linegames.auth;

import android.content.Intent
import android.util.Log
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.linegames.base.NTBase

class Facebook : FacebookCallback<LoginResult>
{
    private var callbackManager: CallbackManager? = null

    enum class FacebookStatus (val value: Int)
    {
        SUCCESS(0),
        UNKNOWN(1),
        CANCEL(2)
    }
    companion object {

        private const val TAG = "NTSDK"

        @JvmStatic val Instance = Facebook()

        @JvmStatic external fun LoginReciever( status : Int, jsFBID : String, jsAccessToken : String, jsMsg : String )

        @JvmStatic fun FacebookLogin( permissions : Array<String> ) { Instance.Login(  permissions ) }
        @JvmStatic fun FacebookLogout() { Instance.Logout() }
    }

    constructor()
    {
        Log.d( TAG, "FacebookSdk Version : "  + FacebookSdk.getSdkVersion() )
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, this)
    }

    fun Login( permissionsArray : Array<String> ) {

        if ( CheckAccessToken() ) {
            AccessToken.getCurrentAccessToken()
                ?.let { LoginReciever(FacebookStatus.SUCCESS.value, it.userId, it.token, "") }
        }
        else
        {
            Log.d( TAG, "FacebookSdk Login ========================================== : "  + FacebookSdk.getSdkVersion() )
            LoginManager.getInstance().logInWithReadPermissions(NTBase.MainActivity , permissionsArray.toList())
        }
    }

    fun CheckAccessToken():Boolean {

        if ( AccessToken.getCurrentAccessToken() == null ) {
            return false;
        };

        //AccessToken.getCurrentAccessToken().isExpired
        if ( !AccessToken.isCurrentAccessTokenActive()) {
            Log.e( TAG, "Facebook AccessToken is Expired" )
            return false;
        }
        return true;
    }

    fun Logout() { LoginManager.getInstance().logOut() }

    fun onActivityResult ( requestCode: Int, resultCode: Int, data: Intent? ){
        callbackManager?.onActivityResult( requestCode, resultCode, data );
    }

    override fun onSuccess(result: LoginResult) {
        LoginReciever( FacebookStatus.SUCCESS.value , result.accessToken.userId, result.accessToken.token , "" )
    }

    override fun onCancel() {
        LoginReciever( FacebookStatus.CANCEL.value , "", "" , "Facebook Login User Cancel" )
    }

    override fun onError(error: FacebookException) {
        LoginReciever( FacebookStatus.UNKNOWN.value , "", "" , error.toString() )
    }

}
