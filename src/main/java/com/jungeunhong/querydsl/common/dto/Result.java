package com.jungeunhong.querydsl.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result <T>{

    private int code;
    private T data;

}
