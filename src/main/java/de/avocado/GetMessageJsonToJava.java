package de.avocado;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class GetMessageJsonToJava {

    private String plaintext;
    private String custompayload;

    public GetMessageJsonToJava(String s){
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(s);
            JSONObject jsonObject = (JSONObject)obj;
            JSONArray array = (JSONArray) jsonObject.get("messages");

            Object objPlainText = parser.parse(array.get(0).toString());
            JSONObject jsonObjectPlainText = (JSONObject)objPlainText;
            this.plaintext=jsonObjectPlainText.get("value").toString();

            if (array.size() > 0){
                Object objCustomPayload = parser.parse(array.get(1).toString());
                JSONObject jsonObjectCustomPayload = (JSONObject)objCustomPayload;
                this.custompayload=jsonObjectCustomPayload.get("value").toString();
            }
        }catch (ParseException pe){
            // JSON Validierung?!
            plaintext = s;
//            System.out.println("position: " + pe.getPosition());
//            System.out.print(pe);
        }
    }

    public String getCustompayload() {
        return custompayload;
    }

    public String getPlaintext() { return plaintext; }
}
