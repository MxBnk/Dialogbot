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
public class DialogflowTest {

    @Autowired
    private Dialogflow dialogflow;


    private final Log logger = LogFactory.getLog(this.getClass());

    @Test
    public void getIssues() throws Throwable {
        logger.error("hilfe");
//        dialogflow.getUtterance("http://schema.org/duration");
//        dialogflow.startMainDialog();
//        dialogflow.startMainDialog();
        dialogflow.startInnerDialog("http://schema.org/duration", "7989");
    }
}