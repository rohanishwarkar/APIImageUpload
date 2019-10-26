package com.example.singularium;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class api<Cursor> extends AppCompatActivity {
    public static final String URL = "http://e90cvm.southeastasia.cloudapp.azure.com:8000/";
    Button select;
    TextView text;
    String filePath;
    String filename1="";
    ImageView imageView;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api);

        select = (Button)findViewById(R.id.button);
        text = findViewById(R.id.text);
        imageView = findViewById(R.id.imageview);

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED||checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED||checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            return;
        }
    }
    private void selectImage() {
        try {
            PackageManager pm = getPackageManager();
            int hasPerm = pm.checkPermission(Manifest.permission.CAMERA, getPackageName());
            if (hasPerm == PackageManager.PERMISSION_GRANTED) {
                final CharSequence[] options = {"Take Photo", "Choose From Gallery","Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select Option");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Take Photo")) {
                            dialog.dismiss();
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, 12);
                        } else if (options[item].equals("Choose From Gallery")) {
                            dialog.dismiss();
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto, 11);
                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            } else {
                Toast.makeText(this, "Camera Permission error", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Camera Permission error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 100: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("Permission Granted");
                } else {
                    System.out.println("Permission not granted");
                }
                return;
            }
        }
    }

    public Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private String getRealPathFromURI(Uri contentUri) {
        android.database.Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = this.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("RequestCode: "+requestCode);
        if (requestCode == 11) {
            if (resultCode == RESULT_OK) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
                    imageView.setImageBitmap(bitmap);
                    uploadImage(data.getData());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            if(resultCode == RESULT_OK){
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(photo);
                Uri tempUri = getImageUri(photo);
                uploadImage(tempUri);
            }
        }
    }


    private void uploadImage(Uri imageBytes) {
        System.out.println(imageBytes.getPath());
        final ProgressDialog p = new ProgressDialog(api.this);
        p.setMessage("Fetching Data...Please Wait");
        p.show();
        File file;
        file = new File(getRealPathFromURI(imageBytes));
        Retrofit retrofit = new Retrofit.Builder().baseUrl(URL).addConverterFactory(GsonConverterFactory.create()).build();
        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        RequestBody name = RequestBody.create(MediaType.parse("multipart/form-data"), "file");
        RequestBody filename = RequestBody.create(MediaType.parse("multipart/form-data"),filename1);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file",file.getName(), requestFile);
        Call<ress> call = retrofitInterface.uploadImage(body,name,filename);
        call.enqueue(new Callback<ress>() {
            @Override
            public void onResponse(Call<ress> call, Response<ress> response) {
                p.cancel();
                if (response.isSuccessful()) {
                    try {
                        text.setText(response.body().getData());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    text.setText("Error Fetching Data");
                    ResponseBody errorBody = response.errorBody();
                    Gson gson = new Gson();
                    System.out.println(gson.toString());
                }
            }
            @Override
            public void onFailure(Call<ress> call, Throwable t) {
                p.cancel();
                System.out.println(t.getLocalizedMessage());
            }
        });
    }
}
