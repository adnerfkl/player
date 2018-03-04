package com.fkl.player.page;

import android.content.Context;
import android.view.View;

/**
 * Created by adner on 2018/1/11.
 */
public abstract class BasePager {
    public final Context context;//上下文
    public View rootView;//视图
    public boolean isData=false;//是否加载数据
    public BasePager(Context context) {
        this.context = context;
        rootView=iniView();
    }

    /**
     * 实例化视图
     * @return View
     */
    public abstract View iniView();
    /**
    *初始化绑定数据
     */
    public void iniData(){}
}
