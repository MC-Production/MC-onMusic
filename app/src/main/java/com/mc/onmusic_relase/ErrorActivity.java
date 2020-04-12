package com.mc.onmusic_relase;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.mc.onmusic_relase.utils.YTutils;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;

public class ErrorActivity extends AppCompatActivity {

    CheckBox sendLogs;
    RelativeLayout crashLayout;
    EditText editText;
    String crashDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        setTitle("Send Report");

        sendLogs = findViewById(R.id.sendLogs);
        crashLayout = findViewById(R.id.crashLayout);
        editText = findViewById(R.id.reportText);

        crashDetails =  CustomActivityOnCrash.getStackTraceFromIntent(getIntent());

        crashLayout.setOnClickListener(v -> {
            ScrollView nestedScrollView = new ScrollView(this);
            TextView textView = new TextView(ErrorActivity.this);
            textView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setPadding(20,20,20,20);
            textView.setTextSize(13);
            textView.setText(crashDetails);
            nestedScrollView.addView(textView);
            final AlertDialog.Builder alert= new AlertDialog.Builder(ErrorActivity.this);
            alert.setTitle("Stack Trace");
            alert.setView(nestedScrollView);
            alert.setPositiveButton("OK", null);
            alert.show();
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Toast.makeText(this, "Application crashed due an error!", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.error_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();
        switch (itemID) {
            case R.id.action_send:
                String message="";
                if (!editText.getText().toString().isEmpty()) {
                    message = editText.getText().toString()+"\n\n";
                }
                message += "------ Device Info ------\nModel: "+ Build.MODEL+"\nProduct: "+ Build.PRODUCT+"\nHardware: "+Build.HARDWARE+"\n" +
                        "Android SDK: "+Build.VERSION.SDK_INT+"\n\n";
                if (sendLogs.isChecked())
                message += "------ StackTrace ------\n"+"<pre>"+crashDetails+"</pre>";
                try {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                    emailIntent.setType("plain/text");
                    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,new String[] { "developerkp16@gmail.com" });
                    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"YTPlayer v"+ pInfo.versionName  +" Crash "+YTutils.getTodayDate());
                    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
                    startActivity(emailIntent);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
               break;
        }
        return super.onOptionsItemSelected(item);
    }
}
