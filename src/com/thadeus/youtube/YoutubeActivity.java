package com.thadeus.youtube;

import java.util.List;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.Key;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.bind.ArrayTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.thadeus.android.LFnC;
import com.thadeus.android.R;
import com.thadeus.android.R.id;
import com.thadeus.android.R.layout;
import com.thadeus.android.R.string;


import android.app.ListActivity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;


public class YoutubeActivity extends ListActivity implements OnItemClickListener {

    private static NetHttpTransport transport = new NetHttpTransport();

    final static JsonFactory jsonFactory = new JacksonFactory();

    private static final String tag = "YoutubeActivity";


    private void run() throws Exception {
        HttpRequestFactory requestFactory =
            transport.createRequestFactory(new HttpRequestInitializer() {
                @Override
              public void initialize(HttpRequest request) {
                request.setParser(new JsonObjectParser(jsonFactory));
              }
            });

        YouTubeClient client = new YouTubeClient();
        YouTubeUrl url = YouTubeUrl.UsersFeed(LFnC.youtube_userfeed);

        //url.author = LFnC.youtube_userfeed;
        // execute GData request for the feed
        YoutubeVideoFeed feed = client.executeGetVideoFeed(url);

        VideoArrayAdapter<YoutubeVideo> adapter = new VideoArrayAdapter<YoutubeVideo>(this, feed);
        getListView().setAdapter(adapter);
        getListView().setOnItemClickListener(this);
        TextView tv = (TextView) findViewById(R.id.youtube_video_tv);
        //showFeed(tv, feed);
    }

    private void showFeed(TextView tv, Feed<? extends Item> feed){
        for(int i=0; i<feed.getItems().size(); i++){
            tv.setText(feed.items.get(i).title);
        }
        YoutubeVideo vid = (YoutubeVideo) feed.items.get(0);
        Intent videoPlayer = new Intent(null, Uri.parse(vid.content.url)
                                        , this, IntroVideoActivity.class);
        startActivity(videoPlayer);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtube_activity_layout);
        String appName = getResources().getString(R.string.app_name);

        try {
            run();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long row) {
        Log.d(tag, "onItemClick");
        String sUrl = (String)view.getTag();
        Intent videoPlayer = new Intent(null, Uri.parse(sUrl)
                                        , this, IntroVideoActivity.class);
        startActivity(videoPlayer);
    }

}
