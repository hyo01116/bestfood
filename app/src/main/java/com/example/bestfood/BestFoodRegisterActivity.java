package com.example.bestfood;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.bestfood.item.GeoItem;
import com.example.bestfood.lib.GoLib;
import com.example.bestfood.lib.MyLog;

public class BestFoodRegisterActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    public static FoodInfoItem currentItem = null;

    Context context;

    //기본적인 정보 설정 + 프래그먼트 실행
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bestfood_register);

        context = this;

        int memberSeq = ((MyApp)getApplication()).getMemberSeq();

        //bestfoodregisterlocationfragment로 넘길 기본적인 정보
        FoodIntoItem infoItem = new FoodInfoItem();
        infoItem.memberSeq = memberSeq;
        infoItem.latitude = GeoItem.getKnownLocation().latitude;
        infoItem.longitude = GeoItem.getKnownLocation().longitude;

        MyLog.d(TAG, "infoItem " + infoItem.toString());
        setToolbar();

        //프래그먼트를 화면에 보여줌
        GoLib.getInstance().goFragment(getSupportFragmentManager(), R.id.content_main, BestFoodRegisterLocationFragment.newInstance(infoItem));

    }

    private void setToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.bestfood_register);
        }
    }
    //오른쪽 상단메뉴 (닫기)
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_close, menu);
        return true;
    }
    //모든 메뉴 지정
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.home:
                finish();
                break;
            case R.id.action_close:
                finish();
                break;
        }
        return true;
    }
    //결과처리 메소드 (requestCode 요청코드, resultCode 결과코드, data 결과데이터)
    //다른 액티비티 실행 시, 결과를 돌려받기 위해 실행하는 메소드 -> 액티비티가 결과를 받아서 프래그먼트로 전달
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        for(Fragment fragment : getSupportFragmentManager().getFragments()){
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
