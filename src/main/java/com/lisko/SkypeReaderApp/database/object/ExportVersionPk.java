package com.lisko.SkypeReaderApp.database.object;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Embeddable
public class ExportVersionPk implements Serializable {

    @Column(name = "user_id")
    private String userId;
    @Column(name="date")
    private Date date;

    public ExportVersionPk() {
    }

    public ExportVersionPk(String userId, Date date) {
        this.userId = userId;
        this.date = date;
    }

    @Override
	public int hashCode() {
		return Objects.hash(userId, date);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportVersionPk exportPk = (ExportVersionPk) o;
        return this.userId.equals(exportPk.userId) 
        		&& this.date.equals(exportPk.date);
	}
    
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
