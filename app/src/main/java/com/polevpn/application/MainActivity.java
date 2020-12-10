package com.polevpn.application;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.orhanobut.logger.Logger;
import com.polevpn.application.services.PoleVPNManager;
import com.polevpn.application.tools.SharePref;
import com.polevpn.application.tools.Utils;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private boolean isLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.requestPermission(this);
        isLogin = SharePref.getInstance().getBoolean("login");

        if (!isLogin){
            Intent intentStart = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intentStart);
            finish();
            return;
        }

        setTheme(R.style.Theme_MyApplication_NoActionBar);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_faq, R.id.nav_share)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        setFullScreen(this.getWindow());

        navigationView.getMenu().getItem(3).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                PoleVPNManager.getInstance().getPoleVPN().stop();
                SharePref.getInstance().setBoolean("login",false);
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                MainActivity.this.finish();
                return false;
            }
        });

        Switch modeSwitch = (Switch) findViewById(R.id.switch_global);

        if(SharePref.getInstance().getBoolean("speed_up_mode")){
            modeSwitch.setChecked(true);
        }

        String email = SharePref.getInstance().getString("email");
        long uid = SharePref.getInstance().getLong("uid");

        TextView textEmail = findViewById(R.id.text_email);
        textEmail.setText(email);
        TextView textPoleId = findViewById(R.id.text_poleid);
        textPoleId.setText("PoleID:"+String.valueOf(uid));

        modeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharePref.getInstance().setBoolean("speed_up_mode",isChecked);
                PoleVPNManager.getInstance().getPoleVPN().setRouteMode(isChecked);
                Bundle bundle = new Bundle();
                bundle.putString("type","speed_setting");
                bundle.putBoolean("speed_up_mode",isChecked);
                PoleVPNManager.getInstance().sendMessage("home",bundle);
            }
        });

    }


    private void setFullScreen(Window window){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            window.setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if(controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }else{
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}