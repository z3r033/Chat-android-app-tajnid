package com.zerophi.tajn;

import com.zerophi.tajn.models.modelutilisateur;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class format_de_url {
    modelutilisateur mUser;
    private String mFormBody;
    public format_de_url(modelutilisateur user){
        this.mUser= user;
    }


    public String packLoginData(){

        List<NameValuePair> formData =new ArrayList<NameValuePair>();
        formData.add(new BasicNameValuePair("email",mUser.getEmail()));
        formData.add(new BasicNameValuePair("password",mUser.getPassword()));

        if(formData == null){
            mFormBody =null;

        }
        StringBuilder sb = new StringBuilder();
        for(int i =0; i<formData.size();i++) {
            NameValuePair item = formData.get(i);

            sb.append(URLEncoder.encode(item.getName()));
            sb.append("=");
            sb.append(URLEncoder.encode(item.getValue()));
            if (i != (formData.size() - 1)) {
                sb.append("&");
            }
        }
        mFormBody=sb.toString();

        return mFormBody;
    }
}
