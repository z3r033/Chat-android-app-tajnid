package com.zerophi.tajn;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.model.Progress;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.zerophi.tajn.models.modelgroup;

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
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class Groups extends AppCompatActivity {
    private String jsonURL = UrlsGlobal.getgroupes;

    ListView mListView;
    Button btn;
    SwipeRefreshLayout refresh;
    CustomAdaptergroupe adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        mListView = (ListView) findViewById(R.id.list_group);
        refresh = (SwipeRefreshLayout) findViewById(R.id.swiperefreshgroup);
        TextInputEditText searchcontact = (TextInputEditText) findViewById(R.id.searchgroup);
        searchcontact.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                (Groups.this).adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Dowloader(Groups.this, jsonURL, mListView).execute();
                refresh.setRefreshing(false);
            }
        });
          new Dowloader(Groups.this, jsonURL, mListView).execute();
    }

    private class Dowloader extends AsyncTask<Void, Integer, String> {
        Context ctx;
        String jurl;
        ListView mListView;
        ProgressDialog mProgressDialog;

        public Dowloader(Context ctx, String jsonURL, ListView listView) {

            this.ctx = ctx;
            this.jurl = jsonURL;
            this.mListView = listView;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(ctx);

            mProgressDialog.setTitle("Attendez s'il veut plait");
            mProgressDialog.setMessage("searching for groups");
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            mProgressDialog.dismiss();
            if (s.startsWith("Error")) {
                Toast.makeText(ctx, s, Toast.LENGTH_SHORT).show();
            } else {
                Parser p = new Parser(ctx, s, mListView);
                p.execute();

            }

        }

        @Override
        protected String doInBackground(Void... voids) {
            return downloadgroups();
        }

        private String downloadgroups() {
            final int utilisateur_id = SharedPref.readSharedSettingint(ctx, "user_id", 0);

            Object connection = connect(jurl);
            HttpURLConnection connection2 = (HttpsURLConnection) connection;
            try {
                OutputStream os = new BufferedOutputStream(connection2.getOutputStream());
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

                String dataurl = "utilisateur_id=" + utilisateur_id;
                bw.write(dataurl);
                bw.flush();
                bw.close();
                os.close();
                if (connection2.getResponseCode() == connection2.HTTP_OK) {
                    InputStream is = new BufferedInputStream(connection2.getInputStream());
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
                    return "Error " + connection2.getResponseMessage();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "Error" + e.getMessage();
            }

        }
    }

    private Object connect(String url) {


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

    private class Parser extends AsyncTask<Void, Void, Boolean> {
        Context ctx;
        String datareturned;
        ListView mListView;
        ArrayList<modelgroup> groups = new ArrayList<>();
        ProgressDialog mProgressDialog;
        public Parser(Context ctx, String datareturned, ListView listView) {
            this.ctx = ctx;
            this.datareturned = datareturned;
            mListView = listView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(ctx);
            mProgressDialog.setTitle("Attending ");
            mProgressDialog.setMessage("un moment");
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mProgressDialog.dismiss();
            adapter = new CustomAdaptergroupe(ctx, groups);
            if (aBoolean) {
                mListView.setAdapter(adapter);

            } else {
                Toast.makeText(ctx, datareturned, Toast.LENGTH_SHORT).show();
            }

        }



        @Override
        protected Boolean doInBackground(Void... voids) {
            return this.parsegroupes();
        }

        private Boolean parsegroupes() {
            try {
                JSONArray ja = new JSONArray(datareturned);
                JSONObject jo;
                groups.clear();

                modelgroup groupmodel;
                for (int i = 0; i < ja.length(); i++) {
                    jo = ja.getJSONObject(i);
                    int groupe_id = jo.getInt("groupe_id");
                    int creatorgroupe = jo.getInt("creator_groupe");
                    String image_groupe = jo.getString("image_groupe");
                    String name_groupe = jo.getString("name_groupe");

                    groupmodel = new modelgroup();

                    groupmodel.setCreatorgroupe(creatorgroupe);
                    groupmodel.setName_groupe(name_groupe);
                    groupmodel.setGroupe_id(groupe_id);
                    groupmodel.setImage_groupe(image_groupe);

                    groups.add(groupmodel);


                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;

            }


        }
    }

    private class CustomAdaptergroupe extends BaseAdapter implements Filterable {
        private Context ctx;
        private ArrayList<modelgroup> arraygroupe;
        private ArrayList<modelgroup> mStringFilterList;

        public CustomAdaptergroupe(Context ctx, ArrayList<modelgroup> arraygroupe) {
            this.ctx = ctx;
            this.arraygroupe = arraygroupe;
            this.mStringFilterList = arraygroupe;
        }


        private ValueFilter valuefilter;

        @Override
        public int getCount() {
            return arraygroupe.size();
        }

        @Override
        public Object getItem(int position) {
            return arraygroupe.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(ctx).inflate(R.layout.rowcontact, parent, false);
            }
            CircleImageView imagegroupe = (CircleImageView) convertView.findViewById(R.id.profile_url);
            TextView txtgroupe = (TextView) convertView.findViewById(R.id.username);
            final int utilisateur_id = SharedPref.readSharedSettingint(ctx, "user_id", 0);
            final modelgroup groupmodel = (modelgroup) this.getItem(position);

           final int creator_groupe = groupmodel.getCreatorgroupe();
          final  int groupe_id = groupmodel.getGroupe_id();
          final  String Image_groupe = groupmodel.getImage_groupe();
          final  String name_groupe = groupmodel.getName_groupe();

            txtgroupe.setText(name_groupe);
            Glide.with(ctx).load(/*"https://chatappmek.000webhostapp.com/"+*/Image_groupe).into(imagegroupe);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openSendMessage(creator_groupe,groupe_id,Image_groupe,name_groupe);
                }
            });

            return convertView;
        }
          private void openSendMessage(int creator_groupe,int groupe_id,String Image_groupe,String name_groupe) {
            Intent intent = new Intent(ctx, GroupeMessages.class);
            intent.putExtra("creator_groupe", creator_groupe);
            intent.putExtra("groupe_id", groupe_id);
            intent.putExtra("image_groupe", Image_groupe);
            intent.putExtra("name_groupe", name_groupe);

            ctx.startActivity(intent);
        }

        @Override
        public Filter getFilter() {
            if (valuefilter == null) {

                valuefilter = new ValueFilter();
            }

            return valuefilter;
        }

        private class ValueFilter extends Filter {


            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint != null && constraint.length() > 0) {
                    ArrayList<modelgroup> filterList = new ArrayList<modelgroup>();
                    for (int i = 0; i < mStringFilterList.size(); i++) {
                        if ((mStringFilterList.get(i).getName_groupe().toUpperCase())
                                .contains(constraint.toString().toUpperCase())) {
                            modelgroup groupes = new modelgroup();
                            groupes.setName_groupe(mStringFilterList.get(i).getName_groupe());
                            groupes.setGroupe_id(mStringFilterList.get(i).getGroupe_id());
                            filterList.add(groupes);
                        }
                    }
                    results.count = filterList.size();
                    results.values = filterList;
                } else {
                    results.count = mStringFilterList.size();
                    results.values = mStringFilterList;
                }
                return results;
            }


            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                arraygroupe = (ArrayList<modelgroup>) results.values;
                notifyDataSetChanged();
            }

        }
    }
}
