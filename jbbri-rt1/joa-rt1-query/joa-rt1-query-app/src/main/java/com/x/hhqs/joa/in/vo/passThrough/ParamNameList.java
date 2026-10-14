package com.x.hhqs.joa.in.vo.passThrough;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class ParamNameList {

    @XmlElement(name = "paramName")
    private List<String> paramNames;

    public List<String> getParamNames() {
        return paramNames;
    }

    public void setParamNames(List<String> paramNames) {
        this.paramNames = paramNames;
    }

    @Override
    public String toString() {
        return "ParamNameList{" +
                "paramNames=" + paramNames +
                '}';
    }
}
