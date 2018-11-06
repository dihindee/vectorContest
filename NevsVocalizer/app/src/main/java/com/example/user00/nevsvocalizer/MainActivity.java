package com.example.user00.nevsvocalizer;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.user00.nevsvocalizer.Yandex_io.MyPhraseSpotter;
import com.example.user00.nevsvocalizer.Yandex_io.MyRecognizer;
import com.example.user00.nevsvocalizer.Yandex_io.MyVocalizer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.UUID;

import ru.yandex.speechkit.Language;
import ru.yandex.speechkit.SpeechKit;

public class MainActivity extends AppCompatActivity {
    MyRecognizer recognizer;
    MyVocalizer vocalizer;
    ListView newsList;
    ArrayList<String> newsHeaders;
    ArrayList<String> postURLs;
    boolean isLoading = false;
    private static final int REQUEST_MICRO =101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("MYAPP", "Permission to record not granted");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_MICRO);
        } MyPhraseSpotter spotter;
        SharedPreferences sRef = getPreferences(MODE_PRIVATE);
        String uuid = sRef.getString("uuid", "none");
        if (uuid.equals("none")) {
            uuid = UUID.randomUUID().toString();
            SharedPreferences.Editor ed = sRef.edit();
            ed.putString("uuid", uuid);
            ed.apply();
            Log.d("MYAPP","generated uuid: "+uuid);
        }
        else{Log.d("MYAPP","loaded uuid: "+uuid.replaceAll("-",""));}
        try {
            SpeechKit.getInstance().init(getApplicationContext(), "fef053c0-013b-49c8-831c-4899c2bfb07a");
            SpeechKit.getInstance().setUuid(uuid.replaceAll("-",""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        VoiceControl.setAssets(getAssets());
        VoiceControl.getInstance().setMainActivity(this);
        vocalizer = MyVocalizer.getInstance();
        recognizer = MyRecognizer.getInstance();

        // spotter = MyPhraseSpotter.createSpotter();
       // spotter.start();
        newsList = findViewById(R.id.newsList);
        newsHeaders = new ArrayList<>();
        postURLs = new ArrayList<>();
        Button button = findViewById(R.id.updatebutton);
        button.setOnClickListener(v-> loadNews("https://habr.com"));
        button = findViewById(R.id.pronounbutton);
        button.setOnClickListener(v -> {
            if(vocalizer.isSynthesising())vocalizer.stopSynthesize();
            else vocalizer.synthesizeList(newsHeaders);
        });
        button = findViewById(R.id.voicebutton);
        final MediaPlayer mp = MediaPlayer.create(this,R.raw.recording);
        button.setOnClickListener(v -> {
            mp.start();
            recognizer.startRecognize(findViewById(R.id.recognizedText));
        });
        newsList.setOnItemClickListener((parent,view,position,id)-> openBrowser(postURLs.get(position)));
        loadNews("https://habr.com");
    }
    public void openBrowser(String url){
        Intent intent = new Intent(MainActivity.this,BrowserActivity.class);
        intent.putExtra("url",url);
        startActivity(intent);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_MICRO: {
                if (grantResults.length == 0
                        || grantResults[0] !=
                        PackageManager.PERMISSION_GRANTED) {

                    Log.i("MYAPP", "Permission has been denied by user");
                } else {
                    Log.i("MYAPP", "Permission has been granted by user");
                }
            }
        }
    }
    void loadNews(final String url) {// habr.com
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo inf = cm.getActiveNetworkInfo();
        if (inf != null && inf.isConnectedOrConnecting()) {
            newsHeaders.clear();
            postURLs.clear();
            new Thread(()-> {
                    isLoading = true;
                    try {
                        URL habr = new URL(url);
                        URLConnection connection = habr.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        Log.d("loading", "opened connection to https://habr.com");
                        InputStream stream = connection.getInputStream();
                        Log.d("loading", "opened stream");
                        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                        Log.d("loading", "reader opened");
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.contains("<h2 class=\"post__title\">")) {
                                line = reader.readLine();
                                int strBegin = line.indexOf('"') + 1;
                                postURLs.add(line.substring(strBegin, line.indexOf('"', strBegin)));
                                strBegin = line.indexOf('>') + 1;
                                newsHeaders.add(line.substring(strBegin, line.indexOf('<', strBegin)));
                            }
                        }
                        reader.close();
                        runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
                                    android.R.layout.simple_list_item_1,
                                    newsHeaders);
                            newsList.setAdapter(adapter);
                        });
                    } catch (Exception e) {
                        Log.e("loading", "error: " + e.toString());
                    } finally {
                        isLoading = false;
                        Log.d("loading", "loading finished");
                    }
            }).start();
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Проверьте интернет-соединение", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
