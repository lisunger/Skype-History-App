package com.lisko.SkypeReaderApp.database.object;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.ForeignKey;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MessagePk implements Serializable {

    @Column(name = "id")
    private String id;

    @Column(name="conversation_id")
    private String conversationId;

    public MessagePk() { }

    public MessagePk(String id, String conversationId) {
        this.id = id;
        this.conversationId = conversationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessagePk messagePk = (MessagePk) o;
        return this.id.equals(messagePk.id) 
        		&& this.conversationId.equals(messagePk.conversationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, conversationId);
    }

    @Override
    public String toString() {
        return this.id + ", " + this.conversationId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
