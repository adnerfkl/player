package com.fkl.player.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;

import com.fkl.player.IMusicPlayService;
import com.fkl.player.R;
import com.fkl.player.activity.PlayAudioActivity;
import com.fkl.player.entity.MediaItem;
import com.fkl.player.utils.CacheUtils;
import com.fkl.player.utils.Constants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.Event;

import java.io.IOException;
import java.util.ArrayList;

public class MusicPlayService extends Service {
    public static final String MSG = "com.fkl.player_AUDIOCHANGE";
    private ArrayList<MediaItem> mediaItems;
    private int position;
    private MediaItem mediaItem;
    private MediaPlayer mediaPlayer;
    private NotificationManager notificationManager;
    //播放模式
    public static final int REPEAT_NORMAL = 1;//顺序播放
    public static final int REPEAT_SINGLE = 2;//单曲循环
    public static final int REPEAT_ALL = 3;//列表循环
    private int playmodle;
    private boolean isUserPuase=false;

    public MusicPlayService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
       getDataFromLocal();
        EventBus.getDefault().register(this);
       playmodle= CacheUtils.getPlaymode(this,"playmode");
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    private IMusicPlayService.Stub stub = new IMusicPlayService.Stub() {
        MusicPlayService service = MusicPlayService.this;

        @Override
        public void openAudio(int position) throws RemoteException {
            service.openAudio(position);
        }

        @Override
        public void start() throws RemoteException {
            isUserPuase=false;
            service.start();
        }

        @Override
        public void puase() throws RemoteException {
            isUserPuase=true;
            service.puase();
        }

        @Override
        public void stop() throws RemoteException {
            service.stop();
        }

        @Override
        public void playNext() throws RemoteException {
            service.playNext();
        }

        @Override
        public void playLast() throws RemoteException {
            service.playLast();
        }

        @Override
        public void setPlayModle(int modle) throws RemoteException {
            service.setPlayModle(modle);
        }

        @Override
        public int getPlayModle() throws RemoteException {
            return service.getPlayModle();
        }

        @Override
        public int getCurrentPostion() throws RemoteException {
            return service.getCurrentPostion();
        }

        @Override
        public int getTotalTime() throws RemoteException {
            return service.getTime();
        }


        @Override
        public String getArtist() throws RemoteException {
            return service.getArtist();
        }

        @Override
        public String getAudioName() throws RemoteException {
            return service.getAudioName();
        }

        @Override
        public String getAudioPath() throws RemoteException {
            return service.getAudioPath();
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return service.isPlaying();
        }

        @Override
        public void seekTo(int progress) throws RemoteException {
            service.seekTo(progress);
        }

        @Override
        public int getAudioSessionId() throws RemoteException {
            return service.getAudioSessionId();
        }
    };

    private int getAudioSessionId() {
        return mediaPlayer.getAudioSessionId();
    }


