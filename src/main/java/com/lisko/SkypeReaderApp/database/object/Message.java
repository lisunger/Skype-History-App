package com.lisko.SkypeReaderApp.database.object;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity(name = "message")
public class Message {

    @Id
    private MessagePk pk;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "original_arrival_time")
    private Date originalArrivalTime;

    @Column(name = "message_type")
    private String messageType;

    @Column(name = "version")
    private String version;

    @Column(name = "content")
    private String content;

    @Column(name = "sender")
    private String sender;

    @Column(name = "properties")
    private String properties;

    @Column(name = "ams_references")
    private String amsReferences;

    public Message() {
        super();
        this.pk = new MessagePk();
    }

    public MessagePk getPk() {
        return pk;
    }

    public void setPk(MessagePk pk) {
        this.pk = pk;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Date getOriginalArrivalTime() {
        return originalArrivalTime;
    }

    public void setOriginalArrivalTime(Date originalArrivalTime) {
        this.originalArrivalTime = originalArrivalTime;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public String getAmsReferences() {
        return amsReferences;
    }

    public void setAmsReferences(String amsReferences) {
        this.amsReferences = amsReferences;
    }

    @Override
    public String toString() {
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String dateString = null;
        if(this.originalArrivalTime != null) {
            dateString = s.format(this.originalArrivalTime);
        }

        StringBuilder sb = new StringBuilder();

        sb.append("M: { skypeId: ").append(this.pk.getId());
        sb.append(" , conversationId: ").append(this.pk.getConversationId());
        sb.append(" , displayName: ").append(this.displayName);
        sb.append(" , originalarrivaltime: ").append(dateString);
        sb.append(" , messagetype: ").append(this.messageType);
        sb.append(" , version: ").append(this.version);
        sb.append(" , content: ").append("\"").append(this.content).append("\"");
        sb.append(" , sender: ").append(this.sender);
        sb.append(" , properties: ").append(this.properties);
        sb.append(" , amsreferences: ").append(this.amsReferences);
        sb.append(" }");

        return sb.toString();
    }
}
