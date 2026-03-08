package org.assistant.ui.controls;

import com.alibaba.druid.DbType;

public class DbDialectComboBox extends ComboBox<DbType> {

    public DbDialectComboBox() {
        super();
        initDialects();
    }

    private void initDialects() {
        for (DbType dbType : DbType.values()) {
            this.addItem(dbType);
        }
        this.setSelectedItem(DbType.mysql); // default to MySQL
    }

    public DbType getSelectedDialect() {
        return (DbType) this.getSelectedItem();
    }
}
