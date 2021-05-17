package com.lisko.SkypeReaderApp.database.dao;

import com.lisko.SkypeReaderApp.database.Jpa;
import com.lisko.SkypeReaderApp.database.object.Message;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MessagesLazyDataModel extends LazyDataModel<Message> {

    private String conversationId;
    private Date dateBegin;

    public MessagesLazyDataModel(String conversationId, Date dateBegin) {
        super();
        this.conversationId = conversationId;
        this.dateBegin = dateBegin;

        String sql = "select count (m) from message m where m.id.conversationId = :convId and originalArrivalTime >= :minDate";
        TypedQuery<Long> query = Jpa.getInstance().getEntityManager().createQuery(sql, Long.class);
        query.setParameter("convId", this.conversationId);
        query.setParameter("minDate", this.dateBegin);
        this.setRowCount(query.getSingleResult().intValue());
    }

    @Override
    public List<Message> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {

        System.out.println("LOAD: " + first + ", " + pageSize);

        String sql = "from message m where m.id.conversationId = :convId and originalArrivalTime >= :minDate order by m.originalArrivalTime";
        TypedQuery<Message> query = Jpa.getInstance().getEntityManager().createQuery(sql, Message.class);
        query.setParameter("convId", this.conversationId);
        query.setParameter("minDate", this.dateBegin);
        query.setFirstResult(first);
        query.setMaxResults(pageSize);

        List<Message> results = query.getResultList();

        return results;
    }

    @Override
    public String getRowKey(Message object) {
        System.out.println("ROW KEY: " + object);
        return String.valueOf(object.getPk());
    }

    @Override
    public Message getRowData(String rowKey) {
        System.out.println("ROW DATA: " + rowKey);

        String sql = "from message m where m.id = :id";
        TypedQuery<Message> query = Jpa.getInstance().getEntityManager().createQuery(sql, Message.class);
        query.setParameter("id", rowKey);

        Message result = null;
        try {
            result = query.getSingleResult();
        }
        catch (NoResultException e) { }

        return result;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Date getDateBegin() {
        return dateBegin;
    }

    public void setDateBegin(Date dateBegin) {
        this.dateBegin = dateBegin;
    }
}
