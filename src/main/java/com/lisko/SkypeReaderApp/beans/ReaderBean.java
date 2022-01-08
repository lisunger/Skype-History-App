package com.lisko.SkypeReaderApp.beans;

import com.lisko.SkypeReaderApp.database.DatabaseErrorException;
import com.lisko.SkypeReaderApp.database.Jpa;
import com.lisko.SkypeReaderApp.database.dao.DatabaseDao;
import com.lisko.SkypeReaderApp.database.dao.MessagesLazyDataModel;
import com.lisko.SkypeReaderApp.database.object.Bookmark;
import com.lisko.SkypeReaderApp.database.object.BookmarkPk;
import com.lisko.SkypeReaderApp.database.object.Conversation;
import com.lisko.SkypeReaderApp.database.object.ConversationDetails;
import com.lisko.SkypeReaderApp.database.object.Message;
import com.lisko.SkypeReaderApp.database.object.MessagePk;
import com.lisko.SkypeReaderApp.parsing.ArchiveMaster;
import com.lisko.SkypeReaderApp.parsing.ParsingMaster;
import com.lisko.SkypeReaderApp.utils.BookmarksColors;
import com.lisko.SkypeReaderApp.utils.Screens;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@Named("reader")
@ViewScoped
public class ReaderBean implements Serializable {

	private static final long serialVersionUID = -7466158749616466387L;
	
	private static final TimeZone timeZone = TimeZone.getDefault();
    private static final Logger LOGGER = LoggerFactory.getLogger(ReaderBean.class);

    private DatabaseDao dbDao;
    private Map<String, Object> statistics;
    private List<ConversationDetails> conversations;
    
    private Screens currentScreen;

    private int selectedChatIndex = -1;
    private Conversation selectedChat;
    private Date filterDateBegin;
    private LazyDataModel<Message> messageLazyModel;
    private Bookmark newBookmark;
    private List<String> bookmarkCategories;

    private boolean displayUploadButton = false;
    private boolean persistMessages = true;
    private boolean persistFiles = true;
    private ParsingMaster parser;


