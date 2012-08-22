package com.thadeus.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.android.vending.expansion.zipfile.ZipResourceFile.ZipEntryRO;
import com.google.android.vending.expansion.downloader.Helpers;
import com.thadeus.android.myMediaController.MediaPlayerControl;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewDebug.FlagToString;
import android.widget.TextView;
import android.widget.Toast;

public class MediaService extends Service implements MediaPlayerControl,
                                OnPreparedListener, OnCompletionListener{
    private String TAG = "MediaService";
    private ArrayList<Album> mAlbums;
    private MediaPlayer mp;
    private ZipResourceFile zrf;
    private Song mCurrentlyPlaying=null;
    private boolean mIsBound;
    private String audioFile;
    private myMediaController mc;
    private boolean mIsPlaying;
    private Handler handler = new Handler();

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.media_service_started;
    private NotificationManager mNM;

    private class Song implements Comparable<Song>{
        public Album mAlbum;
        public String mTitle;
        public ZipEntryRO mFile;
        public int index;

        public Song(Album album, String title, ZipEntryRO file, int trackNum){
            mAlbum = album;
            mTitle = title;
            mFile = file;
            index = trackNum;
        }
        @Override
        public int compareTo(Song another) {
            return mTitle.compareTo(another.mTitle);
        }
    }

    private class Album implements Comparable<Album>{
        public String mName;
        public ZipEntryRO mArt;
        ArrayList<Song> mSongs;
        public Album(String name){
            mSongs = new ArrayList<Song>();
            mName = name;
        }
        public Album(String name, ZipEntryRO art){
            mSongs = new ArrayList<Song>();
            mName = name;
            mArt = art;
        }
        public void addSong(Song song){
            mSongs.add(song);
        }
        public void addArt(ZipEntryRO art){
            mArt = art;
        }
        @Override
        public int compareTo(Album another) {
            return mName.compareTo(another.mName);
        }
        @Override
        public boolean equals(Object another){
            return mName.equals(((Album)another).mName);
        }
    }
    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        MediaService getService() {
            return MediaService.this;
        }
    }

    public List createAlbumList(){
        String TAG = "createAlbumList";
        ArrayList<HashMap> result = new ArrayList<HashMap>();
        for(int i=0; i<mAlbums.size(); i++){
            Log.d(TAG, "mAlbums.size = "+mAlbums.size()+" name: "+mAlbums.get(i).mName);
            HashMap h = new HashMap();
            h.put(LFnC.album_listkey, mAlbums.get(i).mName);
            result.add(h);

        }
        return (List)result;
    }

    public List createSongList(){
        String TAG = "createSongList";
        ArrayList<ArrayList<HashMap>> result = new ArrayList<ArrayList<HashMap>>();
        for(int i=0; i<mAlbums.size(); i++){
            ArrayList<HashMap> songs = new ArrayList<HashMap>();
            Album alb = mAlbums.get(i);
            for(int k=0; k<alb.mSongs.size(); k++){
                Log.d(TAG, "alb.mName= "+alb.mName+" has "+alb.mSongs.size()+" number of songs."+
                      " adding song "+alb.mSongs.get(k).mTitle);
                HashMap h = new HashMap();
                h.put(LFnC.song_listkey, alb.mSongs.get(k).mTitle);
                songs.add(h);
            }
            result.add(songs);
        }
        return (List)result;
    }


    @Override
    public void onCreate() {
        String tag = "MediaService-onCreate";
        Log.d(tag, " in onCreate");
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mp = new MediaPlayer();
        mp.setOnPreparedListener(this);
        mp.setOnCompletionListener(this);
        mAlbums = new ArrayList<Album>();


        String filename = Helpers.getSaveFilePath(this) + "/" +
        Helpers.getExpansionAPKFileName(this, LFnC.xAPKS[0].mIsMain, LFnC.xAPKS[0].mFileVersion);
        Log.d(TAG, "got filename "+filename+" for expansionAPK filename");
        try {
            zrf = APKExpansionSupport.getResourceZipFile(new String[] {filename});
            ZipEntryRO[] zipEntries = zrf.getAllEntries();
            for(int i=0; i<zipEntries.length; i++){
                String zfilename = zipEntries[i].mFileName;
                if(zipEntries[0].isUncompressed()){
                    Log.d(TAG, "zip entry "+i+" name: "+ zfilename+" is uncompressed");
                }
                else Log.d(TAG, "zip entry "+i+" name: "+ zfilename+" is compressed");

            }
            parseAlbumFiles(zipEntries);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Display a notification about us starting.  We put an icon in the status bar.
        //showNotification();
    }

    public void parseAlbumFiles(ZipEntryRO[] ze){
        String TAG = "parseAlbumFiles";
        for(int i=0; i<ze.length; i++){
            String fn = ze[i].mFileName;
            String[] splitFolder = fn.split("/");
            String[] splitFiles;
            Song song;
            Album album;
            if(splitFolder.length>2){
                Log.d(TAG, "split into strings "+splitFolder[1]+" / "+splitFolder[2]+" of length "+splitFolder.length);
                album = new Album(splitFolder[1]);
                if(!mAlbums.contains(album)){
                    Log.d(TAG, "couldn't find album "+album.mName+" in existing list, adding album");
                    mAlbums.add(album);
                }else{
                    album = mAlbums.get(mAlbums.indexOf(album));
                }
                splitFiles = splitFolder[2].split("[.]");
                Log.d(TAG, "splitFiles,split of string "+splitFolder[2]+" on '.' has length "+splitFiles.length);
                if(splitFiles.length > 1){
                    if(splitFiles[1].equals(LFnC.audio_fileformat)){
                        Log.d(TAG, "found file "+splitFolder[1]+
                              ", think it's a song. making new song, with album "
                              +album.mName+", song title "+splitFiles[0]);
                        song = new Song(album, splitFiles[0], ze[i], album.mSongs.size());
                        album.addSong(song);
                    }
                    if(splitFiles[1].equals(LFnC.albumart_fileformat)){
                        Log.d(TAG, "found file "+splitFolder[2]+", think it's art. adding it to album "+album.mName);
                        album.addArt(ze[i]);
                    }
                }
            }
            else{
                Log.d(TAG, "split had length "+splitFolder.length+", which is < 2");
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
//        Notification n = new Notification(android.R.drawable.btn_star, "startin mediaService Foreground notification",
//                                          System.currentTimeMillis());
//        startForeground(startId, n);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("MediaService onBind", "onBinding, about to 'startForeground'");
//        startForeground(NOTIFICATION, buildNotification());
        return mBinder;
    }

    private Notification buildNotification(){
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = mCurrentlyPlaying.mTitle;

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(android.R.drawable.ic_media_play, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, AlbumActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                                               | Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.media_service_label),
                       text, contentIntent);
        return notification;
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // Send the notification.
        mNM.notify(NOTIFICATION, buildNotification());
    }

    private void skipNext(){
        playSong(mCurrentlyPlaying.mAlbum.mSongs.get(mCurrentlyPlaying.index+1));
    }
    private void skipPrev(){
        playSong(mCurrentlyPlaying.mAlbum.mSongs.get(mCurrentlyPlaying.index-1));
    }
    private boolean playSong(Song song){
        String tag = "playSong";
        ZipEntryRO zfile = song.mFile;
        Log.d(tag, "It is unCompressed"+zfile.isUncompressed());
        if(zfile.isUncompressed()){
            Log.d(tag, "in 'isUncompressed code block");
            mp.reset();
            AssetFileDescriptor afd = zrf.getAssetFileDescriptor(zfile.mFileName);
            mCurrentlyPlaying = song; //.mTitle + " - "+song.mAlbum.mName;
            //buildNotification() depends on mCurrentlyPlaying being set. so yeah
            startForeground(NOTIFICATION, buildNotification());
            setPrevNextListeners();
            mc.setEnabled(true);
            try {
                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                mp.prepare();
                return true;
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        Log.e(TAG, "audio files are compressed, can't get filedescriptor");
        return false;
    }


    //--MediaPlayerControl methods----------------------------------------------------
    public void start() {
      mc.setPlayingText(mCurrentlyPlaying.mTitle + " - "+mCurrentlyPlaying.mAlbum.mName);
      mIsPlaying = true;
      startForeground(NOTIFICATION, buildNotification());
      mp.start();
    }

    @Override
    public boolean isCurrentSongSet(){
        if(mCurrentlyPlaying!=null){
            return true;
        }
        return false;
    }
    public void pause() {
      mIsPlaying = false;
      mp.pause();
    }

    public int getDuration() {
      return mp.getDuration();
    }

    public int getCurrentPosition() {
      Log.d("getCurrentPosition", "mPlayer.isPlaying(): "+mp.isPlaying());
      return mp.getCurrentPosition();
    }

    public void seekTo(int i) {
      mp.seekTo(i);
    }

    public boolean isPlaying() {
      return mp.isPlaying();
    }

    public int getBufferPercentage() {
      return 0;
    }

    public boolean canPause() {
      return true;
    }

    public boolean canSeekBackward() {
      return true;
    }

    @Override
    public String getCurrentSongTitle() {
        if(mCurrentlyPlaying!=null){
            return mCurrentlyPlaying.mTitle;
        }
        return null;
    }

    public boolean canSeekForward() {
      return true;
    }
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
    public void setPrevNextListeners(){
        String tag = "MediaService-setPrevNextListeners";
        View.OnClickListener nextClickListener = null;
        if(mCurrentlyPlaying.index < (mCurrentlyPlaying.mAlbum.mSongs.size()-1)){
            nextClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    skipNext();
                }
            };
        }
        View.OnClickListener prevClickListener = null;
        if(mCurrentlyPlaying.index > 0){
            prevClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    skipPrev();
                }
            };
        }
        if(mc!=null){
            mc.setPrevNextListeners(nextClickListener, prevClickListener);
        }
        else Log.e(tag, "couldn't setPrevNextListeners");
    }
    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d("onPrepared", "in onPrepared");
        mc.doPauseResume();

