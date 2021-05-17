package com.lisko.SkypeReaderApp.database;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Jpa {

    private static Jpa jpa;
    private EntityManagerFactory emf;
    private EntityManager em;

    private Jpa(String unitName) {
        this.emf = Persistence.createEntityManagerFactory(unitName);
    }

    public static Jpa getInstance() {
        if (Jpa.jpa == null) {
            Jpa.jpa = new Jpa("lisko");
        }
        return Jpa.jpa;
    }

    public EntityManager getEntityManager() {
        if (this.em == null) {
            this.em = this.emf.createEntityManager();
        }
        return this.em;
    }

    public void begin() {
        getEntityManager().getTransaction().begin();
    }

    public void commit() {
        getEntityManager().getTransaction().commit();
    }

    public void flush() {
        getEntityManager().flush();
    }

    public boolean isAttached(Object entity) {
        return getEntityManager().contains(entity);
    }

    public void rollback() {
        getEntityManager().getTransaction().rollback();
    }

    public void closeConnection() {
        if (getEntityManager() != null && getEntityManager().isOpen()) {
            getEntityManager().close();
        }
    }
}
