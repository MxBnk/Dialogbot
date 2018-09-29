package de.avocado;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.graph.GraphFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import java.util.Map;
import java.util.regex.Pattern;

@Service
public class Virtuoso {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String url  = "jdbc:virtuoso://localhost:1111";
    private VirtGraph dataGraph = new VirtGraph("Example4", url, "dba", "dba");
    private VirtGraph shaclGraph = new VirtGraph("ShaclGraph", url, "dba", "dba");

    private String uri = "http://example.org/ns#";

    public void saveTripleInGraph(String s, String p, String o){
        dataGraph.add(new Triple(Node.createURI(s), Node.createURI(p) , Node.createURI(o)));
    }

    public void saveLiteralInGraph(String s, String p, String o){
        dataGraph.add(new Triple(Node.createURI(s), Node.createURI(p) , Node.createLiteral(o)));
    }

    public void deleteTripleInGraph(String s, String p, String o){
        if (o.length()<1)
            dataGraph.performDelete(new Triple(Node.createURI(s), Node.createURI(p) , Node.ANY));
        else
            dataGraph.performDelete(new Triple(Node.createURI(s), Node.createURI(p) , Node.createLiteral(o)));
    }

    public void clearDataGraph(){
        this.dataGraph.clear();
    }

    private Node createSubjectNode(String identifier){
        if (identifier.equals(""))
            return Node.ANY;
        else {
            Node node = Node.createURI(uri + identifier.replaceAll(" ", "_"));
            return node;
        }
    }

    public Model getGraphAsModel(Map <String, String> map, VirtGraph graph) {
        Graph graph1 = GraphFactory.createDefaultGraph();
        Model dataModel = ModelFactory.createDefaultModel();

        Query sparql = QueryFactory.create("SELECT ?s ?p ?o WHERE { ?s ?p ?o }");
        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql, graph);
        ResultSet results = vqe.execSelect();

        graph1.getPrefixMapping().setNsPrefixes(map);

        while (results.hasNext()) {
            QuerySolution result = results.nextSolution();
            RDFNode graph_name = result.get("graph");
            RDFNode su = result.get("s");
            RDFNode pr = result.get("p");
            RDFNode ob = result.get("o");
            logger.info("VirtuosoConvert " + graph_name + " { " + su + " " + pr + " " + ob + " . }");

            org.apache.jena.rdf.model.Resource subj;
            if (su.asNode().isBlank()) {
                subj = dataModel.createResource(su.toString());
            } else {
                subj = dataModel.createResource(su.asNode().getURI());
            }

            org.apache.jena.rdf.model.Property pred = dataModel.createProperty(pr.asNode().getURI());

            org.apache.jena.rdf.model.RDFNode obj;
            if (ob.asNode().isBlank()) {
                obj = dataModel.createResource(ob.toString());
            } else if (ob.asNode().isLiteral()) {
                if (ob.asNode().getLiteral().getDatatypeURI() != null ) {
                    obj = dataModel.createTypedLiteral(ob.asNode().getLiteral().getLexicalForm(), ob.asNode().getLiteral().getDatatypeURI());
                } else {
                    obj = dataModel.createLiteral(ob.asNode().getLiteral().toString());
                }
            } else {
                obj = dataModel.createResource(ob.asNode().getURI());
            }

            dataModel.add(dataModel.createStatement(subj, pred, obj));
        }

        return dataModel;
    }
    public String predicateSuffix(String innerIntent){
        if (innerIntent.length()>0){
            String[] split = innerIntent.split(Pattern.quote( "/" ));
            innerIntent = split[split.length-1];
            return innerIntent.toLowerCase();
        }else{
            return null;
        }
    }

    public static Object getKeyFromValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o.getClass();
            }
        }
        return null;
    }

    public VirtGraph getDataGraph(){
        return dataGraph;
    }
}