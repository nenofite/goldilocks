import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Objects;

public class ResponseDb {

    /**
     * SQLite connection url for the responses DB
     */
    private static final String CONNECTION_URL = "jdbc:sqlite:goldilocks.db";


    private Connection connection;


    public ResponseDb() {

        // Connect to the DB
        try {
            connection = DriverManager.getConnection(CONNECTION_URL);

            // If the events table doesn't already exist, make it
            connection.createStatement().execute(
                    "create table if not exists Events ( " +
                            "Name text not null unique, " +
                            "OrganizerPhone text not null, " +
                            "Attendees integer not null, " +
                            ");");

            // If the responses table doesn't already exist, make it
            connection.createStatement().execute(
                    "create table if not exists Responses ( " +
                            "Event integer not null, " +
                            "AttendeePhone text not null, " +
                            "Vote integer not null, " +
                            "Timestamp text not null, " +
                            "foreign key (Event) references Events (rowid) " +
                            ");");

        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Error("Could not connect to SQLite");
        }
    }


    public boolean addEvent(String name, String organizerPhone, int attendees) {

        // Validate not null and greater than zero
        Objects.requireNonNull(name);
        Objects.requireNonNull(organizerPhone);
        if (attendees <= 0) {
            throw new IllegalArgumentException("Attendees must be positive");
        }

        // Insert a new row into events table
        try {
            PreparedStatement stmt = connection.prepareStatement("insert or fail into Events (Name, OrganizerPhone, Attendees) values (?, ?, ?);");
            stmt.setString(1, name);
            stmt.setString(2, organizerPhone);
            stmt.setInt(3, attendees);
            stmt.execute();

        } catch (SQLException ex) {
            // If conflict on name, then return false
            return false;
        }

        // If successful, return true
        return true;
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
