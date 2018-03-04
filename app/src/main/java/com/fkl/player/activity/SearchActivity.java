package com.fkl.player.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fkl.player.R;
import com.fkl.player.adpter.SearchAdapter;
import com.fkl.player.entity.SearchBean;
import com.fkl.player.utils.Constants;
import com.fkl.player.utils.JsonParser;
import com.google.gson.Gson;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SearchActivity extends Activity implements View.OnClickListener {

    private String TAG = SearchActivity.class.getSimpleName();
    private EditText edInput;
    private ImageView ivVoiceIcon;
    private TextView tvSearch;
    private ListView lvResult;
    private ProgressBar pbSearching;
    private TextView tvNoresult;
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private List<SearchBean.ItemData> items;
    private String url;
    private SearchAdapter adapter;

    private void findViews() {
        setContentView(R.layout.activity_search);
        edInput = (EditText) findViewById(R.id.ed_input);
        ivVoiceIcon = (ImageView) findViewById(R.id.iv_voice_icon);
        tvSearch = (TextView) findViewById(R.id.tv_search);
        lvResult = (ListView) findViewById(R.id.lv_result);
        pbSearching = (ProgressBar) findViewById(R.id.pb_searching);
        tvNoresult = (TextView) findViewById(R.id.tv_noresult);
    }

    private void setClickListener() {
        tvSearch.setOnClickListener(this);
        ivVoiceIcon.setOnClickListener(this);
        lvResult.setOnItemClickListener(new MyListOnClickListener());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViews();
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5a5f6ebf");
        setClickListener();

    }

    @Override
    public void onClick(View view) {
        if (view == tvSearch) {
            searchText();
        } else if (view == ivVoiceIcon) {
            showDialog();
        }
    }

    private void searchText() {
        String text = edInput.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            if (items != null && items.size() > 0) {
                items.clear();
            }
            try {
                text = URLEncoder.encode(text, "UTF-8");
                url = Constants.SEARCH_URL + text;
                getDataFromNet();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private void getDataFromNet() {
        pbSearching.setVisibility(View.VISIBLE);
        RequestParams params = new RequestParams(url);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                pbSearching.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                pbSearching.setVisibility(View.GONE);
            }
        });
    }

    private void processData(String result) {
          SearchBean searchBean=  parsedJson(result);
        items=searchBean.getItems();
        showData();
    }

    private void showData() {
          if (items!=null){
              adapter=new SearchAdapter(this,items);
              lvResult.setAdapter(adapter);
              tvNoresult.setVisibility(View.GONE);
          }else {
              tvNoresult.setVisibility(View.VISIBLE);
              adapter.notifyDataSetChanged();
          }
        pbSearching.setVisibility(View.GONE);
    }

    private SearchBean parsedJson(String result) {

        return new Gson().fromJson(result,SearchBean.class);
    }


    /////////////////////////////////////////////////////////////////////////////////
    private void showDialog() {
        RecognizerDialog mDialog = new RecognizerDialog(SearchActivity.this, new MyInitListener());
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");//中文
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");//普通话
        mDialog.setListener(new MyRecognizerDialogListener());
        //4.显示dialog，接收语音输入
        mDialog.show();
    }

    class MyRecognizerDialogListener implements RecognizerDialogListener {

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            String result = recognizerResult.getResultString();
            String iatResult = JsonParser.parseIatResult(result);
            //解析好的
            Log.e("MainActivity", "text ==" + iatResult);

            String sn = null;
            // 读取json结果中的sn字段
            try {
                JSONObject resultJson = new JSONObject(recognizerResult.getResultString());
                sn = resultJson.optString("sn");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mIatResults.put(sn, iatResult);
            StringBuffer resultBuffer = new StringBuffer();//拼成一句
            for (String key : mIatResults.keySet()) {
                resultBuffer.append(mIatResults.get(key));
            }

            edInput.setText(resultBuffer.toString());
            edInput.setSelection(edInput.length());
        }

        @Override
        public void onError(SpeechError speechError) {
            //Log.e(TAG, speechError.getMessage()+"");
            showErrow();
        }
    }

    private void showErrow() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("不能识别内容，请重试！");
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }

    class MyInitListener implements InitListener {
        @Override
        public void onInit(int i) {
            if (i != ErrorCode.SUCCESS) {
                Toast.makeText(SearchActivity.this, "初始化失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class MyListOnClickListener implements AdapterView.OnItemClickListener {


        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            Intent intent = new Intent(SearchActivity.this, SearchInfoActivity.class);
            intent.putExtra("weburl",items.get(position).getDetailUrl());
            startActivity(intent);
        }
    }
}
