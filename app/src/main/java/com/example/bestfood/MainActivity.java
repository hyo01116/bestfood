package com.example.bestfood;

import android.os.Bundle;
import android.os.strictmode.WebViewMethodCalledOnWrongThreadViolation;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.example.bestfood.item.MemberInfoItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import de.hdodenhof.circleimageview.CircleImageView;

import com.example.bestfood.item.MemberInfoItem;
import com.example.bestfood.lib.GoLib;
import com.example.bestfood.lib.StringLib;
import com.example.bestfood.remote.RemoteService;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private final String TAG = getClass().getSimpleName();

    MemberInfoItem memberInfoItem;
    DrawerLayout drawer;
    View headerLayout;
    CircleImageView profileIconImage;

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        memberInfoItem = ((MyAPP)getApplication()).getMemberInfoItem();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        headerLayout = navigationView.getHeaderView(0);
        GoLib.getInstance().goFragment(getSupportFragmentManager(), R.id.content_main, BestFoodListFragment.newInstance());
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        /*mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);*/
    }
    @Override
    public void onResume(){
        super.onResume();
        setProfileView();
    }

    public void setProfileView(){
        profileIconImage = (CircleImageView)headerLayout.findViewById(R.id.profile_icon);
        profileIconImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                drawer.closeDrawer(GravityCompat.START);
                GoLib.getInstance().goProfileActivity(MainActivity.this);
            }
        });
        if(StringLib.getInstance().isBlank(memberInfoItem.memberIconFilename)){
            Picasso.with(this).load(R.drawable.ic_profile).into(profileIconImage);
        }
        else{
            Picasso.with(this)
                    .load(RemoteService.MEMBER_ICON_URL + memberInfoItem.memberIconFilename)
                    .into(profileIconImage);
        }

        TextView nameText = (TextView) headerLayout.findViewById(R.id.name);
        if(memberInfoItem.name == null || memberInfoItem.name.equals("")){
            nameText.setText(R.string.name_need);
        }
        else{
            nameText.setText(memberInfoItem.name);
        }
    }

    @Override
    public void onBackPressed(){
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == R.id.nav_list){
            GoLib.getInstance().goFragment(getSupportFragmentManager(), R.id.content_main, BestFoodListFragment.newInstance());
        }
        else if(id == R.id.nav_map){
            GoLib.getInstance().goFragment(getSupportFragmentManager(), R.id.content_main, BestFoodMapFragment.newInstance());
        }
        else if (id == R.id.nav_keep) {
            GoLib.getInstance().goFragment(getSupportFragmentManager(), R.id.content_main, BestFoodKeepFragment.newInstance());
        }
        else if(id == R.id.nav_register){
            GoLib.getInstance().goFragment(getSupportFragmentManager(), R.id.content_main, BestFoodRegisterActivity(this));
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}