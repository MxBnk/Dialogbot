package de.avocado;

import com.pi4j.io.gpio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Led {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void blinkLED() {

        try {

            /** create gpio controller */
            final GpioController gpio = GpioFactory.getInstance();

            final GpioPinDigitalOutput ledPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02);

            logger.info("Jetzt sollte die LED blinken");
            /** Blink every second */
            ledPin.blink(500, 5000);

            ledPin.setShutdownOptions(true, PinState.HIGH, PinPullResistance.OFF);
//            gpio.shutdown();
            gpio.unprovisionPin(ledPin);

//            /** keep program running until user aborts (CTRL-C) */
//            while (true) {
//                Thread.sleep(500);
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
