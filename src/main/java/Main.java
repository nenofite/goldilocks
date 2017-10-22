package com.twilio;

import static spark.Spark.post;

import com.twilio.twiml.Body;
import com.twilio.twiml.Message;
import com.twilio.twiml.MessagingResponse;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SendSms{

    public static final String ACCOUNT_SID = "AC16bbbd6c90c0d42ba29e89bd547fa2ba";
    public static final String AUTH_TOKEN = "364b03ef60906a434112516bd081217d";
    public static final String TWILIO_SMS = System.getenv("TWILIO_SMS");
    public ConvoState convoState;

    public static void convoFlow(){
        switch( convoState ) {
            case UC1_1:
                post("/receive-sms", (req, res) -> {
                    Message sms = new Message.Builder()
                            .body(new Body("Welcome to Goldilocks, the crowdsourced thermostat anyone can use. " +
                                    "To start an event text your event name and number of attendees " +
                                    "(Example: sdHacks 100 ppl)"))
                            .build();
                    MessagingResponse twiml = new MessagingResponse.Builder()
                            .message(sms)
                            .build()
                });
                break;
            case UC1_2:
                post("/receive-sms", (req, res) -> {
                    Message sms = new Message.Builder()
                            .body(new Body("Sorry, we don’t recognize your input. Please try again. " +
                                    "(Here is another example: bobsHouseParty 50 ppl)"))
                            .build();
                    MessagingResponse twiml = new MessagingResponse.Builder()
                            .message(sms)
                            .build()
                });
                break;
            case UC1_3:
                post("/receive-sms", (req, res) -> {
                    Message sms = new Message.Builder()
                            .body(new Body("sdHacks has been set up! Have your attendees text “sdHacks” " +
                                    "to the number " + TWILIO_SMS + "to vote their thermostat preference."))
                            .build();
                    MessagingResponse twiml = new MessagingResponse.Builder()
                            .message(sms)
                            .build()
                });
                break;
            case UC2_1:
                post("/receive-sms", (req, res) -> {
                    Message sms = new Message.Builder()
                            .body(new Body("You are successfully registered at sdHacks! " +
                                    " Text “too hot”, “too cold”, or “just right” to vote on the" +
                                    " thermostat preference of your event."))
                            .build();
                    MessagingResponse twiml = new MessagingResponse.Builder()
                            .message(sms)
                            .build()
                });
                break;
            case UC2_2:
                post("/receive-sms", (req, res) -> {
                    Message sms = new Message.Builder()
                            .body(new Body("Sorry, we don’t recognize that event name. Please try again."))
                            .build();
                    MessagingResponse twiml = new MessagingResponse.Builder()
                            .message(sms)
                            .build()
                });
                break;
            case UC3_1:
                post("/receive-sms", (req, res) -> {
                    Message sms = new Message.Builder()
                            .body(new Body("Your vote has been submitted! Text “too hot”, “too cold”, or “just right”" +
                                    " to change your vote anytime. \n"))
                            .build();
                    MessagingResponse twiml = new MessagingResponse.Builder()
                            .message(sms)
                            .build()
                });
                break;
            case UC3_2:
                post("/receive-sms", (req, res) -> {
                    Message sms = new Message.Builder()
                            .body(new Body("Sorry, we don’t recognize your vote. Please try again."))
                            .build();
                    MessagingResponse twiml = new MessagingResponse.Builder()
                            .message(sms)
                            .build()
                });
                break;
            case UC4:
                post("/receive-sms", (req, res) -> {
                    Message sms = new Message.Builder()
                            .body(new Body("##% of sdHack attendees say the temperature is too hot. \n" +
                                    "##% of sdHack attendees say the temperature is too cold.\n" +
                                    "##% of sdHack attendees say the temperature is just right.\n" +
                                    "To end the Goldilocks service, text “stop” at anytime. \n"))
                            .build();
                    MessagingResponse twiml = new MessagingResponse.Builder()
                            .message(sms)
                            .build()
                });
                break;
            case UC5_1:
                post("/receive-sms", (req, res) -> {
                    Message sms = new Message.Builder()
                            .body(new Body("Are you sure you would like to stop using Goldilocks? (Y/N)"))
                            .build();
                    MessagingResponse twiml = new MessagingResponse.Builder()
                            .message(sms)
                            .build()
                });
                break;
            case UC5_2:
                post("/receive-sms", (req, res) -> {
                    Message sms = new Message.Builder()
                            .body(new Body("Your service has been terminated. Thank you for using Goldilocks!"))
                            .build();
                    MessagingResponse twiml = new MessagingResponse.Builder()
                            .message(sms)
                            .build()
                });
                break;
            case UC5_3:
                post("/receive-sms", (req, res) -> {
                    Message sms = new Message.Builder()
                            .body(new Body("Your service is resumed."))
                            .build();
                    MessagingResponse twiml = new MessagingResponse.Builder()
                            .message(sms)
                            .build()
                });
                break;
            case UC5_4:
                post("/receive-sms", (req, res) -> {
                    Message sms = new Message.Builder()
                            .body(new Body("Sorry, we don’t recognize that response. Please try again."))
                            .build();
                    MessagingResponse twiml = new MessagingResponse.Builder()
                            .message(sms)
                            .build()
                });
                break;
        }
    }

    public static void main(String[] args){
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        //the number the system is receiving messages from
        String fromNumber = request.getParameter("From");

        //HashMap to keep track of the state each attendee is in
        //first String = attendee's number(fromNumber);  second String = attendee's state in conversation
        HashMap<String, ConvoState> attendeeState = new HashMap<String, ConvoState>();

        //if new attendee, create a new element and insert into HashMap
        if (attendeeState.containsKey(fromNumber) == false){
            attendeeState.put( fromNumber, UC1_1);
        }

        //pre-existing attendee
        else if (attendeeState.containsKey(fromNumber) == true) {
            //conversation - switch statement
        }
        Message message = Message.creator(
                new PhoneNumber(System.getenv("MY_PHONE_NUMBER")),
                new PhoneNumber("+14159662769"),
                "Welcome to Goldilocks, the crowdsourced thermostat anyone can use. If you would like to organize an " +
                        "event, please text your event name, an estimate amount of people at your event, and how you want to be notified of your" +
                        "attendees responses. Thank you!";
        ).create();


    }
}


