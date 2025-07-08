package org.workassistant.common.interfaces.impl;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class MetaModel {

    private String name;

    private List<MetaField> fields;
}
