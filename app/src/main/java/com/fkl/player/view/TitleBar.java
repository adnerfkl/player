package com.fkl.player.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.fkl.player.R;
import com.fkl.player.activity.MainActivity;
import com.fkl.player.activity.RecordActivity;
import com.fkl.player.activity.SearchActivity;

/**
 * Created by adner on 2018/1/12.
 */
public class TitleBar extends LinearLayout implements View.OnClickListener {
    private View tvSearch;
    private  View ivRecord;
    private Context context;
    public TitleBar(Context context) {
        this(context,null);
    }

    public TitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
    }

    public TitleBar(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        tvSearch=getChildAt(1);
        ivRecord=getChildAt(2);
        tvSearch.setOnClickListener(this);
        ivRecord.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
         switch (view.getId()){
             case R.id.tv_search:
               context.startActivity(new Intent(context,SearchActivity.class));

                 break;
             case R.id.iv_record:
                context.startActivity(new Intent(context, RecordActivity.class));
                 break;
         }
    }
}
