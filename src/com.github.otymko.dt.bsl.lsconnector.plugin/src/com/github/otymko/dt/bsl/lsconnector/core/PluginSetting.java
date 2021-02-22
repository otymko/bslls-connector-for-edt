package com.github.otymko.dt.bsl.lsconnector.core;

import java.nio.file.Path;

import lombok.Data;

@Data
public class PluginSetting {
    boolean enable;
    Path pathToJava;
    Path pathToLS;
    boolean externalJar;
    String javaOpts;
}
