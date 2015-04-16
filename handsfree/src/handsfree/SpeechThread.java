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
        configuration
                .setDictionaryPath("cmudict-en-us2.dict");
        // Set language model.
        configuration
                .setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.dmp");

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