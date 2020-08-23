package com.example.bestfood;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bestfood.item.MemberInfoItem;
import com.example.bestfood.lib.EtcLib;
import com.example.bestfood.lib.GeoLib;
import com.example.bestfood.lib.MyLog;
import com.example.bestfood.lib.RemoteLib;
import com.example.bestfood.lib.StringLib;
import com.example.bestfood.remote.RemoteService;
import com.example.bestfood.remote.ServiceGenerator;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IndexActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        context = this;

        if(!RemoteLib.getInstance().isConnected(context)){
            showNoService();
            return;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startTask();
            }
        }, 1200);
    }

    private void showNoService(){
        TextView messageText = (TextView)findViewById(R.id.message);
        messageText.setVisibility(View.VISIBLE);

        Button closeButton = (Button) findViewById(R.id.close);
        closeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        closeButton.setVisibility(View.VISIBLE);
    }
    public void startTask(){
        String phone = EtcLib.getInstance().getPhoneNumber(this);     //사용자의 휴대폰번호 가져옴

        selectMemberInfo(phone);
        GeoLib.getInstance().setLastKnownLocation(this);
    }

    public void selectMemberInfo(String phone){
        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);
        Call<MemberInfoItem> call = remoteService.selectMemberInfo(phone);
        call.enqueue(new Callback<MemberInfoItem>(){
            @Override
            public void onResponse(Call<MemberInfoItem> call, Response<MemberInfoItem>response){
                MemberInfoItem item = response.body();

                if(response.isSuccessful() && !StringLib.getInstance().isBlank(item.name)){
                    MyLog.d(TAG, "success "+response.body().toString());
                    setMemberInfoItem(item);
                }
                else{
                    MyLog.d(TAG, "not success");
                    goProfileActivity(item);
                }
            }
            @Override
            public void onFailure(Call <MemberInfoItem> call, Throwable t){
                MyLog.d(TAG, "no internet connectivity");
                MyLog.d(TAG, t.toString());
            }
        });
    }

    private void setMemberInfoItem(MemberInfoItem item){     //memberinfo item을 application 객체에 저장 + 후에 startmain 호출
        ((MyApp) getApplicationContext()).setMemberInfoItem(item);
        startMain();
    }

    public void startMain(){
        Intent intent = new Intent(IndexActivity.this, MainActivity.class);
        startActivity(intent);

        finish();
    }

    private void goProfileActivity(MemberInfoItem item){
        if(item == null || item.seq <= 0){
            insertMemberPhone();
        }
        Intent intent = new Intent(IndexActivity.this, MainActivity.class);
        startActivity(intent);

        //Intent intent2 = new Intent(this, ProfileActivity.class);
        //startActivity(intent2);

        finish();
    }

    private void insertMemberPhone(){
        String phone = EtcLib.getInstance().getPhoneNumber(context);
        RemoteService remoteService =  ServiceGenerator.createService(RemoteService.class);

        Call<String> call = remoteService.insertMemberPhone(phone);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    MyLog.d(TAG, "success insert id "+response.body().toString());
                }
                else{
                    int statusCode = response.code();
                    ResponseBody errorBody = response.errorBody();

                    MyLog.d(TAG, "fail "+statusCode +errorBody.toString());
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t){
                MyLog.d(TAG, "no internet connectivity");
            }
        });
    }
}