    @PostConstruct
    public void initData() {
        LOGGER.debug("ReaderBean initData()");
        Jpa.getInstance().getEntityManager().clear();
        this.dbDao = new DatabaseDao();
        this.statistics = new HashMap<>();
        this.conversations = new ArrayList<>();
        this.currentScreen = Screens.EMPTY;
        
        this.newBookmark = new Bookmark();
        initBookMarkCategories();

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
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Грешка", "Грешка при връзката с базата данни!"));
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

        this.displayUploadButton = true;
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, null, "Архивът е поставен в: " + ArchiveMaster.getTempFolderPath()));
    }

    public void processUploadedArchive() {

        Jpa.getInstance().getEntityManager();

        this.parser = new ParsingMaster(ArchiveMaster.getTempFolderPath());
        try {
            if(this.persistMessages) {
                this.parser.processMessages(true, false);
            }
            if(this.persistFiles) {
                this.parser.processFiles(true, true);
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
        finally {
            Jpa.getInstance().getEntityManager().clear();
        }

        try {
            ArchiveMaster.cleanupTempFolder();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void actionChooseChat(int index) {
        this.selectedChatIndex = index;
        this.selectedChat = Jpa.getInstance().getEntityManager().find(Conversation.class, this.conversations.get(index).getId());
        this.messageLazyModel = null;
        changeScreen(Screens.CHAT);
    }

    public void actionLoadMessages() {
        this.messageLazyModel = new MessagesLazyDataModel(this.selectedChat.getId(), this.filterDateBegin);
        // TODO show scroller
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

    public void deleteChat(int index) {
    	EntityManager em = Jpa.getInstance().getEntityManager();
        try {
            em.getTransaction().begin();
            DatabaseDao dao = new DatabaseDao();
            dao.deleteConversation(this.conversations.get(this.selectedChatIndex).getId());
            em.getTransaction().commit();
            this.conversations.remove(this.selectedChatIndex);
            
            this.selectedChat = null;
            this.selectedChatIndex = -1;
            readStatistics();
        }
        catch (DatabaseErrorException e) {
            em.getTransaction().rollback();
            LOGGER.error(e.getMessage());
        }
    }
    
    public void initNewBookmark() {

    	this.newBookmark = new Bookmark();
    	this.newBookmark.setColor(BookmarksColors.YELLOW.getRgb());
    	
    	String messageId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("messageId");
    	Message selectedMessage = Jpa.getInstance().getEntityManager().find(Message.class, new MessagePk(messageId, this.selectedChat.getId()));
        
    	if(selectedMessage != null) {
    		this.newBookmark.setPk(
        			new BookmarkPk(
        					selectedMessage.getPk().getId(),
        					selectedMessage.getPk().getConversationId()));
    		this.newBookmark.setMessageDate(selectedMessage.getOriginalArrivalTime());
    	}
    }
    
    private void initBookMarkCategories() {
    	DatabaseDao dao = new DatabaseDao();
    	try {
    		this.bookmarkCategories = dao.getBookmarkCategories();
    	}
    	catch(DatabaseErrorException e) {
    		this.bookmarkCategories = new ArrayList<>();
    	}
    }
    
    public void setNewBookmarkColor(String color) {
    	this.newBookmark.setColor(color);
    }
    
    public void actionSaveNewBookmark() {
    	
    	EntityManager em = Jpa.getInstance().getEntityManager();
    	try {
    		if(this.newBookmark.getTitle().isEmpty()) {
    			this.newBookmark.setTitle(null);
    		}
    		
    		em.getTransaction().begin();
    		this.dbDao.saveBookmark(this.newBookmark);
    		em.getTransaction().commit();
	        
	        if(this.newBookmark.getCategory() != null && !this.bookmarkCategories.contains(this.newBookmark.getCategory())) {
	        	this.bookmarkCategories.add(this.newBookmark.getCategory());
	        }
	        
	        this.newBookmark = new Bookmark();
    	}
    	catch(Exception e) {
    		em.getTransaction().rollback();
    		e.printStackTrace();
    		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Грешка", "Отметката не е записана!"));
    	}
    }
    
    public void removeBookmark(Message message) {
		EntityManager em = Jpa.getInstance().getEntityManager();
		try {
			em.getTransaction().begin();
			this.dbDao.deleteBookmark(message);
			em.getTransaction().commit();
			
		} catch (DatabaseErrorException e) {
			em.getTransaction().rollback();
			e.printStackTrace();
		}
    }
    
    public void changeScreen(Screens screen) {
    	
    	switch(screen) {
	    	
    		case EMPTY: {
	    		this.currentScreen = Screens.EMPTY;
	    		break;
	    	}
	    	
	    	case CHAT: {
	    		this.currentScreen = Screens.CHAT;
	    		break;
	    	}
	    	
	    	case BOOKMARKS: {
	    		if(this.currentScreen == Screens.BOOKMARKS) {
	    			changeScreen(Screens.EMPTY);
	    			break;
	    		}
	    			
	    		this.currentScreen = Screens.BOOKMARKS;
	    		BookmarksBean b = FacesContext.getCurrentInstance().getApplication().evaluateExpressionGet(FacesContext.getCurrentInstance(), "#{bookmark}", BookmarksBean.class);
	    		if(b != null) {
	    			b.searchBookmarks();
	    		}
	    		
	    		break;
	    	}
	    	
	    	case STORIES: {
	    		// TODO
	    		this.currentScreen = Screens.STORIES;
	    		break;
	    	}
    	}
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

    public Screens getCurrentScreen() {
		return currentScreen;
	}

	public void setCurrentScreen(Screens currentScreen) {
		this.currentScreen = currentScreen;
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

    public boolean isDisplayUploadButton() {
        return displayUploadButton;
    }

    public void setDisplayUploadButton(boolean displayUploadButton) {
        this.displayUploadButton = displayUploadButton;
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

    public ParsingMaster getParser() {
        return parser;
    }

    public void setParser(ParsingMaster parser) {
        this.parser = parser;
    }
    
    public BookmarksColors[] getBookmarkColors() {
    	return BookmarksColors.values();
    }
    
    public List<String> getBookmarkCategories() {
    	return this.bookmarkCategories;
    }

	public Bookmark getNewBookmark() {
		return newBookmark;
	}

	public void setNewBookmark(Bookmark newBookmark) {
		this.newBookmark = newBookmark;
	}
}
