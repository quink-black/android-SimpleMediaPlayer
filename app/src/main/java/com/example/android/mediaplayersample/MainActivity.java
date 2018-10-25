/*
 * Copyright 2017 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.mediaplayersample;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.security.keystore.KeyNotYetValidException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Allows playback of a single MP3 file via the UI. It contains a {@link MediaPlayerHolder}
 * which implements the {@link PlayerAdapter} interface that the activity uses to control
 * audio playback.
 */
public final class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    private TextView mTextDebug;
    private SeekBar mSeekbarAudio;
    private ScrollView mScrollContainer;
    private PlayerAdapter mPlayerAdapter;
    private boolean mUserIsSeeking = false;
    private SurfaceView mVideoSurface;
    private float mRate = 1.0f;
    private final String mLocalFile = "/sdcard/test.mp4"; //android.resource://com.example.android.mediaplayersample/raw/" + R.raw.test;
    private final String mNetStream = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8";
    private String mUri = mNetStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeUI();
        initializeSeekbar();
        initializePlaybackController();
        mVideoSurface = (SurfaceView)findViewById(R.id.surfaceView);
        mVideoSurface.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mPlayerAdapter.loadMedia(mUri, mVideoSurface.getHolder().getSurface());
                Log.d(TAG, "onStart: create MediaPlayer");
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
        Log.d(TAG, "onCreate: finished");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isChangingConfigurations() && mPlayerAdapter.isPlaying()) {
            Log.d(TAG, "onStop: don't release MediaPlayer as screen is rotating & playing");
        } else {
            mPlayerAdapter.release();
            Log.d(TAG, "onStop: release MediaPlayer");
        }
    }

    private void initializeUI() {
        mTextDebug = (TextView) findViewById(R.id.text_debug);
        mTextDebug.setTextColor(Color.RED);
        mTextDebug.setTypeface(null, Typeface.BOLD);
        final Button mPlayButton = (Button) findViewById(R.id.button_play);
        Button mPauseButton = (Button) findViewById(R.id.button_pause);
        Button mResetButton = (Button) findViewById(R.id.button_reset);
        Button mSpeedFastButton = (Button) findViewById(R.id.speed_fast);
        Button mSpeedSlowButton = (Button) findViewById(R.id.speed_slow);
        Button mStreamToggleButton = (Button) findViewById(R.id.stream);
        mSeekbarAudio = (SeekBar) findViewById(R.id.seekbar_audio);
        mScrollContainer = (ScrollView) findViewById(R.id.scroll_container);

        mPauseButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPlayerAdapter.pause();
                    }
                });
        mPlayButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPlayerAdapter.play();
                    }
                });
        mResetButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mRate = 1.0f;
                        mPlayerAdapter.reset();
                    }
                });
        mSpeedFastButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mRate < 2)
                            mRate += 0.2f;
                        else
                            mRate += 0.5f;
                        if (mRate >= 16.0f)
                            mRate = 16.0f;
                        mPlayerAdapter.setSpeed(mRate);
                    }
                }
        );
        mSpeedSlowButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mRate < 2)
                            mRate -= 0.2f;
                        else
                            mRate -= 0.5f;
                        if (mRate < 0.0f)
                            mRate = 0.0f;
                        mPlayerAdapter.setSpeed(mRate);
                    }
                }
        );
        mStreamToggleButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mUri.equals(mLocalFile))
                            mUri = mNetStream;
                        else
                            mUri = mLocalFile;
                        mRate = 1.0f;
                        mPlayerAdapter.reset(mUri);
                        mPlayerAdapter.play();
                    }
                }
        );
    }

    private void initializePlaybackController() {
        MediaPlayerHolder mMediaPlayerHolder = new MediaPlayerHolder(this);
        Log.d(TAG, "initializePlaybackController: created MediaPlayerHolder");
        mMediaPlayerHolder.setPlaybackInfoListener(new PlaybackListener());
        mPlayerAdapter = mMediaPlayerHolder;
        Log.d(TAG, "initializePlaybackController: MediaPlayerHolder progress callback set");
    }

    private void initializeSeekbar() {
        mSeekbarAudio.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int userSelectedPosition = 0;

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        mUserIsSeeking = true;
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            userSelectedPosition = progress;
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mUserIsSeeking = false;
                        mPlayerAdapter.seekTo(userSelectedPosition);
                    }
                });
        mSeekbarAudio.setOnKeyListener(
                new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
                                keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                            if (event.getAction() == KeyEvent.ACTION_UP) {
                                mUserIsSeeking = false;
                                if (mPlayerAdapter != null)
                                    mPlayerAdapter.seekTo(mSeekbarAudio.getProgress());
                            } else if (event.getAction() == KeyEvent.ACTION_DOWN) {
                                mUserIsSeeking = true;
                            }
                        }
                        return false;
                    }
                }
        );
    }

    public class PlaybackListener extends PlaybackInfoListener {

        @Override
        public void onDurationChanged(int duration) {
            mSeekbarAudio.setMax(duration);
            Log.d(TAG, String.format("setPlaybackDuration: setMax(%d)", duration));
        }

        @Override
        public void onPositionChanged(int position) {
            if (!mUserIsSeeking) {
                //mSeekbarAudio.setProgress(position, true);
                mSeekbarAudio.setProgress(position);
                Log.d(TAG, String.format("setPlaybackPosition: setProgress(%d)", position));
            }
        }

        @Override
        public void onStateChanged(@State int state) {
            String stateToString = PlaybackInfoListener.convertStateToString(state);
            onLogUpdated(String.format("onStateChanged(%s)", stateToString));
        }

        @Override
        public void onPlaybackCompleted() {
        }

        @Override
        public void onLogUpdated(String message) {
            if (mTextDebug != null) {
                mTextDebug.append(message);
                mTextDebug.append("\n");
                // Moves the scrollContainer focus to the end.
                mScrollContainer.post(
                        new Runnable() {
                            @Override
                            public void run() {
                                mScrollContainer.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });
            }
        }
    }
}