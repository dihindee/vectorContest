package com.example.user00.nevsvocalizer.Yandex_io;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

import com.example.user00.nevsvocalizer.MainActivity;
import com.example.user00.nevsvocalizer.VoiceControl;

import ru.yandex.speechkit.Error;
import ru.yandex.speechkit.Language;
import ru.yandex.speechkit.OnlineModel;
import ru.yandex.speechkit.OnlineRecognizer;
import ru.yandex.speechkit.Recognition;
import ru.yandex.speechkit.Recognizer;
import ru.yandex.speechkit.RecognizerListener;
import ru.yandex.speechkit.Track;
import ru.yandex.speechkit.Vocalizer;

public class MyRecognizer implements RecognizerListener {
    private OnlineRecognizer recognizer;
    private Vocalizer answerVocalizer;
    private TextView recognizeStatus;
    private static MyRecognizer instance;
    public static MyRecognizer getInstance(){
        if(instance!=null)return instance;
        return instance=new MyRecognizer(Language.RUSSIAN,OnlineModel.DIALOG,MyVocalizer.getInstance().getVocalizer());
    }
    private MyRecognizer(Language language, OnlineModel model, Vocalizer answerVocalizer){
        recognizer = new OnlineRecognizer.Builder(language,model,this)
                .setDisableAntimat(false)
                .build();
        recognizer.prepare();
        this.answerVocalizer = answerVocalizer;
    }
    public Recognizer getRecognizer(){
        return recognizer;
    }
    public void startRecognize(TextView statusView) throws SecurityException{
        recognizeStatus = statusView;
        recognizer.startRecording();
    }
    @Override
    public void onRecordingBegin(@NonNull Recognizer recognizer) {
        Log.d("MYAPP","recording begin");
    }

    @Override
    public void onSpeechDetected(@NonNull Recognizer recognizer) {
            Log.d("MYAPP","speech detected");
    }

    @Override
    public void onSpeechEnds(@NonNull Recognizer recognizer) {
        Log.d("MYAPP","speech ends");
        //recognizer.stopRecording();
    }

    @Override
    public void onRecordingDone(@NonNull Recognizer recognizer) {
        Log.d("MYAPP","recording stopped");
    }

    @Override
    public void onPowerUpdated(@NonNull Recognizer recognizer, float v) {

    }

    @Override
    public void onPartialResults(@NonNull Recognizer recognizer, @NonNull Recognition recognition, boolean b) {
        Log.d("MYAPP","partial results: " + recognition.getBestResultText());
        recognizeStatus.setText(recognition.getBestResultText());
    }

    @Override
    public void onRecognitionDone(@NonNull Recognizer recognizer) {
        Log.d("MYAPP","recognition done");
        VoiceControl.getInstance().handlePhrase(recognizeStatus.getText().toString());
        recognizeStatus.setText("");
    }

    @Override
    public void onRecognizerError(@NonNull Recognizer recognizer, @NonNull Error error) {
        answerVocalizer.synthesize("Пожалуйста, повторите запрос", Vocalizer.TextSynthesizingMode.APPEND);
    }

    @Override
    public void onMusicResults(@NonNull Recognizer recognizer, @NonNull Track track) {

    }
}
