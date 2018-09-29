package de.avocado;

import com.google.api.gax.rpc.ApiStreamObserver;
import com.google.cloud.dialogflow.v2.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.Value;
import com.taskadapter.redmineapi.bean.Issue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;


@Service
public class Dialogflow {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Redmine redmine = new Redmine();

    private Input input = new Input();
    private Output output = new Output();

    public void startMainDialog() throws Exception{

//        String text = "2322 zur version hinzufügen";
        String languageCode = "de-DE";
        String projectId = "versionsverwaltung";
        String sessionId = UUID.randomUUID().toString();

        try (SessionsClient sessionsClient = SessionsClient.create()) {
            // Set the session name using the sessionId (UUID) and projectID (my-project-id)
//            SessionName session = SessionName.of(projectId, sessionId);

//            TextInput.Builder textInput = TextInput.newBuilder().setText(text).setLanguageCode(languageCode);

            CountDownLatch countDownLatch = new CountDownLatch(1);

            // Starte Dialog
            while (true) {

                input.getInput(countDownLatch);
                logger.info("getInput Done.");
                countDownLatch.await();
                logger.info("Fertig, da Latch-Freigabe erteilt.");

                URL ressource = Dialogflow.class.getResource("input.wav");
                logger.info("hole input als Ressource.");
                QueryResult queryResult = detectIntentStream(projectId, ressource.getPath(), sessionId, languageCode);
                logger.info("Query Result:" + queryResult.getIntent());
                if (queryResult.hasIntent() ) {
                    Intent intent = queryResult.getIntent();
                    logger.info("Intent:" + intent.getDisplayName());

                    boolean hasPayload = queryResult.getFulfillmentMessages(0).hasPayload();
                    if (hasPayload) {
                        //start Validation
                    String mainIntent = queryResult.getIntent().getDisplayName();
                    Validation validation = new Validation();
                    //Validation of main intent
                    validation.validation(mainIntent);
                    } else {
                        // Ausgabe normaler Intent
                        logger.info("ANTWORT DES DIALOGBOTS: " + queryResult.getFulfillmentText());
                        output.getOutput(queryResult.getFulfillmentText());
                    }
                }
            }
        }
    }

    public void startInnerDialog (String innerIntentPath, String ticketId) throws Exception {
        logger.info("InnererDialog gestartet");
        String projectId = "versionsverwaltung";
        String sessionId = UUID.randomUUID().toString();
        String languageCode = "de-DE";
        Input testput = new Input();

        try (SessionsClient innerClient = SessionsClient.create()) {
            logger.info("try erfolgreich");
            SessionName sessionName = SessionName.of(projectId, sessionId);
            CountDownLatch countDownLatch = new CountDownLatch(1);

            while (true) {
                logger.info("while schleife gestartet");
                TextInput.Builder textInput = TextInput.newBuilder().setText(getUtterance(innerIntentPath) + " " + ticketId).setLanguageCode(languageCode);
                QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();
                DetectIntentResponse response = innerClient.detectIntent(sessionName, queryInput);
                QueryResult queryResult = response.getQueryResult();
                logger.info("FRAGE DES DIALOGBOTS: " + queryResult.getFulfillmentText());
                // get question for inner dialog
                output.getOutput(queryResult.getFulfillmentText());
                logger.info("Output ausgegeben");
                testput.getInput(countDownLatch);
                countDownLatch.await();
                logger.info("Fertig, da Latch-Freigabe erteilt.");

                URL ressource = Dialogflow.class.getResource("input.wav");
                QueryResult inputResult = detectIntentStream(projectId, ressource.getPath(), sessionId, languageCode);

                if (!inputResult.getAction().equals("input.unknown")){
                    // speichern Daten in Datenbank
                    saveParameter(inputResult, ticketId);
                    break;
                }
            }
        }
    }

    public static class DialogAsker {
        private final Scanner scanner;
        private final PrintStream out;

        public DialogAsker(InputStream in, PrintStream out) {
            scanner = new Scanner(in);
            this.out = out;
        }

        public String ask(String message) {
            out.println(message);
            return scanner.next();
        }
    }

    public String getUtterance(String innerIntent){
        if (innerIntent.length()>0){
            String[] split = innerIntent.split(Pattern.quote( "/" ));
            innerIntent = split[3];
            return innerIntent.toLowerCase();
        }else{
            return null;
        }
    }

    public static String getInputFromUser(DialogAsker asker) {
        String input = asker.ask("Give a number between 1 and 10");
//        while (input < 1 || input > 10)
//            input = asker.ask("Wrong number, try again.");
        return input;
    }

    private String getMainintentFromResult (QueryResult queryResult){
        String mainIntent = queryResult.getFulfillmentMessages(0).getPayload().getFieldsMap().get("Message").getStringValue();
        return mainIntent;
    }

    private void saveParameter(QueryResult result, String ticketId) throws Exception{
        if (result.getAction().equals("Datum.Datum-custom")){
            String date = result.getParameters().getFieldsMap().get("date").getStringValue();
            //Entferne Uhrzeit
            String dateWithoutTime = date.substring(0, 9);
            logger.info("Speichere für das Ticket " + ticketId + " das Anfangsdatum " + dateWithoutTime);
            saveDate(ticketId, dateWithoutTime);
            redmine.saveNewIssueInfoInGraph(ticketId, "date", dateWithoutTime);
        }
        else if (result.getAction().equals("Aufwand.Aufwand-custom")){
            Map<String, Value> parameter = result.getParameters().getFieldsMap().get("duration").getStructValue().getFieldsMap();
            Double amount = parameter.get("amount").getNumberValue();
            String unit = parameter.get("unit").getStringValue();
            Double hours = getAmountInHours(amount, unit);
            logger.info("Speichere für das Ticket " + ticketId + " die geschätzte Zeit von " + amount + " " + unit);
            saveDuration(ticketId, hours);
            redmine.saveNewIssueInfoInGraph(ticketId, "duration", hours.toString());
        }
    }

