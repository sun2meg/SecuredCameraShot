package com.megacoreapps.sun2meg.securedcamerashot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

public class AppOnTopPermissionHelper {

    public static final int SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST_CODE = 101;

    public static boolean checkSystemAlertWindowPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true; // For versions below Android 6.0, permission is granted by default.
    }

    public static void requestSystemAlertWindowPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(activity)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST_CODE);
            }
        }
    }

    // This method is called in the onActivityResult of your Activity after requesting permission
    public static boolean handleSystemAlertWindowPermissionResult(Activity activity, int requestCode) {
        if (requestCode == SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST_CODE) {
            // Check if the permission was granted
            if (checkSystemAlertWindowPermission(activity)) {
                // Permission granted
                return true;
            } else {
                // Permission denied
                // Handle the denial or inform the user
                return false;
            }
        }
        return false;
    }
}
