package de.avocado;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lexmodelbuilding.AmazonLexModelBuilding;
import com.amazonaws.services.lexmodelbuilding.AmazonLexModelBuildingClientBuilder;
import com.amazonaws.services.lexmodelbuilding.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class AwsLex {

    private Logger logger = LoggerFactory.getLogger(Validation.class);
    private String botName = "IntentBot";
    private String aliasName = "AliasTest";

    public void addInformationIntent(String intentName, String slotType, String plainText) {

        String [] sampleUtterances = new String[]{"Informations Intent " + intentName};
        Message message = new Message().withContentType("PlainText").withContent(plainText);
        Prompt prompt = new Prompt().withMaxAttempts(2)
                .withMessages(message);
        Slot slot = new Slot().withName(intentName)
                .withSlotType(slotType)
                .withSlotTypeVersion("1")
                .withValueElicitationPrompt(prompt)
                .withSlotConstraint("Required");

        PutIntentRequest putIntentRequest = new PutIntentRequest().withCreateVersion(true)
                .withName("Informations_Intent_" + intentName)
                .withSlots(slot)
                .withFulfillmentActivity(new FulfillmentActivity().withType(FulfillmentActivityType.ReturnIntent))
                .withSampleUtterances(sampleUtterances);

        addIntent(putIntentRequest);
    }

    public void addMainIntent(String intentName, String[] sampleUtterances) {

        Message message = new Message().withContentType("CustomPayload").withContent(intentName);

        Statement statement = new Statement().withMessages(message);

        PutIntentRequest putIntentRequest = new PutIntentRequest()
                .withFulfillmentActivity(new FulfillmentActivity().withType(FulfillmentActivityType.ReturnIntent))
                .withName(intentName)
                .withCreateVersion(true)
                .withSampleUtterances(sampleUtterances)
                .withConclusionStatement(statement);

        addIntent(putIntentRequest);
    }

    public void addIntent(PutIntentRequest putIntentRequest) {

        AmazonLexModelBuilding botBuilder = AmazonLexModelBuildingClientBuilder.standard().withRegion(Regions.EU_WEST_1).build();

        botBuilder.putIntent(putIntentRequest);

        GetBotRequest getBotRequest = new GetBotRequest().withName(botName).withVersionOrAlias(aliasName);
        GetBotResult getBotResult = botBuilder.getBot(getBotRequest);

        ArrayList<Intent> intents = new ArrayList<Intent>();
        intents.addAll(getBotResult.getIntents());
        Intent newIntent = new Intent().withIntentName(putIntentRequest.getName()).withIntentVersion("1");
        intents.add(newIntent);

        PutBotRequest putBotRequest = new PutBotRequest().withChildDirected(getBotResult.getChildDirected())
                .withClarificationPrompt(getBotResult.getClarificationPrompt())
                .withDescription(getBotResult.getDescription())
                .withIntents(intents)
                .withName(getBotResult.getName())
                .withChecksum("")
                .withAbortStatement(getBotResult.getAbortStatement())
                .withLocale(getBotResult.getLocale());


        PutBotResult result = botBuilder.putBot(putBotRequest);

        CreateBotVersionRequest createBotVersionRequest = new CreateBotVersionRequest().withChecksum("").withName(getBotResult.getName());
        botBuilder.createBotVersion(createBotVersionRequest);

        PutBotAliasRequest putBotAliasRequest = new PutBotAliasRequest().withBotName(botName)
                .withBotVersion("$LATEST")
                .withName("Alias");
        logger.info(result.getFailureReason());
    }
}
