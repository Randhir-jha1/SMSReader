package com.sips.smsreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    private static MainActivity inst;
    ArrayList<String> smsMessagesList = new ArrayList<String>();
    ListView smsListView;
    ArrayAdapter arrayAdapter;
    String usermarktime,userid,userlat,userlong,userimei ;
    LinearLayout MainLayout;

    public static MainActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        smsListView = (ListView) findViewById(R.id.SMSList);
        MainLayout = findViewById(R.id.MainLayout);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, smsMessagesList);
        smsListView.setAdapter(arrayAdapter);
        smsListView.setOnItemClickListener(this);

        refreshSmsInbox();
    }

    public void refreshSmsInbox() {
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        int indextime = smsInboxCursor.getColumnIndex("date");

        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        arrayAdapter.clear();
        do {

            Long timestamp = Long.parseLong(smsInboxCursor.getString(indextime));
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            Date finaldate = calendar.getTime();
            String smsDate = finaldate.toString();
            String str = "SMS From: " + smsInboxCursor.getString(indexAddress) +"\n" +"AT: " + smsDate+
            "\n" + smsInboxCursor.getString(indexBody) + "\n";
            arrayAdapter.add(str);
        } while (smsInboxCursor.moveToNext());
    }

    public void updateList(final String smsMessage) {
        arrayAdapter.insert(smsMessage, 0);
        arrayAdapter.notifyDataSetChanged();
    }

    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        try {
            String[] smsMessages = smsMessagesList.get(pos).split("\n");
            String address = smsMessages[0];
            String mtextbody = smsMessages[2];
            Log.i("RRRRR84",mtextbody);

            final String secretKey = "ssshhhhhhhhhhh!!!!";

            String decryptedString = AES.decrypt(mtextbody, secretKey);
            if (!Objects.requireNonNull(decryptedString).isEmpty()){
                Log.i("RRRRR8455", Objects.requireNonNull(decryptedString));
                String smsMessage = "";
                for (int i = 1; i < smsMessages.length; ++i) {
                    smsMessage += smsMessages[i];
                }


                String[] convertedmessage = decryptedString.split("/");

                String usersuffix  = convertedmessage[0];
                if(usersuffix.matches("SIPSMS")){
                     usermarktime  = convertedmessage[1];
                     userid = convertedmessage[2];
                     userlat = convertedmessage[3];
                     userlong = convertedmessage[4];
                     userimei = convertedmessage[5];
                    Log.i("RRRRR844",usermarktime+" "+userid+" "+userlat+" "+userlong+" "+userlong+" "+userimei);

                    showSettingsAlert();
//                    String smsMessageStr = address + "\n";
//                    smsMessageStr += smsMessage;
//                    Toast.makeText(this, smsMessageStr, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(this, "THIS IS NOT VALID ATTENDANCE SMS", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(this, "THIS IS NOT VALID ATTENDANCE SMS AGAIN", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("Upload Attendance");

        // Setting Dialog Message
        alertDialog.setMessage("Do you want to mark attendance of "+userid+"?");

        // On pressing Settings button
        alertDialog.setPositiveButton("UPLOAD", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {

                addattendance();
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private void addattendance() {

        Log.i("Responseee1", "HHHHHHHHH");
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = BaseUrlActivity.urlmain+"addAttendanceSMS";

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        Log.i("Responseee", response);


                        try {

                            JSONObject obj = new JSONObject(response);
                            Log.e("REsponce", obj.toString());

                            String success = obj.getString("ResponseCode");
                            String respmessage = obj.getString("ResponseMessage");
                            Log.i("Resp1", success);
                            if (success.equals("200")) {


                                Snackbar snackbar1 = Snackbar.make(MainLayout, respmessage, Snackbar.LENGTH_LONG);
                                snackbar1.show();


                            } else {





                                Snackbar snackbar1 = Snackbar.make(MainLayout, respmessage, Snackbar.LENGTH_LONG);
                                snackbar1.show();

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("attendance_mode_id","2");
                params.put("punchdate_time", usermarktime);
                params.put("imeino",userimei);

                params.put("lat",userlat);
                params.put("long",userlong);
                params.put("users_id",userid);
                params.put("remarks" ,"FROM SMS AGENT");
                return params;
            }
        };
        queue.add(postRequest);
    }


}


