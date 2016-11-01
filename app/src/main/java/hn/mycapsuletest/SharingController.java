package hn.mycapsuletest;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hung on 11/1/2016.
 */
public class SharingController {

    /**
     * constructor(Activity mActivity, Uri uri, String type)
     */
    public static class MediaSharing {

        public MediaSharing(Activity mActivity, Uri uri, String type){

            // declare the intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(type);

            Intent shareToApps = Intent.createChooser(shareIntent, "Share to");
            PackageManager pm = mActivity.getPackageManager();

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType(type);

            List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
            List<LabeledIntent> intentList = new ArrayList<>();

            shareIntent.setPackage("");
            for (int i = 0; i < resInfo.size(); i++) {
                ResolveInfo ri = resInfo.get(i);
                String resolvePackageName = ri.activityInfo.packageName;
                if (resolvePackageName.contains("twitter")
                        || resolvePackageName.contains("facebook")
                        || resolvePackageName.contains("instagram")){
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(resolvePackageName, ri.activityInfo.name));
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType(type);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intentList.add(new LabeledIntent(intent, resolvePackageName, ri.loadLabel(pm), ri.icon));
                }
            }


            LabeledIntent[] extraIntents = intentList.toArray( new LabeledIntent[ intentList.size() ]);
            shareToApps.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);

            // Broadcast the Intent.
            mActivity.startActivity(shareToApps);
        }
    }
}
