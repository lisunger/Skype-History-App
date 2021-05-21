package com.lisko.SkypeReaderApp.parsing;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.lisko.SkypeReaderApp.database.object.Conversation;
import com.lisko.SkypeReaderApp.database.object.ExportVersion;
import com.lisko.SkypeReaderApp.database.object.Message;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
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

public class ParsingMaster {

    private static final String MESSAGES_FILE_NAME =    "messages.json";
    private static final String MEDIA_FOLDER_NAME =     "media";

    private final Path folderPath;
    private int depth;
    private int messageCounter;
    private int persisted;
    private int duplicates;
    private EntityManager em;

    public ParsingMaster(Path folderPath, EntityManager em) {
        this.folderPath = folderPath;
        this.depth = 0;
        this.messageCounter = 0;
        this.persisted = 0;
        this.duplicates = 0;
        this.em = em;
    }

    public void readMessages(boolean persist, boolean log) throws IOException {

        JsonFactory jsonfactory = new JsonFactory();

        Path messagesFile = Files.list(this.folderPath).filter(p ->
                !Files.isDirectory(p) && p.getFileName().toString().equals(MESSAGES_FILE_NAME)
        ).findFirst().orElse(null);

        if(messagesFile == null) {
            System.out.println("No messages file found!");
        }
        else {

            try (BufferedReader reader = Files.newBufferedReader(messagesFile, StandardCharsets.UTF_8)) {

                try (JsonParser parser = jsonfactory.createParser(reader)) {
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
                        System.out.println(export);
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
            System.out.println("... parsing messages... done");
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
        int depthInArrayConversations = depth;
        Conversation c = null;

        // tuk so4im kam [ otvaria6tata skoba na Conversations

        parser.nextToken(); // { opening brace na obekta Conversation
        int depthInObjectConversation = depth + 1; // triabva da e 2

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
                        System.out.println(c);
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
        int depthInArrayMessages = depth;
        Message m = null;

        // tuk so4im kam [ otvaria6tata skoba na Messages

        parser.nextToken(); // { opening brace na obekta Message

        if(parser.currentToken() == JsonToken.END_ARRAY) { // ako masivat e prazen - "MessageList" : []
            depth--;
            return;
        }

        if(persist) {
            this.em.getTransaction().begin();
        }

        int depthInObjectMessage = depth + 1; // triabva da e X

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
                        System.out.println(m);
                    }
                    if(persist) {

                        Message m2 = this.em.find(Message.class, m.getPk());
                        if(m2 != null) {
                            duplicates++;
                        }
                        else {
                            this.em.persist(m);
                            persisted++;
                        }
                    }

                    messageCounter++;

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
        int depthCopy = depth;

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
                    if(depth == depthCopy) {
                        break;
                    }
                }
                else if(parser.currentToken() == JsonToken.END_OBJECT) {
                    depthCopy--;
                    sb.append(" }");
                    if(depth == depthCopy) {
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
        List<String> formats = Arrays.asList("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss.SS'Z'", "yyyy-MM-dd'T'HH:mm:ss.S'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm");
        Date date = null;
        SimpleDateFormat s;
        for(String format : formats) {
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
            System.out.println("error parsing date: " + dateJson);
        }

        return date;
    }

    public Path getFolderPath() {
        return folderPath;
    }

    public int getMessageCounter() {
        return messageCounter;
    }

    public int getPersisted() {
        return persisted;
    }

    public int getDuplicates() {
        return duplicates;
    }

    public EntityManager getEm() {
        return em;
    }
}
