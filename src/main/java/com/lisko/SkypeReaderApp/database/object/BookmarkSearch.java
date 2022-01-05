package com.lisko.SkypeReaderApp.database.object;

public class BookmarkSearch {
	
	private Bookmark bookmark;
	private String conversation;
	private String messageContent;
	
	public BookmarkSearch() { }

	public Bookmark getBookmark() {
		return bookmark;
	}
	
	public BookmarkSearch(Bookmark bookmark, String conversation, String messageContent) {
		this.bookmark = bookmark;
		this.conversation = conversation;
		this.messageContent = messageContent;
	}

	public void setBookmark(Bookmark bookmark) {
		this.bookmark = bookmark;
	}

	public String getConversation() {
		return conversation;
	}

	public void setConversation(String conversation) {
		this.conversation = conversation;
	}

	public String getMessageContent() {
		return messageContent;
	}

	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}
	
}
