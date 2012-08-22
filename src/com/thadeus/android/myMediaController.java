package com.thadeus.android;

import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;

    /**
     * A view containing controls for a MediaPlayer. Typically contains the
     * buttons like "Play/Pause", "Rewind", "Fast Forward" and a progress
     * slider. It takes care of synchronizing the controls with the state
     * of the MediaPlayer.
     * <p>
     * The way to use this class is to instantiate it programatically.
     * The MediaController will create a default set of controls
     * and put them in a window floating above your application. Specifically,
     * the controls will float above the view specified with setAnchorView().
     * The window will disappear if left idle for three seconds and reappear
     * when the user touches the anchor view.
     * <p>
     * Functions like show() and hide() have no effect when MediaController
     * is created in an xml layout.
     *
     * MediaController will hide and
     * show the buttons according to these rules:
     * <ul>
     * <li> The "previous" and "next" buttons are hidden until setPrevNextListeners()
     *   has been called
     * <li> The "previous" and "next" buttons are visible but disabled if
     *   setPrevNextListeners() was called with null listeners
     * <li> The "rewind" and "fastforward" buttons are shown unless requested
     *   otherwise by using the MediaController(Context, boolean) constructor
     *   with the boolean set to false
     * </ul>
     */
    public class myMediaController extends FrameLayout {

        public static final int    SHOW_PROGRESS = 2;

        private MediaPlayerControl  mPlayer;
        private Context             mContext;
        private View                mRoot;
        private ProgressBar         mProgress;
        private TextView            mEndTime, mCurrentTime;
        private boolean             mShowing;
        private boolean             mDragging;
        private boolean             mUseFastForward;
        private boolean             mListenersSet;
        private View.OnClickListener mNextListener, mPrevListener;
        StringBuilder               mFormatBuilder;
        Formatter                   mFormatter;
        private ImageButton         mPauseButton;
        private ImageButton         mFfwdButton;
        private ImageButton         mRewButton;
        private ImageButton         mNextButton;
        private ImageButton         mPrevButton;

        public myMediaController(Context context, AttributeSet attrs) {
            super(context, attrs);
            mContext = context;
            mUseFastForward = true;
            mRoot = this.makeControllerView(this, context);
        }

        @Override
        public void onFinishInflate() {
            super.onFinishInflate();
            if (mRoot != null){
                initControllerView(mRoot);
            }
            else{
                Log.e("onFinishInflate", "mRoot is NULL");
            }
        }

        public myMediaController(Context context, boolean useFastForward) {
            super(context);
            mContext = context;
            mUseFastForward = useFastForward;
        }

        public myMediaController(Context context) {
            super(context);
            mContext = context;
            mUseFastForward = true;
        }

        private OnTouchListener mTouchListener = new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    if (mShowing) {
//                        hide();
//                    }
                }
                return false;
            }
        };
        private void updateButtonsEnabled(){
            if(mPlayer!=null){
                setEnabled(mPlayer.isCurrentSongSet());
            }
        }

        public void setMediaPlayer(MediaPlayerControl player) {
            mPlayer = player;
            updateButtonsEnabled();
            updatePausePlay();
        }

        /**
         * Set the view that acts as the anchor for the control view.
         * This can for example be a VideoView, or your Activity's main view.
         * @param view The view to which to anchor the controller when it is visible.
         */
        public void setAnchorView(View view) {
            Log.d("setAnchorView", "view(arg): "+view.toString()+" setting AnchorView and exec makeControllerView");
            removeAllViews();
            View v = makeControllerView(view, mContext);
            mShowing = true;
            setEnabled(false);
        }

        /**
         * Create the view that holds the widgets that control playback.
         * Derived classes can override this to create their own.
         * @return The controller view.
         * @hide This doesn't work as advertised
         */
        private View makeControllerView(View root, Context context) {
            LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mRoot = inflate.inflate(R.layout.mymediacontroller_layout, (ViewGroup) root);
            //initControllerView(mRoot);
            return mRoot;
        }

        private void initControllerView(View v) {
            String tag = "initControllerView";
            mPauseButton = (ImageButton) v.findViewById(R.id.mc_pp);
            if (mPauseButton != null) {
                mPauseButton.requestFocus();
                mPauseButton.setOnClickListener(mPauseListener);
            }
            else{ Log.e(tag, "mPauseButton not set. it's NULL!");}

            mFfwdButton = (ImageButton) v.findViewById(R.id.mc_ff);
            if (mFfwdButton != null) {
                mFfwdButton.setOnClickListener(mFfwdListener);
            }
            else{ Log.e(tag, "mPFfwdButton not set. it's NULL!");}

            mRewButton = (ImageButton) v.findViewById(R.id.mc_rew);
            if (mRewButton != null) {
                mRewButton.setOnClickListener(mRewListener);
            }
            else{ Log.e(tag, "mRewButton not set. it's NULL!");}

            // By default these are hidden. They will be enabled when setPrevNextListeners() is called
            mNextButton = (ImageButton) v.findViewById(R.id.mc_next);
            if (mNextButton != null && !mListenersSet) {
                mNextButton.setEnabled(false);
            }
            mPrevButton = (ImageButton) v.findViewById(R.id.mc_prev);
            if (mPrevButton != null && !mListenersSet) {
                mPrevButton.setEnabled(false);
            }

            mProgress = (ProgressBar) v.findViewById(R.id.mc_progbar);
            if (mProgress != null) {
                if (mProgress instanceof SeekBar) {
                    SeekBar seeker = (SeekBar) mProgress;
                    seeker.setOnSeekBarChangeListener(mSeekListener);
                }
                mProgress.setMax(1000);
            }

            mEndTime = (TextView) v.findViewById(R.id.mc_endtime);
            mCurrentTime = (TextView) v.findViewById(R.id.mc_currenttime);
            mFormatBuilder = new StringBuilder();
            mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

            installPrevNextListeners();
        }

        /**
         * Disable pause or seek buttons if the stream cannot be paused or seeked.
         * This requires the control interface to be a MediaPlayerControlExt
         */
        private void disableUnsupportedButtons() {
            try {
                if (mPauseButton != null && !mPlayer.canPause()) {
                    mPauseButton.setEnabled(false);
                }
                if (mRewButton != null && !mPlayer.canSeekBackward()) {
                    mRewButton.setEnabled(false);
                }
                if (mFfwdButton != null && !mPlayer.canSeekForward()) {
                    mFfwdButton.setEnabled(false);
                }
            } catch (IncompatibleClassChangeError ex) {
                // We were given an old version of the interface, that doesn't have
                // the canPause/canSeekXYZ methods. This is OK, it just means we
                // assume the media can be paused and seeked, and so we don't disable
                // the buttons.
            }
        }

        public void checkForProgress(){
            if(mPlayer.isPlaying()){
                setEnabled(true);
                mHandler.sendEmptyMessage(SHOW_PROGRESS);
            }
        }

        public boolean isShowing() {
            return mShowing;
        }

        public void updateUI(){
            if(mPlayer.isCurrentSongSet()){
                setPlayingText(mPlayer.getCurrentSongTitle());
                mPlayer.setPrevNextListeners();
                setEnabled(true);
                mHandler.sendEmptyMessage(SHOW_PROGRESS);
                return;
            }
        }

        public void setPlayingText(String text){
            String tag = "setPlayingText";
            String dispText = getContext().getResources().getString(R.string.curplaying_text) + " "+text;
            TextView view = (TextView) getRootView().findViewById(R.id.curplaying_textview);
            if(view!=null){
                view.setText(dispText);
            }
            else{
                Log.d(tag, "couldn't set CurrentlyPlaying text, TextView is null");
            }
        }

        public Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                int pos;
                switch (msg.what) {
//                    case FADE_OUT:
//                        hide();
//                        break;
                    case SHOW_PROGRESS:
                        pos = setProgress();
                        if(mPlayer != null && mPlayer.isPlaying()){
                            if (!mDragging && mPlayer.isPlaying()) {
                                msg = obtainMessage(SHOW_PROGRESS);
                                sendMessageDelayed(msg, 1000 - (pos % 1000));
                            }
                            break;
                        }
                }
            }
        };
        public void emptyQueue(){
            Log.d("emptyQueue", "emptying the queue");
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        private String stringForTime(int timeMs) {
            int totalSeconds = timeMs / 1000;

            int seconds = totalSeconds % 60;
            int minutes = (totalSeconds / 60) % 60;
            int hours   = totalSeconds / 3600;

            mFormatBuilder.setLength(0);
            if (hours > 0) {
                return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
            } else {
                return mFormatter.format("%02d:%02d", minutes, seconds).toString();
            }
        }

        private int setProgress() {
            if (mPlayer == null || mDragging) {
                return 0;
            }
            int position = mPlayer.getCurrentPosition();
            int duration = mPlayer.getDuration();
            if (mProgress != null) {
                if (duration > 0) {
                    // use long to avoid overflow
                    long pos = 1000L * position / duration;
                    mProgress.setProgress( (int) pos);
                }
                int percent = mPlayer.getBufferPercentage();
                mProgress.setSecondaryProgress(percent * 10);
            }

            if (mEndTime != null)
                mEndTime.setText(stringForTime(duration));
            if (mCurrentTime != null)
                mCurrentTime.setText(stringForTime(position));

            return position;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
           // mHandler.sendEmptyMessage(SHOW_PROGRESS);
            return false;
//            show(sDefaultTimeout);
//            return true;
        }

//        @Override
//        public boolean onTrackballEvent(MotionEvent ev) {
//            show(sDefaultTimeout);
//            return false;
//        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            int keyCode = event.getKeyCode();
            if (event.getRepeatCount() == 0 && (event.getAction()==KeyEvent.ACTION_DOWN) && (
                    keyCode ==  KeyEvent.KEYCODE_HEADSETHOOK ||
                    keyCode ==  KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ||
                    keyCode ==  KeyEvent.KEYCODE_SPACE)) {
                Log.d("dispatchKeyEvent", "about to doPauseResume in dispatchKeyEvent");
                doPauseResume();
                //mHandler.sendEmptyMessage(SHOW_PROGRESS);
                //show(sDefaultTimeout);
                if (mPauseButton != null) {
                    mPauseButton.requestFocus();
                }
                return true;
            } else if (keyCode ==  KeyEvent.KEYCODE_MEDIA_STOP) {
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                    updatePausePlay();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
                    keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                // don't show the controls for volume adjustment
                return super.dispatchKeyEvent(event);
            } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
                //hide();

                return true;
            } else {
                //show(sDefaultTimeout);
            }
            return super.dispatchKeyEvent(event);
        }

        private View.OnClickListener mPauseListener = new View.OnClickListener() {
            public void onClick(View v) {
                doPauseResume();
                //show(sDefaultTimeout);
            }
        };

        private void updatePausePlay() {
            String tag = "updatePausePlay";
            if (mRoot == null || mPauseButton == null){
                Log.e(tag, "mRoot: "+mRoot+" or mPauseButton: "+mPauseButton+" are null. FAILING");
                return;
            }
            if (mPlayer.isPlaying()) {
                mPauseButton.setImageResource(android.R.drawable.ic_media_pause);
            } else {
                mPauseButton.setImageResource(android.R.drawable.ic_media_play);
            }
        }

        public void doPauseResume() {
            String tag = "doPauseResume";
            Log.d(tag, "about to updatePausePlay and send message etc");
            if (mPlayer.isPlaying()) {
                Log.d(tag, "mPlayer is playing");
                mPlayer.pause();
            } else {
                Log.d(tag, "mPlayer is not playing");
                mPlayer.start();
            }
            updatePausePlay();
            mHandler.sendEmptyMessage(SHOW_PROGRESS);

        }

        // There are two scenarios that can trigger the seekbar listener to trigger:
        //
        // The first is the user using the touchpad to adjust the posititon of the
        // seekbar's thumb. In this case onStartTrackingTouch is called followed by
        // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
        // We're setting the field "mDragging" to true for the duration of the dragging
        // session to avoid jumps in the position in case of ongoing playback.
        //
        // The second scenario involves the user operating the scroll ball, in this
        // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
        // we will simply apply the updated position without suspending regular updates.
        private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar bar) {
                //show(3600000);

                mDragging = true;

                // By removing these pending progress messages we make sure
                // that a) we won't update the progress while the user adjusts
                // the seekbar and b) once the user is done dragging the thumb
                // we will post one of these messages to the queue again and
                // this ensures that there will be exactly one message queued up.
                mHandler.removeMessages(SHOW_PROGRESS);
            }

            public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
                if (!fromuser) {
                    // We're not interested in programmatically generated changes to
                    // the progress bar's position.
                    return;
                }

                long duration = mPlayer.getDuration();
                long newposition = (duration * progress) / 1000L;
                mPlayer.seekTo( (int) newposition);
                if (mCurrentTime != null)
                    mCurrentTime.setText(stringForTime( (int) newposition));
            }

            public void onStopTrackingTouch(SeekBar bar) {
                String tag = "onStopTrackingTouch";
                mDragging = false;
                setProgress();
                updatePausePlay();
                //show(sDefaultTimeout);

                // Ensure that progress is properly updated in the future,
                // the call to show() does not guarantee this because it is a
                // no-op if we are already showing.
                mHandler.sendEmptyMessage(SHOW_PROGRESS);
            }
        };

        @Override
        public void setEnabled(boolean enabled) {
            if (mPauseButton != null) {
                mPauseButton.setEnabled(enabled);
            }
            if (mFfwdButton != null) {
                mFfwdButton.setEnabled(enabled);
            }
            if (mRewButton != null) {
                mRewButton.setEnabled(enabled);
            }
            if (mNextButton != null) {
                mNextButton.setEnabled(enabled && mNextListener != null);
            }
            if (mPrevButton != null) {
                mPrevButton.setEnabled(enabled && mPrevListener != null);
            }
            if (mProgress != null) {
                mProgress.setEnabled(enabled);
            }
            if(mPlayer != null){
                disableUnsupportedButtons();
            }
            super.setEnabled(enabled);
        }

        private View.OnClickListener mRewListener = new View.OnClickListener() {
            public void onClick(View v) {
                int pos = mPlayer.getCurrentPosition();
                pos -= 5000; // milliseconds
                mPlayer.seekTo(pos);
                setProgress();

                //show(sDefaultTimeout);
            }
        };

        private View.OnClickListener mFfwdListener = new View.OnClickListener() {
            public void onClick(View v) {
                int pos = mPlayer.getCurrentPosition();
                pos += 15000; // milliseconds
                mPlayer.seekTo(pos);
                setProgress();

                //show(sDefaultTimeout);
            }
        };

        private void installPrevNextListeners() {
            if (mNextButton != null) {
                mNextButton.setOnClickListener(mNextListener);
                mNextButton.setEnabled(mNextListener != null);
            }

            if (mPrevButton != null) {
                mPrevButton.setOnClickListener(mPrevListener);
                mPrevButton.setEnabled(mPrevListener != null);
            }
        }

        public void updatePrevNextButtonEnabled(){
            if(mRoot!=null){
                if(mNextListener != null){
                    mNextButton.setEnabled(true);
                } else mNextButton.setEnabled(false);
                if(mPrevListener != null){
                    mPrevButton.setEnabled(true);
                } else mPrevButton.setEnabled(false);
            }
        }

        public void setPrevNextListeners(View.OnClickListener next, View.OnClickListener prev) {
            mNextListener = next;
            mPrevListener = prev;
            mListenersSet = true;

            if (mRoot != null) {
                installPrevNextListeners();
            }
        }

        public interface MediaPlayerControl {
            void    start();
            void    pause();
            int     getDuration();
            int     getCurrentPosition();
            void    seekTo(int pos);
            boolean isPlaying();
            int     getBufferPercentage();
            boolean canPause();
            boolean canSeekBackward();
            boolean canSeekForward();
            void setPrevNextListeners();
            boolean isCurrentSongSet();
            String getCurrentSongTitle();
        }
    }

