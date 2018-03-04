package com.fkl.player.adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.fkl.player.R;
import com.fkl.player.entity.MediaItem;
import com.fkl.player.utils.Utils;

import java.util.List;

/**
 * Created by adner on 2018/1/10.
 */
public class LocalAudioAdapter extends BaseAdapter {
    private boolean isAudio;
    private List<MediaItem> mediaItems;
    private LayoutInflater inflater;
    private Context context;
    private Utils utils;

    public LocalAudioAdapter(Context context, List<MediaItem> mediaItems, boolean isAudio) {
        this.mediaItems = mediaItems;
        this.context = context;
        this.isAudio = isAudio;
        utils = new Utils();
    }

    @Override
    public int getCount() {
        return mediaItems.size() > 0 ?  mediaItems.size():0;
    }

    @Override
    public Object getItem(int i) {
        return mediaItems.get(i);
    }

    @Override
    public long getItemId(int i) {

        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHoder viewHoder;
        if (view == null) {
            viewHoder = new ViewHoder();
            view = View.inflate(context, R.layout.local_audio_item, null);
            viewHoder.iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
            viewHoder.tv_name = (TextView) view.findViewById(R.id.tv_name);
            viewHoder.tv_time = (TextView) view.findViewById(R.id.tv_time);
            viewHoder.tv_size = (TextView) view.findViewById(R.id.tv_size);
            view.setTag(viewHoder);
        } else {
            viewHoder = (ViewHoder) view.getTag();
        }
        MediaItem mediaItem = mediaItems.get(i);
        viewHoder.tv_name.setText(mediaItem.getName());
        viewHoder.tv_size.setText(android.text.format.Formatter.formatFileSize(context, mediaItem.getSize()));
        viewHoder.tv_size.setText(utils.stringForTime((int) mediaItem.getDuration()));
        if (isAudio) {
            viewHoder.iv_icon.setImageResource(R.drawable.music_default_bg);
        }else {
            viewHoder.iv_icon.setImageResource(R.drawable.video_default_icon);
        }

        return view;
    }

    static class ViewHoder {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_time;
        TextView tv_size;
    }
}
