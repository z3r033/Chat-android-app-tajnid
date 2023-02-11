package com.zerophi.tajn;

import android.content.Intent;
import android.os.AsyncTask;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class splashscreen extends AppCompatActivity {


        private static final int time = 1000;
        ProgressBar mProgressBar ;
        @Override
        protected void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_splashscreen);

            mProgressBar = (ProgressBar) findViewById(R.id.splashprogrssbar);
            new BackgroundSplashTask().execute();
        }

        private class BackgroundSplashTask extends AsyncTask {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                mProgressBar.setVisibility(View.GONE);
                CheckSession();
                finish();
            }//http://127.0.0.1/chahttp://127.0.0.1/chatbackend/tbackend/


        }

        private void CheckSession () {
            Boolean CheckSessionb = Boolean.valueOf(SharedPref.readSharedSetting(splashscreen.this,"userconnect","False"));

            Intent intoLogin = new Intent(splashscreen.this,authentificationActivity.class);
            Intent intodrawerchat1 = new Intent (splashscreen.this,DrawerChat1.class);



            if(CheckSessionb){
                startActivity(intodrawerchat1);
                finish();
            }
            else
            {
                startActivity(intoLogin);
                finish();
            }

        }

}
