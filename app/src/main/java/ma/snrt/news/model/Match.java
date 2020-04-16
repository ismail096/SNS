package ma.snrt.news.model;

public class Match {
    private static String TEAM_IMG_BASE = "https://apibotola.snrt.ma/Files/mediumicons/flags/";

    private int id;
    private int play_time;
    private int live_id;
    private int dom_id;
    private int extr_id;
    private String dom;
    private String extr;
    private int s_dom;
    private int s_extr;
    private String status;
    private String time;
    private String domImage;
    private String extrImage;

    public Match() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPlay_time() {
        return play_time;
    }

    public void setPlay_time(int play_time) {
        this.play_time = play_time;
    }

    public int getLive_id() {
        return live_id;
    }

    public void setLive_id(int live_id) {
        this.live_id = live_id;
    }

    public int getDom_id() {
        return dom_id;
    }

    public void setDom_id(int dom_id) {
        this.dom_id = dom_id;
    }

    public int getExtr_id() {
        return extr_id;
    }

    public void setExtr_id(int extr_id) {
        this.extr_id = extr_id;
    }

    public String getDom() {
        return dom;
    }

    public void setDom(String dom) {
        this.dom = dom;
    }

    public String getExtr() {
        return extr;
    }

    public void setExtr(String extr) {
        this.extr = extr;
    }

    public int getS_dom() {
        return s_dom;
    }

    public void setS_dom(int s_dom) {
        this.s_dom = s_dom;
    }

    public int getS_extr() {
        return s_extr;
    }

    public void setS_extr(int s_extr) {
        this.s_extr = s_extr;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDomImage() {
        return TEAM_IMG_BASE + this.dom_id + ".png";
    }

    public void setDomImage(String domImage) {
        this.domImage = domImage;
    }

    public String getExtrImage() {
        return TEAM_IMG_BASE + this.extr_id + ".png";
    }

    public void setExtrImage(String extrImage) {
        this.extrImage = extrImage;
    }
}
