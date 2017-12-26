package com.example.joan.merrychristmas;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //final Date christmas = new Date(2017-1900, 11, 25);
        final Calendar christmas = Calendar.getInstance(TimeZone.getDefault());
        christmas.set(christmas.getWeekYear(), 11, 25, 0, 0, 0);

        final TextView countTextView = (TextView) findViewById(R.id.countTextVew);
        final TextView myText = (TextView) findViewById(R.id.myText);

        final Handler handler = new Handler();
        Runnable run = new Runnable() {
            boolean startRed = true;
            boolean isRed = true;
            @Override
            public void run() {
                //String text = "<font color=#cc0029>Merry </font> <br> <font color=#00ff00>Christmas</font>";
                String htmlText = "";
                String original = "Merry Christmas!";
                isRed = startRed;
                startRed = !startRed;
                for (int i = 0; i < original.length(); i++) {
                    htmlText += "<font color=#";
                    if (isRed) {
                        htmlText += "cc0029";
                    } else {
                        htmlText += "00ff00";
                    }
                    htmlText += ">"+ original.charAt(i) + "</font>";
                    if (original.charAt(i) != ' ') {
                        isRed = !isRed;
                    }
                }
                myText.setText(Html.fromHtml(htmlText));

                String count = "";
                Calendar now = Calendar.getInstance(TimeZone.getDefault());
                long difference = Math.abs(christmas.getTimeInMillis() - now.getTimeInMillis());
                int seconds = (int) (difference / 1000) % 60 ;
                int minutes = (int) ((difference / (1000*60)) % 60);
                int hours   = (int) ((difference / (1000*60*60)) % 24);
                int days = (int) TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS);
                count = "You are in " + TimeZone.getDefault().getDisplayName() + "\nIt's " + days + " days, " + hours + " hours, " + minutes + " minutes, " + seconds + " seconds left!";
                countTextView.setText(count);
                Log.i("Runnable has run", "a second must have passed");
                handler.postDelayed(this, 1000);
            }
        };

        handler.post(run);
    }
}
