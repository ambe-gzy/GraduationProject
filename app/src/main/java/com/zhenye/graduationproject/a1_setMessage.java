package com.zhenye.graduationproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.sql.SQLException;
import java.util.List;

public class a1_setMessage extends AppCompatActivity {
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
                //增
              // showMessage.setText( showMessage.getText()+ );
                try {
                    Message_litepal message_litepal_set = new Message_litepal();
                    message_litepal_set.setMessage(setMessage.getText().toString());
                    message_litepal_set.save();//存数据
                    Message_litepal message_litepal_last = DataSupport.findLast(Message_litepal.class);

                        showMessage.setText( message_litepal_last.getMessage());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

}
