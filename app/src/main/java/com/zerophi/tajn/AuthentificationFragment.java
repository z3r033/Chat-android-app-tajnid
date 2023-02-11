package com.zerophi.tajn;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class AuthentificationFragment extends Fragment {
    MaterialButton btnlogin,btnsign;
    TextInputEditText usernametxt, passwordtxt;
    MaterialCheckBox mCheckBox;
      TextView oublier;

final static String url = UrlsGlobal.login;//"http://10.0.0.1/chatbackend/login.php";
  final static String urlrec = UrlsGlobal.recuperermotdepass;//"http://10.0.0.1/chatbackend/recuperermotdepass.php";
//final static String url = "http://chatappmeknes.eu5.net/login.php";
  //  final static String url = "https://chatappmek.000webhostapp.com/login.php";



    public AuthentificationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_authentification,container,false);

        usernametxt = (TextInputEditText) view.findViewById(R.id.edtemail);
        passwordtxt = (TextInputEditText) view.findViewById(R.id.edtpassword);
        btnlogin = (MaterialButton) view.findViewById(R.id.btnlogin);
        mCheckBox = (MaterialCheckBox) view.findViewById(R.id.checkboxadmin);
        mCheckBox.setChecked(true);
        btnsign = (MaterialButton) view.findViewById(R.id.signn_button);
     //   oublier = (TextView) view.findViewById(R.id.oubliermotpasf);
        btnsign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   startActivity(new Intent(getActivity().getApplicationContext(),sign.class));
            }
        });
         final int utilisateur_id = SharedPref.readSharedSettingint(getActivity().getApplicationContext(), "user_id", 0);

  /*      btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = usernametxt.getText().toString();
                String password = passwordtxt.getText().toString();

                if ((username.length() <= 0 || username == null) || (password.length() <= 0 || password == null)) {
                    Toast.makeText(getActivity().getApplicationContext(), "slv remplire tous les champs!!!", Toast.LENGTH_SHORT).show();
                } else {
                    new LoginAuth(getActivity().getApplicationContext(), url, usernametxt, passwordtxt,mCheckBox).execute();

                }

            }
        });
        */
  /*   oublier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            boolean noErrors = true;
          final  String s = usernametxt.getText().toString();

            if(s.isEmpty()){
                usernametxt.setError("remplire l'email pour recuperer password");
                noErrors = false;
            } else{
                usernametxt.setError(null);
            }

                 if(noErrors)

            {
                AlertDialog.Builder builder = new AlertDialog.Builder(DrawerChat1.this);
                builder.setMessage("tu est sure que vous voulez recuperer cet mot de pass?")
                        .setCancelable(false)
                        .setPositiveButton("oui", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                AndroidNetworking.post(urlrec)
                                        .setPriority(Priority.HIGH)
                                        .addBodyParameter("email",s)
                                        .build().getAsString(new StringRequestListener() {
                                    @Override
                                    public void onResponse(String response) {
                                        Toast.makeText(getActivity().getApplicationContext(), response, Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onError(ANError anError) {
                                        Toast.makeText(getActivity().getApplicationContext(), "error", Toast.LENGTH_LONG).show();
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
        });*/

  btnlogin.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

          String username = usernametxt.getText().toString();
          String password = passwordtxt.getText().toString();

          if ((username.length() <= 0 || username == null) || (password.length() <= 0 || password == null)) {
              Toast.makeText(getActivity().getApplicationContext(), "slv remplire tous les champs!!!", Toast.LENGTH_LONG).show();
          } else {
              new LoginAuth(getActivity().getApplicationContext(), url, usernametxt, passwordtxt,mCheckBox).execute();

          }



   /*    String  clientId = MqttClient.generateClientId();
         final MqttAndroidClient client =
                  new MqttAndroidClient(getActivity().getApplicationContext(), "tcp://10.0.0.1:1883",
                          clientId);
          try {
              IMqttToken token = client.connect();
              token.setActionCallback(new IMqttActionListener() {
                  @Override
                  public void onSuccess(IMqttToken asyncActionToken) {
                      // We are connected
                      Toast.makeText(getActivity().getApplicationContext(),"success connection",Toast.LENGTH_LONG).show();
                      String topic = "noc/"+utilisateur_id;
                      int qos = 1;
                      try {
                          IMqttToken subToken = client.subscribe(topic, qos);
                          subToken.setActionCallback(new IMqttActionListener() {
                              @Override
                              public void onSuccess(IMqttToken asyncActionToken) {
                                  // The message was published
                                  Toast.makeText(getActivity().getApplicationContext(),"success pub",Toast.LENGTH_LONG).show();
                                  client.setCallback(new MqttCallback() {
                                      @Override
                                      public void connectionLost(Throwable throwable) {

                                      }

                                      @Override
                                      public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                                          Toast.makeText(getActivity().getApplicationContext(),new String(mqttMessage.getPayload()),Toast.LENGTH_LONG).show();
                                          generateNotification(getActivity().getApplicationContext(), new String(mqttMessage.getPayload()));
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
                      Toast.makeText(getActivity().getApplicationContext(),"failed connection",Toast.LENGTH_LONG).show();

                  }
              });
          } catch (MqttException e) {
              e.printStackTrace();
          }*/
      }


  });


        return view;
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


}
