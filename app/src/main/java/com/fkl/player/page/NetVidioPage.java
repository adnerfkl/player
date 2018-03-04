package com.fkl.player.page;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;
import com.fkl.player.R;
import com.fkl.player.activity.PlayVideoActivity;
import com.fkl.player.adpter.NetVideoAdapter;
import com.fkl.player.entity.MediaItem;
import com.fkl.player.utils.Constants;
import com.fkl.player.view.XListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by adner on 2018/1/11.
 */
public class NetVidioPage extends BasePager {
    private static final String TAG = NetVidioPage.class.getSimpleName();
    @ViewInject(R.id.lv_net_video)
    private XListView lvNetVideo;
    @ViewInject(R.id.tv_no_net)
    private TextView tvNoNet;
    @ViewInject(R.id.pb_loading)
    private ProgressBar pbLoading;
     private  ArrayList<MediaItem> mediaItems;
    private NetVideoAdapter adapter;
    private boolean isMore=false;
    public NetVidioPage(Context context) {
        super(context);
    }

    @Override
    public View iniView() {
        View view = View.inflate(context, R.layout.page_net_video, null);
        x.view().inject(NetVidioPage.this,view);
        lvNetVideo.setOnItemClickListener(new MyOnItemClickListener());
        lvNetVideo.setPullLoadEnable(true);
        lvNetVideo.setPullRefreshEnable(true);
        lvNetVideo.setXListViewListener(new MIXListViewListener());
        return view;
    }

    @Override
    public void iniData() {
        super.iniData();
        getDataFromNet();
    }
    private void onLoad() {
        lvNetVideo.stopRefresh();
        lvNetVideo.stopLoadMore();
        lvNetVideo.setRefreshTime("更新时间："+getSysteTime());
    }

    private void getDataFromNet() {
        RequestParams params = new RequestParams(Constants.NET_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                paraData(result);
                Log.e(TAG, "onSuccess: "+result );
                isMore=true;
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.e(TAG, "onError: "+ex.getMessage() );
                isMore=false;
                showData();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.e(TAG, "onCancelled: "+cex.getMessage() );
                isMore=false;

            }

            @Override
            public void onFinished() {
                Log.e(TAG, "onFinished: ");
            }
        });
    }

    private void paraData(String json) {
        try {
           if (!isMore){
               mediaItems=parJson(json);
               showData();
           }else {
               ArrayList<MediaItem>  moreMediaItems=parJson(json);
               moreMediaItems.addAll(moreMediaItems);
               adapter.notifyDataSetChanged();
               onLoad();
           }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showData()  {

        if (mediaItems!=null&&mediaItems.size()>0){
            adapter=new NetVideoAdapter(context,mediaItems);
            lvNetVideo.setAdapter(adapter);
            onLoad();
            tvNoNet.setVisibility(View.GONE);
        }else {
            tvNoNet.setVisibility(View.VISIBLE);
        }
        pbLoading.setVisibility(View.GONE);
    }
    public String getSysteTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }
    private ArrayList<MediaItem> parJson(String json) throws JSONException {
        ArrayList<MediaItem> items=new ArrayList<>();
        JSONObject obj=new JSONObject(json);
        JSONArray trailers = obj.optJSONArray("trailers");
        if (trailers!=null&&trailers.length()>0){
          for (int i=0;i<trailers.length();i++){
              MediaItem mediaItem = new MediaItem();
              JSONObject item = (JSONObject) trailers.get(i);
              if (item!=null){
                  String movieName = item.optString("movieName");//name
                  mediaItem.setName(movieName);

                  String videoTitle = item.optString("videoTitle");//desc
                  mediaItem.setDesc(videoTitle);

                  String imageUrl = item.optString("coverImg");//imageUrl
                  mediaItem.setImageUrl(imageUrl);

                  String hightUrl = item.optString("hightUrl");//data
                  mediaItem.setData(hightUrl);
                   items.add(mediaItem);
              }

          }
        }
        return items;
    }
    class MyOnItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            Bundle bundle = new Bundle();
            Intent intent = new Intent(context, PlayVideoActivity.class);
            bundle.putSerializable("videolist",mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position",position);
            context.startActivity(intent);
        }
    }
    class MIXListViewListener implements XListView.IXListViewListener{

        @Override
        public void onRefresh() {//下拉刷新
           getDataFromNet();
            onLoad();
        }

        @Override
        public void onLoadMore() {//加载
          getMoreDate();
        }
    }

    private void getMoreDate() {
        RequestParams params = new RequestParams(Constants.NET_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                paraData(result);
                Log.e(TAG, "onSuccess: "+result );
                isMore=true;
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.e(TAG, "onError: "+ex.getMessage() );
                isMore=false;
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.e(TAG, "onCancelled: "+cex.getMessage() );
                isMore=false;
            }

            @Override
            public void onFinished() {
                Log.e(TAG, "onFinished: ");
                isMore=false;
            }
        });
    }
}
