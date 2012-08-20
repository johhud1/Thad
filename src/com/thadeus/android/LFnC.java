package com.thadeus.android;

public class LFnC {

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
    public static XAPKFile[] xAPKS = {new XAPKFile(true, 1, 116964900)};

}