//        handler.post(new Runnable() {
//            public void run() {
//
//            }
//          });
    }

    public void playGroupChildPos(int groupPosition, int childPosition){
        String tag = "playGroupChildPos";
        Song song = mAlbums.get(groupPosition).mSongs.get(childPosition);
        Log.d(tag, "group position "+groupPosition+" childPosition "+childPosition+". Got song "
              +song.mTitle);
        playSong(song);
    }

    @Override
    public void onCompletion(MediaPlayer MP) {
        Log.d(TAG, "onCompletion, about to update notification");
//        TextView tv = (TextView)findViewById(R.id.curplaying_textview);
//        if(tv!=null){
//            mc.setPlayingText("", tv);
//        }
        MP.reset();
        int nextIndex = mCurrentlyPlaying.index+1;
        ArrayList<Song> songs = mCurrentlyPlaying.mAlbum.mSongs;
        if( nextIndex < songs.size()){
            playSong(songs.get(nextIndex));
            return;
        }
        mIsPlaying = false;
        stopForeground(true);
    }

    public void setMediaController(myMediaController mc) {
        String tag = "setMediaController";
        if(mc!=null){
            this.mc = mc;
            mc.setMediaPlayer(this);
        }
        else{
            Log.e(tag, "MediaController argument is NULL!");
        }
    }


}
