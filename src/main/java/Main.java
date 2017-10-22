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
            body = body.trim().toLowerCase();

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
                    message = "Hello welcome blah blah";
                    nextState = ConvoState.UC1_2;
                    break;

                case UC1_1:
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


