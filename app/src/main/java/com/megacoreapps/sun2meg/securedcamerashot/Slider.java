package com.megacoreapps.sun2meg.securedcamerashot;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;


public class Slider extends AppCompatActivity {
    private ViewFlipper viewFlipper;
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String AGREEMENT_ACCEPTED = "agreementAccepted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Check if the user has already accepted the agreement
        if (prefs.getBoolean(AGREEMENT_ACCEPTED, false)) {
            // User has already accepted, start EmailPasswordActivity (or your main activity)
            startEmailPasswordActivity();
        } else {
            setContentView(R.layout.slider);


            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isAcceptingText()) {
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
            ImageView imageView = findViewById(R.id.imageView14);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            viewFlipper = findViewById(R.id.viewFlipper);
            final Handler handler = new Handler();
            final int delay = 100; //milliseconds
            handler.postDelayed(new Runnable() {
                public void run() {
                    if (viewFlipper.getDisplayedChild() == 4) {
                        Button button = findViewById(R.id.play4);
                        button.setText("CLOSE INSTRUCTIONS");
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Slider.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                    handler.postDelayed(this, delay);
                }
            }, delay);
//            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
//            editor.putBoolean(AGREEMENT_ACCEPTED, true);
//            editor.apply();

            // Start EmailPasswordActivity (or your main activity)
//            startEmailPasswordActivity();
        }
  }
    public void nextView(View v) {
        viewFlipper.setInAnimation(this, android.R.anim.slide_in_left);
        viewFlipper.setOutAnimation(this, android.R.anim.slide_out_right);
        viewFlipper.showNext();

        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(AGREEMENT_ACCEPTED, true);
        editor.apply();
    }

        private void startEmailPasswordActivity() {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish(); // Close the UserAgreementActivity
        }

//    @Override
//    public void onBackPressed() {
//        Intent intent = new Intent(this,MainActivity.class);
//        startActivity(intent);
//        finish();
//    }
}