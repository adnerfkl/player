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

import org.xutils.x;

import java.util.List;


public class NetVideoAdapter extends BaseAdapter {
    private List<MediaItem> mediaItems;
    private Context context;

    public NetVideoAdapter(Context context, List<MediaItem> mediaItems) {
        this.mediaItems = mediaItems;
        this.context = context;
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
            view = View.inflate(context, R.layout.net_video_item, null);
            viewHoder.iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
            viewHoder.tv_name = (TextView) view.findViewById(R.id.tv_name);
            viewHoder.tv_desc = (TextView) view.findViewById(R.id.tv_desc);
            view.setTag(viewHoder);
        } else {
            viewHoder = (ViewHoder) view.getTag();
        }
        MediaItem mediaItem = mediaItems.get(i);
        viewHoder.tv_name.setText(mediaItem.getName());
        viewHoder.tv_desc.setText(mediaItem.getDesc());
        x.image().bind(viewHoder.iv_icon,mediaItem.getImageUrl());
        return view;
    }

    static class ViewHoder {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_desc;
    }
}
