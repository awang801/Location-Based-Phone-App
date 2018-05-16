package example.team5.samplelocation.SQLite;

public class Group {

    private String name;
    private int duration;
    private int period;
    private int stop;
    private int start;
    private int conf;
    private String lng;
    private String lat;
    private int rad;


    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public int getRad() {
        return rad;
    }

    public void setRad(int rad) {
        this.rad = rad;
    }

    public int getConf() { return conf; }

    public void setConf(int conf) { this.conf = conf; }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStop() {
        return stop;
    }

    public void setStop(int take) { this.stop = take; }
}
