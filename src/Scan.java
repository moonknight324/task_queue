public class Scan {
    private final int id;
    private final String name;
    private final int duration;
    private final boolean pause;
    private ScanState state = ScanState.IDLE;

    public Scan(int id, String name, int duration, boolean pause) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.pause = pause;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public boolean isPause() {
        return pause;
    }

    public ScanState getState() {
        return state;
    }

    public void setState(ScanState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return String.format("Scan:%d, %s, %d, %s [%s]",
                id, name, duration, pause ? "Yes" : "No", state);
    }
}
