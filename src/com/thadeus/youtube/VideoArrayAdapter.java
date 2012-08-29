package com.thadeus.youtube;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.thadeus.android.R;

public class VideoArrayAdapter<T extends Item> extends BaseAdapter {
    private String TAG = "VideoArrayAdapter";

    private Context mContext;
    private Feed<T> list;
    private ThumbUrlString[] thumbUrlList = (ThumbUrlString[]) Array.newInstance(ThumbUrlString.class, 100);

    //private HashMap<String, Drawable> thumbnails = new HashMap<String, Drawable>();
    private ArrayList<Drawable> thumbnails = new ArrayList<Drawable>();

    private class GetThumbnailsWeb extends AsyncTask<ThumbUrlString, Integer, ArrayList<Drawable>>{
        private String tag = "GetThumbnails";

        @Override
        protected ArrayList<Drawable> doInBackground(ThumbUrlString... params) {
            Log.d(tag, "doInBackGround");
            Context c = VideoArrayAdapter.this.mContext;
            ArrayList<Drawable> result = new ArrayList<Drawable>();

            for(int i =0; params[i]!=null; i++){
                try {
                    URL url = new URL(params[i].url);
                    InputStream thumbStream = url.openStream();
                    BufferedInputStream bis = new BufferedInputStream(thumbStream);
                    Bitmap bitThumb = BitmapFactory.decodeStream(thumbStream);
                    BitmapDrawable thumbnail = new BitmapDrawable(c.getResources(), bitThumb);
                    FileOutputStream f = null;
                    if(list instanceof YoutubeVideoFeed){
                        f = openYoutubeThumbFile(i, c);
                    }
                    else if(list instanceof VimeoVideoFeed){
                        f = openYoutubeThumbFile(i, c);
                    }
                    if(f !=null){
                        BufferedOutputStream bos = new BufferedOutputStream(f);
                        thumbnail.getBitmap().compress(Bitmap.CompressFormat.PNG,
                                                       1, bos);
                        bos.flush();
                        bos.close();
                    }
                    result.add(thumbnail);

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
            return result;
        }
        private FileOutputStream openYoutubeThumbFile(int i, Context c) throws FileNotFoundException{
            FileOutputStream f = c.openFileOutput(((YoutubeVideo)list.items.get(i)).id, Context.MODE_WORLD_READABLE);
            return f;
        }
        private FileOutputStream openVimeoThumbFile(int i, Context c) throws FileNotFoundException{
            Log.e(tag, "openVimeoThumbFile not implemented yet.");
            //FileOutputStream f = c.openFileOutput(((VimeoVideo)list.items.get(i)), Context.MODE_PRIVATE);
            //return f;
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Drawable> results){
            Log.d(tag, "onPostExecute");
            onFinishedDownloadingThumbs(results);
        }
    };

    protected void onFinishedDownloadingThumbs(ArrayList<Drawable> results){
        thumbnails = results;
        notifyDataSetChanged();
        if(mContext instanceof ListActivity){
            Log.d(TAG, "finished downloading thumbnails, invalidating views");
            ((ListActivity) mContext).getListView().invalidateViews();
        }
        else{
            Log.d(TAG, "finished downloading thumbs, activity doesn't seem to be listActivity though ??");
        }
    }

    private class ThumbUrlString{
        public String url;
        public String id;
        public ThumbUrlString(String url){
            this.url = url;
        }
        public ThumbUrlString(String url, String id){
            this.url = url;
            this.id = id;
        }
    };

    public VideoArrayAdapter(Context context, Feed<T> list){
        mContext = context;
        this.list = list;
        ArrayList<ThumbUrlString> thumbList = new ArrayList<ThumbUrlString>();
        if(list instanceof YoutubeVideoFeed){
            buildYouTubeThumbNailUrlsList(thumbList);
        }
        else if(list instanceof VimeoVideoFeed){
            buildVimeoThumbNailsUrlsList(thumbList);
        }
        if(haveThumbsAlready(thumbList)){
            getLocalThumbs(thumbList);
        }
        else{
            thumbList.toArray(thumbUrlList);
            GetThumbnailsWeb gthmbnls = (GetThumbnailsWeb) new GetThumbnailsWeb().execute(thumbUrlList);
        }
    }

    private void getLocalThumbs(ArrayList<ThumbUrlString> thumbs) {
        for(int i=0; i<thumbs.size(); i++){
            File file = mContext.getFileStreamPath(thumbs.get(i).id);
            if(file.exists()){
               // BitmapFactory.Options options = new BitmapFactory.Options();
               // options.inJustDecodeBounds=false;
                Bitmap bitThmb = BitmapFactory.decodeFile(file.getAbsolutePath());
//                BitmapDrawable tn = (BitmapDrawable)
//                        BitmapDrawable.createFromPath(file.getAbsolutePath());
                thumbnails.add(new BitmapDrawable(bitThmb));
            }
            else{
                Log.e(TAG, "local thumbnail file "+file.getAbsolutePath()+" doesn't exist. (but it should!)");
            }
        }
    }

    private boolean haveThumbsAlready(ArrayList<ThumbUrlString> thumbs){
        for(int i=0; i<thumbs.size(); i++){
            File file = mContext.getFileStreamPath(thumbs.get(i).id);
            if(!file.exists()){
                Log.d(TAG, "haveThumbsAlready returning false");
                return false;
            }
        }
        Log.d(TAG, "haveThumbsAlready returning true");
        return true;
    }


    private void buildVimeoThumbNailsUrlsList(ArrayList<ThumbUrlString> thumbList) {
        // TODO Auto-generated method stub
        Log.e(TAG, "buildVimeoThumbNailsUrlsList NOT implemented yet!");
    }


    private void buildYouTubeThumbNailUrlsList(ArrayList<ThumbUrlString> thumbList) {
        ArrayList<YoutubeVideo> vids = (ArrayList<YoutubeVideo>) list.items;
        for(int i=0; i<vids.size(); i++){
            thumbList.add(new ThumbUrlString(vids.get(i).thumbnail.sqDefault, vids.get(i).id));
        }
    }




    @Override
    public int getCount() {
        return list.items.size();
    }

    @Override
    public Object getItem(int position) {
        return list.items.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    //could put some more code in to have getView check position against list for video id.
    //then set fields based on HashMap<videoId, vids> idk. might be worth it.
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.video_listitem_layout, parent, false);
        ImageView imgView = (ImageView) view.findViewById(R.id.vid_list_thumbnail);
        TextView title = (TextView) view.findViewById(R.id.vid_list_title_tv);
        TextView description = (TextView) view.findViewById(R.id.vid_list_title_description);
        T item = (T) getItem(position);
        if(item instanceof YoutubeVideo){
            YoutubeVideo ytv = (YoutubeVideo) item;
            if(position<thumbnails.size()){
                imgView.setImageDrawable(thumbnails.get(position));
            }
            title.setText(item.title);
            //description.setText(((YoutubeVideo) item).description);
            view.setTag(ytv.content.url);
            return view;
        }
        //if(item instanceof)

        return view;
    }


    @Override
    public int getItemViewType(int position) {
        // TODO Auto-generated method stub
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        // TODO Auto-generated method stub
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return true;
    }


    @Override
    public boolean isEmpty() {
        if(list.items.size()==0){
            return true;
        }
        return false;
    }


    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean areAllItemsEnabled() {
        Log.d(TAG, "areAllItemsEnabled - returns true always");
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        Log.d(TAG, "isEnabled - returns true");
        return true;
    }

}
