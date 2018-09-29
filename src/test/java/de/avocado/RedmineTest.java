package de.avocado;

import com.taskadapter.redmineapi.bean.Issue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedmineTest {

    @Autowired
    private Redmine redmine;


    private final Log logger = LogFactory.getLog(this.getClass());

    @Test
    public void getIssues() throws Exception {
        logger.error("hilfe");

        List<Issue> issues = redmine.getIssues();
        redmine.saveIssuesInGraph(issues);
//        redmine.saveIssuesInGraph(issues);
        System.out.println(issues);
//        awsLex.addMainIntent(intentName, sampleUtterances);
    }
}