    /**
     * 打开音乐
     *
     * @param position //位置
     */
    private void openAudio(int position) {
        this.position = position;
        if (mediaItems != null && mediaItems.size() > 0) {
            mediaItem = mediaItems.get(position);
            resease();
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setOnPreparedListener(new MyOnPreparedListener());
                mediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
                mediaPlayer.setOnErrorListener(new MyOnErrorListener());
                mediaPlayer.setDataSource(mediaItem.getData());
                mediaPlayer.prepareAsync();
                if (playmodle==MusicPlayService.REPEAT_SINGLE){
                    mediaPlayer.setLooping(true);
                }else {
                    mediaPlayer.setLooping(false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * 释放资源
     */
    private void resease() {
        if (mediaPlayer != null) {
            //  mediaPlayer.release();
            mediaPlayer.reset();
        }
    }

    /**
     * 播放音乐
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void start() {
        mediaPlayer.start();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, PlayAudioActivity.class);
        intent.putExtra("Notification", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.notification_music_playing)
                .setContentText("正在播放：" + getAudioName())
                .setContentIntent(pendingIntent)
                .build();
        notificationManager.notify(1, notification);
    }


    /**
     * 暂停
     */
    private void puase() {
        mediaPlayer.pause();
        notificationManager.cancel(1);
    }


    /**
     * 停止
     */
    private void stop() {
        mediaPlayer.stop();
        resease();


    }


    /**
     * 下一首
     */
    private void playNext() {
        setNextPosition();
        openNextAudio();

    }

    //打开音频播放
    private void openNextAudio() {
        int modle = getPlayModle();
        if (modle == MusicPlayService.REPEAT_NORMAL) {
            if (position < mediaItems.size()) {
                openAudio(position);
            } else {
                position = mediaItems.size() - 1;
            }

        } else if (modle == MusicPlayService.REPEAT_SINGLE) {
            openAudio(position);

        } else if (modle == MusicPlayService.REPEAT_ALL) {
            openAudio(position);
        } else {
            if (position < mediaItems.size()) {
                openAudio(position);
            } else {
                position = mediaItems.size() - 1;
            }
        }
    }

    //设置下一个位置
    private void setNextPosition() {
        int modle = getPlayModle();
        if (modle == MusicPlayService.REPEAT_NORMAL) {
            position++;

        } else if (modle == MusicPlayService.REPEAT_SINGLE) {
            position++;
            if (position >= mediaItems.size()) {
                position = 0;
            }

        } else if (modle == MusicPlayService.REPEAT_ALL) {

            position++;
            if (position >= mediaItems.size()) {
                position = 0;
            }
        } else {
            position++;
        }
    }


    /**
     * 上一首
     */
    private void playLast() {
        setPlayLastPosition();
        openLastAudio();

    }

    private void openLastAudio() {
        int modle = getPlayModle();
        if (modle == MusicPlayService.REPEAT_NORMAL) {
            if (position>=0) {
                openAudio(position);
            } else {
                position = 0;
            }

        } else if (modle == MusicPlayService.REPEAT_SINGLE) {
            openAudio(position);

        } else if (modle == MusicPlayService.REPEAT_ALL) {
            openAudio(position);
        } else {
            if (position>=0) {
                openAudio(position);
            } else {
                position = 0;
            }
        }
    }

    private void setPlayLastPosition() {
        int modle = getPlayModle();
        if (modle == MusicPlayService.REPEAT_NORMAL) {
            position--;

        } else if (modle == MusicPlayService.REPEAT_SINGLE) {
            position--;
            if (position <0) {
                position = mediaItems.size()-1;
            }

        } else if (modle == MusicPlayService.REPEAT_ALL) {

            position--;
            if (position <0) {
                position = mediaItems.size()-1;
            }
        } else {
            position--;
        }
    }


    /**
     * 设置播放模式
     *
     * @param modle 播放模式
     */
    private void setPlayModle(int modle) {
       this.playmodle=modle;
        CacheUtils.putPlaymode(this,"playmode",playmodle);
        if (modle==MusicPlayService.REPEAT_SINGLE){
            mediaPlayer.setLooping(true);
        }else {
            mediaPlayer.setLooping(false);
        }


    }


    /**
     * 获取播放模式
     *
     * @return modle 播放模式
     */
    private int getPlayModle() {

        return playmodle;
    }

    /**
     * 获取当前进度
     */
    private int getCurrentPostion() {

        return mediaPlayer.getCurrentPosition();
    }


    /**
     * 获取当前歌曲时间
     *
     * @return 当前歌曲时间
     */
    private int getTime() {

        return mediaPlayer.getDuration();
    }


    /**
     * 获取当前歌曲演唱者
     *
     * @return演唱者
     */
    private String getArtist() {

        return mediaItem.getArtist();
    }


    /**
     * 获取当前歌曲名
     *
     * @return歌曲名
     */
    private String getAudioName() {

        return mediaItem.getName();
    }

    /**
     * 判断是否在播放
     *
     * @return
     */
    private boolean isPlaying() {
        Log.e("****", "isPlaying: "+mediaPlayer.isPlaying() );
        return mediaPlayer.isPlaying();
    }

    private void seekTo(int progress) {
        mediaPlayer.seekTo(progress);
    }

    /**
     * 获取当前歌曲路径
     *
     * @return歌曲路径
     */
    private String getAudioPath() {

        return mediaItem.getData();
    }

    private void notifyAudioChange(MediaItem msg) {
//        Intent intent = new Intent(msg);
//        sendBroadcast(intent);
        EventBus.getDefault().post(msg);
    }

    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            notifyAudioChange(mediaItem);
            start();
        }
    }


    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            Log.e("****", "onCompletion: "+mediaPlayer.isPlaying() );
            if (!mediaPlayer.isPlaying()) {
                playNext();
            }
        }
    }

    class MyOnErrorListener implements MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            playNext();
            return true;
        }
    }
   // @Subscribe(threadMode = ThreadMode.MAIN,sticky = false,priority = 100)
    public void getDataFromLocal() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                //  isGrantExternalRW((Activity) mContext);
                mediaItems = new ArrayList<>();
                ContentResolver resolver = getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        MediaStore.Audio.Media.DISPLAY_NAME,//语音文件在SDCard的名称
                        MediaStore.Audio.Media.DURATION,//总时长
                        MediaStore.Audio.Media.SIZE,//大小
                        MediaStore.Audio.Media.DATA,//绝对地址
                        MediaStore.Audio.Media.ARTIST//艺术家
                };
                Cursor cursor = resolver.query(uri, objs, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        MediaItem mediaItem = new MediaItem();
                        mediaItem.setName(cursor.getString(0));
                        mediaItem.setDuration(cursor.getLong(1));
                        mediaItem.setSize(cursor.getLong(2));
                        mediaItem.setData(cursor.getString(3));
                        mediaItem.setArtist(cursor.getString(4));
                        mediaItems.add(mediaItem);
                        // Log.e("**", mediaItem.toString());
                    }
                    cursor.close();
                }
            }
        }.start();
    }
    @Subscribe(threadMode =ThreadMode.MAIN,sticky = false,priority = 5)
    public void stopAudio(String msg){
        if (msg.equals(Constants.PLAYING_VIDEO)){
            if (mediaPlayer.isPlaying()){
                puase();
            }
        }

    }
    @Subscribe(threadMode =ThreadMode.MAIN,sticky = false,priority =0)
    public void startAudio(String msg){
        if (msg.equals(Constants.STOP_VIDEO)){
            Log.e("msg", "msg: "+msg );
            if (mediaPlayer!=null) {
                Log.e("mediaPlayer", "msg: "+mediaPlayer );
                if (!mediaPlayer.isPlaying()&&!isUserPuase) {
                    start();
                }
            }
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = false,priority = 5)
    public void kill(String msg){
        if (msg.equals("kill")){
            isUserPuase=true;
            this.puase();
        }
    }
}
