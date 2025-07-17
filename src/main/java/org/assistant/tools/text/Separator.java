package org.assistant.tools.text;

import org.assistant.ui.controls.Option;

enum Separator implements Option {

        COMMA() {
            @Override
            public String getLabel() {
                return "逗号 ,";
            }

            @Override
            public Object getValue() {
                return ",";
            }
        },

        SEPARATOR() {
            @Override
            public String getLabel() {
                return "换行符 \n";
            }

            @Override
            public Object getValue() {
                return "\n";
            }
        },

        FENHAO() {
            @Override
            public String getLabel() {
                return "分号 ;";
            }

            @Override
            public Object getValue() {
                return ";";
            }
        };


        @Override
        public Option deserialize(String value) {
            return valueOf(value);
        }
    }