package hashed.app.ampassadors.Objects;

public class ZoomRequest {


    private String topic;
    private String type;
    private String agenda;

    public ZoomRequest(String topic, String type, String agenda) {
        this.topic = topic;
        this.type = type;
        this.agenda = agenda;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAgenda() {
        return agenda;
    }

    public void setAgenda(String agenda) {
        this.agenda = agenda;
    }
}
