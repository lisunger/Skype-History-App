package com.lisko.SkypeReaderApp.beans;

import com.lisko.SkypeReaderApp.database.DatabaseErrorException;
import com.lisko.SkypeReaderApp.database.Jpa;
import com.lisko.SkypeReaderApp.database.dao.DatabaseDao;
import com.lisko.SkypeReaderApp.database.dao.MessagesLazyDataModel;
import com.lisko.SkypeReaderApp.database.object.Conversation;
import com.lisko.SkypeReaderApp.database.object.ConversationDetails;
import com.lisko.SkypeReaderApp.database.object.Message;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.file.UploadedFile;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;

@Named("reader")
@ViewScoped
public class ReaderBean implements Serializable {

    private static final TimeZone timeZone = TimeZone.getDefault();

    private DatabaseDao dbDao;
    private Map<String, Object> statistics;
    private UploadedFile newFile;
    private List<ConversationDetails> conversations;

    private int selectedChatIndex = -1;
    private Conversation selectedChat;
    private Date filterDateBegin;
    private LazyDataModel<Message> messageLazyModel;
    private boolean showChat;


    @PostConstruct
    public void initData() {
        this.dbDao = new DatabaseDao();
        this.statistics = new HashMap<>();
        this.conversations = new ArrayList<>();
        this.showChat = false;

        try {
            this.statistics.put("exportDate", this.dbDao.getExportDate());
            this.statistics.put("convCount", this.dbDao.getConvCount());
            this.statistics.put("countMessages", this.dbDao.getMessageCount());

            this.conversations = this.dbDao.getConversationDetails();
        } catch (DatabaseErrorException e) {
            e.printStackTrace();
        }
    }

    public void actionFileUpload(FileUploadEvent event) {
        newFile = event.getFile();
        if(newFile != null && newFile.getContent() != null && newFile.getContent().length > 0 && newFile.getFileName() != null) {
            FacesMessage msg = new FacesMessage("Successful", newFile.getFileName() + " is uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        else {
            newFile = null;
        }
    }

    public void actionChooseChat(int index) {
        this.selectedChatIndex = index;
        this.selectedChat = Jpa.getInstance().getEntityManager().find(Conversation.class, this.conversations.get(index).getId());
        this.showChat = false;
    }

    public void actionSelectDateBegin() {
        // TODO
    }

    public void actionSelectDateEnd() {
        // TODO
    }

    public void actionLoadMessages() {
        this.messageLazyModel = new MessagesLazyDataModel(this.selectedChat.getId(), this.filterDateBegin);
        this.showChat = true;
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public Map<String, Object> getStatistics() {
        return statistics;
    }

    public void setStatistics(Map<String, Object> statistics) {
        this.statistics = statistics;
    }

    public List<ConversationDetails> getConversations() {
        return conversations;
    }

    public void setConversations(List<ConversationDetails> conversations) {
        this.conversations = conversations;
    }

    public int getSelectedChatIndex() {
        return selectedChatIndex;
    }

    public void setSelectedChatIndex(int selectedChatIndex) {
        this.selectedChatIndex = selectedChatIndex;
    }

    public Conversation getSelectedChat() {
        return selectedChat;
    }

    public void setSelectedChat(Conversation selectedChat) {
        this.selectedChat = selectedChat;
    }

    public Date getFilterDateBegin() {
        return filterDateBegin;
    }

    public void setFilterDateBegin(Date filterDateBegin) {
        this.filterDateBegin = filterDateBegin;
    }

    public LazyDataModel<Message> getMessageLazyModel() {
        return messageLazyModel;
    }

    public void setMessageLazyModel(LazyDataModel<Message> messageLazyModel) {
        this.messageLazyModel = messageLazyModel;
    }

    public boolean isShowChat() {
        return showChat;
    }

    public void setShowChat(boolean showChat) {
        this.showChat = showChat;
    }
}
