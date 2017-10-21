import java.util.Calendar;

public class ResponseDb {

    /**
     * SQLite connection url for the responses DB
     */
    private static final String CONNECTION_URL = "jdbc:sqlite:goldilocks.db";


    public ResponseDb() {

        // Connect to the DB

        // If the events table doesn't already exist, make it

        // If the responses table doesn't already exist, make it
    }


    public boolean addEvent(String name, String organizerPhone, int attendees) {

        // Validate not null and greater than zero

        // Insert a new row into events table

        // If conflict on name, then return false

        // If successful, return true

        return false; // TODO
    }


    public boolean addAttendeeResponse(String phone, AttendeeVote vote, Calendar timestamp) {

        // Validate not null

        // Find which event this phone number is attending

        // If this phone number is not at an event, return false.

        // Insert a row into the responses table

        // Return true on success

        return false; // TODO
    }


    public VoteBreakdown getRecentResponses(Calendar sinceWhen) {

        // Validate not null

        // Query the counts of responses since the given time in the database

        // Get the number of attendees

        // Divide the values and set into a new ResponseBreakdown object

        return null; // TODO
    }
}
