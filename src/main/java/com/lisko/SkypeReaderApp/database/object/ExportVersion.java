package com.lisko.SkypeReaderApp.database.object;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.text.SimpleDateFormat;

@Entity(name = "export_version")
public class ExportVersion {

    @Id
    private ExportVersionPk id;

    public ExportVersion() {
        super();
        this.id = new ExportVersionPk();
    }

    public ExportVersionPk getId() {
        return id;
    }

    public void setId(ExportVersionPk id) {
        this.id = id;
    }

    @Override
    public String toString() {

        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String dateString = s.format(this.id.getDate());
        return "E: { userId: " + this.id.getUserId()
                + " , exportDate: "  + dateString
                + " }";
    }
}
