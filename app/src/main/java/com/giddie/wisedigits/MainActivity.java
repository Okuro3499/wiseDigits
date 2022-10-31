package com.giddie.wisedigits;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.giddie.wisedigits.databinding.ActivityMainBinding;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        clickListeners();
    }

    private void clickListeners() {
        binding.picker.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 0);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        });

        binding.submit.setOnClickListener(v -> {
            if (TextUtils.isEmpty(binding.etFirstName.getText().toString().trim())) {
                binding.etFirstName.setError("Kindly enter your name!");
            } else if (TextUtils.isEmpty(binding.etEmail.getText().toString().trim())) {
                binding.etEmail.setError("Kindly enter email!");
            } else if(!binding.etEmail.getText().toString().trim().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                binding.etEmail.setError("Kindly a valid email!");
            } else if (TextUtils.isEmpty(binding.etPin.getText().toString().trim())) {
                binding.etPin.setError("Kindly enter password");
            } else if (binding.etPin.getText().length() < 6) {
                binding.etPin.setError("Kindly enter input 6 characters");
            } else if (TextUtils.isEmpty(binding.etConfirmPin.getText().toString().trim())) {
                binding.etConfirmPin.setError("Kindly confirm password!");
            } else if (!binding.etConfirmPin.getText().toString().trim().equals(binding.etPin.getText().toString().trim())) {
                binding.etConfirmPin.setError("Passwords do not match!");
            } else {
                createUser(
                        binding.etFirstName.getText().toString().trim(),
                        binding.etEmail.getText().toString().trim(),
                        binding.etPin.getText().toString().trim()
                );
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            Context context = MainActivity.this;
            path = RealPathUtil.getRealPath(context, uri);
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            binding.imageView.setImageBitmap(bitmap);
        }
    }

    public void createUser(String name, String email, String password) {
        // display a progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCancelable(false); // set cancelable to false
        progressDialog.setMessage("Creating User..."); // set message
        progressDialog.show(); // show progress dialog
        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://62.171.164.83/").addConverterFactory(GsonConverterFactory.create()).build();

        File file = new File(path);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

        MultipartBody.Part body = MultipartBody.Part.createFormData("avatar", file.getName(), requestFile);

        RequestBody names = RequestBody.create(MediaType.parse("multipart/form-data"), name);
        RequestBody emails = RequestBody.create(MediaType.parse("multipart/form-data"), email);
        RequestBody passwords = RequestBody.create(MediaType.parse("multipart/form-data"), password);

        WiseDigitsApiService wiseDigitApiService = retrofit.create(WiseDigitsApiService.class);
        Call<ResponseModel> call = wiseDigitApiService.createUser(body, names, emails, passwords);
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                if (response.body().getStatus().equals("200")) {
                    progressDialog.dismiss();
                    Dialog dialog = new Dialog(MainActivity.this);
                    dialog.setContentView(R.layout.success_dialog);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    Button ok = dialog.findViewById(R.id.ok);
                    TextView message = dialog.findViewById(R.id.message);

                    message.setText(response.body().getMessage());

                    ok.setOnClickListener(v -> dialog.dismiss());

                    dialog.setCancelable(true);
                    dialog.show();
                    Toast.makeText(MainActivity.this, "Successfully Added", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "failed to add", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}