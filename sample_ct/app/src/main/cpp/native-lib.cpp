#include <jni.h>
#include <string>
#include <android/log.h>
#include <ncm/jni.h>
#define LOG_TAG "NTSDK"
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG , LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO , LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN , LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR , LOG_TAG, __VA_ARGS__)
using namespace NCM;

extern "C" JNIEXPORT jstring JNICALL Java_com_linegames_ct2_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

/*
JNICALL
Java_com_linegames_NTAdjust_onEventTrack( JNIEnv *env, jobject, jstring jStr ) {
    LOGE( "ffffffff %s " , str  );
}
*/
//public native String stringFromJNI();
extern "C" JNIEXPORT jstring JNICALL Java_com_linegames_ct2_MainActivity_AdidFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "AdidFromJNI";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jstring JNICALL ava_com_linegames_NTAdjust_InitReciever(
        JNIEnv *env,
        jobject ,
        jstring jsAdid,
        jstring jsAdjustAdid) {
    std::string hello = "InitReciever";

    const char *str_Adid = env->GetStringUTFChars(jsAdid, NULL);
    const char *str_AdjustAdid = env->GetStringUTFChars(jsAdjustAdid, NULL);
    LOGD("^^^^ Adjust InitReciever str_Adid %s str_AdjustAdid : %s ^^^^", str_Adid, str_AdjustAdid);

    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jstring JNICALL ava_com_linegames_UMG_nativeCB(
        JNIEnv *env,
        jobject ,
        jstring jStatus,
        jstring jMsg,
        jlong userCB
        ) {
    std::string hello = "nativeCB";

    const char *str_Status = env->GetStringUTFChars(jStatus, NULL);
    const char *str_Msg = env->GetStringUTFChars(jMsg, NULL);
    LOGD("^^^^ UMG nativeCB str_Status %s str_Msg : %s ^^^^", str_Status, str_Msg);

    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jstring JNICALL ava_com_linegames_NTAdjust_EventTrackReciever(
        JNIEnv *env,
        jobject ,
        jstring jsStatus,
        jstring jsMsg
        ) {
    std::string hello = "EventTrackReciever";

    const char *str_status = env->GetStringUTFChars( jsStatus, NULL );
    const char *str_msg = env->GetStringUTFChars( jsMsg, NULL );
    LOGD( "^^^^ Adjust EventTrackReciever status %s msg %s  ^^^^", str_status, str_msg );

    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jstring JNICALL Java_com_linegames_auth_Facebook_LoginReciever(
        JNIEnv *env,
        jobject , jint status, jstring  jsFBID, jstring jsAccessToken, jstring jsMsg) {
    std::string hello = "LoginReciever";

    const char *str_fbid = env->GetStringUTFChars(jsFBID, NULL);
    const char *str_accesstoken = env->GetStringUTFChars(jsAccessToken, NULL);
    const char *str_msg = env->GetStringUTFChars(jsMsg, NULL);
    LOGD("^^^^ Facebook Login LoginReciever status %d fbid : %s assesstoken : %s msg : %s ^^^^", status, str_fbid, str_accesstoken, str_msg);

    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT void JNICALL Java_com_linegames_NTAdjust_EventTrackReciever(
        JNIEnv *env,
        jobject ,
        jstring status,
        jstring msg,
        jlong userCB) {
    const char *str_status = env->GetStringUTFChars(status, NULL);
    const char *str_msg = env->GetStringUTFChars(msg, NULL);
    LOGD("^^^^ ava_com_linegames_NTAdjust_EventTrackReciever status %s msg %s userCB %ld ^^^^", str_status, str_msg, userCB);
}

extern "C" JNIEXPORT void JNICALL Java_com_linegames_Line_nativeCB(
        JNIEnv *env,
        jobject ,
        jstring status,
        jstring msg,
        jlong userCB) {
    const char *str_status = env->GetStringUTFChars(status, NULL);
    const char *str_msg = env->GetStringUTFChars(msg, NULL);
    LOGD("^^^^ Line Native CB status %s msg %s userCB %ld ^^^^", str_status, str_msg, userCB);
}

extern "C" JNIEXPORT void JNICALL Java_com_linegames_Purchase_nativeCB(
        JNIEnv *env,
        jobject ,
        jstring status,
        jstring msg,
        jlong userCB) {
    const char *str_status = env->GetStringUTFChars(status, NULL);
    const char *str_msg = env->GetStringUTFChars(msg, NULL);

    jclass cls = env->FindClass("com/linegames/ct2/MainActivity");
    if (cls == NULL) {
        LOGE("Failed to find MainActivity class");
        env->ReleaseStringUTFChars(status, str_status);
        env->ReleaseStringUTFChars(msg, str_msg);
        return;
    }

    // UpdateInfoText 메서드의 ID 가져오기
    jmethodID methodID = env->GetStaticMethodID(cls, "UpdateInfoText", "(Ljava/lang/String;Ljava/lang/String;)V");
    if (methodID == NULL) {
        LOGE("Failed to find UpdateInfoText method");
        env->ReleaseStringUTFChars(status, str_status);
        env->ReleaseStringUTFChars(msg, str_msg);
        return;
    }

    // 메서드 호출
    env->CallStaticVoidMethod(cls, methodID, status, msg);
    if (env->ExceptionCheck()) {
        env->ExceptionDescribe();
        env->ExceptionClear();
        LOGE("Exception occurred while calling UpdateInfoText");
    }

    // C 문자열 해제
    env->ReleaseStringUTFChars(status, str_status);
    env->ReleaseStringUTFChars(msg, str_msg);
    LOGD("^^^^ Purchase Native CB status %s msg %s userCB %ld ^^^^", str_status, str_msg, userCB);
}

extern "C" JNIEXPORT void JNICALL Java_com_linegames_PurchaseSub_nativeCB(
        JNIEnv *env,
        jobject ,
        jstring status,
        jstring msg,
        jlong userCB) {
    const char *str_status = env->GetStringUTFChars(status, NULL);
    const char *str_msg = env->GetStringUTFChars(msg, NULL);
    //LOGD("^^^^ PurchaseSub Native CB status %s msg %s userCB %ld ^^^^", str_status, str_msg, userCB);
}

extern "C" JNIEXPORT void JNICALL Java_com_linegames_PurchaseGalaxy_nativeCB(
        JNIEnv *env,
        jobject ,
        jstring status,
        jstring msg,
        jlong userCB) {
    const char *str_status = env->GetStringUTFChars(status, NULL);
    const char *str_msg = env->GetStringUTFChars(msg, NULL);
    LOGD("^^^^ Purchase Galaxy Native CB status %s msg %s userCB %ld ^^^^", str_status, str_msg, userCB);
}