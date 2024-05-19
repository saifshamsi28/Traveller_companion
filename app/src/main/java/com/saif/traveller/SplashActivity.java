package com.saif.traveller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashActivity extends AppCompatActivity {
    ImageView logoImage;
    TextView logoName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        logoImage=findViewById(R.id.logo);
        logoName=findViewById(R.id.logo_name);
        Intent goHome= new Intent(this, MainActivity.class);
        // to hold this screen for sometime
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(goHome);
                finish();
            }
        },2000);

        // to set animation
        Animation logoImageAnimate=AnimationUtils.loadAnimation(this,R.anim.logo_image_animation);
        Animation logoNameAnimate=AnimationUtils.loadAnimation(this,R.anim.logo_name_animation);
        logoImage.startAnimation(logoImageAnimate);
        logoName.startAnimation(logoNameAnimate);
    }
}