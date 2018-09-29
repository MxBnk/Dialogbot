package de.avocado;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ValidationTest {

    @Autowired
    private Validation validation;
    private Dialogflow dialog;

    private final Log logger = LogFactory.getLog(this.getClass());

    @Test
    public void getHello() throws Exception {

        Dialogflow.DialogAsker asker = mock(Dialogflow.DialogAsker.class);
        when(asker.ask(anyString())).thenReturn("weight");

        assertEquals(dialog.getInputFromUser(asker), "weight");


    }
    @Test
    public void asksForNewInput() throws Throwable {
        Dialogflow.DialogAsker asker = mock(Dialogflow.DialogAsker.class);
//        when(asker.ask("requestText")).thenReturn("20");
    }
    @Test
    public void getValidation() throws Exception {
        logger.error("hilfe");
    }
}