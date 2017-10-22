import com.twilio.Twilio;
import com.twilio.twiml.Body;
import com.twilio.twiml.Message;
import com.twilio.twiml.MessagingResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static spark.Spark.post;

public class Main {

    // TODO switch to env var
    public static final String ACCOUNT_SID = "AC16bbbd6c90c0d42ba29e89bd547fa2ba";
    public static final String AUTH_TOKEN = "364b03ef60906a434112516bd081217d";
    public static final String TWILIO_SMS = System.getenv("TWILIO_SMS");


    private static final Pattern EVENT_SETUP_PATTERN = Pattern.compile("(.+?)(\\d+) ppl$");


    public static void main(String[] args) {
        new Main().run();
    }


    public void run() {

        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        ResponseDb db = new ResponseDb();

        post("/receive-sms", (request, response) -> {

            Map<String, String> bodyParams = splitQuery(request.body());

            String body = bodyParams.get("Body");
            if (body == null) {
                body = "";
            }
            body = body.trim().toLowerCase().replaceAll("\\s+", " ");

            String fromPhone = bodyParams.get("From");

            // Get previous state
            ConvoState convoState = ConvoState.NONE;
            String convoStateStr = request.cookie("convoState");
            if (convoStateStr != null) {
                convoState = ConvoState.valueOf(convoStateStr);
            }

            String message = null;
            ConvoState nextState = convoState;

            switch (convoState) {

                default:
                case NONE:
                    if (body.isEmpty()) {
                        message = "Welcome to Goldilocks, the crowdsourced thermostat anyone can use." +
                                " To start an event text your event name and number of attendees. " +
                                "(Example: SD Hacks 100 ppl)";
                        nextState = ConvoState.UC1_1;

                    } else {
                        // Try to find this event and add this number
                        boolean success = db.addAttendeeToEvent(fromPhone, body);
                        if (success) {
                            message = String.format(
                                    "You are successfully registered at %s! Text “too hot”, “too cold”, or “just right” to %s to vote.",
                                    body, TWILIO_SMS);
                            nextState = ConvoState.UC2_1;

                        } else {
                            message = "Sorry, we don’t recognize that event name. Please try again.";
                            nextState = ConvoState.NONE;
                        }
                    }
                    break;

                case UC1_1:

                    Matcher matcher = EVENT_SETUP_PATTERN.matcher(body);

                    if (matcher.matches()) {
                        String eventName = matcher.group(1).trim();
                        int attendees = Integer.parseInt(matcher.group(2));

                        boolean success = db.addEvent(eventName, fromPhone, attendees);

                        if (success) {
                            message = String.format(
                                    "%s has been set up! Have your attendees text “%s” to the number %s to vote their thermostat preference.",
                                    eventName, eventName, TWILIO_SMS);
                            nextState = ConvoState.UC1_2;

                        } else {
                            message = String.format(
                                    "Sorry! An event called %s already exists. Please try a different name.",
                                    eventName);
                            nextState = ConvoState.UC1_1;
                        }

                    } else {
                        message = "Sorry, we don’t recognize your input. Please try again. (Here's another example: bobsHouseParty 50 ppl)";
                        nextState = ConvoState.UC1_1;
                    }
                    break;

                case UC1_2:
                    if ("stop".equals(body)) {
                        message = "Are you sure you would like to stop using Goldilocks? (Y/N)";
                        nextState = ConvoState.UC5_1;

                    } else {
                        message = "Sorry, we don’t recognize that response.";
                    }
                    break;

                case UC2_1:
                    AttendeeVote vote = null;
                    switch (body) {
                        case "too cold":
                        case "cold":
                            vote = AttendeeVote.TOO_COLD;
                            break;
                        case "just right":
                            vote = AttendeeVote.JUST_RIGHT;
                            break;
                        case "too hot":
                        case "hot":
                            vote = AttendeeVote.TOO_HOT;
                            break;
                    }
                    if (vote != null) {
                        // Add the vote
                        db.addAttendeeResponse(fromPhone, vote, Calendar.getInstance());
                        message = "Your vote has been submitted! Text “too hot”, “too cold”, or “just right”, to change your vote anytime.";
                        nextState = ConvoState.UC2_1;

                    } else {
                        message = "Sorry, we don’t recognize your vote. Please try again.";
                        nextState = ConvoState.UC2_1;
                    }
                    break;

                case UC5_1:
                    if ("y".equals(body) || "yes".equals(body)) {
                        message = "Your service has been terminated. Thank you for using Goldilocks!";
                        // TODO end event
                        nextState = ConvoState.NONE;

                    } else {
                        message = "Your service is resumed.";
                        nextState = ConvoState.UC1_2;
                    }
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


