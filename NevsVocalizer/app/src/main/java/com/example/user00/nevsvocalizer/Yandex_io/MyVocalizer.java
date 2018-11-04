package com.example.user00.nevsvocalizer.Yandex_io;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

import ru.yandex.speechkit.Emotion;
import ru.yandex.speechkit.Error;
import ru.yandex.speechkit.Language;
import ru.yandex.speechkit.OnlineVocalizer;
import ru.yandex.speechkit.Synthesis;
import ru.yandex.speechkit.Vocalizer;
import ru.yandex.speechkit.VocalizerListener;
import ru.yandex.speechkit.Voice;

public class MyVocalizer implements VocalizerListener {
    private OnlineVocalizer vocalizer;
    private boolean isSynthesising=false,isList;
    private List<String> synthList;
    int lastString;
    private static MyVocalizer instance;
    public static MyVocalizer getInstance(){
        if(instance!=null) return instance;
        return instance = new MyVocalizer(Language.RUSSIAN);
    }
    private MyVocalizer(Language language){
        vocalizer = new OnlineVocalizer.Builder(language,this)
                .setEmotion(Emotion.NEUTRAL)
                .setVoice(Voice.OMAZH)
                .build();
        vocalizer.prepare();
    }
    public void synthesize(String text){
        isList = false;
        vocalizer.synthesize(text, Vocalizer.TextSynthesizingMode.APPEND);

    }
    public void synthesizeList(List<String> list){
        if(list==null||list.isEmpty())return;
        synthList = list;
        lastString = 0;
        isList = true;
        vocalizer.synthesize(synthList.get(lastString), Vocalizer.TextSynthesizingMode.APPEND);
    }
    public void stopSynthesize(){
        isSynthesising = false;
        isList=false;
        vocalizer.cancel();
    }
    public void continueVocalizingList(){
        if(synthList!=null && synthList.size() > lastString){
            isList=true;
            isSynthesising=true;
            vocalizer.synthesize(synthList.get(lastString), Vocalizer.TextSynthesizingMode.APPEND);
        }
    }
    public Vocalizer getVocalizer(){
        return vocalizer;
    }
    public boolean isSynthesising(){
        return isSynthesising;
    }
    @Override
    public void onSynthesisDone(@NonNull Vocalizer vocalizer) {
    }
    @Override
    public void onPartialSynthesis(@NonNull Vocalizer vocalizer, @NonNull Synthesis synthesis) {}
    @Override
    public void onPlayingBegin(@NonNull Vocalizer vocalizer) {
        isSynthesising = true;
    }
    @Override
    public void onPlayingDone(@NonNull Vocalizer vocalizer) {
        if(isList&&synthList.size()>lastString+1){
            lastString++;
            vocalizer.synthesize(synthList.get(lastString), Vocalizer.TextSynthesizingMode.APPEND);
        }
        else
            isSynthesising = false;
    }
    @Override
    public void onVocalizerError(@NonNull Vocalizer vocalizer, @NonNull Error error) {
        Log.e("MYAPP","vocalizer error:"+error.toString());
    }
}
