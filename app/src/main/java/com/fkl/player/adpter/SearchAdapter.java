package com.fkl.player.adpter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fkl.player.R;
import com.fkl.player.activity.SearchActivity;
import com.fkl.player.entity.SearchBean;

import java.util.List;

/**
 * Created by adner on 2018/1/18.
 */
public class SearchAdapter extends BaseAdapter {
    private Context context;
    private List<SearchBean.ItemData> items;

    public SearchAdapter(Context context, List<SearchBean.ItemData> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size() == 0 ? 0 : items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHoder vh = null;
        if (view == null) {
            view = View.inflate(context, R.layout.item_netvideo_pager, null);
            vh = new ViewHoder();
            vh.iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
            vh.tv_name = (TextView) view.findViewById(R.id.tv_name);
            vh.tv_desc = (TextView) view.findViewById(R.id.tv_desc);
            view.setTag(vh);
        } else {
            vh = (ViewHoder) view.getTag();
        }
        SearchBean.ItemData data = items.get(position);
        vh.tv_name.setText(data.getItemTitle());
        vh.tv_desc.setText(data.getKeywords());
        Glide.with(context).load(data.getItemImage().getImgUrl1())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.video_default)
                .error(R.drawable.video_default)
                .into(vh.iv_icon);
        return view;
    }

    static class ViewHoder {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_desc;
    }
}
