package com.mc.onmusic_relase.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.mc.onmusic_relase.MainActivity;
import com.mc.onmusic_relase.R;
import com.mc.onmusic_relase.utils.YTutils;

import java.io.File;
import java.util.ArrayList;

public class SettingsFragment extends PreferenceFragment {

    Activity activity; private FirebaseAuth mAuth;
    GoogleApiClient mGoogleSignInClient; Preference account;
    private static final String TAG = "SettingsFragment";
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = getActivity();

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getResources().getString(R.string.default_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = new GoogleApiClient.Builder(activity)
                .enableAutoManage(MainActivity.activity,connectionResult -> {

                }).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        getPreferenceManager().setSharedPreferencesName("appSettings");
        addPreferencesFromResource(R.xml.settings);



        Preference clear = findPreference("pref_delete");
        clear.setOnPreferenceClickListener(preference -> {
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
                if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            300);
                    return false;
                } else removeBackups();
            }else removeBackups();
            return false;
        });



        Preference backup = findPreference("pref_backup");
        backup.setOnPreferenceClickListener(preference -> {
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
                if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            100);
                    return false;
                } else backupData();
            }else backupData();
            return false;
        });

        Preference restore = findPreference("pref_restore");
        restore.setOnPreferenceClickListener(preference -> {
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
                if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            200);
                    return false;
                } else restoreData();
            }else restoreData();
            return false;
        });
    }

    @Override
    public void onDestroy() {
        mGoogleSignInClient.stopAutoManage(MainActivity.activity);
        mGoogleSignInClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==103) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(activity, "Failed to sign in, Error: "+e.getStatusCode(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d("firebaseAuth", "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        account.setSummary(user.getUid());

                    } else {
                        Log.e("GoogleSignFailed",task.getException().getMessage()+"");
                        Toast.makeText(activity,"Sign in failed!",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    void removeBackups() {
        File files = YTutils.getFile("MC/backups"); files.mkdirs();
        File[] allfiles = files.listFiles();
        if (allfiles.length>0) {
            for (File f : allfiles) f.delete();
            Toast.makeText(getActivity(), "All backups are removed!", Toast.LENGTH_LONG).show();
        } else Toast.makeText(getActivity(), "No backups were found!", Toast.LENGTH_LONG).show();
    }

    void restoreData() {
        File file = YTutils.getFile("MC/backups");
        file.mkdirs();
        File[] allfiles = file.listFiles();
        if (allfiles.length>0) {
            ArrayList<File> fileList = new ArrayList<>();
            ArrayList<String> fileName = new ArrayList<>();
            for(File f : allfiles) {
                fileList.add(f);
                fileName.add(f.getName());
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Select session");
            builder.setItems(fileName.toArray(new String[fileName.size()]),(dialog, which) -> {

                File files = getActivity().getFilesDir();
                File[] tmpFiles = files.listFiles();
                if (tmpFiles.length>0) {
                    for (File f : tmpFiles) f.delete();
                }

                File torestore = fileList.get(which);
                Log.e("FileToExtract", torestore.toString());
                YTutils.extractZip(torestore.toString(),getActivity().getFilesDir().getParent());

                Intent intent = new Intent(getActivity(),MainActivity.class);
                ProcessPhoenix.triggerRebirth(getActivity(),intent);
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else Toast.makeText(getActivity(), "No backups were found!", Toast.LENGTH_LONG).show();
    }

    @SuppressLint("StaticFieldLeak")
    void backupData() {
        new AsyncTask<Void,Void,Void>() {
            ProgressDialog progressDialog=new ProgressDialog(getActivity());
            File location; String postFix;
            @Override
            protected void onPreExecute() {
                progressDialog.setMessage("Backing up...");
                progressDialog.show();
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                postFix = YTutils.getTodayDate_Time();
                SharedPreferences preferences = getActivity().getSharedPreferences("history",
                        Context.MODE_PRIVATE);
                String urls = preferences.getString("urls","");
                if (!urls.isEmpty()) {
                    YTutils.writeContent(getActivity(),"History",urls);
                }
                YTutils.getFile("MC/backups").mkdirs();
                location = YTutils.getFile("MC/backups/backup-"+postFix+".zip");
                YTutils.zipFileAtPath(getActivity().getFilesDir().toString(),
                        location.toString());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                progressDialog.dismiss();
                if (location!=null && location.exists())
                    Toast.makeText(getActivity(), "Created local backup-"+postFix+".zip", Toast.LENGTH_LONG).show();
                else {
                    Toast.makeText(getActivity(), "Failed creating a local backup", Toast.LENGTH_LONG).show();
                    Log.e("FileName_To_Save",location.toString());
                }
                super.onPostExecute(aVoid);
            }
        }.execute();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 100: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    backupData();
                } else {
                    Toast.makeText(getActivity(), "Permission denied!",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case 200:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    restoreData();
                } else {
                    Toast.makeText(getActivity(), "Permission denied!",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            case 300:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    removeBackups();
                } else {
                    Toast.makeText(getActivity(), "Permission denied!",
                            Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }
}
