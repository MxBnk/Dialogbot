package de.avocado;

import de.avocado.service.Dialog;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DialogAskerTest {

    @Autowired
//    private Dialog.DialogAsker dialogAsker;
    private Dialog dialog;


    private final Log logger = LogFactory.getLog(this.getClass());

    @Test
    public void getHello() throws Exception {

        Dialog.DialogAsker asker = mock(Dialog.DialogAsker.class);
        when(asker.ask(anyString())).thenReturn("weight");

        assertEquals(dialog.getInputFromUser(asker), "weight");


    }
    @Test
    public void asksForNewInput() throws Exception {
        Dialog.DialogAsker asker = mock(Dialog.DialogAsker.class);
        when(asker.ask("requestText")).thenReturn("20");
//        when(asker.ask("Wrong number, try again.")).thenReturn(3);

        dialog.startInnerDialog("http://schema.org/weight", "Patient", asker);

//        verify(asker).ask("Wrong number, try again.");
    }@Test
    public void startDialog() throws Exception {

        dialog.startMainDialog();

    }

}