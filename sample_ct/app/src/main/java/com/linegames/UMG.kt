package com.linegames;

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.content.Context
import android.content.SharedPreferences

import org.json.JSONObject
import com.linegames.base.NTBase
import com.linegames.permissionNotice.NTPermissionNoticeActivity

class UMG
{	
	companion object 
	{
		val UMG_REQUEST_CODE = 0x41b

		private var callback: Long = 0L

		@JvmStatic val Instance = UMG()
	
		@JvmStatic private external fun nativeCB( status:String, msg:String, userCB:Long )
			
		@JvmStatic fun CheckSaveData() : Boolean
		{
			val prefs: SharedPreferences = NTBase.MainActivity.getSharedPreferences("prefs_UMG", Context.MODE_PRIVATE)
			return prefs.getBoolean("bFirst", false)
		}
		
		@JvmStatic fun CreateSaveData()
		{
			val prefs: SharedPreferences = NTBase.MainActivity.getSharedPreferences("prefs_UMG", Context.MODE_PRIVATE)
			prefs.edit().putBoolean("bFirst", true).apply()
		}
		
        @JvmStatic fun ShowApplicationPermission( titleArray : Array<String>, descriptionArray : Array<String>, reqTypeArray : Array<String>, userCB: Long )
		{
			callback = userCB

			val intent = Intent(NTBase.MainActivity, NTPermissionNoticeActivity::class.java)

			intent.putExtra("titles", titleArray)
			intent.putExtra("descriptions", descriptionArray)
			intent.putExtra("requires", reqTypeArray.map {
				it.lowercase() == "true"
			}.toBooleanArray())
			
			NTBase.MainActivity.startActivityForResult(intent, UMG_REQUEST_CODE)
		}
	}
	
	fun onActivityResult ( requestCode: Int, resultCode: Int )
	{
		if (requestCode != UMG_REQUEST_CODE)
			 return;

		if (resultCode == Activity.RESULT_OK)	
			nativeCB("NT_SUCCESS", JSONObject().apply {put("Message", "Save Completed")}.toString(), callback)
	}
}