package de.avocado;

import de.avocado.service.Dialog;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HelloControllerIT {

    @Autowired
    private Dialog dialog;


    private final Log logger = LogFactory.getLog(this.getClass());

    @Test
    public void getHello() throws Exception {
        logger.error("hilfe");
//        dialog.startMainDialog();
        dialog.startMainDialog();
    }
}