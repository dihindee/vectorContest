package com.example.user00.nevsvocalizer;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.example.user00.nevsvocalizer.R;
import com.example.user00.nevsvocalizer.Yandex_io.MyRecognizer;
import com.example.user00.nevsvocalizer.Yandex_io.MyVocalizer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class BrowserActivity extends AppCompatActivity {
    String postData;
    ArrayList<String> postText;
    private WebView wv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VoiceControl.getInstance().setBrowserActivity(this);
        setContentView(R.layout.activity_browser);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String url = getIntent().getStringExtra("url");
        wv = findViewById(R.id.webView);
        wv.setWebViewClient(new WebViewClient());
        wv.getSettings().setJavaScriptEnabled(true);
        Log.d("MYAPP","webview connecting to: "+ url);
        final MediaPlayer mp = MediaPlayer.create(this,R.raw.recording);
        Button button = findViewById(R.id.buttonVoice);
        button.setOnClickListener(v -> {
            mp.start();
            MyRecognizer.getInstance().startRecognize(findViewById(R.id.recognizedText));
        });
        button = findViewById(R.id.buttonPronoun);
        button.setOnClickListener(v ->{
            if(MyVocalizer.getInstance().isSynthesising())MyVocalizer.getInstance().stopSynthesize();
            else MyVocalizer.getInstance().synthesizeList(postText);
        });
        wv.loadUrl(url);
        loadPostData(url);
    }
    @Override
    public void onBackPressed(){
        if(wv.canGoBack()){
            wv.goBack();
        }
        else super.onBackPressed();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        VoiceControl.getInstance().setBrowserActivity(null);
    }
    private enum SeekState{TITLE, POST_BODY_HEAD,CODE,POST_BODY_END,COMPLETED}
    public String getURL(){return wv.getUrl();}
    public void loadPostData(String url){
        if(postText==null)
            postText = new ArrayList<>();
        else postText.clear();
        new Thread(()->{
            try {
                URL src = new URL(url);
                URLConnection connection = src.openConnection();
                connection.setDoInput(true);
                connection.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                SeekState state = SeekState.TITLE;
                while((line = reader.readLine())!=null && state!=SeekState.COMPLETED){
                    switch (state){
                        case TITLE:{
                            if(line.contains("<h1 class=\"post__title post__title_full\">")){
                                line = reader.readLine().replaceAll("<[^>]+>","").trim();
                                if(!line.isEmpty())
                                    postText.add(line);
                                state = SeekState.POST_BODY_HEAD;
                            }
                            break;
                        }
                        case POST_BODY_HEAD:{
                            if(line.contains("<div class=\"post__body post__body_full\">")){
                                line = reader.readLine().replaceAll("<[^>]+>","").trim();
                                if(!line.isEmpty())
                                    postText.add(line);
                                state=SeekState.POST_BODY_END;
                            }
                            break;
                        }
                        case CODE:{
                            if(line.contains("</code>"))state = SeekState.POST_BODY_END;
                            break;
                        }
                        case POST_BODY_END:{
                            if(line.contains("<script class=\"js-mediator-script\">")){
                                state = SeekState.COMPLETED;
                                break;
                            }
                            else{
                                if(line.contains("<code>")){
                                    state = SeekState.CODE;
                                    break;
                                }
                                line = line.replaceAll("<[^>]+>","").trim();
                                if(!line.isEmpty())
                                    postText.add(line);
                            }
                        }
                    }
                }
                Log.d("MYAPP","post: "+postText);
            }catch (Exception e){
                Log.e("MYAPP","error: ",e);
            }
        }).start();
    }
}
