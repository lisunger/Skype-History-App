package com.lisko.SkypeReaderApp.parsing;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.lisko.SkypeReaderApp.database.object.Conversation;
import com.lisko.SkypeReaderApp.database.object.ExportVersion;
import com.lisko.SkypeReaderApp.database.object.Message;
import com.lisko.SkypeReaderApp.database.object.SkypeFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.persistence.EntityManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsingMaster {

    private static final String MESSAGES_FILE_NAME =    "messages.json";
    private static final String MEDIA_FOLDER_NAME =     "media";
    private static final Logger LOGGER = LoggerFactory.getLogger(ParsingMaster.class);
    private static final List<String> DATE_FORMATS = Arrays.asList("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss.SS'Z'", "yyyy-MM-dd'T'HH:mm:ss.S'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm");
    private static final Pattern PATTERN_MEDIA_FILE = Pattern.compile("^([^\\.]*)(\\.1)(\\.[^1]*)$");
    private static final Pattern PATTERN_JSON_FILE = Pattern.compile("([^\\.]*)(\\.[^1]*)");

    private final Path folderPath;
    private int depth;
    private int messageCount;
    private long fileCount;
    private int persistedMessages;
    private int persistedFiles;
    private int newMessages;
    private int duplicateMessages;
    private int newFiles;
    private int duplicateFiles;
    private boolean closed;

    private EntityManager em;
    private JsonFactory jsonFactory;

    public ParsingMaster(Path folderPath, EntityManager em) {
        this.folderPath = folderPath;
        this.depth =                0;
        this.messageCount =         0;
        this.fileCount =            0;
        this.persistedMessages =    0;
        this.persistedFiles =       0;
        this.newMessages =          0;
        this.duplicateMessages =    0;
        this.newFiles =             0;
        this.duplicateFiles =       0;
        this.em = em;
        this.closed = false;
        this.jsonFactory = new JsonFactory();
    }

    public void processMessages(boolean persist, boolean log) throws IOException {

        this.countMessages();

        if(this.messageCount == 0) {
            LOGGER.debug("No messages file found!");
        }
        else {
            LOGGER.debug("Parsing messages: begin");
            Path messagesFile = Files.list(this.folderPath).filter(p ->
                    !Files.isDirectory(p) && p.getFileName().toString().equals(MESSAGES_FILE_NAME)
            ).findFirst().orElse(null);

            try (BufferedReader reader = Files.newBufferedReader(messagesFile, StandardCharsets.UTF_8)) {

                try (JsonParser parser = jsonFactory.createParser(reader)) {
                    if (parser.nextToken() != JsonToken.START_OBJECT) {
                        throw new IllegalStateException("Content is not object");
                    }

                    ExportVersion export = new ExportVersion();

                    // start reading the root
                    parser.nextToken();
                    this.depth = 0;

                    while (parser.currentToken() != JsonToken.END_OBJECT || this.depth != 0) {
                        switch (parser.getCurrentName()) {
                            case "userId": {
                                parser.nextToken();
                                export.getId().setUserId(parser.getValueAsString());
                                break;
                            }
                            case "exportDate": {
                                parser.nextToken();
                                export.getId().setDate(parseMyDate(parser.getValueAsString()));
                                break;
                            }
                            case "conversations": {
                                this.parseConversations(parser, persist, log);
                                break;
                            }
                        }

                        parser.nextToken();
                    }
                    if (log) {
                        LOGGER.debug(export.toString());
                    }
                    if (persist) {
                        if (this.em.find(ExportVersion.class, export.getId()) == null) {
                            this.em.getTransaction().begin();
                            this.em.persist(export);
                            this.em.getTransaction().commit();
                        }
                    }
                }
            }
            LOGGER.debug("Parsing messages: done");
        }

    }

    private void countMessages() throws IOException {
        Path messagesFile = Files.list(this.folderPath).filter(p ->
                !Files.isDirectory(p) && p.getFileName().toString().equals(MESSAGES_FILE_NAME)
        ).findFirst().orElse(null);

        try (BufferedReader reader = Files.newBufferedReader(messagesFile, StandardCharsets.UTF_8)) {

            try (JsonParser parser = jsonFactory.createParser(reader)) {
                if (parser.nextToken() != JsonToken.START_OBJECT) {
                    throw new IllegalStateException("Content is not object");
                }

                // start reading the root
                parser.nextToken();
                this.depth = 0;

                while (parser.currentToken() != JsonToken.END_OBJECT || this.depth != 0) {
                    switch (parser.getCurrentName()) {
                        case "userId":
                        case "exportDate": {
                            parser.nextToken();
                            break;
                        }
                        case "conversations": {
                            this.readConversations(parser);
                            break;
                        }
                    }

                    parser.nextToken();
                }
            }
        }
        this.depth = 0;
    }

    private void readConversations(JsonParser parser) throws IOException {
        parser.nextToken(); // so4i kam NULL ili kam [ na4aloto na masiva

        if(parser.currentToken() == JsonToken.VALUE_NULL) {
            return;
        }
        else if (parser.currentToken() != JsonToken.START_ARRAY) {
            throw new IllegalStateException("Content is not array");
        }

        this.depth++; // triabva da e 1
        int depthInArrayConversations = this.depth;

        // tuk so4im kam [ otvaria6tata skoba na Conversations

        parser.nextToken(); // { opening brace na obekta Conversation
        int depthInObjectConversation = this.depth + 1; // triabva da e 2

        do {
            if (parser.currentToken() != JsonToken.START_OBJECT) {
                throw new IllegalStateException("Content is not object");
            }

            this.depth++;
            parser.nextToken(); // so4i kam parvoto property name na Conversation ili kam } kraia mu

            while (true) {

                if(parser.currentToken() == JsonToken.END_OBJECT && this.depth == depthInObjectConversation) {
                    break;
                }

                switch (parser.getCurrentName()) {
                    case "id":
                    case "displayName":
                    case "version": {
                        parser.nextToken();
                        break;
                    }
                    case "properties":
                    case "threadProperties": {
                        this.parseProperties(parser);
                        break;
                    }
                    case "MessageList": {
                        this.readMessages(parser);
                        break;
                    }
                }
                parser.nextToken();

                if(parser.currentToken() == JsonToken.END_OBJECT) {
                    this.depth--;
                    parser.nextToken(); // so4i kam { na4aloto na nov obekt Conversation ili kam ] kraia na masiva
                    if (parser.currentToken() == JsonToken.END_ARRAY) {
                        depth--;
                    }
                    break;
                }
            }

        }
        while(parser.currentToken() != JsonToken.END_ARRAY || depthInArrayConversations == this.depth);
    }

    private void readMessages(JsonParser parser) throws IOException {
        parser.nextToken();
        // so4i kam NULL ili kam [ na4aloto na masiva

        if(parser.currentToken() == JsonToken.VALUE_NULL) {
            return;
        }
        else if (parser.currentToken() != JsonToken.START_ARRAY) {
            throw new IllegalStateException("Content is not array");
        }

        this.depth++; // triabva da e X
        int depthInArrayMessages = this.depth;

        // tuk so4im kam [ otvaria6tata skoba na Messages

        parser.nextToken(); // { opening brace na obekta Message

        if(parser.currentToken() == JsonToken.END_ARRAY) { // ako masivat e prazen - "MessageList" : []
            depth--;
            return;
        }

        int depthInObjectMessage = depth + 1; // triabva da e X

        do {
            if (parser.currentToken() != JsonToken.START_OBJECT) {
                throw new IllegalStateException("Content is not object");
            }

            this.depth++;
            parser.nextToken();

            while(true) {
                if(parser.currentToken() == JsonToken.END_OBJECT && this.depth == depthInObjectMessage) {
                    break;
                }

                switch(parser.getCurrentName()) {
                    case "id":
                    case "from":
                    case "conversationid":
                    case "originalarrivaltime":
                    case "version":
                    case "content":
                    case "displayName":
                    case "messagetype": {
                        parser.nextToken();
                        break;
                    }
                    case "properties":
                    case "amsreferences": {
                        this.parseProperties(parser);
                        break;
                    }
                }
                parser.nextToken();

                if(parser.currentToken() == JsonToken.END_OBJECT) {
                    this.depth--;

                    this.messageCount++;

                    parser.nextToken(); // so4i kam { na4aloto na nov obekt Message ili kam ] kraia na masiva
                    if (parser.currentToken() == JsonToken.END_ARRAY) {
                        this.depth--;
                    }
                    break;
                }
            }

        }
        while(parser.currentToken() != JsonToken.END_ARRAY || depthInArrayMessages == this.depth);

    }

    public void processFiles(boolean persist, boolean log) throws IOException {
        Path mediaFolder = Files.list(this.folderPath).filter(p ->
                Files.isDirectory(p) && p.getFileName().toString().equals(MEDIA_FOLDER_NAME)
        ).findFirst().orElse(null);

        if(mediaFolder == null) {
            LOGGER.debug("No media files found!");
        }
        else {
            LOGGER.debug("Reading media files: begin");
            this.fileCount = Files.list(mediaFolder).count();
            Files.list(mediaFolder).forEach(m -> {
                try {
                    this.readFile(m, persist, log);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.persistedFiles++;
            });
            LOGGER.debug("Reading media files: done");
        }
    }

    private void parseConversations(JsonParser parser, boolean persist, boolean log) throws IOException { // so4i kam "conversations"
        parser.nextToken(); // so4i kam NULL ili kam [ na4aloto na masiva

        if(parser.currentToken() == JsonToken.VALUE_NULL) {
            return;
        }
        else if (parser.currentToken() != JsonToken.START_ARRAY) {
            throw new IllegalStateException("Content is not array");
        }

        this.depth++; // triabva da e 1
        int depthInArrayConversations = this.depth;
        Conversation c = null;

        // tuk so4im kam [ otvaria6tata skoba na Conversations

        parser.nextToken(); // { opening brace na obekta Conversation
        int depthInObjectConversation = this.depth + 1; // triabva da e 2

        do {
            if (parser.currentToken() != JsonToken.START_OBJECT) {
                throw new IllegalStateException("Content is not object");
            }

            c = new Conversation();

            this.depth++;
            parser.nextToken(); // so4i kam parvoto property name na Conversation ili kam } kraia mu

            while (true) {

                if(parser.currentToken() == JsonToken.END_OBJECT && this.depth == depthInObjectConversation) {
                    break;
                }

                switch (parser.getCurrentName()) {
                    case "id": {
                        parser.nextToken();
                        c.setId(parser.getValueAsString());
                        if(persist) {
                            Conversation c2 = this.em.find(Conversation.class, c.getId());
                            if(c2 != null) {
                                c = c2;
                            }
                            else {
                                this.em.getTransaction().begin();
                                c = this.em.merge(c);
                                this.em.getTransaction().commit();
                            }
                        }
                        break;
                    }
                    case "displayName": {
                        parser.nextToken();
                        c.setDisplayName(parser.getValueAsString());
                        break;
                    }
                    case "version": {
                        parser.nextToken();
                        c.setVersion(parser.getValueAsString());
                        break;
                    }
                    case "properties": {
                        String properties = this.parseProperties(parser);
                        c.setProperties(properties);
                        break;
                    }
                    case "threadProperties": {
                        String properties = this.parseProperties(parser);
                        c.setThreadProperties(properties);
                        break;
                    }
                    case "MessageList": {
                        this.parseMessages(parser, c.getId(), persist, log);
                        break;
                    }
                }
                parser.nextToken();

                if(parser.currentToken() == JsonToken.END_OBJECT) {
                    this.depth--;
                    if(log) {
                        LOGGER.debug(c.toString());
                    }
                    if(persist) {
                        this.em.getTransaction().begin();
                        this.em.merge(c);
                        this.em.getTransaction().commit();
                    }
                    parser.nextToken(); // so4i kam { na4aloto na nov obekt Conversation ili kam ] kraia na masiva
                    if (parser.currentToken() == JsonToken.END_ARRAY) {
                        depth--;
                    }
                    break;
                }
            }

        }
        while(parser.currentToken() != JsonToken.END_ARRAY || depthInArrayConversations == this.depth);
    }

    private void parseMessages(JsonParser parser, String conversationId, boolean persist, boolean log) throws IOException { // so4i kam "MessageList"
        parser.nextToken();
        // so4i kam NULL ili kam [ na4aloto na masiva

        if(parser.currentToken() == JsonToken.VALUE_NULL) {
            return;
        }
        else if (parser.currentToken() != JsonToken.START_ARRAY) {
            throw new IllegalStateException("Content is not array");
        }

        this.depth++; // triabva da e X
        int depthInArrayMessages = this.depth;
        Message m = null;

        // tuk so4im kam [ otvaria6tata skoba na Messages

        parser.nextToken(); // { opening brace na obekta Message

        if(parser.currentToken() == JsonToken.END_ARRAY) { // ako masivat e prazen - "MessageList" : []
            this.depth--;
            return;
        }

        if(persist) {
            this.em.getTransaction().begin();
        }

        int depthInObjectMessage = this.depth + 1; // triabva da e X

        do {
            if (parser.currentToken() != JsonToken.START_OBJECT) {
                throw new IllegalStateException("Content is not object");
            }

            m = new Message();
            m.getPk().setConversationId(conversationId);

            this.depth++;
            parser.nextToken();

            while(true) {
                if(parser.currentToken() == JsonToken.END_OBJECT && this.depth == depthInObjectMessage) {
                    break;
                }

                switch(parser.getCurrentName()) {
                    case "id": {
                        parser.nextToken();
                        m.getPk().setId(parser.getValueAsString());
                        break;
                    }
                    case "displayName": {
                        parser.nextToken();
                        m.setDisplayName(parser.getValueAsString());
                        break;
                    }
                    case "originalarrivaltime": {
                        parser.nextToken();
                        m.setOriginalArrivalTime(parseMyDate(parser.getValueAsString()));
                        break;
                    }
                    case "messagetype": {
                        parser.nextToken();
                        m.setMessageType(parser.getValueAsString());
                        break;
                    }
                    case "version": {
                        parser.nextToken();
                        m.setVersion(parser.getValueAsString());
                        break;
                    }
                    case "content": {
                        parser.nextToken();
                        m.setContent(parser.getValueAsString());
                        break;
                    }
                    case "conversationid": {
                        parser.nextToken();
                        m.getPk().setConversationId(parser.getValueAsString());
                        break;
                    }
                    case "from": {
                        parser.nextToken();
                        m.setSender(parser.getValueAsString());
                        break;
                    }
                    case "properties": {
                        String properties = this.parseProperties(parser);
                        m.setProperties(properties);
                        break;
                    }
                    case "amsreferences": {
                        String properties = this.parseProperties(parser);
                        m.setAmsReferences(properties);
                        break;
                    }
                }
                parser.nextToken();

                if(parser.currentToken() == JsonToken.END_OBJECT) {
                    this.depth--;

                    if(log) {
                        LOGGER.debug(m.toString());
                    }
                    if(persist) {

                        Message m2 = this.em.find(Message.class, m.getPk());
                        if(m2 != null) {
                            this.duplicateMessages++;
                        }
                        else {
                            this.em.persist(m);
                            this.newMessages++;
                        }
                    }

                    this.persistedMessages++;

                    parser.nextToken(); // so4i kam { na4aloto na nov obekt Message ili kam ] kraia na masiva
                    if (parser.currentToken() == JsonToken.END_ARRAY) {
                        depth--;
                    }
                    break;
                }
            }

        }
        while(parser.currentToken() != JsonToken.END_ARRAY || depthInArrayMessages == this.depth);

        if(persist && this.em.getTransaction().isActive()) {
            this.em.getTransaction().commit();
        }
    }

    private String parseProperties(JsonParser parser) throws IOException {
        StringBuilder sb = new StringBuilder();
        int depthCopy = this.depth;

        parser.nextToken();

        if(parser.currentToken() == JsonToken.VALUE_NULL) {
            // null
        }
        else {
            do {
                if(parser.currentToken() == JsonToken.START_OBJECT) {
                    depthCopy++;
                    sb.append("{ ");
                }
                else if(parser.currentToken() == JsonToken.START_ARRAY) {
                    depthCopy++;
                    sb.append("[ ");
                }
                else if(parser.currentToken() == JsonToken.END_ARRAY) {
                    depthCopy--;
                    sb.append(" ]");
                    if(this.depth == depthCopy) {
                        break;
                    }
                }
                else if(parser.currentToken() == JsonToken.END_OBJECT) {
                    depthCopy--;
                    sb.append(" }");
                    if(this.depth == depthCopy) {
                        break;
                    }
                }
                else {
                    if(parser.currentToken() == JsonToken.FIELD_NAME) {
                        sb.append(parser.getCurrentName()).append(": ");
                    }
                    else {
                        sb.append(parser.getValueAsString()).append(", ");
                    }
                }

                parser.nextToken();
            }
            while(true);

            if(sb.lastIndexOf(",") >= 0) {
                sb.deleteCharAt(sb.lastIndexOf(","));
            }
        }

        return sb.toString().isEmpty() ? null : sb.toString();
    }

    private Date parseMyDate(String dateJson) {
        Date date = null;
        SimpleDateFormat s;
        for(String format : DATE_FORMATS) {
            s = new SimpleDateFormat(format);
            try {
                date = s.parse(dateJson);
                break;
            }
            catch(ParseException e) {
                continue;
            }
        }

        if(!dateJson.isEmpty() && date == null) {
            LOGGER.error("error parsing date: " + dateJson);
        }

        return date;
    }

    private void readFile(Path filePath, boolean persist, boolean log) throws IOException {
        String filename = filePath.getFileName().toString();

        Matcher matcherMediaFile = PATTERN_MEDIA_FILE.matcher(filename);
        Matcher matcherJsonFile = PATTERN_JSON_FILE.matcher(filename);

        // File is a media file
        if(matcherMediaFile.matches()) {
            try {
                readMediaFile(filePath, persist, log, matcherMediaFile.group(1), filename);
            }
            catch(Exception e) {
                LOGGER.error("Error reading file: " + filename);
                throw e;
            }
        }
        // File is json metadata
        else if(matcherJsonFile.matches()) {
            try {
                readJsonFile(filePath, persist, log, matcherJsonFile.group(1));
            }
            catch (Exception e) {
                LOGGER.error("Error parsing json file: " + filename);
                throw e;
            }
        }
    }

    private void readMediaFile(Path filePath, boolean persist, boolean log, String id, String filename) throws IOException {
        SkypeFile file = this.em.find(SkypeFile.class, id);

        if(file != null) {
            if(file.getBytes() == null) {
                byte[] bytes = Files.readAllBytes(filePath);
                file.setBytes(bytes);
                if(persist) {
                    this.em.getTransaction().begin();
                    this.em.persist(file);
                    this.em.getTransaction().commit();
                }
            }
            else {
                this.duplicateFiles++;
            }
        }
        else {
            byte[] bytes = Files.readAllBytes(filePath);

            file = new SkypeFile();
            file.setId(id);
            file.setFilename(filename);
            file.setBytes(bytes);

            if(persist) {
                this.em.getTransaction().begin();
                this.em.persist(file);
                this.em.getTransaction().commit();
                this.newFiles++;
            }
        }

        if(log) {
            LOGGER.debug(file.toString());
        }
    }

    private void readJsonFile(Path filePath, boolean persist, boolean log, String id) throws IOException {
        String filenameFromJson = null;
        String mime = null;
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {

            try (JsonParser parser = jsonFactory.createParser(reader)) {
                if (parser.nextToken() != JsonToken.START_OBJECT) {
                    throw new IllegalStateException("Content is not object");
                }

                parser.nextToken();

                while (parser.currentToken() != JsonToken.END_OBJECT) {

                    switch (parser.getCurrentName()) {
                        case "filename": {
                            parser.nextToken();
                            filenameFromJson = parser.getValueAsString();
                            break;
                        }
                        case "contents": {
                            parser.nextToken();
                            parser.nextToken();
                            parser.nextToken();
                            parser.nextToken();
                            parser.nextToken();

                            mime = parser.getValueAsString();

                            parser.nextToken();
                            parser.nextToken();
                            break;
                        }
                        default: { // e.g. "expiry_date"...
                            parser.nextToken();
                            break;
                        }
                    }

                    parser.nextToken();

                }
            }
        }

        SkypeFile file = this.em.find(SkypeFile.class, id);
        if(file != null) {

            if(file.getMime() != null) {
                this.duplicateFiles++;
            }
            else {
                file.setMime(mime);

                if(persist) {
                    this.em.getTransaction().begin();
                    this.em.persist(file);
                    this.em.getTransaction().commit();
                }
            }
        }
        else {
            if(persist) {
                file = new SkypeFile();
                file.setId(id);
                file.setMime(mime);
                file.setFilename(filenameFromJson);
                // file.setBytes(...);

                this.em.getTransaction().begin();
                this.em.persist(file);
                this.em.getTransaction().commit();
            }
            this.newFiles++;
        }

        if(log) {
            LOGGER.debug(file.toString());
        }
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    public Path getFolderPath() {
        return folderPath;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public long getFileCount() {
        return fileCount;
    }

    public int getPersistedMessages() {
        return this.persistedMessages;
    }

    public int getPersistedFiles() {
        return persistedFiles;
    }

    public int getNewMessages() {
        return newMessages;
    }

    public int getDuplicateMessages() {
        return duplicateMessages;
    }

    public int getNewFiles() {
        return newFiles;
    }

    public int getDuplicateFiles() {
        return duplicateFiles;
    }

}
