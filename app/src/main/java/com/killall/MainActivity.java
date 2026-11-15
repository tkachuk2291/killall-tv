package com.killall;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * One job: when opened, kill the background processes of every other app,
 * show how much memory was freed, and close itself.
 */
public class MainActivity extends Activity {

    // Never touch these: our own app, the launcher, and core system pieces.
    private static final Set<String> PROTECTED = new HashSet<>(Arrays.asList(
            "android",
            "com.android.systemui",
            "com.android.providers.tv",
            "me.efesser.flauncher",
            "com.google.android.tvlauncher",
            "com.android.inputmethod.latin",
            "com.google.android.inputmethod.latin",
            "com.google.android.tv.remote.service",
            "com.android.bluetooth",
            "xiaomi.tvservice"
    ));

    private long availMb(ActivityManager am) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem / (1024 * 1024);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        PackageManager pm = getPackageManager();
        long before = availMb(am);

        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        // Two passes: some processes respawn their helpers after the first kill.
        for (int pass = 0; pass < 2; pass++) {
            for (ApplicationInfo ai : apps) {
                String pkg = ai.packageName;
                if (pkg.equals(getPackageName())
                        || PROTECTED.contains(pkg)
                        || pkg.startsWith("com.droidlogic")) {
                    continue;
                }
                am.killBackgroundProcesses(pkg);
            }
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ActivityManager am2 = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                long after = availMb(am2);
                long freed = Math.max(0, after - before);
                Toast.makeText(getApplicationContext(),
                        "Memory free: " + after + " MB (+" + freed + " MB)",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }, 1200);
    }
}
