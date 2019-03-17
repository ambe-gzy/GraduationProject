package com.zhenye.graduationproject;



import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class a2_recordVoice extends AppCompatActivity {
    /*
    *录音相关变量
     */
    private static final String TAG = "sound";
    private AudioRecord audioRecord = null;
    private boolean isRecording;
    private int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private int sampleRate = 16000;//设置采样频率
    private int bufferSizeInBytes;
    private static String fileName = null; //录音文件名+目录
    private MediaPlayer player = null; //是否播放录音
    boolean mStartPlaying = true;    //是否播放录音
    /*
    权限获取
     */
    private static final int REQUEST_STORAGE_PERMISSION = 101;
    String[] permissions1 = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    String [] permissions2 = {Manifest.permission.RECORD_AUDIO};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a2_record_voice);
        RecorderInit();        //初始化AudioRecorder
        Button luyin = (Button)findViewById(R.id.luyin);
        Button luyin_stop = (Button)findViewById(R.id.luyin_stop);
        luyin.setOnClickListener(new View.OnClickListener() {//开始录音


            @Override
            public void onClick(View v) {
                /*
                请求权限
                 */
                ActivityCompat.requestPermissions(a2_recordVoice.this,permissions1,REQUEST_STORAGE_PERMISSION);
                ActivityCompat.requestPermissions(a2_recordVoice.this,permissions2,REQUEST_RECORD_AUDIO_PERMISSION);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        a2_recordVoice.this.run();
                    }
                }).start();
            }
        });
        luyin_stop.setOnClickListener(new View.OnClickListener() {//停止录音
            @Override
            public void onClick(View v) {
                stopRecording();
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
        Log.i(TAG, "start recording,file=" + recordingFile.getAbsolutePath());
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

            Log.i(TAG, "stop recording,file=" + recordingFile.getAbsolutePath());

            buffer = baos.toByteArray();

            Log.i(TAG, "audio byte len="+buffer.length);

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
}
