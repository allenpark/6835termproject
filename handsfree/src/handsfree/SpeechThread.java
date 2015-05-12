package handsfree;

import java.io.IOException;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;

public class SpeechThread extends Thread {
    private String speech;

    public SpeechThread() {
        this.speech = "";
    }

    public void run() {
        Configuration configuration = new Configuration();

        // Set path to acoustic model.
        configuration
                .setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        // Set path to dictionary.
        //configuration.setDictionaryPath("owndict.dict");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        // Set language model.
        //configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.dmp");
        configuration.setGrammarName("rawr");
        configuration.setGrammarPath("file:///C:/Users/Allen/Dropbox/backup/MIT Random/6.835/termproject/6835termproject/handsfree");
        configuration.setUseGrammar(true);

        try {
            LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(
                    configuration);
            // Start recognition process pruning previously cached data.
            recognizer.startRecognition(true);
            while (true) {
                SpeechResult result = recognizer.getResult();
                synchronized (this.speech) {
                    this.speech = result.getHypothesis();
                }
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        // Pause recognition process. It can be resumed then with
        // startRecognition(false).
        // recognizer.stopRecognition();
    }

    public String getSpeech() {
        synchronized (this.speech) {
            String r = this.speech;
            this.speech = "";
            return r;
        }
    }
}
