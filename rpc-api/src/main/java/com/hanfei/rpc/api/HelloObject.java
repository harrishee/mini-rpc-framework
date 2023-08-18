package com.hanfei.rpc.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HelloObject implements Serializable {



    private Integer id;

    private String message;
}
