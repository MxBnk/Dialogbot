package de.avocado;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OutputTest {

    @Autowired
    private Output output;

    private final Log logger = LogFactory.getLog(this.getClass());

    @Test
    public void playSound() throws Throwable {
        logger.error("Starte Output Test");
        output.playSound(Output.class.getResource("output.wav").getPath());
    }
}