package de.avocado.service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lexruntime.AmazonLexRuntime;
import com.amazonaws.services.lexruntime.AmazonLexRuntimeClientBuilder;
import com.amazonaws.services.lexruntime.model.PostTextRequest;
import com.amazonaws.services.lexruntime.model.PostTextResult;
import de.avocado.GetMessageJsonToJava;
import de.avocado.Validation;
import de.avocado.Virtuoso;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.regex.Pattern;

import static org.apache.http.util.TextUtils.isEmpty;

@Service
public class Dialog {

    private String BotName = "TestBot";
    private String BotAlias = "TestBot";
    private String UserId = "testuser";

    public void startMainDialog(){

        AmazonLexRuntime client = AmazonLexRuntimeClientBuilder.standard().withRegion(Regions.EU_WEST_1).build();

        PostTextRequest textRequest = new PostTextRequest();

        textRequest.setBotName(BotName);
        textRequest.setBotAlias(BotAlias);
        textRequest.setUserId(UserId);

        Scanner scanner = new Scanner(System.in);


        // Starte Dialog
        while (true) {

            String requestText = scanner.nextLine().trim();
            if (isEmpty(requestText))
                break;

            textRequest.setInputText(requestText);
            PostTextResult textResult = client.postText(textRequest);
            GetMessageJsonToJava message = new GetMessageJsonToJava(textResult.getMessage());

            if (textResult.getDialogState().startsWith("Elicit")) {
                System.out.println("äußerer Dialog: " + message.getPlaintext());
            } else if (textResult.getDialogState().equals("ReadyForFulfillment"))
                System.out.println("äußerer Dialog: " + message.getPlaintext());
            else {
                System.out.println("äußerer Dialog: " + message.getPlaintext());
                System.out.println("äußerer Dialog Intent Name: " + textResult.getIntentName());

                if(message.getCustompayload() != null){
                    Validation validation = new Validation();
//                    validation.validation(textResult.getIntentName());
                }
            }
        }
    }

    public void startInnerDialog(String innerIntentPath, String mainIntent, DialogAsker dialogAsker){

        while (true) {
            Scanner scanner = new Scanner(System.in);
            // starte inneren Dialog: new innnerDialog(getNextIntent(message.getCustompayload()))
            AmazonLexRuntime innerClient = AmazonLexRuntimeClientBuilder.standard().withRegion(Regions.EU_WEST_1).build();

            PostTextRequest innerTextRequest = new PostTextRequest();

            innerTextRequest.setBotName(BotName);
            innerTextRequest.setBotAlias(BotAlias);
            innerTextRequest.setUserId(UserId);

            // nächster Intent aus Custom Markup
            // poste Intent an Client
            innerTextRequest.setInputText(getUtterance(innerIntentPath));
            PostTextResult innerTextResult = innerClient.postText(innerTextRequest);
            // Antwort des Clienten, die Frage nach Slot enthält
            GetMessageJsonToJava innerMessage = new GetMessageJsonToJava(innerTextResult.getMessage());
            System.out.println("innerer Dialog: " + innerMessage.getPlaintext());
            System.out.println("innere Eingabe:");
            // Antwort auf Frage nach Slot
            String requestText = dialogAsker.ask("requestText");
            // Antwort posten
            innerTextRequest.setInputText(requestText);
            innerTextResult = innerClient.postText(innerTextRequest);
            System.out.println("innerer Dialog: " + innerTextResult.getDialogState());
            if (innerTextResult.getDialogState().equals("ReadyForFulfillment")){
                Virtuoso virtuoso = new Virtuoso();
                virtuoso.saveTripleInGraph(mainIntent, innerIntentPath, requestText);
                Validation validation = new Validation();
//                validation.validation(mainIntent, dialogAsker);
                break;
            }
//            else {
//                "Für den Intent kann flogendes gesagt werden..."
//            }
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

    private String getUtterance(String innerIntent){
        if (innerIntent.length()>0){
            String[] split = innerIntent.split(Pattern.quote( "/" ));
            innerIntent = "Informations Intent " + split[3];
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
}
