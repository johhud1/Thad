package com.thadeus.android;


import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.android.vending.expansion.zipfile.ZipResourceFile.ZipEntryRO;
import com.google.android.vending.expansion.downloader.Helpers;
import com.google.android.vending.licensing.APKExpansionPolicy;
import com.google.android.vending.licensing.Obfuscator;
import com.google.android.vending.licensing.ValidationException;
import com.thadeus.android.MediaService.Album;
import com.thadeus.android.MediaService.Song;
import com.thadeus.android.myMediaController.MediaPlayerControl;

import android.app.Activity;
import android.app.ExpandableListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.MediaController;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class AlbumActivity extends ExpandableListActivity implements OnChildClickListener{
    private String TAG = "AlbumActivity";
    private String audioFile;
    private myMediaController mc;
    private Context mContext = this;
    private AbstractExpandableListAdapter<Album, Song> mListAdapter;
    private boolean mIsBound;
    private MediaService mBoundService;
    private Handler handler = new Handler();
    private Bundle mSavedInstanceState;


    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "in onServiceConnected!!");
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((MediaService.LocalBinder)service).getService();

            mBoundService.setMediaController(mc);


            List<Entry<Album, List<Song>>> songList = new ArrayList<Entry<Album, List<Song>>>();
            songList = mBoundService.createExpandList(songList);

            mListAdapter = new AbstractExpandableListAdapter<Album, Song>(mContext,
                R.layout.album_listrow,
                R.layout.album_listrow,
                R.layout.songrow_layout,
                new int[] {R.id.albumlist_textview},
                new int[] {R.id.songitem_textview},
                songList);

            mc.updateUI();
            setListAdapter(mListAdapter);

        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "onServiceDisconnected *******");
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            Toast.makeText(AlbumActivity.this, R.string.media_service_disconnected,
                    Toast.LENGTH_SHORT).show();
        }

    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_activity_layout);

        startService(new Intent(this, MediaService.class));
        doBindService();

        mc = (myMediaController) findViewById(R.id.myMediaController1);

        getExpandableListView().setOnChildClickListener(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(mc!= null){
            mc.updateUI();
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                int childPosition, long id) {
        String tag = "onChildClick";
        if(mBoundService!=null){
            mBoundService.playGroupChildPos(groupPosition, childPosition);
        }
        else{
            Log.e(tag, "can't play song, mBoundService (media service) is null");
        }
        return false;
    }

    @Override
    protected void onStop() {
      super.onStop();
      mc.emptyQueue();

//      if(mp.isPlaying()){
//          mp.pause();
//          mp.stop();
//      }
//      mp.release();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
      //the MediaController will hide after 3 seconds - tap the screen to make it appear again
      //mc.show(0);
      return false;
    }


    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(this,
                MediaService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
//        getExpandableListView().
//        savedInstanceState.putIntArray(LFnC.bundle_key_expandedGroups, )
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
}
