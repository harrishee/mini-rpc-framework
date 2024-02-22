package com.hanfei.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HelloMsg implements Serializable {
    private String serverName;
    private String clientName;
    private String message;
}
