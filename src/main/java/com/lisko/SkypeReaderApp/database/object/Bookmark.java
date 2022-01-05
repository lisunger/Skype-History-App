package com.lisko.SkypeReaderApp.database.object;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "bookmark")
public class Bookmark {

	@Id
	private BookmarkPk pk;
	
	@Column(name = "title")
	private String title;
	
	@Column(name = "message_date")
	private Date messageDate;
	
	@Column(name = "color")
	private String color;
	
	@Column(name = "category")
	private String category;

	public Bookmark() {
		super();
		this.pk = new BookmarkPk();
	}

	public BookmarkPk getPk() {
		return pk;
	}

	public void setPk(BookmarkPk pk) {
		this.pk = pk;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getMessageDate() {
		return messageDate;
	}

	public void setMessageDate(Date messageDate) {
		this.messageDate = messageDate;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@Override
	public String toString() {
		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String dateString = null;
        if(this.messageDate != null) {
            dateString = s.format(this.messageDate);
        }
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("B: { messageId: ").append(this.pk.getMessageId());
        sb.append(" , conversationId: ").append(this.pk.getConversationId());
        sb.append(", title: ").append(this.title);
        sb.append(", messageDate: ").append(dateString);
        sb.append(", color: ").append(this.color);
        sb.append(", category: ").append(this.category);
        sb.append(" }");
        
        return sb.toString();
	}
	
}
