package com.lisko.SkypeReaderApp.database.dao;

import com.lisko.SkypeReaderApp.database.DatabaseErrorException;
import com.lisko.SkypeReaderApp.database.Jpa;
import com.lisko.SkypeReaderApp.database.object.ConversationDetails;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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
}
