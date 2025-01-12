module org.example.workassistant {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires static lombok;
    requires org.mybatis.generator;
    requires org.slf4j;
    requires annotations;
    requires druid;
    requires cn.hutool;
    requires org.apache.commons.lang3;
    requires jsonfive.java;
    requires com.squareup.javapoet;
    requires java.compiler;
    requires com.jcraft.jsch;
    requires com.fasterxml.jackson.databind;
    requires pagehelper;
    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;
    requires spring.jdbc;
    requires org.apache.commons.cli;
    requires com.baomidou.mybatis.plus.annotation;
    requires org.girod.javafx.svgimage;
    requires com.google.gson;
    requires velocity.engine.core;
    requires org.jsoup;
    requires com.github.javaparser.core;
    requires spring.core;
    requires org.mybatis;
    requires org.yaml.snakeyaml;
    requires dom4j;
    requires commons.dbutils;
    requires java.datatransfer;
    requires java.desktop;
    requires com.baomidou.mybatis.plus.core;
    requires com.baomidou.mybatis.plus.extension;
    requires jdk.jsobject;
    requires MaterialFX;
    requires com.google.googlejavaformat;
    requires ognl;
    requires mysql.connector.j;
    requires poi.ooxml.schemas;
    requires ST4;
    requires poi.ooxml;
    requires easypoi.annotation;
    requires xmlbeans;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.materialdesign2;
    requires java.management;
    requires org.apache.commons.collections4;
    requires spring.beans;

    opens org.example.workassistant to javafx.fxml;
    exports org.example.workassistant;
}