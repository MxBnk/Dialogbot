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
public class AddIntentTest {

    @Autowired
    private AwsLex awsLex;


    private final Log logger = LogFactory.getLog(this.getClass());

    @Test
    public void addIntentTest() throws Exception {
        logger.error("hilfe");
        String intentName = "value";
        String slotType = "Nominal";
        String question = "Wie groß ist value?";
        String [] sampleUtterances = new String[]{"Ich möchte einen Angestellten erstellen", "Neuer Angestellter"};

        awsLex.addInformationIntent(intentName, slotType, question);
//        awsLex.addMainIntent(intentName, sampleUtterances);
    }
}