    private Double getAmountInHours(Double amount, String unit)throws Exception{
        if (unit.equals("s")){
            return amount/3600;
        }
        if (unit.equals("min")){
            return amount/60;
        }
        else {
            return amount;
        }
    }

    private void saveDate(String ticketId, String dateString) throws Exception{
        String[] dataSplit = dateString.split("-");
        Date date = new Date(Integer.parseInt(dataSplit[0]), Integer.parseInt(dataSplit[1]), Integer.parseInt(dataSplit[2]));
        Issue issue = redmine.getIssueById(ticketId);

        //für test ausgeklammert

//        issue.setStartDate(date);
    }

    private void saveDuration(String ticketId, Double hours) throws Exception{
        Issue issue = redmine.getIssueById(ticketId);

        //für test ausgeklammert

//        issue.setEstimatedHours(hours.floatValue());
    }

    public QueryResult detectIntentStream(String projectId, String audioFilePath, String sessionId,
                                          String languageCode) throws Exception {
        logger.info("Detect Intent from Stream");
        // Start bi-directional StreamingDetectIntent stream.
        final CountDownLatch notification = new CountDownLatch(1);
        final List<Throwable> responseThrowables = new ArrayList<>();
        final List<StreamingDetectIntentResponse> responses = new ArrayList<>();

        // Instantiates a client
        try (SessionsClient sessionsClient = SessionsClient.create()) {
            // Set the session name using the sessionId (UUID) and projectID (my-project-id)
            SessionName session = SessionName.of(projectId, sessionId);
            System.out.println("Session Path: " + session.toString());

            // Note: hard coding audioEncoding and sampleRateHertz for simplicity.
            // Audio encoding of the audio content sent in the query request.
            AudioEncoding audioEncoding = AudioEncoding.AUDIO_ENCODING_LINEAR_16;
            int sampleRateHertz = 16000;

            // Instructs the speech recognizer how to process the audio content.
            InputAudioConfig inputAudioConfig = InputAudioConfig.newBuilder()
                    .setAudioEncoding(audioEncoding) // audioEncoding = AudioEncoding.AUDIO_ENCODING_LINEAR_16
                    .setLanguageCode(languageCode) // languageCode = "en-US"
                    .setSampleRateHertz(sampleRateHertz) // sampleRateHertz = 16000
                    .build();

            ApiStreamObserver<StreamingDetectIntentResponse> responseObserver =
                    new ApiStreamObserver<StreamingDetectIntentResponse>() {
                        @Override
                        public void onNext(StreamingDetectIntentResponse response) {
                            logger.info("onNext");
                            // Do something when receive a response
                            responses.add(response);
                        }

                        @Override
                        public void onError(Throwable t) {
                            // Add error-handling
                            logger.error("onError", t);
                            responseThrowables.add(t);
                        }

                        @Override
                        public void onCompleted() {
                            // Do something when complete.
                            logger.info("onCompleted");
                            notification.countDown();
                        }
                    };

            // Performs the streaming detect intent callable request
            ApiStreamObserver<StreamingDetectIntentRequest> requestObserver =
                    sessionsClient.streamingDetectIntentCallable().bidiStreamingCall(responseObserver);

            // Build the query with the InputAudioConfig
            QueryInput queryInput = QueryInput.newBuilder().setAudioConfig(inputAudioConfig).build();

            try (FileInputStream audioStream = new FileInputStream(audioFilePath)) {
                // The first request contains the configuration
                StreamingDetectIntentRequest request = StreamingDetectIntentRequest.newBuilder()
                        .setSession(session.toString())
                        .setQueryInput(queryInput)
                        .build();

                // Make the first request
                requestObserver.onNext(request);

                // Following messages: audio chunks. We just read the file in fixed-size chunks. In reality
                // you would split the user input by time.
                byte[] buffer = new byte[4096];
                int bytes;
                while ((bytes = audioStream.read(buffer)) != -1) {
                    requestObserver.onNext(
                            StreamingDetectIntentRequest.newBuilder()
                                    .setInputAudio(ByteString.copyFrom(buffer, 0, bytes))
                                    .build());
                }
            } catch (RuntimeException e) {
                // Cancel stream.
                requestObserver.onError(e);
            }
            // Half-close the stream.
            requestObserver.onCompleted();
            // Wait for the final response (without explicit timeout).
            notification.await();
            // Process errors/responses.
            if (!responseThrowables.isEmpty()) {
                throw new RuntimeException("On Error.");
            }
            if (responses.isEmpty()) {
                throw new RuntimeException("No response from Dialogflow.");
            }

            for (StreamingDetectIntentResponse response : responses) {
                if (response.hasRecognitionResult()) {
                    logger.info(
                            "Intermediate transcript: '%s'\n" + response.getRecognitionResult().getTranscript());
                }
            }

            // Display the last query result
            QueryResult queryResult = responses.get(responses.size() - 1).getQueryResult();
            return queryResult;
        }
    }

}
