package com.lisko.SkypeReaderApp.database.dao;

import com.lisko.SkypeReaderApp.database.DatabaseErrorException;
import com.lisko.SkypeReaderApp.database.Jpa;
import com.lisko.SkypeReaderApp.database.object.Bookmark;
import com.lisko.SkypeReaderApp.database.object.BookmarkSearch;
import com.lisko.SkypeReaderApp.database.object.Conversation;
import com.lisko.SkypeReaderApp.database.object.ConversationDetails;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseDao {

    private EntityManager getEntityManager() {
        return Jpa.getInstance().getEntityManager();
    }

    public Date getExportDate() throws DatabaseErrorException {
        try {
            String sql = "select e.id.date from export_version e";
            return getEntityManager().createQuery(sql, Date.class).getSingleResult();
        }
        catch(NoResultException e) {
            return null;
        }
        catch(Exception e) {
            throw new DatabaseErrorException(e);
        }
    }

    public Long getConvCount() throws DatabaseErrorException {
        try {
            String sql = "select count(c) from conversation c";
            return getEntityManager().createQuery(sql, Long.class).getSingleResult();
        }
        catch(Exception e) {
            throw new DatabaseErrorException(e);
        }
    }

    public Long getMessageCount() throws DatabaseErrorException {
        try {
            String sql = "select count(m) from message m";
            return getEntityManager().createQuery(sql, Long.class).getSingleResult();
        }
        catch(Exception e) {
            throw new DatabaseErrorException(e);
        }
    }

    public List<ConversationDetails> getConversationDetails() throws DatabaseErrorException {
        try {
            String sql = "select c from conversation_details c";
            return getEntityManager().createQuery(sql, ConversationDetails.class).getResultList();
        }
        catch(Exception e) {
            throw new DatabaseErrorException(e);
        }
    }

    public void deleteConversation(String conversationId) throws DatabaseErrorException {
        try {
        	
        	String sql = "delete from bookmark where pk.conversationId = :id";
            getEntityManager().createQuery(sql).setParameter("id", conversationId).executeUpdate();
            
            sql = "delete from message where id.conversationId = :id";
            getEntityManager().createQuery(sql).setParameter("id", conversationId).executeUpdate();
            
            Conversation c = getEntityManager().find(Conversation.class, conversationId);
            getEntityManager().remove(c);
        }
        catch (Exception e) {
            throw new DatabaseErrorException(e);
        }
    }
    
    public List<String> getBookmarkCategories() throws DatabaseErrorException {
    	try {
    		String sql = "select distinct category from bookmark order";
            List<String> resultList = getEntityManager().createQuery(sql, String.class).getResultList();
            resultList.remove(null);
            return resultList;
    	}
    	catch (Exception e) {
            throw new DatabaseErrorException(e);
        }
    }
    
    public List<BookmarkSearch> searchBookmarks(String color, String conversationId, String category, String title) throws DatabaseErrorException {
    	try {
    		String select = "select new com.lisko.SkypeReaderApp.database.object.BookmarkSearch (b, c.displayName, m.content) " +
    						"from bookmark b " + 
    						"join conversation c on b.pk.conversationId = c.id " +
							"join message m on b.pk.messageId = m.pk.id and b.pk.conversationId = m.pk.conversationId ";
    		
    		String where = "";
    		
    		if(color != null) {
    			where += "where color = '" + color + "'";
    		}
    		
    		if(conversationId != null) {
    			if(!where.isEmpty()) {
    				where += " and ";
    			}
    			else {
    				where += " where ";
    			}
    			where += "b.pk.conversationId = '" + conversationId + "'";
    		}
    		
    		if(category != null) {
    			if(!where.isEmpty()) {
    				where += " and ";
    			}
    			else {
    				where += " where ";
    			}
    			where += "category = '" + category + "'";
    		}
    		
    		if(title != null) {
    			if(!where.isEmpty()) {
    				where += " and ";
    			}
    			else {
    				where += " where ";
    			}
    			where += "UPPER(title) like UPPER('%" + title + "%')" ;
    		}
    		
    		String orderBy = " order by b.messageDate asc";
    		
    		String sql = select + where + orderBy;
    		
            List<BookmarkSearch> resultList = getEntityManager().createQuery(sql, BookmarkSearch.class).getResultList();
            
            return resultList;
    	}
    	catch (Exception e) {
            throw new DatabaseErrorException(e);
        }
    }
    
    public void deleteBookmark(Bookmark bookmark) throws DatabaseErrorException {
    	try {
    		if(!Jpa.getInstance().isAttached(bookmark)) {
    			bookmark = Jpa.getInstance().getEntityManager().find(Bookmark.class, bookmark.getPk());
    		}
    		
    		Jpa.getInstance().getEntityManager().remove(bookmark);
    	}
    	catch (Exception e) {
            throw new DatabaseErrorException(e);
        }
    }
}
