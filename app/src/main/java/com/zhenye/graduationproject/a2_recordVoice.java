package com.zhenye.graduationproject;



import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.io.UnsupportedEncodingException;

public class a2_recordVoice extends AppCompatActivity {
    /*
    *录音相关变量
     */
    private static final String TAG = "a2_recordVoice";
    private AudioRecord audioRecord = null;
    private boolean isRecording;
    private int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private int sampleRate = 16000;//设置采样频率
    private int bufferSizeInBytes;
    /*
    权限获取
     */
    private static final int REQUEST_STORAGE_PERMISSION = 101;
    String[] permissions1 = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    String [] permissions2 = {Manifest.permission.RECORD_AUDIO};
    /*
    *腾讯云api参数配置
     */
    //用户需修改为自己的SecretId，SecretKey
    String SecretId = "AKIDMbgeS5lotx3yhF5rZDgJl1AE9tTtE91i";
    String SecretKey = "d6NadV5au480my2vhKmIh0C8yd6QH1gG";
    // 识别引擎 8k or 16k
    String EngSerViceType = "16k";
    // 语音数据来源 0:语音url or 1:语音数据bodydata(data数据大小要小于800k)
    String SourceType = "1";
    //音频格式 wav，mp3
    String VoiceFormat = "wav";
    //录音文件名+目录
    private static String fileName = null;
    /*
    *UI相关变量
     */
    private  TextView receive;
    private String OriginalMessage; //获取识别到的语音数据
    private String participleMessage;//获取分词后的语音数据

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a2_record_voice);
        RecorderInit();        //初始化AudioRecorder
        Button luyin = (Button)findViewById(R.id.luyin);
        Button luyin_stop = (Button)findViewById(R.id.luyin_stop);
        Button shibie =(Button)findViewById(R.id.recognize_voice);
        Button fenci =(Button)findViewById(R.id.participle_Button);
        receive = (TextView)findViewById(R.id.textView);
        luyin.setOnClickListener(new View.OnClickListener() {//开始录音
            @Override
            public void onClick(View v) {
                // 请求权限
                ActivityCompat.requestPermissions(a2_recordVoice.this,permissions1,REQUEST_STORAGE_PERMISSION);
                ActivityCompat.requestPermissions(a2_recordVoice.this,permissions2,REQUEST_RECORD_AUDIO_PERMISSION);
                receive.setText("Recoding...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        a2_recordVoice.this.run();
                    }
                }).start();
            }
        });
        luyin_stop.setOnClickListener(new View.OnClickListener() {//停止录音并识别
            @Override
            public void onClick(View v) {
                stopRecording();
                //while (!isRecording);
            }
        });
        shibie.setOnClickListener(new View.OnClickListener() {//显示出来
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(a2_recordVoice.this,a1_setMessage.class);
                startActivity(intent);


            }
        });
        fenci.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                participle();
            }
        });
    }

    public void RecorderInit() {//配置AudioRecord
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRate,
                channelConfiguration, audioEncoding); // need to be larger than size of a frame
        Log.i(TAG, "bufferSizeInBytes=" + bufferSizeInBytes);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,//配置AudioRecord
                sampleRate, channelConfiguration, audioEncoding,
                bufferSizeInBytes); //麦克风
    }

    public AudioRecord getAudioRecord() {
        return audioRecord;
    }

    public void startRecording() {
        try {
            audioRecord.startRecording();
            isRecording = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        try {
            audioRecord.stop();
            isRecording = false;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    *1.录音
    * 2.调用sendVoice发送到腾讯云
     */
    public void run() {

        File dir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), "ivr_record");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //  File recordingFile = new File(dir, "ivr_" + System.currentTimeMillis()
        //        + ".wav");
        File recordingFile = new File(dir, "ivr_test1.wav"
        );
        // Log.i(TAG, "start recording,file=" + recordingFile.getAbsolutePath());
        fileName = recordingFile.getAbsolutePath();
        OutputStream out = null;
        ByteArrayOutputStream baos = null;

        try {
            baos = new ByteArrayOutputStream();

            startRecording();

            byte[] buffer = new byte[bufferSizeInBytes];

            int bufferReadResult = 0;

            while (isRecording) {

                bufferReadResult = audioRecord.read(buffer, 0,
                        bufferSizeInBytes);
                if(bufferReadResult>0){
                    baos.write(buffer, 0, bufferReadResult);
                }
            }
            isRecording = true;

          //  Log.i(TAG, "stop recording,file=" + recordingFile.getAbsolutePath());

            buffer = baos.toByteArray();

          //  Log.i(TAG, "audio byte len="+buffer.length);

            out = new FileOutputStream(recordingFile);
            out.write(getWavHeader(buffer.length));
            out.write(buffer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(baos!=null){
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //在此处发信息到腾讯云
        sendVoice();//开一个线程调用腾讯云API
    }

    public byte[] getWavHeader(long totalAudioLen){
        int mChannels = 1;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = sampleRate;
        long byteRate = sampleRate * 2 * mChannels;

        byte[] header = new byte[44];
        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) mChannels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * mChannels);  // block align
        header[33] = 0;
        header[34] = 16;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        return header;
    }

    @Override//请求权限回调函数
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                break;
            case REQUEST_STORAGE_PERMISSION:
                break;
            default:
                break;
        }
    }

    /*
    *发送到腾讯云
    * 获取返回的数据
    * 开启震动
     */
    private void  sendVoice(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //调用setConfig函数设置相关参数
                int res = SASRsdk.setConfig(SecretId, SecretKey, EngSerViceType, SourceType, VoiceFormat, fileName);
                if (res < 0) {
                    return;
                }
                try {
                    SASRsdk.sendVoice();//要放在子线程中运行
                    OriginalMessage = SASRsdk.TAG1;
                    new RenewUITask().execute(OriginalMessage);//更新UI
                    myVibrator("666");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /*
    * 取出汉字
    * OriginalMessage:待发送的汉字
    * 发送到安智自然语言处理进行分词
    * 将接收到的分词信息进行统计
     */
    private void participle(){
        OriginalMessage = SASRsdk.TAG1.substring(23);
        int len = OriginalMessage.length();//获取字符串总长度，不让for循环太久
        for(int i =0;i!=len;i++) {//真，执行，假，不执行
            if (OriginalMessage.charAt(i) == '。') {
                len = i;
                OriginalMessage = OriginalMessage.substring(0, len);//获得待发送的message
                break;
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
              //  int res = WENZHIsdk.setConfig("AKIDz8krbsJ5yKBZQpn74WFkmLPx3EXAMPLE","Gu5t9xGARNpq86cd98joQYCN3EXAMPLE",OriginalMessage,2097152);
                int res = WENZHIsdk.setConfig(SecretId,SecretKey,OriginalMessage,2097152);
                if (res < 0) {
                    return;
                }
                try {
                    WENZHIsdk.sendMessage();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
            participleMessage = WENZHIsdk.TAG1;//获取分词个数，并提取出来

    }

    /*
    *AsysncTask,更新UI
     */
    public class RenewUITask extends AsyncTask<String,Integer,String> {
            //参数1:传递进来的数据，参数2：传递到onProgressUpdate的数据，参数3：返回的数据
        @Override
        protected void onPreExecute(){//该类被调用前执行，用于界面的初始化

        }
        @Override
        protected String doInBackground(String ... params){//此方法所有代码均在子线程中执行
          //params是一个字符串数组
            Log.d("aaa",params[0]);
            return params[0];
        }

        @Override
        protected void onPostExecute(String result){//当doInbackground完成时，返回return时，执行此方法，可以利用返回的数据进行一些UI操作

            //可以在此代码更改UI
            result =result.substring(23);
            int len = result.length();//获取字符串总长度，不让for循环太久
            for(int i =0;i!=len;i++) {//真，执行，假，不执行
                if (result.charAt(i) == '。') {
                    len = i;
                    result = result.substring(0, len);
                    receive.setText(result);
                    break;
                }

            }

        }

    }

    /*
    *进行震动操作
     */
    public void myVibrator(String message){
        Vibrator vibrator = (Vibrator)this.getSystemService(this.VIBRATOR_SERVICE);
        vibrator.vibrate(1000);
    }

    /*
     * 中文转unicode编码
     */
    public static String gbEncoding(final String gbString) {
        char[] utfBytes = gbString.toCharArray();
        String unicodeBytes = "";
        for (int i = 0; i < utfBytes.length; i++) {
            String hexB = Integer.toHexString(utfBytes[i]);
            if (hexB.length() <= 2) {
                hexB = "00" + hexB;
            }
            unicodeBytes = unicodeBytes + "\\u" + hexB;
        }
        return unicodeBytes;
    }
    /*
     * unicode编码转中文
     */
    public static String decodeUnicode(final String dataStr) {
        int start = 0;
        int end = 0;
        final StringBuffer buffer = new StringBuffer();
        while (start > -1) {
            end = dataStr.indexOf("\\u", start + 2);
            String charStr = "";
            if (end == -1) {
                charStr = dataStr.substring(start + 2, dataStr.length());
            } else {
                charStr = dataStr.substring(start + 2, end);
            }
            char letter = (char) Integer.parseInt(charStr, 16); // 16进制parse整形字符串。
            buffer.append(new Character(letter).toString());
            start = end;
        }
        return buffer.toString();
    }

}
