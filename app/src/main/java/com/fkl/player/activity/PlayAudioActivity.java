package com.fkl.player.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fkl.player.IMusicPlayService;
import com.fkl.player.R;
import com.fkl.player.entity.Lyric;
import com.fkl.player.entity.MediaItem;
import com.fkl.player.service.MusicPlayService;
import com.fkl.player.utils.Constants;
import com.fkl.player.utils.LyricUtils;
import com.fkl.player.utils.Utils;
import com.fkl.player.view.BaseVisualizerView;
import com.fkl.player.view.ShowLyricView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;

public class PlayAudioActivity extends Activity implements View.OnClickListener {

    private static final int PROGRESS = 1;
    private static final int SHOWLYRIC=2;
    private static final String TAG = PlayAudioActivity.class.getSimpleName();
    private int position;
    private ImageView ivIcon;
    private TextView tvArtist;
    private TextView tvAudioName;
    private TextView tvTime;
    private SeekBar sbAudio;
    private Button btnAudioPlaymode;
    private Button btnAudioPre;
    private Button btnAudioStartPause;
    private Button btnAudioNext;
    private Button btnLyrc;
    private ShowLyricView showLyricView;
   private IMusicPlayService service;
    private BaseVisualizerView visualizerView;
private Utils utils;
    private boolean notification=false;
    private ArrayList<MediaItem> audioList;

