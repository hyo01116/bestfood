package com.example.bestfood;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toolbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bestfood.item.MemberInfoItem;
import com.example.bestfood.lib.MyLog;
import com.example.bestfood.lib.StringLib;
import com.example.bestfood.remote.RemoteService;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ProfileIconActivity extends AppCompatActivity implements View.onClickListener {
    private final String TAG = getClass().getSimpleName();

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int CROP_FROM_ALBUM = 3;

    Context context;
    ImageView profileIconImage;
    MemberInfoItem memberInfoItem;

    File profileIconFile;
    String profileIconFilename;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_icon);

        context = this;

        memberInfoItem = ((MyAPP)getApplication()).getMemberInfoItem();
        setToolbar();
        setView();
        setProfileIcon();
    }

    private void setToolbar(){
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.profile_setting);
        }
    }
    public void setView(){
        profileIconImage = (ImageView) findViewById(R.id.profile_icon);
        Button albumButton = (Button)findViewById(R.id.album);
        albumButton.setOnClickListener(this);

        Button cameraButton = (Button)findViewById(R.id.camera);
        cameraButton.setOnClickListener(this);
    }
    private void setProfileIcon(){
        MyLog.d(TAG, "onResume " + RemoteService.MEMBER_ICON_URL + memberInfoItem.memberIconFilename);
        if(StringLib.getInstance().isBlank(memberInfoItem.memberIconFilename)){
            Picasso.with(this).load(R.drawable.ic_profile).into(profileIconImage);
        }
        else{
            Picasso.with(this).load(RemoteService.MEMBER_ICON_URL + memberInfoItem.memberIconFilename)
                    .into(profileIconImage);
        }
    }
}
