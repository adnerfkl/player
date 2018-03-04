package com.fkl.player.page;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fkl.player.R;
import com.fkl.player.activity.PlayAudioActivity;
import com.fkl.player.adpter.LocalAudioAdapter;
import com.fkl.player.entity.MediaItem;

import java.util.ArrayList;

/**
 * 本地音乐页面
 */
public class LocalAudioPage extends BasePager {
    private ArrayList<MediaItem> mediaItems;
     private TextView tvHide;
    public LocalAudioPage(Context context) {
        super(context);
    }
    private ListView lvLocalAudio;
    @Override
    public View iniView() {
        View view = View.inflate(context, R.layout.fragment_local_audio, null);
        lvLocalAudio=(ListView) view.findViewById(R.id.lv_local_audio);
        tvHide=(TextView) view.findViewById(R.id.tv_hide);
        lvLocalAudio.setOnItemClickListener(new MyItemListner());
        return view;
    }

    @Override
    public void iniData() {
        super.iniData();
        getDataFromLocal();
    }

    private LocalAudioAdapter adapter;
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 10:

                    if (mediaItems!=null&&mediaItems.size()>0) {
                        tvHide.setVisibility(View.GONE);
                        lvLocalAudio.setVisibility(View.VISIBLE);
                        adapter=new LocalAudioAdapter(context,mediaItems,true);
                        lvLocalAudio.setAdapter(adapter);
                    }else {
                        tvHide.setVisibility(View.VISIBLE);
                        lvLocalAudio.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    };
    private void  getDataFromLocal(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                //  isGrantExternalRW((Activity) mContext);
                mediaItems=new ArrayList<>();
                ContentResolver resolver = context.getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] objs={
                        MediaStore.Audio.Media.DISPLAY_NAME,//语音文件在SDCard的名称
                        MediaStore.Audio.Media.DURATION,//总时长
                        MediaStore.Audio.Media.SIZE,//大小
                        MediaStore.Audio.Media.DATA,//绝对地址
                        MediaStore.Audio.Media.ARTIST//艺术家
                };
                Cursor cursor = resolver.query(uri, objs, null, null, null);
                if (cursor!=null){
                    while (cursor.moveToNext()){
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
                    handler.sendEmptyMessage(10);
                }
            }
        }.start();
    }
    class MyItemListner implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            Intent intent = new Intent(context, PlayAudioActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("audioList",mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position",position);
            context.startActivity(intent);
        }
    }
}
