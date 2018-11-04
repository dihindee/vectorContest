package com.example.user00.nevsvocalizer;

import android.content.res.AssetManager;
import android.util.Log;

import com.example.user00.nevsvocalizer.Yandex_io.MyRecognizer;
import com.example.user00.nevsvocalizer.Yandex_io.MyVocalizer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class VoiceControl {
    public MyRecognizer recognizer;
    public MyVocalizer vocalizer;
    //private ArrayList<String> okPhraseList, errorPhraseList, helpPhraseList;
    private ArrayList<String> phrases[];
    private ArrayList<String> commands[];
    private static final int HELP = 0, STOP = 1, CONTINUE = 2, RESTART = 3, STARTHEADER = 4, STARTPOST = 5, SELECTION = 6;
    private static VoiceControl instance;
    BrowserActivity browser;
    MainActivity main;
    private static AssetManager assets;
    public static void setAssets(AssetManager assets){VoiceControl.assets = assets;}
    public void setMainActivity(MainActivity m){
        this.main = m;
    }
    public void setBrowserActivity(BrowserActivity b){
        this.browser = b;
    }
    public static VoiceControl getInstance() {
        if (instance != null)
            return instance;
        return instance = new VoiceControl();
    }

    private VoiceControl() {
        vocalizer = MyVocalizer.getInstance();
        recognizer = MyRecognizer.getInstance();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(assets.open("Phrases.txt")));
            String s;
            int state = -1;
            phrases = new ArrayList[3];
            //okPhraseList = new ArrayList<>();
            //errorPhraseList = new ArrayList<>();
            //helpPhraseList = new ArrayList<>();
            while ((s = reader.readLine()) != null) {
                if (s.charAt(0) == '/') {
                    state++;
                    phrases[state] = new ArrayList<>();
                }
                else {
                    phrases[state].add(s);
                }
            }
            reader.close();
            reader = new BufferedReader(new InputStreamReader(assets.open("Comands.txt")));
            state = -1;
            commands = new ArrayList[7];
            while ((s = reader.readLine()) != null) {
                if (s.charAt(0) == '/') {
                   // Log.d("MYAPP","new cathegory:"+s);
                    commands[++state] = new ArrayList<>();
                } else {
                    commands[state].add(s);
                }
            }
            reader.close();
           // for(ArrayList a:commands){Log.d("MYAPP","list:"+a);}
            //reader.readLine();
        } catch (Exception e) {
            Log.e("MYAPP", "error :", e);
        }
    }

    public void handlePhrase(String recognizedText) {
        Log.d("MYAPP","handling: "+ recognizedText);
        boolean isFound = false;
        int cathegory;
        for (cathegory=0; (!isFound) && cathegory < commands.length; cathegory++) {
            for (String a : commands[cathegory]) {
                if (recognizedText.contains(a)) {
                    if (cathegory == 6) recognizedText = recognizedText.replace( a, "");
                    isFound = true;
                    break;
                }
            }
        }
        if(!isFound){speakError();return;}
        Log.d("MYAPP","cathegory: "+cathegory);
        //speakSuccess();
        switch (cathegory-1) {
            case HELP: {
                Log.d("MYAPP","do Help");
                vocalizer.synthesizeList(phrases[2]);
                break;
            }
            case STOP: {
                Log.d("MYAPP","do Stop");
                if(vocalizer.isSynthesising())
                    vocalizer.stopSynthesize();
                break;
            }
            case CONTINUE: {
                Log.d("MYAPP","do Continue");
                vocalizer.continueVocalizingList();
                break;
            }
            case RESTART:{ Log.d("MYAPP","do Restart");}
            case STARTPOST: {
                Log.d("MYAPP","do start post");
                if(browser!=null && browser.getURL().contains("habr.com")){
                    vocalizer.synthesizeList(browser.postText);
                    break;
                }
            }
            case STARTHEADER: {
                Log.d("MYAPP","do start header");
                if(browser!=null)speakError();
                else vocalizer.synthesizeList(main.newsHeaders);
                break;
            }
            case SELECTION: {
                Log.d("MYAPP","do selection post");
                if(browser!=null){speakError();break;}
                ArrayList<String> list = main.newsHeaders;
                ArrayList<String> words[] = new ArrayList[list.size()];
                recognizedText = recognizedText.toLowerCase().trim();
                Log.d("MYAPP","selection on "+recognizedText);
                boolean found=false;
                for(int i=0;i<words.length&&!found;i++){
                    words[i] = new ArrayList<>();
                    words[i].addAll(Arrays.asList(list.get(i).toLowerCase().replaceAll("[«»().,;\":!?/]", "").split(" ")));
                    for(int j=0;j<words[i].size();j++){
                        if(words[i].get(j).length()<=3){
                            words[i].remove(j--);
                        }
                    }
                    Log.d("MYAPP","matching: "+words[i]);
                    for(String a:words[i]){
                        if(recognizedText.matches(a.substring(0,a.length()-3)+"[a-zа-я]{0,4}")){
                            found = true;
                            main.openBrowser(main.postURLs.get(i));
                        }
                    }
                }
                break;
                }
            }
        }

    public void speakSuccess() {
        if (phrases!=null&&!phrases[0].isEmpty())
            vocalizer.synthesize(phrases[0].get((int) (Math.random() * phrases[0].size())));
    }

    public void speakError() {
        if (phrases!=null&&!phrases[1].isEmpty())
            vocalizer.synthesize(phrases[1].get((int) (Math.random() * phrases[1].size())));
    }
}
