package dev.joven.fourthlib;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mgd.mgddevtools.mgdGameView;
import com.mgd.mgddevtools.mgdUserPolicy;
import com.mgd.mgddevtools.mgdUtil;

public class MainScreenActivity extends AppCompatActivity {


    mgdGameView gameView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(1024, 1024); // Remove system UI at the Top of the Screen
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE); // Remove Title BAR

        setContentView(R.layout.activity_main_screen);

        // Fix the initialization of the class-level variable
        gameView = findViewById(R.id.gameContent);
        gameView.setVisibility(View.INVISIBLE);

        mgdUserPolicy policy = new mgdUserPolicy(this);
        policy.setPositiveButtonClickListener( positive ->{
            policy.closeCreatedDialog();


            LaunchGameContent();
        });
        policy.setNegativeButtonClickListener( negative -> {
            policy.closeCreatedDialog();

            //Add Logic to save to SharedPrefs once User does not accept Data Policy

            finishAffinity();
        });   // otherwise call LaunchGameContent()
        policy.createPolicyDialog("https://www.iubenda.com/privacy-policy/32052245", "I Agree", Color.RED, "Dont Agree", Color.GREEN);
    }

    private void LaunchGameContent() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            /**
             * Updated: Add Navigation UI/UX to GameView on Side A:
             * Determined by mgdUtil.navStatus
             * a response value of False -> Signifies that the NavUI is not Hidden
             * while a response value of True -> Signifies that the NavUI is hidden
             *
             * Add other Vector Icons, and additional UI's based on your preference
             * like viewing the Data Policy, Adding a Share Link etc..
             */
            //region [ Bottom Navigation UI/UX ]
            BottomNavigationView navUI = findViewById(R.id.navBar);
            if(!mgdUtil.navStatus)
            {
                navUI.setVisibility(View.VISIBLE);
                navUI.setOnNavigationItemSelectedListener(item -> {
                    if(item.getItemId() == R.id.nav_reload)
                    {
                        recreate();
                    }
                    else if(item.getItemId() == R.id.nav_notify)
                    {
                        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    else if(item.getItemId() == R.id.nav_quit)
                    {
                        finishAffinity();
                    }

                    return true;
                });
            }
            else
            {
                navUI.setVisibility(View.GONE);
            }
            //endregion

            Log.d(mgdUtil.AppTAG, "GameURL: " + mgdUtil.gameURL);
            gameView.setVisibility(View.VISIBLE);



            gameView.createGameUI(mgdUtil.gameURL, true, "ca-app-pub-3940256099942544/6300978111");
        }, 300);

    }
}