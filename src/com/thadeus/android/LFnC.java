package com.thadeus.android;

public class LFnC {

    public static String album_listkey = "album_key";
    public static String song_listkey = "song_key";
    public static String audio_fileformat = "mp3";
    public static String albumart_fileformat = "jpg";

    public static class XAPKFile {
        public final boolean mIsMain;
        public final int mFileVersion;
        public final long mFileSize;

        XAPKFile(boolean isMain, int fileVersion, long fileSize) {
            mIsMain = isMain;
            mFileVersion = fileVersion;
            mFileSize = fileSize; //in bytes
        }
    }
    public static XAPKFile[] xAPKS = {new XAPKFile(true, 1, 64912563)};

}
