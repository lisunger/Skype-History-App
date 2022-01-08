package com.lisko.SkypeReaderApp.beans;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lisko.SkypeReaderApp.database.DatabaseErrorException;
import com.lisko.SkypeReaderApp.database.Jpa;
import com.lisko.SkypeReaderApp.database.dao.DatabaseDao;
import com.lisko.SkypeReaderApp.database.object.Bookmark;
import com.lisko.SkypeReaderApp.database.object.BookmarkSearch;
import com.lisko.SkypeReaderApp.utils.BookmarksColors;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @author n.kanev
 *
 * Bean на екрана с отметките (bookmarks). Извършва търсене, триене и отваряне на отбелязано съобщение.
 */
@Named("bookmark")
@ViewScoped
public class BookmarksBean implements Serializable {
	
	private static final long serialVersionUID = 723237799776943949L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ReaderBean.class);
	
	private String color;
	private String category;
	private String title;
	private String chatId;
	private List<BookmarkSearch> bookmarks;
	private DatabaseDao dao;
	

	@PostConstruct
    public void initData() {
		this.dao = new DatabaseDao();
    }
	
	public void searchBookmarks() {
		
		if(this.color != null && this.color.isEmpty()) this.color = null;
		if(this.chatId != null && this.chatId.isEmpty()) this.chatId = null;
		if(this.category != null && this.category.isEmpty()) this.category = null;
		if(this.title != null && this.title.isEmpty()) this.title = null;
		
		try {
			this.bookmarks = this.dao.searchBookmarks(this.color, this.chatId, this.category, this.title);
		} catch (DatabaseErrorException e) {
			e.printStackTrace();
		}
	}
	
	public void actionDeleteBookmark(BookmarkSearch bookmark) {
		EntityManager em = Jpa.getInstance().getEntityManager();
		
		try {
			em.getTransaction().begin();
			this.dao.deleteBookmark(bookmark.getBookmark());
			em.getTransaction().commit();
			searchBookmarks();
			
		} catch (DatabaseErrorException e) {
			em.getTransaction().rollback();
			e.printStackTrace();
		}
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getChatId() {
		return chatId;
	}

	public void setChatId(String chatId) {
		this.chatId = chatId;
	}

	public List<BookmarkSearch> getBookmarks() {
		return bookmarks;
	}

	public void setBookmarks(List<BookmarkSearch> bookmarks) {
		this.bookmarks = bookmarks;
	}
	
}
