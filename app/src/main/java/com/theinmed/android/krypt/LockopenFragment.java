package com.theinmed.android.krypt;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
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
import java.io.PrintWriter;
import java.util.regex.Pattern;


public class LockopenFragment extends Fragment {

    private EditText ptDec,ctDec,kDec;
    private Button btnDec,btnFlDec,btnSave,btnClear;
    public static final int PERMISSIONS_REQUEST_CODE = 0;
    public static final int FILE_PICKER_REQUEST_CODE = 1;

    public LockopenFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lockopen, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        ptDec = getActivity().findViewById(R.id.plainTextDec);
        ctDec = getActivity().findViewById(R.id.cipherTextDec);
        kDec = getActivity().findViewById(R.id.keyDec);
        btnDec = getActivity().findViewById(R.id.btnDec);
        btnFlDec = getActivity().findViewById(R.id.btnFlDec);
        btnSave = getActivity().findViewById(R.id.btnSafeDec);
        btnClear = getActivity().findViewById(R.id.btnclear);
        btnDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Decrypt();
            }
        });
        btnFlDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionsAndOpenFilePicker();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                safeMessage();
            }
        });
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ptDec.setText("");
                ctDec.setText("");
                kDec.setText("");
            }
        });
    }

    private void Decrypt(){
        String plaintext,key;
        plaintext = ptDec.getText().toString();
        key = kDec.getText().toString();
        int anotherKey = 0;
        char kunci;
        String[] huruf = new String[128];
        for (int c = 0; c <= 127; c++) {
            huruf[c] = String.valueOf((char) c);
        }
        for(int k = 0; k<key.length(); k++){
            kunci = key.charAt(k);
            anotherKey += Character.getNumericValue(kunci);
        }
        String cipher = ctDec.getText().toString();
        loop1:
        for(int a=0; a< cipher.length(); a++){
            int index_cipher = -1;
            for(int b=0; b<huruf.length; b++){
                index_cipher = (String.valueOf(cipher.charAt(a)).equals(huruf[b])) ? b : -1;
                if(index_cipher != -1){
                    //plaintext += ((index_cipher - anotherKey) >= 0) ? huruf[(index_cipher - anotherKey)%128] : huruf[128 + (index_cipher - anotherKey)];
                    if((index_cipher - anotherKey) >= 0){
                        plaintext += huruf[(index_cipher - anotherKey)%128];
                    }else if((128 + index_cipher - anotherKey) < 0){
                        plaintext += huruf[128 + (index_cipher-anotherKey) + 128];
                    }else{
                        plaintext += huruf[128 + (index_cipher-anotherKey)];
                    }
                    continue loop1;
                }
            }
            plaintext += cipher.charAt(a);
        }
        ptDec.setText(plaintext);
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
                    Toast.makeText(getContext(),"error : "+e.getMessage().toString(),Toast.LENGTH_LONG).show();
                }
                ctDec.setText(text);
                Toast.makeText(getContext(),"Mengambil File : "+path, Toast.LENGTH_LONG).show();
            }
        }
    }
    private void safeMessage(){
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File (root.getAbsolutePath() + "/EncDecProject");
        dir.mkdirs();
        File file = new File(dir, "DecryptMessage.txt");
        try {
            String dec = ptDec.getText().toString();
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
