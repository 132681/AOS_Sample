package com.linegames

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.linegames.base.LGLog
import com.linegames.base.LGBase
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.android.gms.tasks.OnCompleteListener
import org.json.JSONObject

class NotiFCMService : FirebaseMessagingService()
{
    companion object
	{
		private const val NT_KEY_ICON_NAME_ = "NT_KEY_ICON_NAME"

		@JvmStatic external fun ntsdkFCMReciever( sData : String )
        @JvmStatic external fun ntsdkFCMUpdateToken( sToken : String )

        @Suppress("unused")
		@JvmStatic fun start(sDefaultIcon :String, sFirebaseAppID :String)
        {

			LGLog.d( "lss NTNoti : start =======================" )
	        // Set Default Icon
	        LGBase.SharedPreferences(LGBase.MainActivity).set(NT_KEY_ICON_NAME_,sDefaultIcon)

	        // Init Firebase
            if( FirebaseApp.getApps( LGBase.MainActivity ).isEmpty() )
            {
				LGLog.d( "lss NTNoti Firebase InitializeApp: $sFirebaseAppID" )
                FirebaseApp.initializeApp( LGBase.MainActivity, FirebaseOptions.Builder()
                    .setApplicationId( sFirebaseAppID ).build()
                )
            }

            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener( OnCompleteListener { task ->
                    if( !task.isSuccessful )
                    {
                        LGLog.e( "lss NTNoti: getInstanceId failed : $task.exception" )
                        return@OnCompleteListener
                    }
                    val token : String = task.result?.token!!
					LGLog.d( "lss NTNoti : start token: $token" )
//                    ntsdkFCMUpdateToken( token )
                })
        }
    }

    enum class ENoticationType( val value : Int )
    {
        Default(0),
        Custom(1);

		companion object
		{
//			fun fromInt( type: Int? ) : ENoticationType
//			{
//				if( null == type )
//                    return Default
//                return values().first { it.value == type }
//			}
		}
    }

	override fun onMessageReceived(remoteMessage: RemoteMessage) {

		LGLog.i("lss onMessageReceived==============")

//		// FirebaseApp 초기화 확인 및 필요 시 초기화
//		if (FirebaseApp.getApps(LGBase.MainActivity).isEmpty()) {
//			FirebaseApp.initializeApp(LGBase.MainActivity)
//		}
//
		remoteMessage.notification?.let {
			LGLog.i("lss NTNoti Noti : { title : ${it.title}, body: ${it.body} }")
		}

				remoteMessage.data.let { data ->
			if (data.isNotEmpty()) {
				LGLog.i("lss data.isNotEmpty NTNoti Data: $data")
			}
			else{
				LGLog.i("lss data.isEmpty NTNoti Data: $data")
			}
		}

//		LGLog.i("lss NTNoti Noti : test")

//		remoteMessage.data.let { data ->
//			if (data.isNotEmpty()) {
//				LGLog.i("lss NTNoti Data: $data")
//				val type: ENoticationType = data["type"]?.toInt()?.let { ENoticationType.fromInt(it) }
//					?: ENoticationType.Default
//
//				if (type == ENoticationType.Default) {
//					sendNotification(data)
//					LGLog.i("lss NTNoti Noti sendNotification")
//				} else {
//					sendReciever(data)
//					LGLog.i("lss NTNoti Noti sendReciever")
//				}
//			}
//			else{
//				LGLog.i("lss NTNoti Noti data is null")
//				sendDefaultNotification(remoteMessage);
//			}
//		}
	}

	private fun sendDefaultNotification(remoteMessage: RemoteMessage) {
		LGLog.i("lss NTNoti: sendDefaultNotification")

		val id = System.currentTimeMillis().toInt()
		val intent = packageManager.getLaunchIntentForPackage(packageName)!!
			.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
		val pendingIntent = PendingIntent.getActivity(this, id, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

		remoteMessage.notification?.let {
			LGLog.i("lss NTNoti Noti : { title : ${it.title}, body: ${it.body} }")
		}
		val channelId = "NOTIFICATION"
		val icon = getDefaultIcon()
		val title = remoteMessage.notification?.title ?: getAppName()
		val contextText = remoteMessage.notification?.body ?: "새로운 알림이 있습니다."

		val notificationBuilder = NotificationCompat.Builder(this, channelId)
			.setSmallIcon(icon)
			.setPriority(NotificationCompat.PRIORITY_HIGH)
			.setContentTitle(title)
			.setContentText(contextText)
			.setContentIntent(pendingIntent)
			.setAutoCancel(true)
			.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

		val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val channel = NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_HIGH)
			notificationManager.createNotificationChannel(channel)
		}

