package com.fkl.player.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;



/**
 * Created by adner on 2018/1/13.
 */
public class VitamioVideoView extends io.vov.vitamio.widget.VideoView{
    public VitamioVideoView(Context context) {
        this(context,null);
    }

    public VitamioVideoView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public VitamioVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
    }
    public void setVideoSize(int width,int height){
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width=width;
        params.height=height;
        setLayoutParams(params);
    }
}
