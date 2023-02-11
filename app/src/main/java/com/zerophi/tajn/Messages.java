package com.zerophi.tajn;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.zerophi.tajn.models.modelmessages;
import com.zerophi.tajn.models.modelutilisateur;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class Messages extends AppCompatActivity {
    private String jsonURL = UrlsGlobal.getmessages;// "http://10.0.0.1/chatbackend/getmessages.php";
    private String messagesendurl = UrlsGlobal.ajoutermessage;//"http://10.0.0.1/chatbackend/ajoutermessage.php";
    Button btnsync, btnsend;
    EditText medtmessage;
    SwipeRefreshLayout refresh;
    String client_id;
    static String profile_url_ana, profile_url_howa, username_howa, username_ana;
    static int user_id_howa;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    //	Need handler for callbacks to the UI thread
    final Handler mHandler = new Handler();

    final Runnable deletemessageretained = new Runnable() {
        public void run() {
            deleteretained();
        }
    };

/*    private void scrolltoend() {
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.setSelection(mRecyclerView.getCount() - 1);
            }
        });
    }*/

    private void deleteretained() {
        Toast.makeText(Messages.this, "sal", Toast.LENGTH_LONG).show();
        final int utilisateur_id = SharedPref.readSharedSettingint(Messages.this, "user_id", 0);
        final int recipient_id = Integer.parseInt(getIntent().getStringExtra("contact_id"));
        // String clientId = MqttClient.generateClientId();
        final MqttAndroidClient client =
                new MqttAndroidClient(Messages.this, UrlsGlobal.urlmqtt,
                        client_id);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    // We are connected

                    String topic = "noc/" + recipient_id;
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

                    //        Toast.makeText(Messages.this, "success connection", Toast.LENGTH_LONG).show();
//https://chatappmek.000webhostapp.com/getmessages.php
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(Messages.this, "failed connection", Toast.LENGTH_LONG).show();

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
        mRecyclerView = (RecyclerView) findViewById(R.id.listviewmessage);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        client_id = getIntent().getStringExtra("client_id");
        //  layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);
        refresh = (SwipeRefreshLayout) findViewById(R.id.swipemessage);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Dowloader(Messages.this, jsonURL, mRecyclerView).execute();

                refresh.setRefreshing(false);
            }
        });


        String clientId = MqttClient.generateClientId();


        final int utilisateur_id = SharedPref.readSharedSettingint(Messages.this, "user_id", 0);
        final int recipient_id = Integer.parseInt(getIntent().getStringExtra("contact_id"));
        final String username = SharedPref.readSharedSetting(Messages.this, "username", "0");
        medtmessage = (EditText) findViewById(R.id.edtmessages);
        btnsend = (Button) findViewById(R.id.btnsendmessage);
        btnsync = (Button) findViewById(R.id.btnsyncmessage);

        new Dowloader(Messages.this, jsonURL, mRecyclerView).execute();


        final MqttAndroidClient client =
                new MqttAndroidClient(Messages.this, UrlsGlobal.urlmqtt,
                        client_id);
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    //  Toast.makeText(getApplicationContext(),"success connection",Toast.LENGTH_LONG).show();
                    String topic = "noc/" + utilisateur_id;
                    int qos = 1;
                    try {
                        IMqttToken subToken = client.subscribe(topic, qos);
                        subToken.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                // The message was published
                                // Toast.makeText(getApplicationContext(),"success pub",Toast.LENGTH_LONG).show();
                                client.setCallback(new MqttCallback() {
                                    @Override
                                    public void connectionLost(Throwable throwable) {

                                    }

                                    @Override
                                    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                                        //    Toast.makeText(getApplicationContext(),new String(mqttMessage.getPayload()),Toast.LENGTH_LONG).show();
                                        generateNotification(getApplicationContext(), new String(mqttMessage.getPayload()));
                                        //  mHandler.post(deletemessageretained);

                                        new Dowloader(Messages.this, jsonURL, mRecyclerView).execute();
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
                    Toast.makeText(getApplicationContext(), "failed connection", Toast.LENGTH_LONG).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }


        profile_url_ana = SharedPref.readSharedSetting(Messages.this, "profile_url", "0");

        profile_url_howa = getIntent().getStringExtra("profile_url");
        username_howa = getIntent().getStringExtra("username");
        final String email = getIntent().getStringExtra("email");
        final String contact_id = getIntent().getStringExtra("contact_id");
        username_ana = SharedPref.readSharedSetting(Messages.this, "username", "0");
        user_id_howa = Integer.parseInt(getIntent().getStringExtra("contact_id"));


        btnsync.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           new Dowloader(Messages.this, jsonURL, mRecyclerView).execute();

                                       }


                                   }
        );

        btnsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new messagesend(Messages.this, messagesendurl, medtmessage, utilisateur_id, recipient_id).execute();
                new Dowloader(Messages.this, jsonURL, mRecyclerView).execute();


                try {
                    IMqttToken token = client.connect();
                    token.setActionCallback(new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {

                            // We are connected

                            String topic = "noc/" + recipient_id;
                            String payload = "vous avez un nouveau message from : " + username/* +"///"+ profile_url_howa+"///"+username+"///"+contact_id */;

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
                            Toast.makeText(Messages.this, "failed connection", Toast.LENGTH_LONG).show();

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
        RecyclerView mListView;
        ProgressDialog pd;

        public Dowloader(Context c, String jurl, RecyclerView listView) {
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

                Parser p = new Parser(mContext, s, mRecyclerView);
                p.execute();

            }
        }

        private String downloadMessages() {
            final int utilisateur_id = SharedPref.readSharedSettingint(Messages.this, "user_id", 1);
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
        RecyclerView mListView;
        String jdata;
        int jj;

        ArrayList<modelmessages> message = new ArrayList<>();

        ProgressDialog pd;

        public Parser(Context context, String jdata, RecyclerView listView) {
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

                    String message_id = jo.getString("message_id");
                    StringBuffer sb0 = new StringBuffer(message_id);

                    int message_idd = Integer.parseInt(sb0.substring(0, 1));
                    String message_text = jo.getString("message");
                    String message_date = jo.getString("date_e");
                    String sender_id = jo.getString("sender_id");
                    String recipient_id = jo.getString("recipient_id");
                    StringBuffer sb = new StringBuffer(sender_id);
                    int sender_idd = Integer.parseInt(sb.substring(0, 1));
                    StringBuffer sb2 = new StringBuffer(recipient_id);
                    int recipient_idd = Integer.parseInt(sb2.substring(0, 1));

                    con = new modelmessages();

                    con.setMessage_id(message_idd);
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
                mAdapter = new adaptermessage(mContext, message);
                mRecyclerView.setAdapter(mAdapter);
                // mListView.setAdapter(new CustomAdaptermessages(mContext, message));


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
            // Toast.makeText(c, "atteint !!! ", Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // Toast.makeText(c, s, Toast.LENGTH_LONG).show();
            edtmessage.setText("");
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
        builder.setDefaults(Notification.DEFAULT_VIBRATE);
        builder.setDefaults(Notification.DEFAULT_SOUND);
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


    /*   private void scrollMyListViewToBottom() {
           mListView.post(new Runnable() {
               @Override
               public void run() {
                   // Select the last row so it will scroll into view...
                   mListView.setSelection(mListView.getCount() - 1);
               }
           });
       }*/
    class adaptermessage extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Context ctx;
        private ArrayList<modelmessages> md;

        public adaptermessage(Context ctx, ArrayList<modelmessages> md) {
            this.ctx = ctx;
            this.md = md;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view0 = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item_left, parent, false);
            View view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item_right, parent, false);

            switch (viewType) {
                case 0:
                    return new ViewHolder0(view0);
                case 1:
                    return new ViewHolder1(view1);

            }
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

            switch (holder.getItemViewType()) {
                case 0:
                    ViewHolder0 viewHolder0 = (ViewHolder0) holder;
                    modelmessages ms = md.get(position);
                    StringBuffer sb = new StringBuffer(ms.getDate());
                    String date = sb.subSequence(10, 16).toString();
                    viewHolder0.date_message.setText(date);
                    viewHolder0.textMessage.setText(ms.getMessage());
                    viewHolder0.textMessage.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            Toast.makeText(ctx, "ggggggggggggggg", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    });

                    Glide.with((ctx)).load(/*"https://chatappmek.000webhostapp.com/"+*/Messages.profile_url_howa).into(viewHolder0.imageprofile);
                    break;

                case 1:
                    ViewHolder1 viewHolder1 = (ViewHolder1) holder;
                    modelmessages ms2 = md.get(position);
                    StringBuffer sb2 = new StringBuffer(ms2.getDate());
                    final int message_id = ms2.getMessage_id();
                    String date2 = sb2.subSequence(10, 16).toString();
                    viewHolder1.date_message.setText(date2);
                    viewHolder1.textMessage.setText(ms2.getMessage());
                    viewHolder1.textMessage.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                            builder.setMessage("tu est sure que vous voulez supprimer ce message ?")
                                    .setCancelable(false)
                                    .setPositiveButton("oui", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            new messagedelete(ctx, UrlsGlobal.deletemessage, message_id);
                                            //  Toast.makeText(ctx,"ggggggggggggggg",Toast.LENGTH_SHORT).show();

                                        }
                                    })
                                    .setNegativeButton("non", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                            return true;
                        }
                    });


                    Glide.with((ctx)).load(/*"https://chatappmek.000webhostapp.com/"+*/Messages.profile_url_ana).into(viewHolder1.imageprofile);
                    break;
            }


