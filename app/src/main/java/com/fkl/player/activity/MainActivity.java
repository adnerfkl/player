package com.fkl.player.activity;

import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.fkl.player.MyApp;
import com.fkl.player.R;
import com.fkl.player.page.BasePager;
import com.fkl.player.page.LocalAudioPage;
import com.fkl.player.page.LocalVidioPage;
import com.fkl.player.page.NetAudioPage;
import com.fkl.player.page.NetVidioPage;
import com.fkl.player.page.ReplaceFragment;

import org.greenrobot.eventbus.EventBus;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends FragmentActivity {
      private ArrayList<BasePager> pagers;
     private RadioGroup rg;
    private int position;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rg= (RadioGroup) findViewById(R.id.rg_main);
        pagers=new ArrayList<>();
        pagers.add(new LocalVidioPage(this));
        pagers.add(new LocalAudioPage(this));
        pagers.add(new NetVidioPage(this));
//        pagers.add(new NetAudioPage(this));
        rg.setOnCheckedChangeListener(new MyChaeckChangeListner());
        rg.check(R.id.rb_local_video);

    }
    class MyChaeckChangeListner implements RadioGroup.OnCheckedChangeListener{

        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int childId) {
            switch (childId){
                case R.id.rb_local_video:
                    position=0;
                    break;
                case R.id.rb_local_audio:
                    position=1;
                    break;
                case R.id.rb_net_video:
                    position=2;
                    break;
//                case R.id.rb_net_audio:
//                    position=3;
//                    break;
                default:
                    position=0;
            }
            setFragMent();
        }
    }

    public void setFragMent() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.fr_main_content,new ReplaceFragment(getPage()));
        ft.commit();
    }
    private BasePager getPage(){
        BasePager pager = pagers.get(position);
        if (pager!=null&&!pager.isData){
            pager.iniData();
            pager.isData=true;
        }
        return pager;
    }
    private boolean isExit=false;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK){
            if (position!=0){
                position=0;
                rg.check(R.id.rb_local_video);
                return true;
            }else if (!isExit){
                isExit=true;
                Toast.makeText(MainActivity.this, "再按一次退出！", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isExit=false;
                    }
                },2000);

                return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (isExit){
            EventBus.getDefault().post("kill");
        }
        super.onDestroy();
    }
}
