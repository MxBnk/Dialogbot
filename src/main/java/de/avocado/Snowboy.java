package de.avocado;

import ai.kitt.snowboy.SnowboyDetect;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Service
public class Snowboy {
    private final Log logger = LogFactory.getLog(this.getClass());
    static {
        System.loadLibrary("snowboy-detect-java");
    }

    public void start () {
        // Sets up audio.
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);

        logger.info(Snowboy.class.getResource("common.res").getPath());
        logger.info(Snowboy.class.getResource("/models/Herbert.pmdl").getPath());

        // Sets up Snowboy.
        SnowboyDetect detector = new SnowboyDetect(Snowboy.class.getResource("common.res").getPath(),
                Snowboy.class.getResource("/models/Herbert.pmdl").getPath());
        detector.SetSensitivity("0.5");
        detector.SetAudioGain(1);
        detector.ApplyFrontend(false);

        try {
            TargetDataLine targetLine =
                    (TargetDataLine) AudioSystem.getLine(targetInfo);
            targetLine.open(format);
            targetLine.start();

            // Reads 0.1 second of audio in each call.
            byte[] targetData = new byte[3200];
            short[] snowboyData = new short[1600];
            int numBytesRead;

            logger.info("Rede mit Herbert....");

            while (true) {
                // Reads the audio data in the blocking mode. If you are on a very slow
                // machine such that the hotword detector could not process the audio
                // data in real time, this will cause problem...

                numBytesRead = targetLine.read(targetData, 0, targetData.length);

                if (numBytesRead == -1) {
                    logger.info("Fails to read audio data.");
                    break;
                }

                // Converts bytes into int16 that Snowboy will read.
                ByteBuffer.wrap(targetData).order(
                        ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(snowboyData);
                logger.info("ByteBuffer wrapped");
                // Detection.
                int result = detector.RunDetection(snowboyData, snowboyData.length);
                if (result > 0) {
                    logger.info("Hotword " + result + " detected!\n");
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}