package com.linegames.ct2;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class UserAction {
    // Enum to define various user actions
    public enum Action {
        GOOGLE_SIGN,
        GOOGLE_SIGN_OUT,
        GOOGLE_PLAY_SERVICE_SIGN,
        GOOGLE_CLOUD_LIST,
        GOOGLE_CLOUD_SAVE,
        GOOGLE_CLOUD_LOAD,
        PURCHASE_CONNECT,
        PURCHASE_REFRESHPRODUCTINFO,
        PURCHASE_BUYPURCHASE,
        PURCHASE_CONSUME,
        PURCHASE_RESTORE,
        PURCHASE_CONSUMEALL,
        PURCHASE_SENDEMAILRECEIPTINFO,
        ADJUST_EVENT1,
        ADJUST_EVENT2,
        UMG_EVENT1,
        UMG_EVENT2,
        ETC1_EVENT1,
        ETC1_EVENT2,
        ETC2_EVENT1,
        ETC2_EVENT2
    }

    public enum AndroidFunction {
        GOOGLE,
        PURCHASE,
        ANDROID,
        Adjust,
        Line
    }


    // Map to store the mapping between button texts and actions
    private static final Map<String, Action> actionMap = new HashMap<>();

    static {//Button Display
        // Initialize the mapping
        actionMap.put("GOOGLE 1", Action.GOOGLE_SIGN);
        actionMap.put("GOOGLE 2", Action.GOOGLE_SIGN_OUT);
        actionMap.put("GOOGLE 3", Action.GOOGLE_PLAY_SERVICE_SIGN);
        actionMap.put("GOOGLE 4", Action.GOOGLE_CLOUD_LIST);
        actionMap.put("GOOGLE 5", Action.GOOGLE_CLOUD_SAVE);
        actionMap.put("GOOGLE 6", Action.GOOGLE_CLOUD_LOAD);
        actionMap.put("PURCHASE 1", Action.PURCHASE_CONNECT);
        actionMap.put("PURCHASE 2", Action.PURCHASE_REFRESHPRODUCTINFO);
        actionMap.put("PURCHASE 3", Action.PURCHASE_BUYPURCHASE);
        actionMap.put("PURCHASE 4", Action.PURCHASE_CONSUME);
        actionMap.put("PURCHASE 5", Action.PURCHASE_RESTORE);
        actionMap.put("PURCHASE 6", Action.PURCHASE_CONSUMEALL);
        actionMap.put("PURCHASE 7", Action.PURCHASE_SENDEMAILRECEIPTINFO);
        actionMap.put("ADJUST 1", Action.ADJUST_EVENT1);
        actionMap.put("ADJUST 2", Action.ADJUST_EVENT2);
        actionMap.put("UMG 1", Action.UMG_EVENT1);
        actionMap.put("UMG 2", Action.UMG_EVENT2);
        actionMap.put("ETC1 1", Action.ETC1_EVENT1);
        actionMap.put("ETC1 2", Action.ETC1_EVENT2);
        actionMap.put("ETC2 1", Action.ETC2_EVENT1);
        actionMap.put("ETC2 2", Action.ETC2_EVENT2);
    }

    // Method to get Action by button text and convert to string
    public static String getActionByIndex(String title, int index) {
        String buttonText = title + " " + index;
        Log.d("", "lss getActionByIndex ======================== buttonText : " + buttonText);

        Action action = actionMap.get(buttonText);
        return action != null ? action.name() : null;
    }

    // Method to get Action by button text
    public static Action fromString(String buttonText) {
        Log.d("", "lss fromString ======================== buttonText : " + buttonText);
        return actionMap.get(buttonText);
    }
}
