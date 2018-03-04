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

import com.fkl.player.R;
import com.fkl.player.activity.PlayVideoActivity;
import com.fkl.player.adpter.LocalAudioAdapter;
import com.fkl.player.entity.MediaItem;

import java.util.ArrayList;


public class LocalVidioPage extends BasePager implements AdapterView.OnItemClickListener{
    private ArrayList<MediaItem> mediaItems;
   private ListView lvVideo;
    private TextView tvHide;
    private LocalAudioAdapter adapter;
    public LocalVidioPage(Context context) {
        super(context);
    }

    @Override
    public View iniView() {
        View view = View.inflate(context, R.layout.local_video, null);
       lvVideo=(ListView)view.findViewById(R.id.lv_local_video);
        tvHide=(TextView)view.findViewById(R.id.tv_hide);
        lvVideo.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void iniData() {
        super.iniData();
        getDataFromLocal();
    }
Handler handler=new Handler(){
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what){
            case 10:
                if (mediaItems!=null&&mediaItems.size()>0){
                    tvHide.setVisibility(View.GONE);
                    lvVideo.setVisibility(View.VISIBLE);
                    adapter=new LocalAudioAdapter(context,mediaItems,false);
                    lvVideo.setAdapter(adapter);
                }else {
                    lvVideo.setVisibility(View.GONE);
                    tvHide.setVisibility(View.VISIBLE);
                }
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
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] objs={
                        MediaStore.Video.Media.DISPLAY_NAME,//语音文件在SDCard的名称
                        MediaStore.Video.Media.DURATION,//总时长
                        MediaStore.Video.Media.SIZE,//大小
                        MediaStore.Video.Media.DATA,//绝对地址
                        MediaStore.Video.Media.ARTIST//艺术家
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Bundle bundle = new Bundle();
        Intent intent = new Intent(context, PlayVideoActivity.class);
        bundle.putSerializable("videolist",mediaItems);
        intent.putExtras(bundle);
        intent.putExtra("position",position);
        context.startActivity(intent);
//        //调取系统播放器
//        MediaItem mediaItem = mediaItems.get(position);
//        Intent intent = new Intent(context, PlayVideoActivity.class);
//        intent.setDataAndType(Uri.parse(mediaItem.getData()),"video/*");
//        context.startActivity(intent);

    }
}
