package com.thadeus.android;


import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.android.vending.expansion.zipfile.ZipResourceFile.ZipEntryRO;
import com.google.android.vending.expansion.downloader.Helpers;
import com.google.android.vending.licensing.APKExpansionPolicy;
import com.google.android.vending.licensing.Obfuscator;
import com.google.android.vending.licensing.ValidationException;
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
    //private ArrayList<Album> mAlbums;
    //private MediaPlayer mp;
    private myMediaController mc;
    private Context mContext = this;
    private SimpleExpandableListAdapter mListAdapter;
    //private ZipResourceFile zrf;
    //private Song currentlyPlaying;
    private boolean mIsBound;
    private MediaService mBoundService;
    private Handler handler = new Handler();

//    public class Song implements Comparable<Song>{
//        public Album mAlbum;
//        public String mTitle;
//        public ZipEntryRO mFile;
//        public int index;
//
//        public Song(Album album, String title, ZipEntryRO file, int trackNum){
//            mAlbum = album;
//            mTitle = title;
//            mFile = file;
//            index = trackNum;
//        }
//        @Override
//        public int compareTo(Song another) {
//            return mTitle.compareTo(another.mTitle);
//        }
//    }
//
//    public class Album implements Comparable<Album>{
//        public String mName;
//        public ZipEntryRO mArt;
//        ArrayList<Song> mSongs;
//        public Album(String name){
//            mSongs = new ArrayList<Song>();
//            mName = name;
//        }
//        public Album(String name, ZipEntryRO art){
//            mSongs = new ArrayList<Song>();
//            mName = name;
//            mArt = art;
//        }
//        public void addSong(Song song){
//            mSongs.add(song);
//        }
//        public void addArt(ZipEntryRO art){
//            mArt = art;
//        }
//        @Override
//        public int compareTo(Album another) {
//            return mName.compareTo(another.mName);
//        }
//        @Override
//        public boolean equals(Object another){
//            return mName.equals(((Album)another).mName);
//        }
//    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((MediaService.LocalBinder)service).getService();

//            // Tell the user about this for our demo.
//            Toast.makeText(AlbumActivity.this, R.string.media_service_connected,
//                    Toast.LENGTH_SHORT).show();
            mc.setMediaPlayer(mBoundService);
            mBoundService.setMediaController(mc);
            mListAdapter = new SimpleExpandableListAdapter(mContext,
                                                           mBoundService.createAlbumList(), R.layout.album_listrow,
                                                           new String[] { LFnC.album_listkey }, new int[] {R.id.albumlist_textview},
                                                           mBoundService.createSongList(), R.layout.songrow_layout, new String[] {LFnC.song_listkey},
                                                           new int[] {R.id.songitem_textview});
            setListAdapter(mListAdapter);
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            Toast.makeText(AlbumActivity.this, R.string.media_service_disconnected,
                    Toast.LENGTH_SHORT).show();
        }

    };
//
//    private List createAlbumList(){
//        String TAG = "createAlbumList";
//        ArrayList<HashMap> result = new ArrayList<HashMap>();
//        for(int i=0; i<mAlbums.size(); i++){
//            Log.d(TAG, "mAlbums.size = "+mAlbums.size()+" name: "+mAlbums.get(i).mName);
//            HashMap h = new HashMap();
//            h.put(LFnC.album_listkey, mAlbums.get(i).mName);
//            result.add(h);
//
//        }
//        return (List)result;
//    }
//
//    private List createSongList(){
//        String TAG = "createSongList";
//        ArrayList<ArrayList<HashMap>> result = new ArrayList<ArrayList<HashMap>>();
//        for(int i=0; i<mAlbums.size(); i++){
//            ArrayList<HashMap> songs = new ArrayList<HashMap>();
//            Album alb = mAlbums.get(i);
//            for(int k=0; k<alb.mSongs.size(); k++){
//                Log.d(TAG, "alb.mName= "+alb.mName+" has "+alb.mSongs.size()+" number of songs."+
//                      " adding song "+alb.mSongs.get(k).mTitle);
//                HashMap h = new HashMap();
//                h.put(LFnC.song_listkey, alb.mSongs.get(k).mTitle);
//                songs.add(h);
//            }
//            result.add(songs);
//        }
//        return (List)result;
//    }
//    private void skipNext(){
//        playSong(currentlyPlaying.mAlbum.mSongs.get(currentlyPlaying.index+1));
//    }
//    private void skipPrev(){
//        playSong(currentlyPlaying.mAlbum.mSongs.get(currentlyPlaying.index-1));
//    }
//    private boolean playSong(Song song){
//        String tag = "playSong";
//        ZipEntryRO zfile = song.mFile;
//        Log.d(tag, "It is unCompressed"+zfile.isUncompressed());
//        if(zfile.isUncompressed()){
//            Log.d(tag, "in 'isUncompressed code block");
//            mp.reset();
//            AssetFileDescriptor afd = zrf.getAssetFileDescriptor(zfile.mFileName);
//            currentlyPlaying = song; //.mTitle + " - "+song.mAlbum.mName;
//            try {
//                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
//                mp.prepare();
//                View.OnClickListener nextClickListener = null;
//                if(song.index < (song.mAlbum.mSongs.size()-1)){
//                    nextClickListener = new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            skipNext();
//                        }
//                    };
//                }
//                View.OnClickListener prevClickListener = null;
//                if(song.index > 0){
//                    prevClickListener = new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            skipPrev();
//                        }
//                    };
//                }
//                mc.setPrevNextListeners(nextClickListener, prevClickListener);
//                return true;
//            } catch (IllegalArgumentException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            } catch (IllegalStateException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//        Log.e(TAG, "audio files are compressed, can't get filedescriptor");
//        return false;
//    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mAlbums = new ArrayList<Album>();
        setContentView(R.layout.album_activity_layout);



