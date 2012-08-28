package com.thadeus.youtube;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.api.client.util.Key;

public class ThumbNail {


    private class ThumbUrlString{
        public String url;
        public ThumbUrlString(String url){
            this.url = url;
        }
    };
    private class GetThumbnails extends AsyncTask<ThumbUrlString, Integer, HashMap<String, Drawable>>{

        @Override
        protected HashMap<String, Drawable> doInBackground(ThumbUrlString... params) {
            HashMap<String, Drawable> result = new HashMap<String, Drawable>();

            for(int i =0; i<params.length; i++){
                URL url;
                try {
                    url = new URL(params[0].url);
                    InputStream thumbStream = url.openStream();
                } catch (MalformedURLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return result;
        }


    };
//    public ThumbNail(String id){
//        mId = id;
//    }
    private String mId;

    @Key
    String sqDefault;

    @Key
    String hqDefault;
}
