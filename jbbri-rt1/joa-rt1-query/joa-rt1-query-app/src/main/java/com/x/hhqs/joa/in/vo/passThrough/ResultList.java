package com.x.hhqs.joa.in.vo.passThrough;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class ResultList {

    @XmlElement(name = "result")
    private List<Result> results = new ArrayList<Result>();

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "ResultList{" +
                "results=" + results +
                '}';
    }
}
