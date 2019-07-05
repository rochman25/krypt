package com.theinmed.android.krypt;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.regex.Pattern;


public class LockFragment extends Fragment {

    private EditText ptEnc,cEnc,kEnc;
    private Button btnEnc,btnFl,btnSave,btnClear;
    public static final int PERMISSIONS_REQUEST_CODE = 0;
    public static final int FILE_PICKER_REQUEST_CODE = 1;

    public LockFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_lock, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        ptEnc = getActivity().findViewById(R.id.plainTextEnc);
        cEnc = getActivity().findViewById(R.id.cipherTextEnc);
        btnEnc = getActivity().findViewById(R.id.btnEnc);
        btnSave = getActivity().findViewById(R.id.btnSafeEnc);
        kEnc = getActivity().findViewById(R.id.keyEnc);
        btnClear = getActivity().findViewById(R.id.btnclear);
        btnFl = getActivity().findViewById(R.id.btnFlEnc);
        btnEnc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Encrypt();
                //safeDecrypt();
            }
        });
        btnFl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionsAndOpenFilePicker();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                safeDecrypt();
            }
        });
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cEnc.setText("");
                ptEnc.setText("");
                kEnc.setText("");
            }
        });
    }

    private void Encrypt() {
        String plaintext,key;
        plaintext = ptEnc.getText().toString();
        key = kEnc.getText().toString();
        String[] huruf = new String[128];
        int anotherKey = 0;
        char kunci;
        for (int c = 0; c <= 127; c++) {
            huruf[c] = String.valueOf((char) c);
        }
        for(int k = 0; k<key.length(); k++){
            kunci = key.charAt(k);
            anotherKey += Character.getNumericValue(kunci);
        }
        String cipher = "";
        loop1:
        for(int a=0; a<plaintext.length(); a++){
            int index_plain;
            for(int b=0; b<huruf.length; b++){
                //index_plain = (String.valueOf(plaintext.charAt(a)).equals(huruf[b]))? b : -1;
                if((String.valueOf(plaintext.charAt(a)).equals(huruf[b]))){
                    index_plain = b;
                }else{
                    index_plain = -1;
                }
                if(index_plain != -1){
                    cipher += huruf[(index_plain + anotherKey) % 128];
                    continue loop1;
                }
            }
            cipher += plaintext.charAt(a);
        }
        cEnc.setText(cipher);
    }

    private void checkPermissionsAndOpenFilePicker() {
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission)) {
                showError();
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, PERMISSIONS_REQUEST_CODE);
            }
        } else {
            openFilePicker();
        }
    }

    private void showError() {
        Toast.makeText(getContext(), "Allow external storage reading", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openFilePicker();
                } else {
                    showError();
                }
            }
        }
    }

    private void openFilePicker() {
        new MaterialFilePicker()
                .withSupportFragment(this)
                .withRequestCode(FILE_PICKER_REQUEST_CODE)
                .withFilter(Pattern.compile(".*\\.txt$"))
                .withHiddenFiles(true)
                .start();
    }

    @Override
    public void onActivityResult(int RequestCode,int ResultCode,Intent Data){
        super.onActivityResult(RequestCode,ResultCode,Data);
        if(RequestCode == FILE_PICKER_REQUEST_CODE && ResultCode == Activity.RESULT_OK){
            String path = Data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            if(path != null){
                File file = new File(path);
                StringBuilder text = new StringBuilder();

                try {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;

                    while ((line = br.readLine()) != null) {
                        text.append(line);
                        text.append('\n');
                    }
                    br.close();

                }
                catch (IOException e) {
                    //You'll need to add proper error handling here
                }
                ptEnc.setText(text.toString());
                Toast.makeText(getContext(),"Mengambil File : "+path, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void safeDecrypt(){
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File (root.getAbsolutePath() + "/EncDecProject");
        dir.mkdirs();
        File file = new File(dir, "EncryptMessage.txt");
        try {
            String dec = cEnc.getText().toString();
            FileOutputStream fout = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(fout);
            pw.write(dec);
            pw.flush();
            pw.close();
            fout.close();
            Toast.makeText(getContext(),"berhasil save file ke : "+dir,Toast.LENGTH_LONG).show();
        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(getContext(),"error "+e.getMessage().toString(),Toast.LENGTH_LONG).show();
        }
    }

}
