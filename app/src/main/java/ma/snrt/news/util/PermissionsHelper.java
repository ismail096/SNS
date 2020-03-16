package ma.snrt.news.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


public class PermissionsHelper {

    public static final int WRITE_STORAGE = 10;
    public static final int SEND_SMS = 11;
    public static final int PHONE_STATE = 12;
    public static final int USE_CAMERA = 13;
    public static final int CALL_PHONE = 14;
    public static final int ACCESS_LOCATION = 15;
    public static final int MICROPHONE = 16;



    public static boolean canAccessAccounts(Context context) {

        return (hasPermission(context, Manifest.permission.GET_ACCOUNTS));
    }
    public static boolean canAccessLocation(Context context) {

        return (hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION));
    }
    public static boolean canAccessInternet(Context context) {
        return (hasPermission(context, Manifest.permission.INTERNET));
    }

    public static boolean canAccessCamera(Context context) {
        return (hasPermission(context, Manifest.permission.CAMERA));
    }

    public static boolean canCallPhone(Context context) {
        return (hasPermission(context, Manifest.permission.CALL_PHONE));
    }

    public static boolean canAccessMicrophone(Context context) {
        return (hasPermission(context, Manifest.permission.RECORD_AUDIO));
    }

    public static boolean canSendSMS(Context context) {
        return (hasPermission(context, Manifest.permission.SEND_SMS));
    }

    public static boolean canAccessPhoneState(Context context) {
        return (hasPermission(context, Manifest.permission.READ_PHONE_STATE));
    }

    public static boolean canAccessStorage(Context context) {
        return (hasPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE));
    }

    public static boolean hasPermission(Context context, String perm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(context,perm));
        }
        return (true);
    }

    public static boolean hasPermission(Context context, String perm1, String perm2) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ((PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(context,perm1))
            && (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(context,perm2)));
        }
        return (true);
    }

    public static void askLocationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION);
        }
    }

    public static void askCallPermission(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, CALL_PHONE);
        }
    }

    public static void askMicrophonePermission(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE);
        }
    }

    public static void askStoragePermission(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE);
        }
    }

    public static void askSMSPermission(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SEND_SMS);
        }
    }

    public static void askPhoneStatePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, PHONE_STATE);
        }
    }

    public static void askCameraPermission(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(new String[]{Manifest.permission.CAMERA}, USE_CAMERA);
        }
    }

    public static void askPermission(AppCompatActivity activity, String permission, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(new String[]{permission}, requestCode);
        }
    }
}
