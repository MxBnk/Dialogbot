package de.avocado;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@Service
public class Input {

    Logger log = LoggerFactory.getLogger(Input.class);

    Led led = new Led();

    // getInput duration, in milliseconds
    static final long RECORD_TIME = 6000;  // 6 Seconds

    // path of the wav file
    File wavFile = new File(Input.class.getResource("input.wav").getPath());

    // format of audio file
    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

    // the line from which audio data is captured
    TargetDataLine line;

    /**
     * Defines an audio format
     */
    AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                channels, signed, bigEndian);
        return format;
    }

    /**
     * Captures the sound and getInput into a WAV file
     */
    void start() {
        log.info("Path der input.wav Datei: "+ Input.class.getResource("input.wav").getPath());
        try {

            Mixer.Info minfo[] = AudioSystem.getMixerInfo();
            Mixer mixer = AudioSystem.getMixer(minfo[3]);

            log.info("DAS IST DAS RICHTIGE MIC " + minfo[3].toString());

            for (Mixer.Info minfo1 : minfo) {
                log.info(minfo1.toString());
            }

            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            // checks if system supports the data line
            if (!AudioSystem.isLineSupported(info)) {
                log.info("Line not supported");
                System.exit(0);
            }

            line = (TargetDataLine) mixer.getLine(info);
//            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();   // start capturing

            log.info("Start capturing...");

            AudioInputStream ais = new AudioInputStream(line);

            log.info("Start recording...");

            // start recording
            AudioSystem.write(ais, fileType, wavFile);

        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    /**
     * Closes the target data line to finish capturing and recording
     */
    void finish() {
        line.stop();
        line.close();
        log.info("Finished");
    }

    /**
     * Entry to run the program
     */
    public void getInput(CountDownLatch countDownLatch) {
        final Input recorder = new Input();

        // creates a new thread that waits for a specified
        // of time before stopping
        Thread stopper = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(RECORD_TIME);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                } finally {
                    recorder.finish();
                    countDownLatch.countDown();
                    log.info("counted Down");
                }
            }
        });

        log.info("starte Stoppper");
        stopper.start();
        log.info("starte Recorder");
        led.blinkLED();
        recorder.start();
    }
}

