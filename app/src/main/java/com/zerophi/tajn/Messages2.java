package com.zerophi.tajn;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.zerophi.tajn.models.modelmessages;
import com.zerophi.tajn.models.modelutilisateur;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Messages2 extends AppCompatActivity {

    private String jsonURL = "http://10.0.0.1/chatbackend/getmessages.php";
    private String messagesendurl = "http://10.0.0.1/chatbackend/ajoutermessage.php";
    ListView mListView;
    Button btnsync, btnsend;
    EditText medtmessage;
    SwipeRefreshLayout refresh;
    //	Need handler for callbacks to the UI thread
    final Handler mHandler = new Handler();

    final Runnable deletemessageretained = new Runnable() {
        public void run() {
            deleteretained();
        }
    };
   private void scrolltoend() {
       mListView.post(new Runnable() {
           @Override
           public void run() {
               mListView.setSelection(mListView.getCount() - 1);
           }
       });
   }
    private void deleteretained() {
        Toast.makeText(Messages2.this,"sal",Toast.LENGTH_LONG).show();
        final int utilisateur_id = SharedPref.readSharedSettingint(Messages2.this, "user_id", 0);
        final int recipient_id = Integer.parseInt(getIntent().getStringExtra("contact_id"));
        String clientId = MqttClient.generateClientId();
        final MqttAndroidClient client =
                new MqttAndroidClient(Messages2.this, "tcp://10.0.0.1:1883",
                        clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    // We are connected

                    String topic = "noc/"+recipient_id;
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

                    Toast.makeText(Messages2.this,"success connection",Toast.LENGTH_LONG).show();
//https://chatappmek.000webhostapp.com/getmessages.php
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(Messages2.this,"failed connection",Toast.LENGTH_LONG).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        String clientId = MqttClient.generateClientId();
        final MqttAndroidClient client =
                new MqttAndroidClient(Messages2.this, "tcp://10.0.0.1:1883",
                        clientId);

        final int utilisateur_id = SharedPref.readSharedSettingint(Messages2.this, "user_id", 0);
        final int recipient_id = Integer.parseInt(getIntent().getStringExtra("contact_id"));
        final String username = SharedPref.readSharedSetting(Messages2.this,"username","0");
        medtmessage = (EditText) findViewById(R.id.edtmessages);
        btnsend = (Button) findViewById(R.id.btnsendmessage);
        btnsync = (Button) findViewById(R.id.btnsyncmessage);
        mListView = (ListView) findViewById(R.id.listviewmessage);
        new Dowloader(Messages2.this, jsonURL, mListView).execute();
      //  scrollMyListViewToBottom();

 refresh = (SwipeRefreshLayout) findViewById(R.id.swipemessage);
 refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
     @Override
     public void onRefresh() {
         new Dowloader(Messages2.this, jsonURL, mListView).execute();
         scrolltoend();
         refresh.setRefreshing(false);
     }
 });
      /*  btnsync.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                //                           new Dowloader(Messages2.this, jsonURL, mListView).execute();


/*
             try {
                    IMqttToken token = client.connect();
                    token.setActionCallback(new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            // We are connected
                            Toast.makeText(Messages.this,"success connection",Toast.LENGTH_LONG).show();
                            String topic = "noc/"+utilisateur_id;
                            int qos = 1;
                            try {
                                IMqttToken subToken = client.subscribe(topic, qos);
                                subToken.setActionCallback(new IMqttActionListener() {
                                    @Override
                                    public void onSuccess(IMqttToken asyncActionToken) {
                                        // The message was published
                                        Toast.makeText(Messages.this,"success pub",Toast.LENGTH_LONG).show();
                                        client.setCallback(new MqttCallback() {
                                            @Override
                                            public void connectionLost(Throwable throwable) {

                                            }

                                            @Override
                                            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                                    Toast.makeText(Messages.this,new String(mqttMessage.getPayload()),Toast.LENGTH_LONG).show();
                                    generateNotification(getApplicationContext(), new String(mqttMessage.getPayload()));
                                              mHandler.post(deletemessageretained);
                                       /*      try {
                                                    IMqttToken token = client.connect();
                                                    token.setActionCallback(new IMqttActionListener() {
                                                        @Override
                                                        public void onSuccess(IMqttToken asyncActionToken) {

                                                            // We are connected

                                                            String topic = "noc/"+utilisateur_id;
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

                                                            Toast.makeText(Messages.this,"success connection",Toast.LENGTH_LONG).show();

                                                        }

                                                        @Override
                                                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                                            // Something went wrong e.g. connection timeout or firewall problems
                                                            Toast.makeText(Messages.this,"failed connection",Toast.LENGTH_LONG).show();

                                                         }
                                                    });
                                                } catch (MqttException e) {
                                                    e.printStackTrace();
                                                }*/
                           /*                 }

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
                            Toast.makeText(Messages.this,"failed connection",Toast.LENGTH_LONG).show();

                        }
                    });
                } catch (MqttException e) {
                    e.printStackTrace();
                }

*/
                              //         }


                                   //}
   //     );

        btnsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new messagesend(Messages2.this, messagesendurl, medtmessage, utilisateur_id, recipient_id).execute();
                new Dowloader(Messages2.this, jsonURL, mListView).execute();


                try {
                    IMqttToken token = client.connect();
                    token.setActionCallback(new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {

                            // We are connected

                            String topic = "noc/"+recipient_id;
                            String payload = "vous avez un nouveau message from"+username;
                            byte[] encodedPayload = new byte[0];
                            try {
                                encodedPayload = payload.getBytes("UTF-8");
                                MqttMessage message = new MqttMessage(encodedPayload);
                                message.setRetained(true);
                                client.publish(topic, message);
                            } catch (UnsupportedEncodingException | MqttException e) {
                                e.printStackTrace();
                            }

                            //           Toast.makeText(Messages.this,"success connection",Toast.LENGTH_LONG).show();

                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            // Something went wrong e.g. connection timeout or firewall problems
                            Toast.makeText(Messages2.this,"failed connection",Toast.LENGTH_LONG).show();

                        }
                    });
                } catch (MqttException e) {
                    e.printStackTrace();
                }


            }
        });

    }

    private class Dowloader extends AsyncTask<Void, Integer, String> {
        modelutilisateur c;

        Context mContext;
        String jurl;
        ListView mListView;
        ProgressDialog pd;

        public Dowloader(Context c, String jurl, ListView listView) {
            this.mContext = c;
            this.jurl = jurl;
            this.mListView = listView;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(mContext);
            pd.setTitle("Attendez s'il veut plait !");
            pd.setMessage("searching messages :) !!! ");
            pd.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            return downloadMessages();

        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            pd.dismiss();
            ;

            if (s.startsWith("Error")) {
                Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();

            } else { // Toast.makeText(mContext,s,Toast.LENGTH_SHORT).show();

               Parser p = new Parser(mContext, s, mListView);
                p.execute();

            }
        }

        private String downloadMessages() {
            final int utilisateur_id = SharedPref.readSharedSettingint(Messages2.this, "user_id", 1);
            String sender_id = getIntent().getStringExtra("contact_id");
            Object connection = connect.connect(jurl);
            HttpURLConnection connection1 = (HttpURLConnection) connection;
            try {
                OutputStream os = new BufferedOutputStream(connection1.getOutputStream());
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

                String dataurl = "recipient_id=" + utilisateur_id + "&sender_id=" + sender_id;


                bw.write(dataurl);
                bw.flush();
                bw.close();
                os.close();
                if (connection1.getResponseCode() == connection1.HTTP_OK) {

                    InputStream is = new BufferedInputStream(connection1.getInputStream());
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));

                    String line;
                    StringBuffer jsonData = new StringBuffer();
                    while ((line = br.readLine()) != null) {
                        jsonData.append(line + "\n");
                    }
                    br.close();
                    is.close();
                    return jsonData.toString();
                } else {
                    return "Error " + connection1.getResponseMessage();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "Error" + e.getMessage();
            }
        }
    }

    private class Parser extends AsyncTask<Void, Void, Boolean> {


        Context mContext;
        ListView mListView;
        String jdata;
        int jj;

        ArrayList<modelmessages> message = new ArrayList<>();

        ProgressDialog pd;

        public Parser(Context context, String jdata, ListView listView) {
            this.mContext = context;
            this.jdata = jdata;
            this.mListView = listView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(mContext);
            pd.setTitle("attend ");
            pd.setMessage("atteint un moment !!! ");
            pd.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            return this.parsecontact();

        }


        private Boolean parsecontact() {
            try {

                JSONArray ja = new JSONArray(jdata);
                JSONObject jo;

                message.clear();
                modelmessages con;

                for (int i = 0; i < ja.length(); i++) {
                    jo = ja.getJSONObject(i);


                    String message_text = jo.getString("message");
                    String message_date = jo.getString("date_e");
                    String sender_id = jo.getString("sender_id");
                    String recipient_id = jo.getString("recipient_id");
                    StringBuffer sb = new StringBuffer(sender_id);
                    int sender_idd =    Integer.parseInt(sb.substring(0,1));
                    StringBuffer sb2 = new StringBuffer(recipient_id);
                    int recipient_idd =    Integer.parseInt(sb2.substring(0,1));

                    con = new modelmessages();

                    con.setMessage(message_text);
                    con.setDate(message_date);
                    con.setSender_id(sender_idd);
                    con.setRecipient_id(recipient_idd);

                    message.add(con);
                }

                return true;

            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean isParsed) {
            super.onPostExecute(isParsed);

            pd.dismiss();
            if (isParsed) {

                mListView.setAdapter(new CustomAdaptermessages(mContext, message));

            } else {
                Toast.makeText(mContext, jdata, Toast.LENGTH_SHORT).show();
            }


        }

    }

    private static class connect {

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

        public static Object connectget(String jurl) {
            try {
                URL url = new URL(jurl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //CON PROPS
                con.setRequestMethod("GET");
                con.setConnectTimeout(15000);
                con.setReadTimeout(15000);
                con.setDoInput(true);

                return con;

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "Error " + e.getMessage();

            } catch (IOException e) {
                e.printStackTrace();
                return "Error " + e.getMessage();

            }
        }

    }

    private class CustomAdaptermessages extends BaseAdapter {

        private Context mContext;
        private ArrayList<modelmessages> arraymessages;

        public CustomAdaptermessages(Context context, ArrayList<modelmessages> arraymessages) {
            mContext = context;
            this.arraymessages = arraymessages;
        }

        @Override
        public int getCount() {
            return arraymessages.size();
        }

        @Override
        public Object getItem(int position) {
            return arraymessages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.message_item_left, parent, false);
            }
            ImageView imageprofile = (ImageView) convertView.findViewById(R.id.profile_url_messageleft);
          //  TextView txtusername = (TextView) convertView.findViewById(R.id.username_message);
            TextView textMessage = (TextView) convertView.findViewById(R.id.text_messageleft);
            TextView date_message = (TextView) convertView.findViewById(R.id.date_messageleft);


            final modelmessages message = (modelmessages) this.getItem(position);

            final String messages = message.getMessage();
            final String date = message.getDate();
            final int  sender_id_from_base = message.getSender_id();
            final int recipient_id_from_base = message.getRecipient_id();

            final String profile_url_howa = getIntent().getStringExtra("profile_url");
            final String profile_url_ana = SharedPref.readSharedSetting(Messages2.this, "profile_url", "0");

            final String username_howa = getIntent().getStringExtra("username");
            final String username_ana = SharedPref.readSharedSetting(Messages2.this, "username","0");
            final int user_id_howa = Integer.parseInt(getIntent().getStringExtra("contact_id"));
            final int user_id_ana = SharedPref.readSharedSettingint(Messages2.this,"user_id",0);


            if(sender_id_from_base == user_id_ana) {


              //  txtusername.setText(username_ana);
                textMessage.setText(messages);
                date_message.setText(date);
                Glide.with(getApplicationContext()).load(/*"https://chatappmek.000webhostapp.com/"+*/profile_url_ana).into(imageprofile);
            }else if (sender_id_from_base ==user_id_howa){

            //    txtusername.setText(username_howa);
                textMessage.setText(messages);
                date_message.setText(date);
                Glide.with(getApplicationContext()).load(/*"https://chatappmek.000webhostapp.com/"+*/profile_url_howa).into(imageprofile);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //     openSendMessage(username,email,profile_url,contact_id+"");
                    Toast.makeText(Messages2.this, "hahaha", Toast.LENGTH_LONG).show();

                }
            });
            return convertView;

        }



    }


    public class messagesend extends AsyncTask<Void, Void, String> {
        Context c;
        String urlAddress;
        EditText edtmessage;
        int sender_id;
        int recipient_id;

        ProgressDialog pd;

        public messagesend(Context c, String urlAddress, EditText edtmessage, int sender_id, int recipient_id) {
            this.c = c;
            this.urlAddress = urlAddress;
            this.edtmessage = edtmessage;

            this.sender_id = sender_id;
            this.recipient_id = recipient_id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(c, "atteint !!! ", Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(c, s, Toast.LENGTH_LONG).show();
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
                String dataurl = "sender_id=" + sender_id + "&recipient_id=" + recipient_id + "&message=" + edtmessage.getText().toString();

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
    // Issues a notification to inform the user that server has sent a message.
    private static void generateNotification(Context context, String message) {
        int icon = R.drawable.ic_sync_black_24dp;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context, Contact.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification.Builder builder = new Notification.Builder(context);
        builder.setDefaults( Notification.DEFAULT_VIBRATE);
        builder.setDefaults( Notification.DEFAULT_SOUND);
        builder.setAutoCancel(true);
        builder.setContentTitle("hahah");
        builder.setContentText("vous avez un nouveau message");
        builder.setSmallIcon(icon);
        builder.setContentIntent(pendingIntent);
        //    builder.setOnlyAlertOnce(true);
        builder.setOngoing(true);
        builder.setNumber(100);
        builder.build();

        Notification notification = builder.getNotification();
        notificationManager.notify(0, notification);
    }



    private void scrollMyListViewToBottom() {
        mListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                mListView.setSelection(mListView.getCount() - 1);
            }
        });
    }
}
