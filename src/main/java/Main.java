import com.twilio.Twilio;
import com.twilio.twiml.Body;
import com.twilio.twiml.Message;
import com.twilio.twiml.MessagingResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import static spark.Spark.post;

public class Main {

    // TODO switch to env var
    public static final String ACCOUNT_SID = "AC16bbbd6c90c0d42ba29e89bd547fa2ba";
    public static final String AUTH_TOKEN = "364b03ef60906a434112516bd081217d";
    public static final String TWILIO_SMS = System.getenv("TWILIO_SMS");


    public static void main(String[] args) {
        new Main().run();
    }


    public void run() {

        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        post("/receive-sms", (request, response) -> {

            String body = splitQuery(request.body()).get("Body");
            if (body == null) {
                body = "";
            }
            body = body.trim().toLowerCase().replaceAll("\\s+", " ");

            // Get previous state
            ConvoState convoState = ConvoState.NONE;
            String convoStateStr = request.cookie("convoState");
            if (convoStateStr != null) {
                convoState = ConvoState.valueOf(convoStateStr);
            }

            String message = null;
            ConvoState nextState = ConvoState.NONE;

            switch (convoState) {
                default:
                case NONE:
                    message = "Welcome to Goldilocks, the crowdsourced thermostat anyone can use." +
                            " To start an event text your event name and number of attendees. " +
                            "(Example: sdHacks 100 ppl)";
                    nextState = ConvoState.UC1_3;
                    break;

                case UC1_2:
                    message ="Sorry, we don’t recognize your input. Please try again. " +
                            "(Here's another example: bobsHouseParty 50 ppl)";
                    nextState = ConvoState.UC1_3;
                    break;

                case UC1_3:
                    message = "sdHacks has been set up! Have your attendees text “sdHacks” to the number ### " +
                            "to vote their thermostat preference.";
                    break;

                case UC2_1:
                    message = "You are successfully registered at sdHacks!  Text “too hot”, “too cold”, or “just right”" +
                            " to ### to vote on the thermostat preference of your event.";
                    nextState = ConvoState.UC3_1;
                    break;

                case UC2_2:
                    message = "Sorry, we don’t recognize that event name. Please try again.";
                    nextState = ConvoState.UC3_1;
                    break;

                case UC3_1:
                    message = "Your vote has been submitted! Text “too hot”, “too cold”, or “just right”," +
                            " to change your vote anytime.";
                    break;

                case UC3_2:
                    message = "Sorry, we don’t recognize your vote. Please try again.";
                    nextState = ConvoState.UC3_1;
                    break;

                case UC4:
                    message = "##% of sdHack attendees say the temperature is too hot. \n" +
                            "##% of sdHack attendees say the temperature is too cold.\n" +
                            "##% of sdHack attendees say the temperature is just right.\n" +
                            "To end the Goldilocks service, text “stop” at anytime.";
                    nextState = ;
                    break;

                case UC5_1:
                    message = "Are you sure you would like to stop using Goldilocks? (Y/N)";
                    break;

                case UC5_2:
                    message = "Your service has been terminated. Thank you for using Goldilocks!";
                    break;

                case UC5_3:
                    message = "Your service is resumed.";
                    break;

                case UC5_4:
                    message = "Sorry, we don’t recognize that response. Please try again.";
                    break;
            }

            response.cookie("convoState", nextState.name());

            if (message != null) {
                Message sms = new Message.Builder().body(new Body(message)).build();
                MessagingResponse twiml = new MessagingResponse.Builder().message(sms).build();
                return twiml.toXml();

            } else {
                MessagingResponse twiml = new MessagingResponse.Builder().build();
                return twiml.toXml();
            }
        });
    }


    private static Map<String, String> splitQuery(String query) {

        try {
            Map<String, String> query_pairs = new LinkedHashMap<String, String>();
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }
            return query_pairs;

        } catch (UnsupportedEncodingException ex) {
            throw new Error(ex);
        }
    }
}


