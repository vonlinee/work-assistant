package org.assistant.tools.db;

import org.assistant.tools.db.export.ExcelSchemaExporter;
import org.assistant.tools.db.export.MarkdownSchemaExporter;
import org.assistant.tools.db.export.WordSchemaExporter;
import org.assistant.tools.db.export.XmlSchemaExporter;
import org.assistant.tools.db.parser.DbScanner;
import org.assistant.tools.db.parser.DbSchema;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class DbScannerTest {

    private static Connection connection;

    @BeforeAll
    public static void setup() throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username VARCHAR(50) NOT NULL, " +
                    "email VARCHAR(100), " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            stmt.execute("CREATE TABLE posts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "title VARCHAR(200) NOT NULL, " +
                    "body TEXT, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id)" +
                    ")");
        }
    }

    @AfterAll
    public static void teardown() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void testScanAndExport() throws Exception {
        DbScanner scanner = new DbScanner();
        DbSchema schema = scanner.scan(connection);

        assertNotNull(schema);
        assertTrue(schema.getTables().size() >= 2); // Might pick up sqlite_sequence

        // Output to a temp directory to verify exporters
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "db_export_test");
        if (!tempDir.exists())
            tempDir.mkdirs();

        // Markdown
        File mdFile = new File(tempDir, "schema.md");
        new MarkdownSchemaExporter().export(schema, mdFile);
        assertTrue(mdFile.exists() && mdFile.length() > 0);

        // XML
        File xmlFile = new File(tempDir, "schema.xml");
        new XmlSchemaExporter().export(schema, xmlFile);
        assertTrue(xmlFile.exists() && xmlFile.length() > 0);

        // Excel
        File excelFile = new File(tempDir, "schema.xlsx");
        new ExcelSchemaExporter().export(schema, excelFile);
        assertTrue(excelFile.exists() && excelFile.length() > 0);

        // Word
        File wordFile = new File(tempDir, "schema.docx");
        new WordSchemaExporter().export(schema, wordFile);
        assertTrue(wordFile.exists() && wordFile.length() > 0);

        System.out.println("Exported test files to: " + tempDir.getAbsolutePath());
    }
}
