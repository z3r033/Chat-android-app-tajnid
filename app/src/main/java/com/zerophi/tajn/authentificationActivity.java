package com.zerophi.tajn;

import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class authentificationActivity extends AppCompatActivity {


    MaterialButton btnlogin,btnsign;
    TextView oublier;
    TextInputEditText usernametxt, passwordtxt;
    MaterialCheckBox mCheckBox;
    final static String url =  UrlsGlobal.login; // "http://10.0.0.1/chatbackend/login.php";
    final static String urlrec = UrlsGlobal.recuperermotdepass ; //"http://10.0.0.1/chatbackend/recuperermotdepass.php";
    //final static String url = "http://chatappmeknes.eu5.net/login.php";
    //final static String url = "https://chatappmek.000webhostapp.com/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentification);

        usernametxt = (TextInputEditText) findViewById(R.id.edtemail2);
        passwordtxt = (TextInputEditText) findViewById(R.id.edtpassword2);
        btnlogin = (MaterialButton) findViewById(R.id.btnlogin2);
        btnsign = (MaterialButton) findViewById(R.id.sign_button);
        mCheckBox = (MaterialCheckBox) findViewById(R.id.checkboxadmin2);
        mCheckBox.setChecked(true);
        oublier = (TextView) findViewById(R.id.oubliermotpas);

        oublier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean noErrors = true;
                final String s = usernametxt.getText().toString();

                if (s.isEmpty()) {
                    usernametxt.setError("remplire l'email pour recuperer password");
                    noErrors = false;
                } else {
                    usernametxt.setError(null);
                }

                if (noErrors) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(authentificationActivity.this);
                    builder.setMessage("tu est sure que vous voulez recuperer cet mot de pass?")
                            .setCancelable(false)
                            .setPositiveButton("oui", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    AndroidNetworking.post(urlrec)
                                            .setPriority(Priority.HIGH)
                                            .addBodyParameter("email", s)
                                            .build().getAsString(new StringRequestListener() {
                                        @Override
                                        public void onResponse(String response) {
                                            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                                        }

                                        @Override
                                        public void onError(ANError anError) {
                                            Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            })
                            .setNegativeButton("non", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();

                }


            }
        });

        btnsign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),sign.class));
            }
        });
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = usernametxt.getText().toString();
                String password = passwordtxt.getText().toString();

                if ((username.length() <= 0 || username == null) || (password.length() <= 0 || password == null)) {
                    Toast.makeText(getApplicationContext(), "slv remplire tous les champs!!!", Toast.LENGTH_LONG).show();
                } else {
                    new LoginAuth(authentificationActivity.this, url, usernametxt, passwordtxt,mCheckBox).execute();

                }

            }
        });
    }
}
