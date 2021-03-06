package com.bhsd.bustayo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bhsd.bustayo.R;
import com.bhsd.bustayo.adapter.LostGoodsAdapter;
import com.bhsd.bustayo.application.APIManager;
import com.bhsd.bustayo.dto.LostGoodsDetailInfo;
import com.bhsd.bustayo.dto.LostGoodsInfo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class LostGoodsActivity extends AppCompatActivity {

    // Spinner categorize;
    EditText searchLostGoods;
    Button searchButton;
    ArrayList<HashMap<String, String>> array;
    HashMap<String, String> detail_d;
    ArrayList<LostGoodsInfo> lostGoodsInfos;
    ArrayList<LostGoodsDetailInfo> lostGoodsDetailInfos;
    RecyclerView lgRecyclerView;
    LostGoodsAdapter lgAdapter;
    FloatingActionButton goUp;
    View loading_background;
    ProgressBar loading_progressBar;
    int page = 0;
    String searchStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost_goodsinfo);

        findViewById(R.id.searchGoBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // categorize = findViewById(R.id.lostGroup);
        searchButton = findViewById(R.id.searchButton);
        searchLostGoods = findViewById(R.id.userLostGoods);
        lgRecyclerView = findViewById(R.id.lostList);
        goUp = findViewById(R.id.goUp);
        loading_background = findViewById(R.id.loading_bg);
        loading_progressBar = findViewById(R.id.loading_progress);


        lgRecyclerView.setHasFixedSize(true);
        lgRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        lgRecyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(final View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (!v.canScrollVertically(1)) {    // ???????????? ???????????? ?????????????
                    loading_background.setVisibility(View.VISIBLE);
                    loading_progressBar.setVisibility(View.VISIBLE);
                    new Thread() {
                        @Override
                        public void run() {
                            getData();  // api ???????????? (data ??????!!)
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    lgAdapter.notifyDataSetChanged();
                                    loading_background.setVisibility(View.GONE);
                                    loading_progressBar.setVisibility(View.GONE);
                                }
                            });

                            interrupt();
                        }
                    }.start();
                }
            }
        });

        // ???????????? ??????
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page = 0;   // ????????? ??????????????? ???????????? ???????????? 0????????????
                loading_background.setVisibility(View.VISIBLE);
                loading_progressBar.setVisibility(View.VISIBLE);
                new Thread() {
                    @Override
                    public void run() {
                        searchStr = searchLostGoods.getText().toString();  // ????????? ????????? ??????
                        getData();  // ????????? ???????????? ?????? API ????????????

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setAdapter();
                                loading_background.setVisibility(View.GONE);
                                loading_progressBar.setVisibility(View.GONE);
                            }
                        });

                        interrupt();
                    }
                }.start();
            }
        });

        /* api??? ??????????????? ????????? ????????? ?????? ??????
        String[] categorizes ={"??????", "?????????", "??????", "??????", "????????????", "?????????", "??????", "?????????", "??????"};
        ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, categorizes);
        categorize.setAdapter(arrayAdapter);
        */

        new Thread() {
            @Override
            public void run() {
                loading_background.setVisibility(View.VISIBLE);
                loading_progressBar.setVisibility(View.VISIBLE);
                getData();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setAdapter();
                        loading_background.setVisibility(View.GONE);
                        loading_progressBar.setVisibility(View.GONE);
                    }
                });

                interrupt();
            }
        }.start();


        goUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lgRecyclerView.smoothScrollToPosition(0);
            }
        });
    }

    void getData() {
        if(page == 0) {
            lostGoodsInfos = new ArrayList<>();
            lostGoodsDetailInfos = new ArrayList<>();
        }

        array = APIManager.getAPIArray(APIManager.GET_LOST_GOODS,
                new String[]{ searchStr, Integer.toString(++page) },
                new String[]{ "atcId", "lstLctNm", "lstPrdtNm", "lstSbjt", "lstYmd" });
        // atcId : ??????ID     IstFilePathImg : ????????? ?????????

        for(HashMap<String,String> data : array) {
            LostGoodsInfo lostGoods;
            LostGoodsDetailInfo lostGoodsDetail;
            String atcId = data.get("atcId");
            String title = data.get("lstSbjt");
            String goods = data.get("lstPrdtNm");
            String date = data.get("lstYmd");
            String imageFile = "";
            detail_d = APIManager.getAPIMap((APIManager.GET_LOST_GOODS_DETAIL),
                    new String[]{ atcId }, new String[]{ "lstFilePathImg", "lstHor", "lstLctNm", "lstPlaceSeNm", "lstSteNm", "prdtClNm", "orgNm", "tel" });
            imageFile = detail_d.get("lstFilePathImg");

            String time = date + " " + detail_d.get("lstHor") + "??????";

            // data
            lostGoods = new LostGoodsInfo(imageFile, atcId, cutString(title), cutString(goods), date);

            // ???????????? data
            lostGoodsDetail = new LostGoodsDetailInfo(imageFile, atcId, title, detail_d.get("lstLctNm"), time,
                    detail_d.get("prdtClNm"), detail_d.get("lstSteNm"), detail_d.get("orgNm"), detail_d.get("tel"));
            lostGoodsInfos.add(lostGoods);
            lostGoodsDetailInfos.add(lostGoodsDetail);
        }
    }

    void setAdapter(){
        lgAdapter = new LostGoodsAdapter(lostGoodsInfos);lgRecyclerView.setAdapter(lgAdapter);

        lgAdapter.setOnListItemSelected(new LostGoodsAdapter.OnListItemSelected() {
            @Override
            public void onItemSelected(View v, int position) {
                Intent intent = new Intent(getApplicationContext(), LostGoodsDetailActivity.class);
                // ?????? ???????????? ??????
                intent.putExtra("goods_info", (Serializable) lostGoodsDetailInfos.get(position));
                startActivity(intent);
            }
        });

        lgRecyclerView.setAdapter(lgAdapter);
        lgAdapter.notifyDataSetChanged();
    }

    String cutString(String str) {
        if(str.length() > 10) { // ????????? 10????????? ????????????
            str = str.substring(0, 9);  // 10????????? ?????????
            str += "...";               // ... ?????????!
        }
        return str;
    }
}
