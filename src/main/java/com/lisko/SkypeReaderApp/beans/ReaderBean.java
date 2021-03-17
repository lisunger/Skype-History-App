package com.lisko.SkypeReaderApp.beans;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named("reader")
@ViewScoped
public class ReaderBean implements Serializable {

    @PostConstruct
    public void initData() {

    }

}
