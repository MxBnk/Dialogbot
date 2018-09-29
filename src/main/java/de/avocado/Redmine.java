package de.avocado;

import com.taskadapter.redmineapi.Params;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.Version;
import com.taskadapter.redmineapi.bean.VersionFactory;
import de.avocado.config.RedmineConfig;
import de.avocado.service.SSLCertificateValidation;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class Redmine {

    private  Virtuoso virtuoso = new Virtuoso();

    public List<Issue> getIssues() throws Exception{
        SSLCertificateValidation.disable();

        final ClientConnectionManager connectionManager;
        try {
            connectionManager = createConnectionManagerWithOurDevKeystore();
        } catch (Exception e) {
            throw new RuntimeException("cannot create connection manager: " + e, e);
        }

        RedmineConfig redmineConfig = new RedmineConfig();

        HttpClient client = RedmineManagerFactory.getNewHttpClient(redmineConfig.getURI(), connectionManager);

        RedmineManager mgr = RedmineManagerFactory.createWithApiKey(redmineConfig.getURI(), redmineConfig.getApiKey(), client);

        Params params = new Params()
                .add("status_id", "1");

        Params paramInProcess = new Params()
                .add("status_id", "2");

        List<Issue> issues = mgr.getIssueManager().getIssues(params).getResults();
        List<Issue> issuesInProcess = mgr.getIssueManager().getIssues(paramInProcess).getResults();
        issues.addAll(issuesInProcess);
        issues.sort((Issue z1, Issue z2) -> {
            if (z1.getPriorityId() < z2.getPriorityId())
                return 1;
            if (z1.getPriorityId() > z2.getPriorityId())
                return -1;
            return 0;
        });
        for (Issue issue : issues) {

            System.out.println(issue.toString()+ issue.getStartDate() + issue.getEstimatedHours() + "-" + issue.getSpentHours());
        }

        return issues;

    }

    private static ClientConnectionManager createConnectionManagerWithOurDevKeystore() throws KeyManagementException, KeyStoreException {
//        final Optional<KeyStore> builtInExtension = getExtensionKeystore();
//        if (builtInExtension.isPresent()) {
//            return RedmineManagerFactory.createConnectionManagerWithExtraTrust(
//                    Collections.singletonList(builtInExtension.get()));
//        }
        return RedmineManagerFactory.createInsecureConnectionManager();
    }

    /**
     * Returns a key store with the additional SSL certificates.
     * this is how we provide the self-signed SSL certificate for our
     * Redmine dev server.
     */
    private static Optional<KeyStore> getExtensionKeystore() {
        final InputStream extStore =
                Redmine.class.getClassLoader().getResourceAsStream("redmine-ssl-cacerts");
        if (extStore == null) {
            return Optional.empty();
        }
        try {
            final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(extStore, "changeit".toCharArray());
            return Optional.of(ks);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        } finally {
            try {
                extStore.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveIssuesInGraph(List<Issue> issues) throws Exception {
        virtuoso.clearDataGraph();
        for (Issue issue: issues){
            virtuoso.saveTripleInGraph("http://example.org/ns#"+issue.getId().toString(), "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://schema.org/ticket");
            if (issue.getStartDate() != null){

                Date date = issue.getStartDate();

                Object dateFormatted = new SimpleDateFormat("yyyy-MM-dd").format(date);

                virtuoso.saveTripleInGraph("http://example.org/ns#"+issue.getId(), "http://schema.org/startDate", dateFormatted.toString());
            }
            if (issue.getEstimatedHours() != null){
                virtuoso.saveLiteralInGraph("http://example.org/ns#"+issue.getId(), "http://schema.org/duration", issue.getEstimatedHours().toString());
            }
        }
    }

    public void saveNewIssueInfoInGraph(String issueId, String category, String value){
        if (category.equals("date")){
            virtuoso.saveTripleInGraph("http://example.org/ns#"+issueId, "http://schema.org/startDate", value);
        }
        else if (category.equals("duration")){
            virtuoso.saveLiteralInGraph("http://example.org/ns#"+issueId, "http://schema.org/duration", value);
        }
    }

    public Issue getIssueById(String id) throws Exception{
        final ClientConnectionManager connectionManager;
        try {
            connectionManager = createConnectionManagerWithOurDevKeystore();
        } catch (Exception e) {
            throw new RuntimeException("cannot create connection manager: " + e, e);
        }

        RedmineConfig redmineConfig = new RedmineConfig();

        HttpClient client = RedmineManagerFactory.getNewHttpClient(redmineConfig.getURI(), connectionManager);

        RedmineManager mgr = RedmineManagerFactory.createWithApiKey(redmineConfig.getURI(), redmineConfig.getApiKey(), client);
        Issue issue = mgr.getIssueManager().getIssueById(Integer.parseInt(id));
        return issue;
    }

    private void createVersion(){
        Version version = VersionFactory.create();
        version.setName("");
    }

}
