package com.x.hhqs.joa.in.vo.passThrough;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "resultTemplate")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResultTemplate {

    private int minSleepTime;
    private int maxSleepTime;
    private String title;

    private ParamNameList paramNameList;

    private ResultList resultList;

    // Getters and Setters
    public int getMinSleepTime() { return minSleepTime; }
    public void setMinSleepTime(int minSleepTime) { this.minSleepTime = minSleepTime; }

    public int getMaxSleepTime() { return maxSleepTime; }
    public void setMaxSleepTime(int maxSleepTime) { this.maxSleepTime = maxSleepTime; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public ParamNameList getParamNameList() { return paramNameList; }
    public void setParamNameList(ParamNameList paramNameList) { this.paramNameList = paramNameList; }

    public ResultList getResultList() { return resultList; }
    public void setResultList(ResultList resultList) { this.resultList = resultList; }

    @Override
    public String toString() {
        return "ResultTemplate{" +
                "minSleepTime=" + minSleepTime +
                ", maxSleepTime=" + maxSleepTime +
                ", title='" + title + '\'' +
                ", paramNameList=" + paramNameList +
                ", resultList=" + resultList +
                '}';
    }
}
