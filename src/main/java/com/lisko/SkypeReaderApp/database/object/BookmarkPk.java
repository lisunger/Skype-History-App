package com.lisko.SkypeReaderApp.database.object;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class BookmarkPk implements Serializable {

	@Column(name = "message_id")
	private String messageId;
	
	@Column(name = "conversation_id")
	private String conversationId;
	
	public BookmarkPk () { }

	public BookmarkPk(String messageId, String conversationId) {
		super();
		this.messageId = messageId;
		this.conversationId = conversationId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(messageId, conversationId);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookmarkPk bookmarkPk = (BookmarkPk) o;
        return this.messageId.equals(bookmarkPk.messageId) 
        		&& this.conversationId.equals(bookmarkPk.conversationId);
	}

	@Override
	public String toString() {
		return this.messageId + ", " + this.conversationId;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getConversationId() {
		return conversationId;
	}

	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}
	
}
