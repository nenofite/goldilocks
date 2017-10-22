import org.sqlite.JDBC;

import java.sql.*;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Objects;
import java.util.logging.Logger;

public class ResponseDb {

    /**
     * SQLite connection url for the responses DB
     */
    private static final String CONNECTION_URL = "jdbc:sqlite:goldilocks.db";


    private Connection connection;


    public ResponseDb() {

        // Connect to the DB
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(CONNECTION_URL);

            // Enable foreign keys (disabled by default)
            connection.createStatement().execute(
                    "pragma foreign_keys = on");

            // If the events table doesn't already exist, make it
            connection.createStatement().execute(
                    "create table if not exists Events ( " +
                            "Name text not null unique, " +
                            "OrganizerPhone text not null, " +
                            "Attendees integer not null " +
                            ")");

            // If the responses table doesn't already exist, make it
            connection.createStatement().execute(
                    "create table if not exists Responses ( " +
                            "Event text not null, " +
                            "AttendeePhone text not null, " +
                            "Vote integer not null, " +
                            "Timestamp text not null, " +
                            "foreign key (Event) references Events (Name) " +
                            ")");

            // If the attendees table doesn't already exist, make it
            connection.createStatement().execute(
                    "create table if not exists Attendees ( " +
                            "AttendeePhone text not null unique, " +
                            "Event text not null, " +
                            "foreign key (Event) references Events (Name) " +
                            ")");

        } catch (SQLException | ClassNotFoundException ex) {
            throw new Error("Could not connect to SQLite", ex);
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
            ex.printStackTrace();
            // If conflict on name, then return false
            return false;
        }

        // If successful, return true
        return true;
    }


    public boolean addAttendeeToEvent(String attendeePhone, String eventName) {

        // Validate not null
        Objects.requireNonNull(attendeePhone);
        Objects.requireNonNull(eventName);

        // Insert or replace a row
        try {
            PreparedStatement stmt = connection.prepareStatement("insert or replace into Attendees (AttendeePhone, Event) values (?, ?);");
            stmt.setString(1, attendeePhone);
            stmt.setString(2, eventName);
            stmt.execute();

        } catch (SQLException ex) {
            ex.printStackTrace();
            // If the event doesn't exist, return false
            return false;
        }

        return true;
    }


    public boolean addAttendeeResponse(String phone, AttendeeVote vote, Calendar timestamp) {

        // Validate not null
        Objects.requireNonNull(phone);
        Objects.requireNonNull(vote);
        Objects.requireNonNull(timestamp);

        try {
            // Find which event this phone number is attending
            PreparedStatement stmt = connection.prepareStatement(
                    "select (Event) from Attendees " +
                            "where AttendeePhone = ? ");
            stmt.setString(1, phone);
            ResultSet results = stmt.executeQuery();

            // If this phone number is not at an event, return false.
            if (!results.next()) {
                // The attendee is not part of an event yet
                return false;
            }

            String eventName = results.getString(1);

            // Insert a row into the responses table
            PreparedStatement responseStmt = connection.prepareStatement(
                    "insert into Responses (Event, AttendeePhone, Vote, Timestamp) values (?,?,?,?);");
            responseStmt.setString(1, eventName);
            responseStmt.setString(2, phone);
            responseStmt.setString(3, vote.name());
            responseStmt.setString(4, timestamp.toInstant().toString());
            responseStmt.execute();

            // Return true on success
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }


    public VoteBreakdown getRecentResponses(String eventName, Calendar sinceWhen) {

        // Validate not null
        Objects.requireNonNull(eventName);
        Objects.requireNonNull(sinceWhen);

        try {
            // Query the counts of responses since the given time in the database
            PreparedStatement responsesStmt = connection.prepareStatement(
                    "select (AttendeePhone, Vote) from Responses where " +
                            "Event = ? and " +
                            "Timestamp > ? " +
                            "order by Timestamp desc");
            responsesStmt.setString(1, eventName);
            responsesStmt.setString(2, sinceWhen.toInstant().toString());

            // Get a list of all votes
            ResultSet votes = responsesStmt.executeQuery();

            // Tally up the votes, ignoring multiple votes from the same phone
            int tooColds = 0;
            int justRights = 0;
            int tooHots = 0;

            HashSet<String> alreadyVoted = new HashSet<>();
            while (votes.next()) {

                String phone = votes.getString(1);
                AttendeeVote vote = AttendeeVote.valueOf(votes.getString(2));

                if (!alreadyVoted.contains(phone)) {
                    alreadyVoted.add(phone);
                    switch (vote) {
                        case TOO_COLD:
                            ++tooColds;
                            break;
                        case JUST_RIGHT:
                            ++justRights;
                            break;
                        case TOO_HOT:
                            ++tooHots;
                            break;
                    }
                }
            }

            // Get the number of attendees
            PreparedStatement attendeesStmt = connection.prepareStatement(
                    "select (Attendees) from Events where " +
                            "Name = ?");
            attendeesStmt.setString(1, eventName);
            ResultSet attendees = attendeesStmt.executeQuery();
            if (!attendees.next()) {
                return null;
            }
            int attendeesCount = attendees.getInt(1);

            int totalVotes = tooColds + tooHots + justRights;

            // Divide the values and set into a new ResponseBreakdown object
            return new VoteBreakdown(attendeesCount, totalVotes, tooColds, tooHots, justRights);

        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
