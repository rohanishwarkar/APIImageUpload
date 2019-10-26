package com.example.singularium;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    Button login;
    String TAG="TAGGGGG";
    EditText username,password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog p = new ProgressDialog(MainActivity.this);
                p.setMessage("Checking your credentials...Please Wait");
                p.show();
                final String uname = username.getText().toString();
                final String pass = password.getText().toString();
                System.out.println(uname+" "+pass);
                if(uname.equals("test")&&pass.equals("test")) {
                    p.cancel();
                    startActivity(new Intent(MainActivity.this, api.class));
                    finish();
                }

            }
        });
    }
}
