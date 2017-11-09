package dashdroid.dashdroidplayer.logic;

public enum RepLevel {
    LOW, MID, HIGH;

    public RepLevel higher() {
        switch(this) {
            case LOW:
                return MID;
            case MID:
                return HIGH;
            default:
                return HIGH;
        }
    }

    public RepLevel lower() {
        switch(this) {
            case HIGH:
                return MID;
            case MID:
                return LOW;
            default:
                return LOW;
        }
    }
}