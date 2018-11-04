package com.example.user00.nevsvocalizer.Yandex_io;

import android.support.annotation.NonNull;
import android.util.Log;

import ru.yandex.speechkit.Error;
import ru.yandex.speechkit.PhraseSpotter;
import ru.yandex.speechkit.PhraseSpotterListener;

public class MyPhraseSpotter implements PhraseSpotterListener {
    private PhraseSpotter spotter;
    private static MyPhraseSpotter instance = null;
    public static MyPhraseSpotter createSpotter(){
        if(instance==null){
            return instance = new MyPhraseSpotter();
        }
        return instance;
    }
    private MyPhraseSpotter(){
        spotter = new PhraseSpotter.Builder("phrase-spotter/commands",this).build();
        spotter.prepare();
    }
    public void start() throws SecurityException{
        spotter.start();
        Log.d("MYAPP-spotter","start()");
    }
    @Override
    public void onPhraseSpotted(@NonNull PhraseSpotter phraseSpotter, @NonNull String s, int i) {
        Log.d("MYAPP-spotter","spotted: "+s+ " num: "+i);
    }

    @Override
    public void onPhraseSpotterStarted(@NonNull PhraseSpotter phraseSpotter) {
        Log.d("MYAPP-spotter","started recognizing");
    }

    @Override
    public void onPhraseSpotterError(@NonNull PhraseSpotter phraseSpotter, @NonNull Error error) {
        Log.e("MYAPP-spotter","Error: "+error);
    }
}
