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
public class ClearGraph {

    @Autowired
    private Virtuoso virtuoso;


    private final Log logger = LogFactory.getLog(this.getClass());

    @Test
    public void clearGraph() throws Exception {
        logger.error("hilfe");
        virtuoso.clearDataGraph();
    }
    @Test
    public void deleteTripleInGraph() throws Exception {
        logger.error("hilfe");
        virtuoso.deleteTripleInGraph("http://example.org/ns#Bob", "http://schema.org/givenName", "Robert");
    }
    @Test
    public void addTripleToGraph(){
        virtuoso.saveLiteralInGraph("http://example.org/ns#1111", "http://schema.org/startDate", "2018-05-24");
    }
}