package de.avocado;

import com.google.cloud.texttospeech.v1beta1.*;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sound.sampled.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Service
public class Output {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void getOutput(String text) throws Exception {

        String outputAudioFilePath = Output.class.getResource("output.wav").getPath();

        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            logger.info("Set the text input to be synthesized");
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            logger.info("Build the voice request; languageCode = de-DE");
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder().setLanguageCode("de-DE")
                    .setSsmlGender(SsmlVoiceGender.MALE)
                    .build();

            logger.info("Select the type of audio file you want returned");
            AudioConfig audioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.LINEAR16)// MP3 audio.
                    .build();

            logger.info("Perform the text-to-speech request");
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            logger.info("Get the audio contents from the response");
            ByteString audioContents = response.getAudioContent();

            logger.info("Write the response to the output file.");
            try (OutputStream out = new FileOutputStream(outputAudioFilePath)) {
                out.write(audioContents.toByteArray());
                logger.info("Audio content written to file \"output.wav\"");
            }
            playSound(outputAudioFilePath);
        }
    }
    public void playSound(String path){
        // initialize Toolkit
//        com.sun.javafx.application.PlatformImpl.startup(()->{});
        AudioInputStream audioIn;
        try {
            logger.info("try playSound from path " + path);
            audioIn = AudioSystem.getAudioInputStream(new File(path));
            logger.info("audio in" + audioIn.toString());
            Line.Info linfo = new Line.Info(Clip.class);
            logger.info("Clip.class: " + Clip.class + "; linfo: " + linfo.toString());
            Line line = AudioSystem.getLine(linfo);
            Clip clip = (Clip) line;
            clip.open(audioIn);
            logger.info("warte " + (clip.getMicrosecondLength()/1000) + " Sekunden, bis Output fertig ist.");
            clip.start();
            Thread.sleep(clip.getMicrosecondLength()/1000);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException  e1) {
            e1.printStackTrace();
        }

        // exit Toolkit
//        com.sun.javafx.application.PlatformImpl.exit();
    }
}

