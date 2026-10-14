package com.x.hhqs.joa.in.vo.passThrough;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class Result {

    @XmlElement(name = "paramValue")
    private List<String> paramValues;

    public List<String> getParamValues() {
        return paramValues;
    }

    public void setParamValues(List<String> paramValues) {
        this.paramValues = paramValues;
    }

    @Override
    public String toString() {
        return "Result{" +
                "paramValues=" + paramValues +
                '}';
    }
}