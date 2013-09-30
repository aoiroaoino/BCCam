package com.applicative.goldrush.bccam;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.http.NameValuePair;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class PostPictureAsyncTask extends AsyncTask<PostPictureParams, Void, String> {
    private Activity activity;

    public PostPictureAsyncTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(PostPictureParams... params) {
    	String result = "";
    	
    	for(PostPictureParams param : params) {
    	  result = sendPicture(param.input, param.url, param.userName);
    	}
    	return result;
    }

    @Override
    protected void onPostExecute(String result) {
    	Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
    }

    private String sendPicture(InputStream input, String url, String userName){
        try{
            MultipartEntityBuilder entity = MultipartEntityBuilder.create();
            HttpPost httpPost = new HttpPost(url);
            DefaultHttpClient client = new DefaultHttpClient();

            // リクエストパラメータの設定
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            entity.addBinaryBody("attachment", input, ContentType.MULTIPART_FORM_DATA ,"filename");
//            entity.setCharset(Charset.forName("UTF-8"));
//            entity.addTextBody("sender", userName);
//            entity.addTextBody("login", "goldrush");
//            entity.addTextBody("password", "furuponpon");
            
            entity.addPart("sender", new StringBody(userName, Charset.forName("UTF-8")));
            entity.addPart("login", new StringBody("goldrush", Charset.forName("UTF-8")));
            entity.addPart("password", new StringBody("furuponpon", Charset.forName("UTF-8")));
            
            Log.i("user_name in sendPic", userName);
            Log.i("url in sendPic", url);
            Log.i("send params", httpPost.toString());

            // POST データの設定
            httpPost.setEntity(entity.build());

            HttpResponse response = client.execute(httpPost);
            int status = response.getStatusLine().getStatusCode();

            // 結果が正しく帰って来なければエラー
            if ( status != HttpStatus.SC_OK ){
                throw new Exception("Send error");
            }
            Log.i("response", EntityUtils.toString(response.getEntity()));
            return "送信成功しました";
        } catch (Exception e) {
            return "送信失敗しました";
        }
    }
}