//Glide.with(()).load(/*"https://chatappmek.000webhostapp.com/"+*/profile_url_ana).into(holder.imageprofile);

        }

        @Override
        public int getItemViewType(int position) {

            modelmessages me = md.get(position);

            final int sender_id_from_base = me.getSender_id();
            final int recipient_id_from_base = me.getRecipient_id();

            final int user_id_ana = SharedPref.readSharedSettingint(ctx, "user_id", 0);

            if (sender_id_from_base == user_id_ana) {
                return 1;

            } else {
                return 0;
            }

        }

        @Override
        public int getItemCount() {
            return md.size();
        }

        public class ViewHolder0 extends RecyclerView.ViewHolder {
            ImageView imageprofile;
            //TextView txtusername ;
            TextView textMessage;
            TextView date_message;

            public ViewHolder0(@NonNull View itemView) {
                super(itemView);
                imageprofile = (ImageView) itemView.findViewById(R.id.profile_url_messageleft);
                //  TextView txtusername = (TextView) convertView.findViewById(R.id.username_message);
                textMessage = (TextView) itemView.findViewById(R.id.text_messageleft);
                date_message = (TextView) itemView.findViewById(R.id.date_messageleft);

            }
        }

        public class ViewHolder1 extends RecyclerView.ViewHolder {
            ImageView imageprofile;
            //TextView txtusername ;
            TextView textMessage;
            TextView date_message;

            public ViewHolder1(@NonNull View itemView) {
                super(itemView);
                imageprofile = (ImageView) itemView.findViewById(R.id.profile_url_messageright);
                //  TextView txtusername = (TextView) convertView.findViewById(R.id.username_message);
                textMessage = (TextView) itemView.findViewById(R.id.text_messageright);
                date_message = (TextView) itemView.findViewById(R.id.date_messageright);

            }
        }

        public  class messagedelete extends AsyncTask<Void, Void, String> {
            Context c;
            String urlAddress;
            int message_id;
            ProgressDialog pd;

            public messagedelete(Context c, String urlAddress, int message_id) {
                this.c = c;
                this.urlAddress = urlAddress;
                this.message_id = message_id;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                // Toast.makeText(c, "atteint !!! ", Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                // Toast.makeText(c, s, Toast.LENGTH_LONG).show();

            }

            @Override
            protected String doInBackground(Void... voids) {
                return this.send();

            }

            private String send() {
                Object mconnect = connect(UrlsGlobal.deletemessage);
                if (mconnect.toString().startsWith("Error")) {
                    return mconnect.toString();
                }
                try {
                    HttpURLConnection connection = (HttpURLConnection) mconnect;

                    OutputStream os = new BufferedOutputStream(connection.getOutputStream());
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                    String dataurl = "message_id=" + message_id;

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
            public  Object connect(String url) {

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
}




