package com.zerophi.tajn;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.zerophi.tajn.models.modelutilisateur;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class DrawerChat1 extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    final ArrayList<modelutilisateur> models = new ArrayList<>();
    String clientId;
    MqttAndroidClient client;
    int utilisateur_id ;
    String prof;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_drawer_chat1);
           prof = SharedPref.readSharedSetting(DrawerChat1.this, "profile_url", "0");
        utilisateur_id = SharedPref.readSharedSettingint(DrawerChat1.this, "user_id", 0);
         clientId = MqttClient.generateClientId();
         client =
                new MqttAndroidClient(DrawerChat1.this, UrlsGlobal.urlmqtt,
                        clientId);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
       getSupportFragmentManager().beginTransaction().add(R.id.main_cont,new acceuilfragment()).commit();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View h = navigationView.getHeaderView(0);

        ImageView img = h.findViewById(R.id.imageViewprofile);
        TextView us = h.findViewById(R.id.usernamedrawer);
        TextView emailtxt = h.findViewById(R.id.emaildrawer);
        Glide.with(getApplicationContext()).load(/*"https://chatappmek.000webhostapp.com/"+*/prof).into(img);
        final String email = SharedPref.readSharedSetting(DrawerChat1.this, "email", "www.tajnid.ma");
          final String username = SharedPref.readSharedSetting(DrawerChat1.this, "username", "Tajnid chat");

        us.setText(username);
        emailtxt.setText(email);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
          //  super.onBackPressed();
              AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("est ce que tu est sure que vous voullez quitez lapp?")
                        .setCancelable(false)
                        .setPositiveButton("oui", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_HOME);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer_chat1, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            SharedPref.saveSharedSetting(DrawerChat1.this,"userconnect","False");
            Intent logout = new Intent(getApplicationContext(),authentificationActivity.class);
            startActivity(logout);
            finish();
            return true;


        }
        else if (id == R.id.add_contact){
startActivity(new Intent(getApplicationContext(),add_contact.class));
        }
        else if (id == R.id.action_setting){
            startActivity(new Intent(getApplicationContext(),setting.class));
        }


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_contact) {
            Intent intent = new Intent(getApplicationContext(),Contact.class);
            intent.putExtra("client_id",clientId);
           startActivity(intent);
        } else if (id == R.id.nav_start_noc) {
/*
            try {
                IMqttToken token = client.connect();
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        // We are connected
                        Toast.makeText(DrawerChat1.this,"success connection",Toast.LENGTH_LONG).show();
                        String topic = "noc/"+utilisateur_id;
                        int qos = 1;
                        try {
                            IMqttToken subToken = client.subscribe(topic, qos);
                            subToken.setActionCallback(new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    // The message was published
                                    Toast.makeText(DrawerChat1.this,"success pub",Toast.LENGTH_LONG).show();
                                    client.setCallback(new MqttCallback() {
                                        @Override
                                        public void connectionLost(Throwable throwable) {

                                        }

                                        @Override
                                        public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                                            Toast.makeText(DrawerChat1.this,new String(mqttMessage.getPayload()),Toast.LENGTH_LONG).show();
                                           generateNotification(getApplicationContext(), new String(mqttMessage.getPayload()));
                                          //  mHandler.post(deletemessageretained);

                                        }

                                        @Override
                                        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {



                                        }
                                    });
                                }

                                @Override
                                public void onFailure(IMqttToken asyncActionToken,
                                                      Throwable exception) {
                                    // The subscription could not be performed, maybe the user was not
                                    // authorized to subscribe on the specified topic e.g. using wildcards

                                }
                            });
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        // Something went wrong e.g. connection timeout or firewall problems
                        Toast.makeText(DrawerChat1.this,"failed connection",Toast.LENGTH_LONG).show();

                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
*/


              clientId = MqttClient.generateClientId();
            final MqttAndroidClient client =
                    new MqttAndroidClient(getApplicationContext(), UrlsGlobal.urlmqtt,
                            clientId);
            try {
                IMqttToken token = client.connect();
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        // We are connected
                    //    Toast.makeText(getApplicationContext(),"success connection",Toast.LENGTH_LONG).show();
                        String topic = "noc/"+utilisateur_id;
                        int qos = 1;
                        try {
                            IMqttToken subToken = client.subscribe(topic, qos);
                            subToken.setActionCallback(new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    // The message was published
                            //        Toast.makeText(getApplicationContext(),"success pub",Toast.LENGTH_LONG).show();
                                    client.setCallback(new MqttCallback() {
                                        @Override
                                        public void connectionLost(Throwable throwable) {

                                        }

                                        @Override
                                        public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                                      //      Toast.makeText(getApplicationContext(),new String(mqttMessage.getPayload()),Toast.LENGTH_LONG).show();
                                            generateNotification(getApplicationContext(), new String(mqttMessage.getPayload()));
                                            //  mHandler.post(deletemessageretained);

                                        }

                                        @Override
                                        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {



                                        }
                                    });
                                }

                                @Override
                                public void onFailure(IMqttToken asyncActionToken,
                                                      Throwable exception) {
                                    // The subscription could not be performed, maybe the user was not
                                    // authorized to subscribe on the specified topic e.g. using wildcards

                                }
                            });
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        // Something went wrong e.g. connection timeout or firewall problems
                        Toast.makeText(getApplicationContext(),"failed connection",Toast.LENGTH_LONG).show();

                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }

        }else if (id == R.id.nav_login) {
    //        getSupportFragmentManager().beginTransaction().replace(R.id.main_cont,new AuthentificationFragment()).commit();
            SharedPref.saveSharedSetting(DrawerChat1.this,"userconnect","False");
            Intent logout = new Intent(getApplicationContext(),authentificationActivity.class);
            startActivity(logout);
            finish();
            return true;
        }
         else if (id == R.id.nav_setting) {
    //        getSupportFragmentManager().beginTransaction().replace(R.id.main_cont,new AuthentificationFragment()).commit();

                startActivity(new Intent(getApplicationContext(),setting.class));
            return true;
        }
        else if (id ==R.id.nav_connect_nv){
            getSupportFragmentManager().beginTransaction().replace(R.id.main_cont,new AuthentificationFragment()).commit();
        }
        else if (id ==R.id.nav_acceuil){
             getSupportFragmentManager().beginTransaction().replace(R.id.main_cont,new acceuilfragment()).commit();
        }
        else if (id==R.id.nav_groupe){
            startActivity(new Intent(getApplicationContext(),Groups.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void generateNotification(Context context, String message) {
        String [] messages = message.split("///");


        int icon = R.drawable.ic_sync_black_24dp;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context, Contact.class);
       /*     intent.putExtra("username", messages[1]);
            intent.putExtra("profile_url", messages[2]);
            intent.putExtra("username", messages[3]);
            intent.putExtra("contact_id", messages[4]);*/
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        Notification.Builder builder = new Notification.Builder(context);
       // builder.setDefaults( Notification.DEFAULT_VIBRATE);
       // builder.setDefaults( Notification.DEFAULT_SOUND);
        builder.setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.slow));
        long[] vibrate = { 0, 100, 200, 300 };
        builder.setVibrate(vibrate);
        builder.setAutoCancel(true);
        builder.setContentTitle("new message");
        builder.setContentText("vous avez un nouveau message"+message);
        builder.setSmallIcon(icon);
        builder.setContentIntent(pendingIntent);

        //    builder.setOnlyAlertOnce(true);
        builder.setOngoing(true);
        builder.setNumber(100);
        builder.build();

        Notification notification = builder.getNotification();
        notificationManager.notify(0, notification);

    }
    private boolean deleteretained() {
        Toast.makeText(DrawerChat1.this, "sal", Toast.LENGTH_LONG).show();
        final int utilisateur_id = SharedPref.readSharedSettingint(DrawerChat1.this, "user_id", 0);


        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    // We are connected

                    String topic = "noc/" + utilisateur_id;
                    String payload = "";
                    byte[] encodedPayload = new byte[0];
                    try {
                        encodedPayload = payload.getBytes("UTF-8");
                        MqttMessage message = new MqttMessage(encodedPayload);
                        message.setRetained(true);
                        client.publish(topic, message);
                    } catch (UnsupportedEncodingException | MqttException e) {
                        e.printStackTrace();
                    }

      //              Toast.makeText(DrawerChat1.this, "success connection", Toast.LENGTH_LONG).show();

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(DrawerChat1.this, "failed connection", Toast.LENGTH_LONG).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();

        }
        return true;
    }

}
