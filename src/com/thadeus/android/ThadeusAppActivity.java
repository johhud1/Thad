package com.thadeus.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ThadeusAppActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void gotoActivity(View view){
        Intent intent = null;
        switch(view.getId()){
        case (R.id.video_layout):
            intent = new Intent(this, VideoActivity.class);
            break;
        case (R.id.album_layout):
            intent = new Intent(this, AlbumActivity.class);
            break;
        case (R.id.link_layout):
            intent = new Intent(this, LinkActivity.class);
        }
        startActivity(intent);
    }
}
