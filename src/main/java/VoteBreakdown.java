public class VoteBreakdown {

    private final int attendees;
    private final int votes;
    private final double tooCold;
    private final double tooHot;
    private final double justRight;


    public VoteBreakdown(int attendees, int votes, double tooCold, double tooHot, double justRight) {
        this.attendees = attendees;
        this.votes = votes;
        this.tooCold = tooCold;
        this.tooHot = tooHot;
        this.justRight = justRight;
    }


    public int getAttendees() {
        return attendees;
    }


    public int getVotes() {
        return votes;
    }


    public double getTooCold() {
        return tooCold;
    }


    public double getTooHot() {
        return tooHot;
    }


    public double getJustRight() {
        return justRight;
    }
}
