package com.zerophi.tajn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.zerophi.tajn.models.modelgroupmessage;
import com.zerophi.tajn.models.modelmessages;

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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class GroupeMessages extends AppCompatActivity {
    private String urlgetmessagesgroupe = UrlsGlobal.getmessagesgroupe;
    private String urlajoutermessage = UrlsGlobal.ajoutermessagegroupe;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private MaterialButton btnsend;
    private TextInputEditText edtmessage;
    private SwipeRefreshLayout refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupe_messages);
        mRecyclerView = (RecyclerView) findViewById(R.id.listviewmessagegroupe);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);
        refresh = (SwipeRefreshLayout) findViewById(R.id.swipemessagegroupe);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Dowloader(GroupeMessages.this, urlgetmessagesgroupe, mRecyclerView).execute();

                refresh.setRefreshing(false);
            }
        });

        final int utilisateur_id = SharedPref.readSharedSettingint(GroupeMessages.this, "user_id", 0);
   //     final int groupe_id = getIntent().getIntExtra("groupe_id"));
        final String groupe_name = getIntent().getStringExtra("name_groupe");
        edtmessage = (TextInputEditText) findViewById(R.id.edtmessagesgroupe);
        btnsend = (MaterialButton) findViewById(R.id.btnsendmessagegroupe);

        new Dowloader(GroupeMessages.this, urlgetmessagesgroupe, mRecyclerView).execute();


        final String profile_url_ana = SharedPref.readSharedSetting(GroupeMessages.this, "profile_url", "0");

        final String image_groupe = getIntent().getStringExtra("image_groupe");

        btnsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new messagesend(GroupeMessages.this, urlajoutermessage, edtmessage, utilisateur_id, 1).execute();
                new Dowloader(GroupeMessages.this, urlgetmessagesgroupe, mRecyclerView).execute();

            }
        });
    }

    private class Dowloader extends AsyncTask<Void, Integer, String> {

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

            } else {

                Parser p = new Parser(mContext, s, mRecyclerView);
                p.execute();

            }
        }

        private String downloadMessages() {
            final int utilisateur_id = SharedPref.readSharedSettingint(GroupeMessages.this, "user_id", 1);
            int groupe_id = Integer.parseInt( getIntent().getStringExtra("groupe_id"));
            Object connection = connect(jurl);
            HttpURLConnection connection1 = (HttpURLConnection) connection;
            try {
                OutputStream os = new BufferedOutputStream(connection1.getOutputStream());
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

                String dataurl = "groupe_id=" + groupe_id ;


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

        private Object connect(String jurl) {
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) new URL(jurl).openConnection();
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
        TextInputEditText edtmessage;
        int sender_id;
        int groupe_id;

        ProgressDialog pd;

        public messagesend(Context c, String urlAddress, TextInputEditText edtmessage, int sender_id, int groupe_id) {
            this.c = c;
            this.urlAddress = urlAddress;
            this.edtmessage = edtmessage;

            this.sender_id = sender_id;
            this.groupe_id = groupe_id;
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
            Object mconnect = connect(urlAddress);
            if (mconnect.toString().startsWith("Error")) {
                return mconnect.toString();
            }
            try {
                HttpURLConnection connection = (HttpURLConnection) mconnect;

                OutputStream os = new BufferedOutputStream(connection.getOutputStream());
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                String dataurl = "groupe_id=" + groupe_id + "&sender_id=" + sender_id + "&message=" + edtmessage.getText().toString();

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

        private Object connect(String urlAddress) {
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) new URL(urlAddress).openConnection();
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

    private class Parser extends AsyncTask<Void, Void, Boolean> {


        Context mContext;
        RecyclerView mListView;
        String jdata;
        int jj;

        ArrayList<modelgroupmessage> message = new ArrayList<>();

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
                modelgroupmessage con;

                for (int i = 0; i < ja.length(); i++) {
                    jo = ja.getJSONObject(i);

                    int message_id = jo.getInt("message_id");
                    String message_text = jo.getString("message_text");
                    String message_date = jo.getString("date_e");
                    int sender_id = jo.getInt("sender_id");
                    int groupe_id = jo.getInt("groupe_id");


                    con = new modelgroupmessage();

                    con.setMessage_id(message_id);
                    con.setGroupe_id(groupe_id);
                    con.setMessage_date(message_date);
                    con.setSender_id(sender_id);
                    con.setMessage_text(message_text);

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



            } else {
                Toast.makeText(mContext, jdata, Toast.LENGTH_SHORT).show();
            }


        }

    }

    private class adaptermessage extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Context ctx;
        private ArrayList<modelgroupmessage> md;

        public adaptermessage(Context ctx, ArrayList<modelgroupmessage> md) {
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
                    modelgroupmessage ms = md.get(position);
                    StringBuffer sb = new StringBuffer(ms.getMessage_date());
                    String date = sb.subSequence(10, 16).toString();
                    viewHolder0.date_message.setText(date);
                    viewHolder0.textMessage.setText(ms.getMessage_text());
                    viewHolder0.textMessage.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            Toast.makeText(ctx, "ggggggggggggggg", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    });

                   Glide.with((ctx)).load(UrlsGlobal.urlimages+ms.getSender_id()+"png").into(viewHolder0.imageprofile);
                    break;

                case 1:
                    ViewHolder1 viewHolder1 = (ViewHolder1) holder;
                    modelgroupmessage ms2 = md.get(position);
                    StringBuffer sb2 = new StringBuffer(ms2.getMessage_date());
                    final int message_id = ms2.getMessage_id();
                    String date2 = sb2.subSequence(10, 16).toString();
                    viewHolder1.date_message.setText(date2);
                    viewHolder1.textMessage.setText(ms2.getMessage_text());
                    viewHolder1.textMessage.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                            builder.setMessage("tu est sure que vous voulez supprimer ce message ?")
                                    .setCancelable(false)
                                    .setPositiveButton("oui", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

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


                   Glide.with((ctx)).load(UrlsGlobal.urlimages+ms2.getSender_id()+".png").into(viewHolder1.imageprofile);
                    break;
            }


//Glide.with(()).load(/*"https://chatappmek.000webhostapp.com/"+*/profile_url_ana).into(holder.imageprofile);

        }

        @Override
        public int getItemViewType(int position) {

            modelgroupmessage me = md.get(position);

            final int sender_id_from_base = me.getSender_id();


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


    }
}