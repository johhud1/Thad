package com.thadeus.android;

import java.util.Collection;

import com.thadeus.android.MediaService.Song;

public class LFnC {

    public static final int albumSongsListInitialSize = 20;

    public static String album_listkey = "album_key";
    public static String song_listkey = "song_key";
    public static String audio_fileformat = "mp3";
    public static String albumart_fileformat = "jpg";
    public static String bundle_key_expandedGroups = "expanded_groups_array";
    public static int folder_album_depth = 0;
    public static int folder_song_depth = 1;
    public static int file_tracknum_depth = 0;
    public static int file_songname_depth = 1;
    public static int file_extension_depth=2;
    public static int aa_file_extension_depth = 1;

    public static final String youtube_userfeed = "witherberry";
    public static final String youtube_url_authorfield = "author";

    public static final String thumbnail_local_dir = "thumbs";

    public static String youtube_tab_tag = "youtube_tab";
    public static String vimeo_tab_tag = "vimeo_tab";

    public static double skipPrevThresh = 0.25;

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
    public static XAPKFile[] xAPKS = {new XAPKFile(true, 1, 238994939)};

}
