package com.zhenye.graduationproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class a1_setMessage extends AppCompatActivity {
    public String recMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a1_set_message);
        final EditText  setMessage = findViewById(R.id.editText2_setMessage);
        Button setButton = findViewById(R.id.button2_setMessage);
        final TextView showMessage = findViewById(R.id.showMessage);
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               showMessage.setText( showMessage.getText()+ setMessage.getText().toString());

            }
        });
    }
}
