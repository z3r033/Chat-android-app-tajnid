package com.zerophi.tajn;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.zerophi.tajn.models.modelutilisateur;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

public class LoginAuth extends AsyncTask <Void, Void , String> {

    Context c;
    String url;
    EditText usernametxt, passwordtxt;
    ProgressBar pd;
    modelutilisateur mUser;
    CheckBox mCheckBox;

    public LoginAuth(Context c, String url, EditText username, EditText password, CheckBox mCheckBox) {

        this.c = c;
        this.url = url;
        this.usernametxt = username;
        this.passwordtxt = password;
        this.mCheckBox = mCheckBox;

        mUser = new modelutilisateur();

        mUser.setEmail(usernametxt.getText().toString());
        mUser.setPassword(passwordtxt.getText().toString());

    }


    @Override
    protected String doInBackground(Void... voids) {
        return this.login();
    }

    private String login() {
        Object mconnect = connection_backend.connect(url);
        if (mconnect.toString().startsWith("Error")) {
            return mconnect.toString();
        }
        try {
            HttpURLConnection connection = (HttpURLConnection) mconnect;

            OutputStream os = new BufferedOutputStream(connection.getOutputStream());
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

            String loginData = new format_de_url(mUser).packLoginData();

            bw.write(loginData);
            bw.flush();

            os.close();

            //ici get la reponse sans decoder
            int responsecode = connection.getResponseCode();
            if (responsecode == connection.HTTP_OK) {


                InputStream is = new BufferedInputStream(connection.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while ((line = br.readLine()) != null) {
                    response.append(line + "\n");
                }
                br.close();
                is.close();
                return response.toString();


            } else {
                return "Erreur" + String.valueOf(responsecode);
            }


        } catch (IOException e) {
            e.printStackTrace();
            return "dd";
        }

  //     return "dd";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

      display(s);
      // Toast.makeText(c,s+"",Toast.LENGTH_LONG).show();


    }

    private void display(String s) {
        final String JSON_STRING = "{\"chat\":" + s + "}";
       // Toast.makeText(c,"d"+JSON_STRING,Toast.LENGTH_LONG).show();

        JSONObject js = null;
        try {
            js = (new JSONObject(JSON_STRING)).getJSONObject("chat");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String error = null;
        try {
            error = js.getString("error");
        } catch (JSONException e) {
            e.printStackTrace();
        }
       // Toast.makeText(c,"d"+error,Toast.LENGTH_LONG).show();
            if (error != "true") {
                 if (mCheckBox.isChecked()) {
                    SharedPref.saveSharedSetting(c, "userconnect", "True");
                }
                String username = null;
                try {
                    username = js.getString("username");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String email = null;
                try {
                    email = js.getString("email");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String urlprofile = null;
                try {
                    urlprofile = js.getString("profile_url");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                int user_id = 0;
                try {
                    user_id = js.getInt("user_id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                SharedPref.saveSharedSetting(c, "profile_url", urlprofile);
                SharedPref.saveSharedSetting(c, "email", email);
                SharedPref.saveSharedSettingint(c, "user_id", user_id);
                 SharedPref.saveSharedSetting(c, "username", username);
                Toast.makeText(c, "d" + urlprofile, Toast.LENGTH_LONG).show();


                    c.startActivity(new Intent(c, DrawerChat1.class));


                } else {
                String error_msg = null;
                try {
                    error_msg = js.getString("error_msg");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Toast.makeText(c, " !!" + error_msg, Toast.LENGTH_LONG).show();
                }
            }


    }