//        mp =  new MediaPlayer();
//        mp.setOnPreparedListener(this);


        startService(new Intent(this, MediaService.class));
        doBindService();

        mc = new myMediaController(this);
//        mc.setMediaPlayer(mBoundService);
        //LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //getExpandableListView().getRootView().
        //this.addContentView(, params)

        mc.setAnchorView(findViewById(R.id.myMediaController1));

        //findViewById(R.id.mediaController1).setClickable(true);

//        String filename = Helpers.getSaveFilePath(this) + "/" +
//                Helpers.getExpansionAPKFileName(this, LFnC.xAPKS[0].mIsMain, LFnC.xAPKS[0].mFileVersion);
//        Log.d(TAG, "got filename "+filename+" for expansionAPK filename");
//        try {
//            zrf = APKExpansionSupport.getResourceZipFile(new String[] {filename});
//            ZipEntryRO[] zipEntries = zrf.getAllEntries();
//            for(int i=0; i<zipEntries.length; i++){
//                String zfilename = zipEntries[i].mFileName;
//                if(zipEntries[0].isUncompressed()){
//                    Log.d(TAG, "zip entry "+i+" name: "+ zfilename+" is uncompressed");
//                }
//                else Log.d(TAG, "zip entry "+i+" name: "+ zfilename+" is compressed");
//
//            }
//            parseAlbumFiles(zipEntries);
//
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

//        SimpleExpandableListAdapter listA = new SimpleExpandableListAdapter(this,
//                                       mBoundService.createAlbumList(), R.layout.album_listrow,
//                                       new String[] { LFnC.album_listkey }, new int[] {R.id.albumlist_textview},
//                                       mBoundService.createSongList(), R.layout.songrow_layout, new String[] {LFnC.song_listkey},
//                                       new int[] {R.id.songitem_textview});

        getExpandableListView().setOnChildClickListener(this);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                int childPosition, long id) {
        String tag = "onChildClick";
        if(mBoundService!=null){
            mBoundService.playGroupChildPos(groupPosition, childPosition);
        }
        Log.e(tag, "can't play song, mBoundService (media service) is null");
        return false;
    }
//
//    public void parseAlbumFiles(ZipEntryRO[] ze){
//        String TAG = "parseAlbumFiles";
//        for(int i=0; i<ze.length; i++){
//            String fn = ze[i].mFileName;
//            String[] splitFolder = fn.split("/");
//            String[] splitFiles;
//            Song song;
//            Album album;
//            if(splitFolder.length>2){
//                Log.d(TAG, "split into strings "+splitFolder[1]+" / "+splitFolder[2]+" of length "+splitFolder.length);
//                album = new Album(splitFolder[1]);
//                if(!mAlbums.contains(album)){
//                    Log.d(TAG, "couldn't find album "+album.mName+" in existing list, adding album");
//                    mAlbums.add(album);
//                }else{
//                    album = mAlbums.get(mAlbums.indexOf(album));
//                }
//                splitFiles = splitFolder[2].split("[.]");
//                Log.d(TAG, "splitFiles,split of string "+splitFolder[2]+" on '.' has length "+splitFiles.length);
//                if(splitFiles.length > 1){
//                    if(splitFiles[1].equals(LFnC.audio_fileformat)){
//                        Log.d(TAG, "found file "+splitFolder[1]+
//                              ", think it's a song. making new song, with album "
//                              +album.mName+", song title "+splitFiles[0]);
//                        song = new Song(album, splitFiles[0], ze[i], album.mSongs.size());
//                        album.addSong(song);
//                    }
//                    if(splitFiles[1].equals(LFnC.albumart_fileformat)){
//                        Log.d(TAG, "found file "+splitFolder[2]+", think it's art. adding it to album "+album.mName);
//                        album.addArt(ze[i]);
//                    }
//                }
//            }
//            else{
//                Log.d(TAG, "split had length "+splitFolder.length+", which is < 2");
//            }
//        }
//    }

//    @Override
//    public void onPrepared(MediaPlayer mp) {
//        Log.d("onPrepared", "in onPrepared");
//        mc.setEnabled(true);
//        mc.doPauseResume();
//
//        handler.post(new Runnable() {
//            public void run() {
//
//            }
//          });
 //   }

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

//    //--MediaPlayerControl methods----------------------------------------------------
//    public void start() {
//      mc.setPlayingText(currentlyPlaying.mTitle + " - "+currentlyPlaying.mAlbum.mName,
//                        (TextView)findViewById(R.id.curplaying_textview));
//      mp.start();
//    }
//
//    public void pause() {
//      mp.pause();
//    }
//
//    public int getDuration() {
//      return mp.getDuration();
//    }
//
//    public int getCurrentPosition() {
//      Log.d("getCurrentPosition", "mPlayer.isPlaying(): "+mp.isPlaying());
//      return mp.getCurrentPosition();
//    }
//
//    public void seekTo(int i) {
//      mp.seekTo(i);
//    }
//
//    public boolean isPlaying() {
//      return mp.isPlaying();
//    }
//
//    public int getBufferPercentage() {
//      return 0;
//    }
//
//    public boolean canPause() {
//      return true;
//    }
//
//    public boolean canSeekBackward() {
//      return true;
//    }
//
//    public boolean canSeekForward() {
//      return true;
//    }
    //------
//
//    @Override
//    public void onCompletion(MediaPlayer MP) {
//        TextView tv = (TextView)findViewById(R.id.curplaying_textview);
//        if(tv!=null){
//            mc.setPlayingText("", tv);
//        }
//        MP.reset();
//        //mc.hide();
//    }

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
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
}
