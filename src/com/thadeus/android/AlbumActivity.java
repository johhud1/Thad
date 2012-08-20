package com.thadeus.android;


import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.android.vending.expansion.zipfile.ZipResourceFile.ZipEntryRO;
import com.google.android.vending.expansion.downloader.Helpers;
import com.google.android.vending.licensing.APKExpansionPolicy;
import com.google.android.vending.licensing.Obfuscator;
import com.google.android.vending.licensing.ValidationException;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;


public class AlbumActivity extends Activity implements OnPreparedListener {
    private String TAG = "AlbumActivity";
    private String audioFile;
    private ArrayList<Album> mAlbums;

    private class Song implements Comparable<Song>{
        public Album mAlbum;
        public String mTitle;
        public ZipEntryRO mFile;

        public Song(Album album, String title, ZipEntryRO file){
            mAlbum = album;
            mTitle = title;
            mFile = file;
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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_activity_layout);

        mAlbums = new ArrayList<Album>();

        MediaPlayer mp =  new MediaPlayer();
        mp.setOnPreparedListener(this);

        MediaController mc = new MediaController(this);
        mc.setAnchorView(this.findViewById(R.id.album_activity_layout));


        String filename = Helpers.getSaveFilePath(this) + "/" +
                Helpers.getExpansionAPKFileName(this, LFnC.xAPKS[0].mIsMain, LFnC.xAPKS[0].mFileVersion);
        Log.d(TAG, "got filename "+filename+" for expansionAPK filename");
        try {
            ZipResourceFile zrf = APKExpansionSupport.getResourceZipFile(new String[] {filename});
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


    }

    public void parseAlbumFiles(ZipEntryRO[] ze){
        String TAG = "parseAlbumFiles";
        for(int i=0; i<ze.length; i++){
            String fn = ze[i].mFileName;
            String[] splitFolder = fn.split("/");
            String[] splitFiles;
            Song song;
            Album album;
            if(splitFolder.length>1){
                Log.d(TAG, "split into strings "+splitFolder[0]+" / "+splitFolder[1]+" of length "+splitFolder.length);
                album = new Album(splitFolder[0]);
                if(!mAlbums.contains(album)){
                    Log.d(TAG, "couldn't find album "+album.mName+" in existing list, adding album");
                    mAlbums.add(album);
                }else{
                    album = mAlbums.get(mAlbums.indexOf(album));
                }
                splitFiles = splitFolder[1].split("[.]");
                Log.d(TAG, "splitFiles,split of string "+splitFolder[1]+" on '.' has length "+splitFiles.length);
                if(splitFiles.length > 1){
                    if(splitFiles[1].equals(LFnC.audio_fileformat)){
                        Log.d(TAG, "found file "+splitFolder[1]+
                              ", think it's a song. making new song, with album "
                              +album.mName+", song title "+splitFiles[0]);
                        song = new Song(album, splitFiles[0], ze[i]);
                        album.addSong(song);
                    }
                    if(splitFiles[1].equals(LFnC.albumart_fileformat)){
                        Log.d(TAG, "found file "+splitFolder[1]+", think it's art. adding it to album "+album.mName);
                        album.addArt(ze[i]);
                    }
                }
            }
            else{
                Log.d(TAG, "split had length "+splitFolder.length+", which is < 2");
            }

        }
    }

    public void onPrepared(MediaPlayer mp) {
        // TODO Auto-generated method stub

    }
}
