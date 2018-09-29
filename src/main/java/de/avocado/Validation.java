package de.avocado;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.taskadapter.redmineapi.bean.Issue;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.topbraid.shacl.model.SHResult;
import org.topbraid.shacl.util.ModelPrinter;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.spin.util.JenaUtil;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import java.util.List;

@Service
public class Validation {

    private Virtuoso virtuoso = new Virtuoso();
    private Redmine redmine = new Redmine();
    private Dialogflow dialogflow = new Dialogflow();
    private Logger logger = LoggerFactory.getLogger(Validation.class);

    public void validation(String mainIntent)throws Exception{

        // Update tickets
        List<Issue> issues = redmine.getIssues();
        redmine.saveIssuesInGraph(issues);

        //Start SHACL Validation

        while(true){

            // load Data Model

            Model shapesModel = JenaUtil.createMemoryModel();
            String shape = getShapeOfMainIntent(mainIntent);
            shapesModel.read(Application.class.getResourceAsStream(shape), "urn:dummy", FileUtils.langN3);

            Model dataModel = virtuoso.getGraphAsModel(shapesModel.getNsPrefixMap(), virtuoso.getDataGraph());

            // Perform the validation of everything, using the data model
            // also as the shapes model - you may have them separated

            Resource report = ValidationUtil.validateModel(dataModel, shapesModel, true);

            // Print violations

            logger.info(ModelPrinter.get().print(report.getModel()));

            List<SHResult> resultList = SHACLUtil.getAllTopLevelResults(report.getModel());

            // if validation has at least one result, start inner dialog

            if (resultList.size()>0) {
                logger.info("Ticket mit fehlender Information: " + resultList.get(0).getFocusNode() + "; Fehlende Info: " + resultList.get(0).getPath().toString());
                String path = resultList.get(0).getPath().toString();
                logger.info("path geholt");

                String focusNodeUrl = resultList.get(0).getFocusNode().toString();
                logger.info("hole Ticket von Redmine");
                String ticketId = getIdOfFocusTicket(focusNodeUrl);
                logger.info("Starte inneren Dialog");
                dialogflow.startInnerDialog(path, ticketId);
            } else {

             // If no results are shown, return to main dialog
                logger.info("Validierung liefert keine weiteren Resultate");
                Output output = new Output();
                output.getOutput("Alle Informationen wurden erfolgreich ergänzt und der innere Dialog wird geschlossen.");
                break;
            }
        }

    }

    private void validateResult (SHResult shResult){
        if (shResult.getMessage().startsWith("Value")|| shResult.getMessage().startsWith("More")){
            Query sparql = QueryFactory.create("DELETE WHERE { <" + shResult.getFocusNode().toString() + "> <" + shResult.getPath().toString() + "> ?o }");
            VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql, virtuoso.getDataGraph());
        }
    }

    private String getIdOfFocusTicket (String focusNode){
        String [] divided = focusNode.split("#");
        return divided[1];
    }

    private String getShapeOfMainIntent (String intent){
        return "/ticketShape.ttl";
    }


}