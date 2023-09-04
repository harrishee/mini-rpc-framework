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
public class HelloPOJO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String serverName;

    private String clientName;

    private String message;
}
