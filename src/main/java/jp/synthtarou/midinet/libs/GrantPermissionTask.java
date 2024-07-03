package jp.synthtarou.midinet.libs;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import jp.synthtarou.midinet.task.SingleTaskFlag;
import jp.synthtarou.midinet.task.SingleTaskQueue;

public class GrantPermissionTask {
    String TAG = "GrantPermissionTask";
    String _permission;
    Activity _activity;
    public GrantPermissionTask(String permission, Activity activity) {
        _permission = permission;
        _activity = activity;
    }

    @RequiresApi(Build.VERSION_CODES.S)
    public boolean grantPermission() {
        SingleTaskFlag f = SingleTaskQueue.getMainLooper().push(() -> {
            Activity activity = _activity;
            String permission = _permission;
            Log.w(TAG, "grantPermission Start " + _permission);

            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "need unlock " + permission);
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    Log.w(TAG, "shouldShow");
                    ActivityCompat.requestPermissions(activity, new String[]{permission}, 1);
                } else {
                    ActivityCompat.requestPermissions(activity, new String[]{permission}, 1);
                }
            } else {
                Log.w(TAG, "permission OK " + permission);
            }
        });

        Log.w(TAG, "grantPermission Prepare " + _permission);
        if (f.awaitResult(10000) == false) {
            Throwable ex = f.awaitThrowable(0);
            if (ex != null) {
                Log.e(TAG, ex.getMessage(), ex);
            }
            return false;
        }
        return true;
   }
}
