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
public class GetGraphAsModelTest {

    @Autowired
    private Virtuoso virtuoso;


    private final Log logger = LogFactory.getLog(this.getClass());

    @Test
    public void getGraphAsModel() throws Exception {
        logger.error("hilfe");
//        virtuoso.getGraphAsModel();

    }
}