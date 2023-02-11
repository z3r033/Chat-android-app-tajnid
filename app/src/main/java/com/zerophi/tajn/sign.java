package com.zerophi.tajn;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

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
import java.net.MalformedURLException;
import java.net.URL;

public class sign extends AppCompatActivity {
MaterialButton btnajouteretudiant ;
TextInputEditText usernameedt, passwordedt, emailedt,passwordedt2 ;
String urlAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        urlAddress = UrlsGlobal.ajouternvutilisateur;//"http://10.0.0.1/chatbackend/ajouternvutilisateur.php";


        btnajouteretudiant = (MaterialButton) findViewById(R.id.ajouterutilisateur);
        usernameedt = (TextInputEditText) findViewById(R.id.username_edit_text);
        passwordedt = (TextInputEditText) findViewById(R.id.password_edit_text);
        emailedt = (TextInputEditText) findViewById(R.id.email_edit_text);
        passwordedt2 =(TextInputEditText)findViewById(R.id.password_edit_text2);



        btnajouteretudiant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean noErrors = true;
                    String usernameedtst = usernameedt.getText().toString();
                    String emailedtst = emailedt.getText().toString() ;
                    String passwordedtst = passwordedt.getText().toString();
                    String passwrodedtst2 = passwordedt2.getText().toString();

                    String profile_url = "http://10.0.0.1/chatbackend/images/1.png";
                    if (usernameedtst.isEmpty()) {
                   usernameedt.setError("remplire ce champ obligatoire");
                   noErrors = false;
               } else {
                   usernameedt.setError(null);
               }
                if (emailedtst.isEmpty()) {
                    emailedt.setError("remplire ce champ obligatoire");
                    noErrors = false;
                } else {
                    emailedt.setError(null);
                }
                if (passwordedtst.isEmpty()) {
                    passwordedt.setError("remplire ce champ obligatoire");
                    noErrors = false;
                } else {
                    passwordedt.setError(null);
                }
                  if (!passwordedtst.equals(passwrodedtst2)) {
                    passwordedt.setError("les deux mot de pass pas les memes");
                      passwordedt2.setError("les deux mot de pass pas les memes");
                    noErrors = false;
                } else {
                    passwordedt.setError(null);
                      passwordedt2.setError(null);
                }
                 if (noErrors) {
               new ajouterutilisateur(sign.this, usernameedtst, emailedtst, passwordedtst, profile_url).execute();
           }

            }
        });


    }

    private class ajouterutilisateur extends AsyncTask <Void,Void,String> {
        String username , password , email , profile_url;
        Context ct;
        public ajouterutilisateur(Context ctx, String usernameedtst, String emailedtst, String passwordedtst, String profile_url) {
            this.username =usernameedtst;
            this.ct = ctx;
            this.password= passwordedtst;
            this.email = emailedtst;
            this.profile_url = profile_url;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
              Toast.makeText(ct, "atteint !!! ", Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(getApplicationContext(),"response : "+s,Toast.LENGTH_LONG);
        }

        @Override
        protected String doInBackground(Void... voids) {
               return this.send();
        }

        private String send() {
              Object mconnect = connect.connect(urlAddress);
            if (mconnect.toString().startsWith("Error")) {
                return mconnect.toString();
            }
            try {
                HttpURLConnection connection = (HttpURLConnection) mconnect;

                OutputStream os = new BufferedOutputStream(connection.getOutputStream());
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                String dataurl = "username=" + username + "&email=" + email + "&password=" + password+ "&profile_url"+profile_url;

                bw.write(dataurl);
                bw.flush();
                bw.close();
                os.close();
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
            //        edtmessage.setText("");
                    return response.toString();


                } else {
                    return "erreurs" + String.valueOf(responsecode);
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            //Get response


            return "go go go !!! ";
        }


    }
    static class connect {

        public static Object connect(String url) {

            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(25000);
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(true);
                connection.setDefaultUseCaches(true);
                return connection;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "Error : url n'existe pas";

            } catch (IOException e) {
                e.printStackTrace();
                return "Error : erreur de connection !! ";
            }


        }


    }
}
