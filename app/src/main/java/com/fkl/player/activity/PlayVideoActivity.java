package com.fkl.player.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fkl.player.R;
import com.fkl.player.entity.MediaItem;
import com.fkl.player.utils.Constants;
import com.fkl.player.utils.Utils;
import com.fkl.player.view.VideoView;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PlayVideoActivity extends Activity implements View.OnClickListener {
    private static final int PROGRESS = 1;
    private static final int HIDE = 2;
    private static final int VOICECHANGE = 3;
    private  static final int SHOWSPEED=4;
    private static final int FULLSCREEN = 1;
    private static final int LIGHTCONTROLLER = 5;
    private static final  int PLAYCONTROLLER=6;
    private static final  int BACKUPCONTROLLER=7;
    private static final  int FASTFARWARDCONTROLLER=8;
    private static final int DEFUALTSCREEN = 0;

    private static final String TAG = PlayVideoActivity.class.getSimpleName();
    private RelativeLayout rlPlayController;
    private LinearLayout llBackUp;
    private LinearLayout llFastForward;
    private TextView tvBackUp;
    private TextView tvFastForward;

    private LinearLayout llTop;
    private TextView tvVideoName;
    private ImageView ivBattery;
    private TextView tvTime;
    private ImageView btnVoice;
    private SeekBar voiceSeekBar;
    private  SeekBar lightSeekBar;

    private Button btnSwitchPlayer;
    private LinearLayout llBottom;
    private TextView tvCurrTime;
    private SeekBar videoSeekBar;
    private TextView tvTotalTime;
    private Button btnExit;
    private Button btnVideoPre;
    private Button btnVideoStartPause;
    private Button btnVideoNext;
    private Button btnVideoSiwchScreen;
    private LinearLayout llVoiceController;
   private  LinearLayout llLightController;
    private LinearLayout llLoading;
    private  TextView tvLoadingSpeed;
    private LinearLayout llBuffer;
    private  TextView tvNetSpeed;

    private Utils utils = new Utils();
    private VideoView vd;
    private Uri uri;
    private MyReciver myReciver;
    private ArrayList<MediaItem> videolist;
    private int position;
    private GestureDetector detector;//定义 手势识别器
    private boolean isShowMediaController = false;
    private boolean isShowVoiceController = false;
    private boolean isFullScreen = false;
    private int screenWidth = 0;
    private int screenHeight = 0;
    private int videoWidth = 0;
    private int videoHeight = 0;
    private AudioManager am;
    private int currentVoice;
    //音量 0-15
    private int maxVoice = 15;
    private int maxLight = 255;
    private int currentLight;
    private boolean isMute = false;
    private Vibrator vibrator;
    private boolean netUri = false;
    private boolean isUserSystem=false;
    private int prePosition;
    private void  hidePlayController(){
        rlPlayController.setVisibility(View.GONE);
        llFastForward.setVisibility(View.GONE);
        llBackUp.setVisibility(View.GONE);
    }
    private void showFastForwardController(){
        rlPlayController.setVisibility(View.VISIBLE);
        llFastForward.setVisibility(View.VISIBLE);
        llBackUp.setVisibility(View.GONE);
    }
    private void showBackUpController(){
        rlPlayController.setVisibility(View.VISIBLE);
        llFastForward.setVisibility(View.GONE);
        llBackUp.setVisibility(View.VISIBLE);
    }
    private void findViews() {
        rlPlayController= (RelativeLayout) findViewById(R.id.rl_play_controller);
        llBackUp= (LinearLayout) findViewById(R.id.ll_back_up);
        llFastForward= (LinearLayout) findViewById(R.id.ll_fast_forward);
        tvFastForward= (TextView) findViewById(R.id.tv_fast_forward_time);
        tvBackUp= (TextView) findViewById(R.id.tv_back_up_time);
        vd = (VideoView) findViewById(R.id.vd);
        llTop = (LinearLayout) findViewById(R.id.ll_top);
        tvVideoName = (TextView) findViewById(R.id.tv_video_name);
        ivBattery = (ImageView) findViewById(R.id.iv_battery);
        tvTime = (TextView) findViewById(R.id.tv_time);
        btnVoice = (ImageView) findViewById(R.id.btn_voice);
        voiceSeekBar = (SeekBar) findViewById(R.id.voice_seek_bar);
        btnSwitchPlayer = (Button) findViewById(R.id.btn_switch_player);
        llBottom = (LinearLayout) findViewById(R.id.ll_bottom);
        tvCurrTime = (TextView) findViewById(R.id.tv_curr_time);
        videoSeekBar = (SeekBar) findViewById(R.id.video_seek_bar);
        tvTotalTime = (TextView) findViewById(R.id.tv_total_time);
        btnExit = (Button) findViewById(R.id.btn_exit);
        btnVideoPre = (Button) findViewById(R.id.btn_video_pre);
        btnVideoStartPause = (Button) findViewById(R.id.btn_video_start_pause);
        btnVideoNext = (Button) findViewById(R.id.btn_video_next);
        btnVideoSiwchScreen = (Button) findViewById(R.id.btn_video_siwch_screen);
        llVoiceController = (LinearLayout) findViewById(R.id.ll_voice_controller);
        llLightController= (LinearLayout) findViewById(R.id.ll_light_controller);
        lightSeekBar= (SeekBar) findViewById(R.id.light_seek_bar);
        // rlSwitchPlayer = (RelativeLayout) findViewById(R.id.rl_switch_player);
       llBuffer= (LinearLayout) findViewById(R.id.ll_buffer);
        tvNetSpeed=  (TextView) findViewById(R.id.tv_netspeed);
          llLoading=  (LinearLayout) findViewById(R.id.ll_loading);
        tvLoadingSpeed= (TextView) findViewById(R.id.tv_netspeed_loading);
        hideVoiceController();
        hidePlayController();
    }


    @Override
    public void onClick(View v) {
        if (v == btnVoice) {
            isMute = !isMute;
            updateVoice(currentVoice, isMute);
        } else if (v == btnSwitchPlayer) {
            showSwichPlayerDialog();
        } else if (v == btnExit) {
            // Handle clicks for btnExit
            finish();
        } else if (v == btnVideoPre) {
            playLastVideo();
        } else if (v == btnVideoStartPause) {
            startOrPause();
        } else if (v == btnVideoNext) {
            playNextVideo();
        } else if (v == btnVideoSiwchScreen) {
            // Handle clicks for btnVideoSiwchScreen
            if (isFullScreen) {
                setVideoType(DEFUALTSCREEN);
            } else {
                setVideoType(FULLSCREEN);
            }
        }
    }


    private void showSwichPlayerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("系统播放器提醒您");
        builder.setMessage("当您播放视频，有声音没有画面的时候，请切换万能播放器播放");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               startToVitamioActivity();
            }
        });
        builder.setNegativeButton("取消",null);
        builder.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 注意顺序
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_play_video);
        findViews();
        Log.e(TAG, "系统播放器");
        detector = new GestureDetector(this, new MyGestureDetectorListener());
        iniData();
        getData();
        setVideoViewListener();
        setData();


    }

    private void setVideoViewListener() {
        btnVoice.setOnClickListener(this);
        btnSwitchPlayer.setOnClickListener(this);
        btnExit.setOnClickListener(this);
        btnVideoPre.setOnClickListener(this);
        btnVideoStartPause.setOnClickListener(this);
        btnVideoNext.setOnClickListener(this);
        btnVideoSiwchScreen.setOnClickListener(this);
        videoSeekBar.setOnSeekBarChangeListener(new VideoSeekBarListener());//视频进度监听
        voiceSeekBar.setOnSeekBarChangeListener(new VoiceSeekBarListener());//声音进度监听
        lightSeekBar.setOnSeekBarChangeListener(new LightSeekBarListener());
        vd.setOnPreparedListener(new MyOnpreparedListner());//准备监听
        vd.setOnCompletionListener(new MyOnCompletionListner());//播放完成
        vd.setOnErrorListener(new MyOnErroListner());
        //只有android 版本大于等于17时才可以用
        if (isUserSystem) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                vd.setOnInfoListener(new MyOnInfoListener());
            }
        }
        //  setVideoType(DEFUALTSCREEN);
        llLoading.setVisibility(View.VISIBLE);
    }

    private void setData() {
        if (videolist != null && videolist.size() > 0) {
            MediaItem mediaItem = videolist.get(position);
            netUri = utils.isNetUri(mediaItem.getData());
            tvVideoName.setText(mediaItem.getName());
            String data = mediaItem.getData();
            vd.setVideoPath(data);
        } else if (uri != null) {
            tvVideoName.setText(uri.toString());
            netUri = utils.isNetUri(uri.toString());
            vd.setVideoURI(uri);
        } else {
            Toast.makeText(PlayVideoActivity.this, "文件不存在呢！", Toast.LENGTH_SHORT).show();
        }
        setButtonSate();
        llBuffer.setVisibility(View.GONE);
        handler.sendEmptyMessage(SHOWSPEED);
    }

    private void getData() {
        uri = getIntent().getData();
        videolist = (ArrayList<MediaItem>) getIntent().getSerializableExtra("videolist");
        position = getIntent().getIntExtra("position", 0);

    }

    private void iniData() {
        //获取屏幕宽高
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        currentVoice = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVoice = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        llLightController.setVisibility(View.GONE);
        voiceSeekBar.setMax(maxVoice);
        lightSeekBar.setMax(maxLight);
        voiceSeekBar.setProgress(currentVoice);
        currentLight = Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, 255);
        lightSeekBar.setProgress(currentVoice);
        myReciver = new MyReciver();
        IntentFilter interfilter = new IntentFilter();
        interfilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(myReciver, interfilter);

    }

    private void startOrPause() {
        if (vd.isPlaying()) {
            vd.pause();
            EventBus.getDefault().post(Constants.STOP_VIDEO);
            showMediaController();
            handler.removeMessages(HIDE);
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_start_selector);
        } else {
               EventBus.getDefault().post(Constants.PLAYING_VIDEO);
            hideMediaController();
            vd.start();
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
        }
    }

    //播放下一个
    private void playNextVideo() {
        llLoading.setVisibility(View.VISIBLE);
        if (videolist != null && videolist.size() > 0) {
            position++;
            if (position < videolist.size()) {
                MediaItem mediaItem = videolist.get(position);
                netUri = utils.isNetUri(mediaItem.getData());
                tvVideoName.setText(mediaItem.getName());
                vd.setVideoPath(mediaItem.getData());
                setButtonSate();
            } else if (uri != null) {
                netUri = utils.isNetUri(uri.toString());
                setButtonSate();
            }
        }
    }

    //播放上一个
    private void playLastVideo() {
        llLoading.setVisibility(View.VISIBLE);
        if (videolist != null && videolist.size() > 0) {
            position--;
            if (position >= 0) {
                MediaItem mediaItem = videolist.get(position);
                netUri = utils.isNetUri(mediaItem.getData());
                tvVideoName.setText(mediaItem.getName());
                vd.setVideoPath(mediaItem.getData());
                setButtonSate();
            } else if (uri != null) {
                netUri = utils.isNetUri(uri.toString());
                setButtonSate();
            }
        }
    }

    private void setButtonSate() {
        if (videolist != null && videolist.size() > 0) {
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
            if (videolist.size() == 1) {//只有一个视频
                setButtonEnable(false);
            } else if (videolist.size() == 2) {//只有两个ship
                if (position == 0) {
                    btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btnVideoPre.setEnabled(false);
                    btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
                    btnVideoNext.setEnabled(true);
                } else if (position == 1) {
                    btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
                    btnVideoPre.setEnabled(true);
                    btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                    btnVideoNext.setEnabled(false);
                }


            } else {//三个及以上
                if (position == 0) {
                    btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btnVideoPre.setEnabled(false);
                } else if (position == videolist.size() - 1) {
                    btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                    btnVideoNext.setEnabled(false);
                } else {
                    setButtonEnable(true);
                }
            }

        } else if (uri != null) {
            setButtonEnable(false);
        }
    }

    private void setButtonEnable(boolean b) {
        if (b) {
            btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
            btnVideoPre.setEnabled(true);
            btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
            btnVideoNext.setEnabled(true);
        } else {
            btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
            btnVideoPre.setEnabled(false);
            btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
            btnVideoNext.setEnabled(false);
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {//更新播放进度
                case PROGRESS:
                    int currentPosition = vd.getCurrentPosition();
                    videoSeekBar.setProgress(currentPosition);
                    tvCurrTime.setText(utils.stringForTime(currentPosition));
                    tvTime.setText(getSystemTime());
                    if (netUri) {//网络视频才有缓冲效果
                        int bufferPercentage = vd.getBufferPercentage();
                        int totalBuffer = bufferPercentage * voiceSeekBar.getMax();
                        voiceSeekBar.setSecondaryProgress(totalBuffer / 100);
                        //监听卡
                        if (!isUserSystem&&vd.isPlaying()){
                            int buffer = currentPosition - prePosition;
                            Log.e("**", "currentPosition: "+currentPosition );
                            Log.e("**", "buffer: "+buffer );
                            if (buffer<500){
                                llBuffer.setVisibility(View.VISIBLE);
                            }else {
                                llBuffer.setVisibility(View.GONE);
                            }
                        }else {
                            llBuffer.setVisibility(View.GONE);
                        }

                        prePosition=currentPosition;
                    } else {
                        voiceSeekBar.setSecondaryProgress(0);
                    }
                    removeMessages(PROGRESS);
                    sendEmptyMessageDelayed(PROGRESS, 1000);
                    break;
                case HIDE:
                    hideMediaController();
                    break;
                case VOICECHANGE:
                    hideVoiceController();
                    break;
                case SHOWSPEED:
                    String netSpeed = utils.getNetSpeed(PlayVideoActivity.this);
                    tvNetSpeed.setText("玩命加载中...."+netSpeed);
                    tvLoadingSpeed.setText("玩命加载中...."+netSpeed);
                    removeMessages(SHOWSPEED);
                    sendEmptyMessageDelayed(SHOWSPEED,2000);
                    break;
                case LIGHTCONTROLLER:
                    llLightController.setVisibility(View.GONE);
                    break;
                case PLAYCONTROLLER:
                    hidePlayController();
                    break;
                case FASTFARWARDCONTROLLER:
                    showFastForwardController();
                    break;
                case BACKUPCONTROLLER:
                    showBackUpController();
                    break;
            }
        }
    };

    private String getSystemTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        String time = format.format(new Date());
        return time;
    }

    class MyOnCompletionListner implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            EventBus.getDefault().post(Constants.STOP_VIDEO);
            playNextVideo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
            EventBus.getDefault().post(Constants.STOP_VIDEO);
    }

    private void startToVitamioActivity(){

      Intent intent = new Intent(PlayVideoActivity.this, VitamioPlayVideoActivity.class);
      if (videolist!=null&&videolist.size()>0){
          Bundle bundle = new Bundle();
         bundle.putSerializable("videolist",videolist);
         intent.putExtras(bundle);
          int currentPosition = vd.getCurrentPosition();
          intent.putExtra("videoPosition",currentPosition);
         intent.putExtra("position",position);
     }else if (uri!=null){
       intent.setData(uri);
      }
        if (vd!=null){

            vd.stopPlayback();
        }
      startActivity(intent);
      finish();
  }
    /**
     * 播放出错监听
     * 出错有3中情况
     * 1.格式不支持
     * 2.播放网络视频时网络中断
     * 3.视频有缺失
     */
    class  MyOnErroListner implements MediaPlayer.OnErrorListener{

        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
           startToVitamioActivity();
            return true;
        }

    }

    class MyOnpreparedListner implements MediaPlayer.OnPreparedListener {
        //底层解码准备好
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            videoWidth = mediaPlayer.getVideoWidth();
            videoHeight = mediaPlayer.getVideoHeight();
            int duration = vd.getDuration();
            videoSeekBar.setMax(duration);
            tvTotalTime.setText(utils.stringForTime(duration));
            hideMediaController();
            EventBus.getDefault().post(Constants.PLAYING_VIDEO);
            int videoPosition = getIntent().getIntExtra("videoPosition", 0);
            vd.seekTo(videoPosition);
            videoSeekBar.setProgress(videoPosition);
            vd.start();
            llLoading.setVisibility(View.GONE);
            handler.sendEmptyMessage(PROGRESS);

        }
    }

    //视频监听卡
    class MyOnInfoListener implements MediaPlayer.OnInfoListener{


        @Override
        public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
          switch (what){
              case MediaPlayer.MEDIA_INFO_BUFFERING_START://开始卡了
                  llBuffer.setVisibility(View.VISIBLE);
                  break;
              case MediaPlayer.MEDIA_INFO_BUFFERING_END://不卡了
                llBuffer.setVisibility(View.GONE);
                  break;
          }
            return true;
        }
    }

    ///视频进度条拖动
    class VideoSeekBarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (b) {
                vd.seekTo(i);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }
