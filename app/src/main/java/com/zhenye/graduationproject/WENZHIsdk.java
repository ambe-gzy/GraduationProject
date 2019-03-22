package com.zhenye.graduationproject;


import android.util.Log;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.net.URLEncoder;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStreamReader;


public class WENZHIsdk {
    private static String   Text, SecretId, SecretKey;
    private static int Code;
    final static String TAG = "WENZHIsdk";
    static String TAG1 ="----------------------------------------no message------------------------------------------------------------------";//返回录音结果
    static String TAG2 ="1";

    /*
     *将地址和参数整合起来。2019.3.14
     */
    public static String formSignstr(String serverUrl, Map<String, String> mapReq) {
        StringBuilder strBuilder = new StringBuilder(serverUrl);
        //TreeMap是一个有序的key-value集合，通过红黑树实现。2019.3.14
        // to make that all the parameters are sorted by ASC order
        TreeMap<String, String> sortedMap = new TreeMap(mapReq);

        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            strBuilder.append(entry.getKey());//.append添加数据在后面。2019.3.14
            strBuilder.append('=');
            strBuilder.append(entry.getValue());
            strBuilder.append('&');
        }
        if (mapReq.size() > 0) {
            strBuilder.setLength(strBuilder.length() - 1);
        }
        //System.out.println("sign str: " + strBuilder);
        Log.d(TAG,"formSignstr: " + strBuilder);

        return strBuilder.toString();
    }

    /*
     *将参数整合起来。2019.3.14
     */
    public static String formPostbody(Map<String, String> mapReq) {
        StringBuilder stringBuilder = new StringBuilder();
        // to make that all the parameters are sorted by ASC order
        TreeMap<String, String> sortedMap = new TreeMap(mapReq);
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            try {
                stringBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                stringBuilder.append('=');
                stringBuilder.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                stringBuilder.append('&');
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG,"formPostbody    "+stringBuilder.toString());
        return stringBuilder.toString();
    }

    /*
    生成  HmacSHA1签名，并添加在后面,后续要进行URL编码
     */
    public static String base64_hmac_sha1(String value, String keyStr) {
        String encoded = "";
        String type = "HmacSHA1";
        try {
            byte[] key = (keyStr).getBytes("UTF-8");
            byte[] Sequence = (value).getBytes("UTF-8");

            Mac HMAC = Mac.getInstance(type);
            SecretKeySpec secretKey = new SecretKeySpec(key, type);

            HMAC.init(secretKey);
            byte[] Hash = HMAC.doFinal(Sequence);
            encoded = Base64.getEncoder().encodeToString(Hash);

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG,"base64_hmac_sha1    "+encoded);
        return encoded;
    }

    /*
     * 获得unix时间戳
     */
    public static String toUNIXEpoch() {
        long unixTime = System.currentTimeMillis() / 1000L;
        return unixTime + "";
    }

    /*
     * 生成随机nonce
     */
    public static String toUNIXNonce() {
        long unixTime = System.currentTimeMillis() / 1000L;
        String str = unixTime + "";
        String nonce = str.substring(0, 4);
        return nonce;
    }

    /*
     *调用base64_hmac_sha1，生成签名
     */
    public static String createSign(String signStr, String secretKey) {
        return base64_hmac_sha1(signStr, secretKey);
    }



    /*
    传入参数
     */
    public static int setConfig(
            String SecretId,
            String SecretKey,
            String Text,
            int Code
    ) {
        if (SecretId.length() <= 0) {
            // System.out.println("SecretId can not be empty!");
            Log.d(TAG,"SecretId can not be empty!");
            return -1;
        }
        if (SecretKey.length() <= 0) {
            //System.out.println("SecretKey can not be empty!");
            Log.d(TAG,"SecretKey can not be empty!");


            return -1;
        }
        if (Text.length() <= 0) {
            //System.out.println("SecretKey can not be empty!");
            Log.d(TAG,"Text can not be empty!");
            return -1;
        }


        if ( Code != 2097152 ) {
            Log.d(TAG,"Code Error!");
            return -1;
        }

        WENZHIsdk.SecretId = SecretId;
        WENZHIsdk.SecretKey = SecretKey;
        WENZHIsdk.Text = Text;
        WENZHIsdk.Code = Code;
        return 0;
    }

    public static int sendMessage() {
        Map<String, String> reqMap = new TreeMap();
        reqMap.put("Action", "LexicalAnalysis");//接口功能提供智能分词、词性标注、命名实体识别功能
        reqMap.put("Region","gz");//广州
        reqMap.put("Timestamp", toUNIXEpoch());
        reqMap.put("Nonce", toUNIXNonce());
        reqMap.put("SecretId", SecretId);
        reqMap.put("code","2097152");
        reqMap.put("text",Text);
        reqMap.put("SignatureMethod","HmacSHA1");
        String _url = "POSTwenzhi.api.qcloud.com/v2/index.php?";//生成签名用的地址
        String signstr = formSignstr(_url, reqMap);//将地址和参数整合
        String signing = createSign(signstr, SecretKey);//将地址和参数和签名整合
        String tmppostdata = formPostbody(reqMap);//将参数整合起来
        String sign = "";
        try {
            sign = URLEncoder.encode(signing, "UTF-8");//将签名转成UTF-8编码
            Log.d(TAG,"sign"+sign);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder postdata = new StringBuilder(tmppostdata);
        postdata.append("Signature=");//添加签名
        postdata.append(sign);
        String post = postdata.toString();//转化成字符串
        //System.out.println("post : "+post);
        Log.d(TAG,"POST:"+post);
        TAG2 = post;

        String serverUrl = "https://wenzhi.api.qcloud.com/v2/index.php";

        HttpURLConnection con = null;
        StringBuilder sbResult = new StringBuilder();
        try {
            URL url = new URL(serverUrl);//POST的地址
            con = (HttpURLConnection) url.openConnection();//开启连接
            con.setRequestMethod("POST");//设置请求方法
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setUseCaches(false);//不用缓存
            con.setRequestProperty("Host", "wenzhi.api.qcloud.com");//发送请求报文头 的参数
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Charset", "utf-8");


            // 往服务器写入数据
            OutputStream out = con.getOutputStream();
            out.write(post.getBytes());
            out.flush();

            // 接收服务器返回的数据
            InputStream in = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;// 每一行的数据
            while ((line = br.readLine()) != null) {
                sbResult.append(line);
            }
            System.out.println(sbResult.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (con != null) {
                con.disconnect();
                con = null;
                TAG1 = sbResult.toString();//供外部读取。
            }

        }
        return 0;
    }

}
