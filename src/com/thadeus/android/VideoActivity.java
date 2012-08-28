package com.thadeus.android;

import java.net.URI;

import com.thadeus.youtube.YoutubeActivity;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

//public class VideoActivity extends TabActivity {
public class VideoActivity extends TabActivity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_activity_layout);
        TabHost th = getTabHost();
        Resources res = getResources();
        TabSpec yt_spec = th.newTabSpec(LFnC.youtube_tab_tag);
        yt_spec.setIndicator(res.getString(R.string.youtube_tab_label));
        //URI youtube_uri = new URI(uri)
        yt_spec.setContent(new Intent(this, YoutubeActivity.class));
        th.addTab(yt_spec);

        TabSpec vimeo_spec =  th.newTabSpec(LFnC.vimeo_tab_tag);
        vimeo_spec.setIndicator(res.getString(R.string.vimeo_tab_label));
        vimeo_spec.setContent(new Intent(this, VimeoActivity.class));
        th.addTab(vimeo_spec);


    }
}