class LightSeekBarListener implements SeekBar.OnSeekBarChangeListener{

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
          if (b){
              int tmpInt = seekBar.getProgress();

              //当进度小于80时，设置成80，防止太黑看不见的后果。
              if (tmpInt < 80) {
                  tmpInt = 80;
              }

              //根据当前进度改变亮度
              Settings.System.putInt(getContentResolver(),
                      Settings.System.SCREEN_BRIGHTNESS, tmpInt);
              tmpInt = Settings.System.getInt(getContentResolver(),
                      Settings.System.SCREEN_BRIGHTNESS, -1);
              WindowManager.LayoutParams wl = getWindow()
                      .getAttributes();

              float tmpFloat = (float) tmpInt / 255;
              if (tmpFloat > 0 && tmpFloat <= 1) {
                  wl.screenBrightness = tmpFloat;
              }
              getWindow().setAttributes(wl);
          }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
    //声音进度条拖动
    class VoiceSeekBarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (b) {
                if (i > 0) {
                    isMute = false;
                } else {
                    isMute = true;
                }
                updateVoice(i, isMute);
                showVoiceController();

            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessageDelayed(VOICECHANGE, 5000);


        }
    }

    //更新音量
    private void updateVoice(int voice, boolean isMute) {
        if (isMute) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            voiceSeekBar.setProgress(0);
        } else {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, voice, 0);
            voiceSeekBar.setProgress(voice);
            currentVoice = voice;
        }
    }

    class MyGestureDetectorListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        //长按
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            startOrPause();

        }

        ///////////////双击
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (isFullScreen) {
                setVideoType(DEFUALTSCREEN);
            } else {
                setVideoType(FULLSCREEN);
            }

            return super.onDoubleTap(e);

        }

        /////////////单击
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (isShowMediaController) {
                hideMediaController();
            } else {
                showMediaController();
                handler.sendEmptyMessageDelayed(HIDE, 5000);
            }

            return super.onSingleTapUp(e);


        }


    }

    private void setVideoType(int fullscreen) {
        switch (fullscreen) {

            case FULLSCREEN:
                Log.e("***", "全屏模式========》》》》》》》》》》》》");
                vd.setVideoSize(screenWidth, screenHeight);
                isFullScreen = true;
                btnVideoSiwchScreen.setBackgroundResource(R.drawable.btn_video_siwch_screen_default_selector);
//                Log.e("***", "屏幕原始尺寸 "+"宽："+screenWidth+"高:" +screenHeight);
//                Log.e("***", "视频尺寸 "+"宽："+videoWidth+"高:" +videoHeight);
                break;
            case DEFUALTSCREEN:
                Log.e("***", "默认模式========》》》》》》》》》》》》");
                int mVideoWidth = videoWidth;
                int mVideoHeight = videoHeight;
                int width = screenWidth;
                int height = screenHeight;
                if (mVideoWidth * height < mVideoHeight * width) {
                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > mVideoHeight * width) {
                    height = width * mVideoHeight / mVideoWidth;
                }
//                Log.e("***", "屏幕原始尺寸 "+"宽："+screenWidth+"高:" +screenHeight);
//                Log.e("***", "视频尺寸 "+"宽："+videoWidth+"高:" +videoHeight);
//                Log.e("***", "改变尺寸 "+"宽："+width+"高:" +height);
                vd.setVideoSize(width, height);
                isFullScreen = false;
                btnVideoSiwchScreen.setBackgroundResource(R.drawable.btn_video_siwch_screen_full_selector);

                break;

        }
    }


    private void showVoiceController() {
        llVoiceController.setVisibility(View.VISIBLE);
        isShowVoiceController = true;
    }

    private void hideVoiceController() {
        isShowVoiceController = false;
        llVoiceController.setVisibility(View.GONE);
    }

    //显示媒体控制器
    private void showMediaController() {
        llBottom.setVisibility(View.VISIBLE);
      //  rlSwitchPlayer.setVisibility(View.VISIBLE);
        isShowMediaController = true;
    }

    //隐藏媒体控制器
    private void hideMediaController() {
        llBottom.setVisibility(View.GONE);
       // rlSwitchPlayer.setVisibility(View.GONE);
        isShowMediaController = false;
    }

    class MyReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0);
            setBattery(level);
        }
    }


    private void setBattery(int level) {
        if (level <= 0) {
            ivBattery.setImageResource(R.drawable.ic_battery_0);
        } else if (level <= 10) {
            ivBattery.setImageResource(R.drawable.ic_battery_10);
        } else if (level <= 20) {
            ivBattery.setImageResource(R.drawable.ic_battery_20);
        } else if (level <= 40) {
            ivBattery.setImageResource(R.drawable.ic_battery_40);
        } else if (level <= 60) {
            ivBattery.setImageResource(R.drawable.ic_battery_60);
        } else if (level <= 80) {
            ivBattery.setImageResource(R.drawable.ic_battery_80);
        } else if (level <= 100) {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        } else {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        }
    }

    @Override
    protected void onDestroy() {
        if (myReciver != null) {
            unregisterReceiver(myReciver);
            myReciver = null;
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == event.KEYCODE_VOLUME_DOWN) {
            currentVoice--;
            showVoiceController();
            updateVoice(currentVoice, false);
            handler.removeMessages(VOICECHANGE);
            handler.sendEmptyMessageDelayed(VOICECHANGE, 3000);
            return true;
        } else if (keyCode == event.KEYCODE_VOLUME_UP) {
            currentVoice++;
            showVoiceController();
            updateVoice(currentVoice, false);
            handler.removeMessages(VOICECHANGE);
            handler.sendEmptyMessageDelayed(VOICECHANGE, 3000);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private float startY;
    private float startX;
    private float touchRang;
    private float touchRangX;
    private float currentPlayTime;
    private int mVol;
    private  int mLight;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN://手指按下
                startY = event.getY();
                startX = event.getX();
                mVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                mLight=Settings.System.getInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, 255);
                currentPlayTime=vd.getCurrentPosition();
                touchRang = Math.min(screenHeight, screenWidth);
                touchRangX=Math.max(screenWidth,screenHeight);
                handler.removeMessages(PLAYCONTROLLER);
                handler.removeMessages(FASTFARWARDCONTROLLER);
                handler.removeMessages(BACKUPCONTROLLER);
               handler.removeMessages(LIGHTCONTROLLER);
                handler.removeMessages(VOICECHANGE);
                break;
            case MotionEvent.ACTION_MOVE://手指移动
                float endY = event.getY();
                float endX = event.getX();
                float disdenceY = startY - endY;
                float disdenceX=startX-endX;
                //左右滑动
                if (Math.abs(disdenceX)>20&&Math.abs(disdenceY)<10){
                    float dalte=(-1)*((disdenceX/touchRangX)*vd.getDuration());
                    playController((int) dalte);
                }
                //上下滑动
                if (endX < (screenWidth / 2)) {//左边调节亮度
                if (Math.abs(disdenceY)>20&&Math.abs(disdenceX)<10){
                    handler.removeMessages(VOICECHANGE);
                    handler.sendEmptyMessage(VOICECHANGE);
                    final double FLING_MIN_DISTANCE = 0.5;
                    final double FLING_MIN_VELOCITY = 0.5;
                    llLightController.setVisibility(View.VISIBLE);
                  float dalte=(disdenceY/touchRang)*maxLight;
                    int brightNess=Math.min(Math.max((int) (mLight+dalte),0),maxLight);
                    if (dalte!=0){
                        setBrightness(brightNess);
                    }
                }

                } else {//右边调节声音
                    if (Math.abs(disdenceY) > 20&&Math.abs(disdenceX)<10) {
                        handler.removeMessages(LIGHTCONTROLLER);
                        handler.sendEmptyMessage(LIGHTCONTROLLER);
                        showVoiceController();
                        float dalte = (disdenceY / touchRang) * maxVoice;
                        int voice = Math.min(Math.max((int) (mVol + dalte), 0), maxVoice);
                        if (dalte != 0) {
                            updateVoice(voice, false);
                        }
                    }
                }

                break;
            case MotionEvent.ACTION_UP://手指离开
                handler.sendEmptyMessageDelayed(VOICECHANGE, 3000);
                handler.sendEmptyMessageDelayed(LIGHTCONTROLLER,2000);
                handler.sendEmptyMessageDelayed(PLAYCONTROLLER,100);
                break;
        }
        return super.onTouchEvent(event);
    }

    private void playController(int progress) {
       // Log.e(TAG, "playController: "+progress );
        if (progress>0){//快进
            int p= (int) Math.min(progress+currentPlayTime,vd.getDuration());
            handler.removeMessages(FASTFARWARDCONTROLLER);
           handler.sendEmptyMessage(FASTFARWARDCONTROLLER);
            tvFastForward.setText(utils.stringForTime(p));
            videoSeekBar.setProgress(p);
            vd.seekTo(p);
            if (p>=vd.getDuration()){
                playNextVideo();
            }

        }else {//后退
            handler.removeMessages(BACKUPCONTROLLER);
            handler.sendEmptyMessage(BACKUPCONTROLLER);
            int p= (int) Math.min(Math.max(progress+currentPlayTime,0),vd.getDuration());
            tvBackUp.setText(utils.stringForTime(p));
            videoSeekBar.setProgress(p);
            vd.seekTo(p);
            if (p<0){
                playLastVideo();
            }
        }
    }

    public void setBrightness(float brightness) {


      WindowManager.LayoutParams lp = getWindow().getAttributes();
        //根据当前进度改变亮度
        Settings.System.putInt(getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, (int) brightness);
        brightness = Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, -1);
        WindowManager.LayoutParams wl = getWindow()
                .getAttributes();
     float tmpFloat= brightness;
       if (tmpFloat>0&&tmpFloat<=1){
           wl.screenBrightness=tmpFloat;
       }
        Log.e(TAG, "setBrightness: "+tmpFloat );
        lightSeekBar.setProgress((int) tmpFloat);
        getWindow().setAttributes(wl);
    }

}
