package com.lisko.SkypeReaderApp.database.object;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "conversation")
public class Conversation {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "version")
    private String version;

    @Column(name = "thread_properties")
    private String threadProperties;

    @Column(name = "properties")
    private String properties;

    public Conversation() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getThreadProperties() {
        return threadProperties;
    }

    public void setThreadProperties(String threadProperties) {
        this.threadProperties = threadProperties;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("C: { id: ").append(this.id);
        sb.append(" , skypeId: ").append(this.id);
        sb.append(" , displayName: ").append(this.displayName);
        sb.append(" , version: ").append(this.version);
        sb.append(" , properties: ").append(this.properties);
        sb.append(" , threadProperties: ").append(this.threadProperties);
        sb.append(" }");

        return sb.toString();
    }
}