		notificationManager.notify(id, notificationBuilder.build())
	}

	override fun onCreate() {
		super.onCreate()
		// FirebaseApp 초기화
		LGLog.i("lss NTNoti Noti override fun onCreate() =========")
		if (FirebaseApp.getApps(this).isEmpty()) {

			val options = FirebaseOptions.Builder()
				.setApplicationId("1:764920947053:android:3a48ce8f849f332794ba64") // Firebase 프로젝트의 Application ID
				.setApiKey("AIzaSyBYDS11j6m-o9UzGk-YMd-x3d2vT-FmbFo") // Firebase 프로젝트의 API Key
				.setDatabaseUrl("https://cointop-32607200.firebaseio.com") // Firebase Database URL
				.build()
			FirebaseApp.initializeApp(this, options)
		}
	}

    override fun onNewToken( token: String? )
	{
       LGLog.d( "lss NTNoti : Refreshed token: $token" )
    }

	@SuppressLint("DiscouragedApi")
	private fun getAppName() : String {
		return resources.getString(resources.getIdentifier("app_name", "string", packageName ))
	}

	@SuppressLint("DiscouragedApi")
	private fun getDefaultIcon() : Int
	{
		var id = 0
		LGBase.SharedPreferences(this ).get(NT_KEY_ICON_NAME_)?.let {
			id = resources.getIdentifier( it, "drawable", packageName )
		}
		return when( id ) {
			0 -> applicationInfo.icon
			else -> id
		}
	}

	private fun sendReciever( data: Map<String,String> )
	{
	    LGLog.i( "lss NTNoti: sendReciever : $data" )
		val json = JSONObject()
		data.forEach { (key, value) -> json.put( key, value ) }
		ntsdkFCMReciever( json.toString() )
	}

	@SuppressLint("DiscouragedApi")
    private fun sendNotification( data: Map<String,String> )
	{
	    LGLog.i( "lss NTNoti: sendNotification : $data" )
		val id = System.currentTimeMillis().toInt()
        val intent = packageManager.getLaunchIntentForPackage( packageName )!!
		    .addFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP )
        val pendingIntent = PendingIntent.getActivity( this, id, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE )

// -----------Local Notifiction  -------------------------------------------------------
// Long triggerTime,            System.currentTimeMillis() + (long)(delay * 1000);
// float   delay,                0   ( long )

		var groupKey : String? = null
		val channelId = data["channel"] ?: "NOTIFICATION"
		val icon = data["icon"]?.toInt() ?: getDefaultIcon()
		val title = data["title"] ?: getAppName()
		val contextText = data["message"] ?: ""
        val notificationBuilder = NotificationCompat.Builder(this, channelId )
            .setSmallIcon( icon )
		    .setPriority( NotificationCompat.PRIORITY_HIGH )
			.setContentTitle( title )
			.setContentText( contextText )
            .setContentIntent(pendingIntent)
		    .setAutoCancel(true)
		    .setSound(
				data["sound"]?.let {
					val id_sound = resources.getIdentifier( it, "raw", packageName )
					var uri: Uri? = null
					if( id_sound != 0 )
						uri = Uri.parse("android.resource://$packageName/$id_sound" )
					uri
				} ?: RingtoneManager.getDefaultUri( RingtoneManager.TYPE_NOTIFICATION )
		    )
		val bigStyle = NotificationCompat.BigTextStyle().bigText( contextText )
		if( data.containsKey("title") ){
			bigStyle.setBigContentTitle( title )
		}
		notificationBuilder.setStyle( bigStyle )

		data["groupKey"]?.also {
			LGLog.i( "lss NTNoti: groupKey : $it")
			groupKey = it
			notificationBuilder.setGroup( it )
		}
		data["subtext"]?.let {
			notificationBuilder.setSubText( it )
		}

		val notificationManager = getSystemService( NOTIFICATION_SERVICE ) as NotificationManager
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O )
		{
			val channel = NotificationChannel( channelId, channelId, NotificationManager.IMPORTANCE_HIGH )
			notificationManager.createNotificationChannel(channel)
					//	AudioAttributes.Builder soundAttrs = new AudioAttributes.Builder()
					//			.setContentType( AudioAttributes.CONTENT_TYPE_SONIFICATION )
					//			.setUsage( AudioAttributes.USAGE_NOTIFICATION );
					//	channel.setSound(getSoundUri(context, sound), soundAttrs.build());
					//	notificationManager.createNotificationChannel(channel);
		}
		if( null != groupKey )
		{
			val notificationGroup = NotificationCompat.Builder(this, channelId )
					.setSmallIcon( icon )
					.setGroup( groupKey )
			        .setGroupSummary(true)
					.setAutoCancel(true)
					.setContentIntent( pendingIntent ).build()
//			.setContentTitle( getAppName() +  " ( $groupKey )" )
			notificationManager.notify( groupKey,0, notificationGroup )
		}
		notificationManager.notify( id, notificationBuilder.build() )
    }
}