    @Override
    public void onClick(View v) {
        if (v == btnAudioPlaymode) {//设置播放模式
            setAudioPlaymode();
        } else if (v == btnAudioPre) {//播放上一首
            if (service!=null){
                try {
                    service.playLast();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (v == btnAudioStartPause) {//暂停或开始播放
           if (service!=null){
               try {
                   if (service.isPlaying()){
                       service.puase();
                       handler.removeMessages(PROGRESS);
                       btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
                   }else {
                     service.start();
                       handler.sendEmptyMessage(PROGRESS);
                       btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
                   }
               } catch (RemoteException e) {
                   e.printStackTrace();
               }
           }
        } else if (v == btnAudioNext) {//播放下一首
            if (service!=null){
                try {
                    service.playNext();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (v == btnLyrc) {


        }
    }

    /**
     * 设置播放模式
     */
    private void setAudioPlaymode() {
        try {
            int mplayModle = service.getPlayModle();
            if (mplayModle==MusicPlayService.REPEAT_NORMAL){
              mplayModle=MusicPlayService.REPEAT_SINGLE;
            }else if (mplayModle==MusicPlayService.REPEAT_SINGLE){
                mplayModle=MusicPlayService.REPEAT_ALL;
            }else if (mplayModle==MusicPlayService.REPEAT_ALL){
                mplayModle=MusicPlayService.REPEAT_NORMAL;
            }else {
                mplayModle=MusicPlayService.REPEAT_NORMAL;
            }
            service.setPlayModle(mplayModle);
            showModle();

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 提示播放模式
     */
    private void showModle() {
        try {
            int playModle = service.getPlayModle();
            if (playModle==MusicPlayService.REPEAT_NORMAL){
                Toast.makeText(PlayAudioActivity.this, "顺序播放", Toast.LENGTH_SHORT).show();
               btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
            }else if (playModle==MusicPlayService.REPEAT_SINGLE){
                Toast.makeText(PlayAudioActivity.this, "单曲循环", Toast.LENGTH_SHORT).show();
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_single_selector);
            }else if (playModle==MusicPlayService.REPEAT_ALL){
                Toast.makeText(PlayAudioActivity.this, "列表循环", Toast.LENGTH_SHORT).show();
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_all_selector);
            }else {
                Toast.makeText(PlayAudioActivity.this, "顺序播放", Toast.LENGTH_SHORT).show();
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    /**
     * 校验播放模式
     */
    private void checkPlayModle() {
        try {
            service.setPlayModle(service.getPlayModle());
            int playModle = service.getPlayModle();
            if (playModle==MusicPlayService.REPEAT_NORMAL){
               btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
            }else if (playModle==MusicPlayService.REPEAT_SINGLE){
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_single_selector);
            }else if (playModle==MusicPlayService.REPEAT_ALL){
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_all_selector);
            }else {
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViews();
        setListner();
        iniData();
        bindAndStartService();

    }

    /**
     * 绑定播放器的服务
     */
    private void bindAndStartService() {
        Intent intent = new Intent(this, MusicPlayService.class);
        intent.setAction("com.fkl.player_OPENAUDIO");
        bindService(intent,conn, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    /**
     * 设置监听
     */
    private void setListner() {
        btnAudioPlaymode.setOnClickListener(this);
        btnAudioPre.setOnClickListener(this);
        btnAudioStartPause.setOnClickListener(this);
        btnAudioNext.setOnClickListener(this);
        btnLyrc.setOnClickListener(this);
        sbAudio.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());
    }


    /**
     * 初始化数据
     */
    private void iniData() {
        ivIcon.setBackgroundResource(R.drawable.animation_list);
        AnimationDrawable animation = (AnimationDrawable) ivIcon.getBackground();
        animation.start();
        EventBus.getDefault().register(this);
        getData();
        utils=new Utils();
    }

    private void getData() {
        Intent intent = getIntent();
        audioList = (ArrayList<MediaItem>) intent.getSerializableExtra("audioList");

         notification = getIntent().getBooleanExtra("Notification", false);
      if (!notification) {
          position = intent.getIntExtra("position", 0);
      }
    }
 private Handler handler=new Handler(){
     @Override
     public void handleMessage(Message msg) {
         super.handleMessage(msg);
         switch (msg.what){
             case SHOWLYRIC:
                 try {
                     int currentPostion = service.getCurrentPostion();
                     showLyricView.setshowNextLyric(currentPostion);

                 } catch (RemoteException e) {
                     e.printStackTrace();
                 }
                 handler.removeMessages(SHOWLYRIC);
                 handler.sendEmptyMessage(SHOWLYRIC);
                 break;
             case PROGRESS:
                 try {
                     sbAudio.setProgress(service.getCurrentPostion());
                     tvTime.setText(utils.stringForTime(service.getCurrentPostion())+"/"+utils.stringForTime(service.getTotalTime()));
                 } catch (RemoteException e) {
                     e.printStackTrace();
                 }
                 removeMessages(PROGRESS);
                 sendEmptyMessageDelayed(PROGRESS,1000);
                 break;
         }
     }
 };
    private void showViewData() throws RemoteException {
        tvArtist.setText(service.getArtist());
        tvAudioName.setText(service.getAudioName());
        sbAudio.setMax(service.getTotalTime());
        checkPlayModle();
        //校验播放和暂停的按钮
        Log.e(TAG, "showViewData: "+service.isPlaying() );
        if(service.isPlaying()){
            btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
        }else{
            btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
        }

    }

    @Override
    protected void onPause() {
        if (visualizer!=null){
            visualizer.release();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        EventBus.getDefault().unregister(this);
        if (conn!=null){
            unbindService(conn);
            conn=null;
        }
        super.onDestroy();
    }

    /**
     * 拖动音乐播放时间
     */
    class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (b){
                if (service!=null){
                    try {
                        service.seekTo(i);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN,sticky = false,priority = 0)
    public void showData(MediaItem mediaItem){
        if (service!=null){
            try {
                showViewData();
                showLyric();
               setupVisualizerFxAndUi();
                handler.removeMessages(PROGRESS);
                handler.sendEmptyMessage(PROGRESS);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
    private Visualizer visualizer;
    private  void  setupVisualizerFxAndUi(){
        int audioSessionId= 0;
        try {
            audioSessionId = service.getAudioSessionId();
            Log.e(TAG, "setupVisualizerFxAndUi: "+audioSessionId );
            visualizer=new Visualizer(audioSessionId);
            visualizerView.setVisualizer(visualizer);
            visualizer.setEnabled(true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }


    }


    @Override
    protected void onRestart() {
        super.onRestart();
        showLyric();
        setupVisualizerFxAndUi();
    }
    private void showLyric() {
        LyricUtils lyricUtils = new LyricUtils();
        try {
            String audioPath = service.getAudioPath();
            Log.e(TAG, "showLyric: "+audioPath );
            String path=audioPath.substring(0,audioPath.lastIndexOf("."));
            File file = new File(path + ".lrc");

            if (!file.exists()){
                file = new File(path + ".txt");
            }
            Log.e(TAG, "showLyric: "+file.getPath() );
            lyricUtils.readLyricFile(file);
            ArrayList<Lyric> lyrics = lyricUtils.getLyrics();
            showLyricView.setLyrics(lyrics);
            if (file.exists()){
                handler.removeMessages(SHOWLYRIC);
                handler.sendEmptyMessage(SHOWLYRIC);
            }else {
                handler.removeMessages(SHOWLYRIC);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    /**
     * 链接服务
     */
    private ServiceConnection conn=new ServiceConnection() {
        @Override//链接成功
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
           service=IMusicPlayService.Stub.asInterface(iBinder);
//          if (service!=null){
//
//              if(EventBus.getDefault()!=null)
//              EventBus.getDefault().post(audioList);
//          }
            if (service!=null){
                try {
                    if (!notification) {
                        service.openAudio(position);
                    }else {
                        showViewData();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        //断开链接
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            try {
                if (service!=null) {
                    service.stop();
                    service=null;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 实例化视图
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2018-01-15 18:49:03 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        setContentView(R.layout.activity_play_audio);
        visualizerView= (BaseVisualizerView) findViewById(R.id.visualizerView);
        ivIcon = (ImageView) findViewById(R.id.iv_icon);
        tvArtist = (TextView) findViewById(R.id.tv_artist);
        tvAudioName = (TextView) findViewById(R.id.tv_audio_name);
        tvTime = (TextView) findViewById(R.id.tv_time);
        sbAudio = (SeekBar) findViewById(R.id.sb_audio);
        btnAudioPlaymode = (Button) findViewById(R.id.btn_audio_playmode);
        btnAudioPre = (Button) findViewById(R.id.btn_audio_pre);
        btnAudioStartPause = (Button) findViewById(R.id.btn_audio_start_pause);
        btnAudioNext = (Button) findViewById(R.id.btn_audio_next);
        btnLyrc = (Button) findViewById(R.id.btn_lyrc);
        showLyricView= (ShowLyricView) findViewById(R.id.show_lyric);


    }

}
