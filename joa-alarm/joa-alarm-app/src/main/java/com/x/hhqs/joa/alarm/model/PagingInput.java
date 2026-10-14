package com.x.hhqs.joa.alarm.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class PagingInput {

    private int firstResult;

    private int maxResults;

    public PagingInput(int firstResult, int maxResults) {
        this.firstResult = firstResult;
        this.maxResults = maxResults;
    }
}