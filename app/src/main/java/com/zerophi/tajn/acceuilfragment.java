package com.zerophi.tajn;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;


public class acceuilfragment extends Fragment {
 TextView emailtxt , usernametxt ;
 CircleImageView image;
 MaterialButton btnsetting, btncontact, btngroupe;

    public acceuilfragment() {

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
                 String email = SharedPref.readSharedSetting(getActivity().getApplicationContext(),"email","");
                 String profile_url = SharedPref.readSharedSetting(getActivity().getApplicationContext(),"profile_url","");
                 String username = SharedPref.readSharedSetting(getActivity().getApplicationContext(),"username","");

        View view = inflater.inflate(R.layout.fragment_acceuilfragment, container, false);
        usernametxt =view.findViewById(R.id.utilisateur_acceuil);
        emailtxt = view.findViewById(R.id.emailacceuil);
        btncontact = view.findViewById(R.id.contactacceuil);
        btnsetting =view.findViewById(R.id.btnsettingacceuil);
        btngroupe = view.findViewById(R.id.btngroupeacceuil);

        image = view.findViewById(R.id.imageacceuil);
        usernametxt.setText(username);
        emailtxt.setText(email);

        btnsetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity().getApplicationContext(),setting.class));
            }
        });
        btngroupe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        btncontact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity().getApplicationContext(),Contact.class));
            }
        });
         Glide.with(getActivity().getApplicationContext()).load(profile_url).into(image);
        return view;
    }


}
