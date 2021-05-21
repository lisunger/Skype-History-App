package com.lisko.SkypeReaderApp.beans;

import com.lisko.SkypeReaderApp.database.DatabaseErrorException;
import com.lisko.SkypeReaderApp.database.Jpa;
import com.lisko.SkypeReaderApp.database.dao.DatabaseDao;
import com.lisko.SkypeReaderApp.database.dao.MessagesLazyDataModel;
import com.lisko.SkypeReaderApp.database.object.Conversation;
import com.lisko.SkypeReaderApp.database.object.ConversationDetails;
import com.lisko.SkypeReaderApp.database.object.Message;
import com.lisko.SkypeReaderApp.parsing.ArchiveMaster;
import com.lisko.SkypeReaderApp.parsing.ParsingMaster;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.file.UploadedFile;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@Named("reader")
@ViewScoped
public class ReaderBean implements Serializable {

    private static final TimeZone timeZone = TimeZone.getDefault();

    private DatabaseDao dbDao;
    private Map<String, Object> statistics;
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

    /**
     * New export file has been uploaded
     * @param event contains the uploaded .tar file
     */
    public void actionFileUpload(FileUploadEvent event) {

        if(event.getFile() == null) {
            return;
        }

        ArchiveMaster archiver = new ArchiveMaster(event);

        try {
            archiver.unpackFiles();
        }
        catch(IOException e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Грешка", "Грешка при разархивиране и четене на архива!"));
        }

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("lisko");
        EntityManager em = emf.createEntityManager();

        ParsingMaster parser = new ParsingMaster(archiver.getTempFolderPath(), em);
        try {
            parser.readMessages(false, false);
            System.out.println(parser.getFolderPath());
            System.out.println(parser.getDuplicates());
            System.out.println(parser.getPersisted());
            System.out.println(parser.getMessageCounter());
        }
        catch(IOException e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Грешка", "Грешка при четене на съобщения и запис!"));
        }

        em.close();
        emf.close();

        int a = 1;

        try {
            archiver.cleanup();
        } catch (IOException e) {
            e.printStackTrace();
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
