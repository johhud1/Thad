package com.thadeus.android;

import java.io.File;
import java.util.Vector;

import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;
import com.google.android.vending.expansion.downloader.Helpers;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class ThadeusAppActivity extends Activity {

    private String TAG = "ThadeusAppActivity";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if(!expansionFilesDelivered()){//if the files aren't here, we need to check if we can download them.
            Toast.makeText(this, "expansion files not found", Toast.LENGTH_LONG).show();
            Intent notifierIntent = new Intent(this, this.getClass());
            notifierIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifierIntent,
                                                    PendingIntent.FLAG_UPDATE_CURRENT);

            int startResult=DownloaderClientMarshaller.DOWNLOAD_REQUIRED;
            try {
                startResult = DownloaderClientMarshaller.startDownloadServiceIfRequired(
                                                    this, pendingIntent, APKDownloadService.class);
            } catch (NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if(startResult != DownloaderClientMarshaller.NO_DOWNLOAD_REQUIRED){
                //need to download

            }
            //start expansion file download service.
        }
        //the files are here. Or they are downloading


    }

 // The shared path to all app expansion files
    private final static String EXP_PATH = "/Android/obb/";

    boolean expansionFilesDelivered() {
        String TAG = "expansionFilesDelivered";

        for (LFnC.XAPKFile xf : LFnC.xAPKS) {
            String fileName = Helpers.getExpansionAPKFileName(this, xf.mIsMain, xf.mFileVersion);
            Log.d(TAG, "checking file "+fileName);
            if (!Helpers.doesFileExist(this, fileName, xf.mFileSize, false)){
                Log.d(TAG, "file not found");
                return false;
            }
            Log.d(TAG, "file found");
        }
        return true;
    }

    public void gotoActivity(View view){
        Intent intent = null;
        switch(view.getId()){
        case (R.id.video_selector_layout):
            intent = new Intent(this, VideoActivity.class);
            break;
        case (R.id.album_selector_layout):
            intent = new Intent(this, AlbumActivity.class);
            break;
        case (R.id.link_selector_layout):
            intent = new Intent(this, LinkActivity.class);
        }
        startActivity(intent);
    }
}
