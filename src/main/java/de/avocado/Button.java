package de.avocado;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Button {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void pressButtonToStart() throws InterruptedException {

        logger.info("Knopf druecken fuer Spracheingabe.");

        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // gpio pin #02 as an input pin with its internal pull down resistor enabled
        final GpioPinDigitalInput myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00, PinPullResistance.PULL_DOWN);

        // create and register gpio pin listener
        myButton.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                try {
                    if(event.getState().toString().equals("LOW")){
                        //if button pushed, start main dialog and listen to user input to get intents
                        Dialogflow dialogflow = new Dialogflow();
                        dialogflow.startMainDialog();
                    }
                } catch (Exception e) {
                    logger.info(e.getMessage());
                }
            }
        });


        // keep program running until user aborts (CTRL-C)
        for (;;) {
            Thread.sleep(1500);
        }

    }
}