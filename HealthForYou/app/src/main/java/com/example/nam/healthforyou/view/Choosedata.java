package com.example.nam.healthforyou.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.nam.healthforyou.component.DBhelper;
import com.example.nam.healthforyou.R;
import com.example.nam.healthforyou.item.HealthChooseItem;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Choosedata extends AppCompatActivity {
    DBhelper dBhelper;
    ArrayList<JSONObject> healthJSONChatarray;
    String searchDate=null;
    String searchYear=null;
    String searchMonth=null;
    List<String> MONTHLIST = null;
    ArrayAdapter<String> arrayYear;
    ArrayAdapter<String> arrayMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosedata);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.remove_symbol);
        dBhelper = new DBhelper(getApplicationContext(),"healthforyou.db", null, 1);

        final List<String> YEARLIST=dBhelper.getYearofHealthdata();
        YEARLIST.add(0,"연도 검색");
        System.out.println(YEARLIST+"년");
        arrayYear = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, YEARLIST);
        MaterialBetterSpinner spinner_year = (MaterialBetterSpinner)findViewById(R.id.spinner_year);

        final MaterialBetterSpinner spinner_month = (MaterialBetterSpinner)findViewById(R.id.spinner_month);
        spinner_year.setAdapter(arrayYear);
        spinner_month.setEnabled(false);
        spinner_month.setClickable(false);
        spinner_month.setFocusable(false);
        ListView ListView_health = (ListView)findViewById(R.id.lv_healthdata);//GridView 찾기
        final healthChooseChatAdapter healthChooseChatAdapter = new healthChooseChatAdapter();//GridView Adapter
        ListView_health.setAdapter(healthChooseChatAdapter);

        spinner_year.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchYear = YEARLIST.get(position);
                if(position!=0)////선택을 제대로 했으면
                {
                    spinner_month.setEnabled(true);//월을 고르는 Spinner를 활성화 시켜주고
                    //연도에 따른 월 정보를 채워넣어줌
                    spinner_month.setFocusable(true);
                    spinner_month.setClickable(true);
                    MONTHLIST=dBhelper.getMonthofHealthdata(searchYear);//찾고자 하는 연도의 월
                    MONTHLIST.add(0,"월 검색");
                    arrayMonth = new ArrayAdapter<>(Choosedata.this,android.R.layout.simple_list_item_1, MONTHLIST);
                    spinner_month.setAdapter(arrayMonth);
                    Toast.makeText(Choosedata.this, "검색하고자 하는 월을 선택해주세요", Toast.LENGTH_SHORT).show();

                }else{//년도가 초기메뉴면 월을 초기화 시킴
                    MONTHLIST.clear();//검색할 수 있는 달들을 초기화
                    arrayMonth.clear();
                    arrayMonth.notifyDataSetChanged();

                    spinner_month.setText("월");
                    spinner_month.setEnabled(false);//할수 없게
                    spinner_month.setFocusable(false);//클릭 안되게
                    spinner_month.setClickable(false);//클릭 안되게

                    healthChooseChatAdapter.deleteItem();//리스트뷰 초기화
                    healthChooseChatAdapter.notifyDataSetChanged();
                    Toast.makeText(Choosedata.this, "검색하고자 하는 연도를 선택해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });//Setting완료 //결과를 보여주는 부분

        spinner_month.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchMonth = MONTHLIST.get(position);
                if(position!=0)
                {
                    healthChooseChatAdapter.deleteItem();
                    searchDate = searchYear+"-"+searchMonth+"-01";
                    System.out.println(searchDate+"SearchDate");
                    //'" + searchDate + "'

                    healthJSONChatarray=dBhelper.PrintMyAvgDataForChat("SELECT avg(user_bpm),avg(user_res),strftime('%Y-%m-%d',data_signdate),strftime('%Y-%m',data_signdate) as date from User_health WHERE date = strftime('%Y-%m','" + searchDate + "') GROUP BY strftime('%Y-%m-%d',data_signdate) ORDER BY date desc;");

                    System.out.println(healthJSONChatarray);

                    for(int i=0;i<healthJSONChatarray.size();i++)
                    {
                        JSONObject jsonObject = healthJSONChatarray.get(i);
                        ////JSON -> healthChooseItem
                        HealthChooseItem healthChooseItem = new HealthChooseItem();
                        healthChooseItem.gv_userbpm = jsonObject.optInt("user_bpm");
                        healthChooseItem.gv_userres = jsonObject.optInt("user_res");
                        healthChooseItem.gv_signdate = jsonObject.optString("data_signdate");

                        healthChooseChatAdapter.addItem(healthChooseItem);
                    }
                    healthChooseChatAdapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(Choosedata.this, "검색하고자 하는 달을 선택해주세요", Toast.LENGTH_SHORT).show();
                    healthChooseChatAdapter.deleteItem();
                    healthChooseChatAdapter.notifyDataSetChanged();
                }
            }
        });



        ListView_health.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = getIntent();
                HealthChooseItem healthChooseItem = healthChooseChatAdapter.getGridItem(position);
                intent.putExtra("date", healthChooseItem.gv_signdate);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                overridePendingTransition(0,0);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
