package com.fkl.player.page;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

/**
 * Created by adner on 2018/1/11.
 */
public class NetAudioPage extends BasePager {
    public NetAudioPage(Context context) {
        super(context);
    }

    @Override
    public View iniView() {
        TextView textView = new TextView(context);
        textView.setText("网络音频");
        return textView;
    }
}
