package com.lisko.SkypeReaderApp.database.object;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "file")
public class SkypeFile {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "file_name")
    private String filename;

    @Column(name = "mime")
    private String mime;

    @Column(name = "bytes")
    private byte[] bytes;

    public SkypeFile() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("F: { id: ").append(this.id);
        sb.append(" , filename: ").append(this.filename);
        sb.append(" , mime: ").append(this.mime);
        sb.append(" , size: ").append(this.bytes != null ? this.bytes.length : null);
        sb.append(" }");

        return sb.toString();
    }
}
