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
import org.primefaces.PrimeFaces;
import org.primefaces.component.progressbar.ProgressBar;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(ReaderBean.class);

    private DatabaseDao dbDao;
    private Map<String, Object> statistics;
    private List<ConversationDetails> conversations;

    private int selectedChatIndex = -1;
    private Conversation selectedChat;
    private Date filterDateBegin;
    private LazyDataModel<Message> messageLazyModel;
    private boolean showChat;

    private boolean displayUploadSection = false;
    private boolean persistMessages = true;
    private boolean persistFiles = true;
    private ParsingMaster parser;

    private ProgressBar progressBarMessages;
    private ProgressBar progressBarFiles;


    @PostConstruct
    public void initData() {
        LOGGER.debug("ReaderBean initData()");
        this.dbDao = new DatabaseDao();
        this.statistics = new HashMap<>();
        this.conversations = new ArrayList<>();
        this.showChat = false;

        readStatistics();
    }

    private void readStatistics() {
        this.statistics.clear();
        this.conversations.clear();

        try {
            this.statistics.put("exportDate", this.dbDao.getExportDate());
            this.statistics.put("convCount", this.dbDao.getConvCount());
            this.statistics.put("countMessages", this.dbDao.getMessageCount());

            this.conversations = this.dbDao.getConversationDetails();
        } catch (DatabaseErrorException e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Грешка", "Грешка при връзка с базата данни!"));
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
            return;
        }

        this.displayUploadSection = true;
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, null, "Архивът е поставен в: " + ArchiveMaster.getTempFolderPath()));
    }

    public void processUploadedArchive() {

        Runnable runnable = () -> {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("lisko");
            EntityManager em = emf.createEntityManager();

            this.parser = new ParsingMaster(ArchiveMaster.getTempFolderPath(), em);
            try {
                if(this.persistMessages) {
                    this.parser.processMessages(true, false);
                }
                if(this.persistFiles) {
                    this.parser.processFiles(true, false);
                }

                LOGGER.debug("Temp folder: " + parser.getFolderPath());
                LOGGER.debug("Duplicate messages: " + parser.getDuplicateMessages());
                LOGGER.debug("New messages: " + parser.getNewMessages());
                LOGGER.debug("Duplicate files: " + parser.getDuplicateFiles());
                LOGGER.debug("New files: " + parser.getNewFiles());
                LOGGER.debug("Read messages: " + parser.getPersistedMessages());
            }
            catch(Exception e) {
                e.printStackTrace();
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Грешка", "Грешка при четене на съобщения и запис!"));
            }

            em.close();
            emf.close();

            try {
                ArchiveMaster.cleanupTempFolder();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        runnable.run();

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

    public void deleteDatabase() {
        LOGGER.debug("Deleting database");
        Jpa.getInstance().begin();
        Jpa.getInstance().getEntityManager().createQuery("delete from file").executeUpdate();
        Jpa.getInstance().getEntityManager().createQuery("delete from message").executeUpdate();
        Jpa.getInstance().getEntityManager().createQuery("delete from conversation").executeUpdate();
        Jpa.getInstance().getEntityManager().createQuery("delete from export_version ").executeUpdate();
        Jpa.getInstance().commit();

        readStatistics();
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

    public boolean isPersistMessages() {
        return persistMessages;
    }

    public void setPersistMessages(boolean persistMessages) {
        this.persistMessages = persistMessages;
    }

    public boolean isPersistFiles() {
        return persistFiles;
    }

    public void setPersistFiles(boolean persistFiles) {
        this.persistFiles = persistFiles;
    }

    public boolean isDisplayUploadSection() {
        return displayUploadSection;
    }

    public void setDisplayUploadSection(boolean displayUploadSection) {
        this.displayUploadSection = displayUploadSection;
    }

    public int getUploadProgressMessages() {
        double result = 0;
        if(this.parser != null) {
            result = this.parser.getPersistedMessages() / (double) this.parser.getMessageCount();
            result *= 100;
        }
        System.out.println("getUploadProgressMessages " + result);
        return (int) result;
    }

    public int getUploadProgressFiles() {
        double result = 0;
        if(this.parser != null) {
            result = this.parser.getPersistedFiles() / (double) this.parser.getFileCount();
            result *= 100;
        }
        System.out.println("getUploadProgressFiles " + result);
        return (int) result;
    }

    public ProgressBar getProgressBarMessages() {
        return progressBarMessages;
    }

    public void setProgressBarMessages(ProgressBar progressBarMessages) {
        this.progressBarMessages = progressBarMessages;
    }

    public ProgressBar getProgressBarFiles() {
        return progressBarFiles;
    }

    public void setProgressBarFiles(ProgressBar progressBarFiles) {
        this.progressBarFiles = progressBarFiles;
    }

    public ParsingMaster getParser() {
        return parser;
    }

    public void setParser(ParsingMaster parser) {
        this.parser = parser;
    }
}
