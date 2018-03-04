// IMusicPlay.aidl
package com.fkl.player;

interface IMusicPlayService {
     /**
            * 打开音乐
            * @param position
            */
            void openAudio( int position);

           /**
            * 播放音乐
            */
            void start();
           /**
            * 暂停
            */
            void puase();



           /**
            * 停止
            */
            void stop();




           /**
            * 下一首
            */
            void playNext();



           /**
            * 上一首
            */
            void playLast();



           /**
            * 设置播放模式
            * @param modle 播放模式
            */
              void setPlayModle(int modle);



           /**
            * 获取播放模式
            * @return modle 播放模式
            */
            int getPlayModle();



           /**
            * 获取当前进度
            */
            int getCurrentPostion() ;



           /**
            * 获取当前歌曲时间
            *
            * @return 当前时间
            */
            int getTotalTime();



           /**
            * 获取当前歌曲演唱者
            *
            * @return演唱者
            */
            String getArtist();



           /**
            * 获取当前歌曲名
            *
            * @return歌曲名
            */
            String getAudioName();



           /**
            * 获取当前歌曲路径
            *
            * @return歌曲路径
            */
            String getAudioPath();
             /**
              * 判断是否在播放
              * @return
             */
               boolean isPlaying();
               /**
               *拖动音频
               */
               void seekTo(int progress);
               int getAudioSessionId();
}
