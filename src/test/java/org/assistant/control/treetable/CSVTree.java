package org.assistant.control.treetable;

import org.assistant.ui.controls.table.TreeTablePane;
import org.assistant.ui.controls.table.TreeUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * The CSVTree class provides functionality for reading hierarchical data from a CSV file,
 * constructing a tree structure, and displaying the tree in a graphical JTable-based viewer.
 * It includes methods for parsing CSV data, creating tree nodes, and rendering the tree as a GUI.
 * <p>
 * It also replaces the default mouse click behaviour on the headers, so that it will sort for each header click,
 * unless the cursor is a resize cursor.  In that case, if it is a double-click, it will resize the column to fit
 * the contents.
 */
public class CSVTree {

	/**
	 * Displays a Swing form containing a JTable bound to a TreeTableModelObjectArray.
	 * The table column model is generated using TreeUtils.buildTableColumnModel() from the headers.
	 *
	 * @param rootNode The root node of the tree structure.
	 * @param headers  An array of column headers for the table.
	 */
	public static void displayTree(DefaultMutableTreeNode rootNode, Object[] headers) throws IOException {
		SwingUtilities.invokeLater(() -> {
			TreeTablePane<Object> objectTreeTablePane = new TreeTablePane<>(rootNode, headers);
			// Create a JFrame to display the table
			javax.swing.JFrame frame = new javax.swing.JFrame("CSV Tree Viewer");
			frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
			frame.add(objectTreeTablePane);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}

	/**
	 * The entry point of the CSVTree program. This method reads a CSV file, parses the data,
	 * and builds a tree structure based on the specified ID and parent ID columns.
	 *
	 * @param args The command-line arguments. Expected arguments are:
	 *             args[0] - The name of the CSV file to read.
	 *             args[1] - The name of the column representing the unique ID for each row.
	 *             args[2] - The name of the column representing the parent ID for each row.
	 *             args[3] - The character to use as a separator, or the 2-digit hex value of the character to use.
	 *             If nothing is provided, it will default to a comma.
	 */
	public static void main(String[] args) {
		if (args.length < 3 || args.length > 4) {
			System.err.println("Usage: java CSVTree <fileName> <idColumnName> <parentIdColumnName> [<separatorChar>]");
			return;
		}
		char separatorChar = args.length == 3 ? ',' : args[3].length() == 1 ? args[3].charAt(0) : (char) Integer.parseInt(args[3], 16);
		try (BufferedReader reader = new BufferedReader(new FileReader(args[0]))) {
			readAndDisplayTree(reader, args[1], args[2], separatorChar);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Reads CSV data and constructs a tree structure based on specified ID and parent ID columns,
	 * then displays the tree.
	 *
	 * @param reader             The reader to read data from
	 * @param idColumnName       The name of the column representing the unique identifier for each row.
	 * @param parentIdColumnName The name of the column representing the parent identifier for each row.
	 * @param separatorChar      The character used to separate columns in the data.
	 */
	public static void readAndDisplayTree(BufferedReader reader, String idColumnName, String parentIdColumnName, char separatorChar) throws IOException {
		// Get the CSV headers and the indexes of the id and parent id columns:
		CSVTableHeaderInfo headerInfo =
			processCSVHeaders(reader, idColumnName, parentIdColumnName, separatorChar);

		// Create an iterator for the table tree data:
		CSVTreeTableRowIterator rowIterator =
			new CSVTreeTableRowIterator(reader,
				headerInfo.getIdColumnIndex(),
				headerInfo.getParentIdColumnIndex(), separatorChar);

		// Build the tree from the rows:
		DefaultMutableTreeNode rootNode = TreeUtils.buildTree(rowIterator);

		// Display the tree
		displayTree(rootNode, headerInfo.getHeaders());
	}

	/**
	 * Processes the CSV headers from the provided BufferedReader, locates the index of the id and parent id columns,
	 * and returns an object containing header information.
	 *
	 * @param reader             the BufferedReader to read the CSV data from
	 * @param idColumnName       the name of the column representing the id
	 * @param parentIdColumnName the name of the column representing the parent id
	 * @param separatorChar      the character used as a delimiter in the CSV file
	 * @return a CSVTableHeaderInfo object containing the processed headers and the indices of the id and parent id columns
	 * @throws IOException if the header line cannot be read or if the specified id or parent id column names are not found
	 */
	public static CSVTableHeaderInfo processCSVHeaders(BufferedReader reader, String idColumnName, String parentIdColumnName, char separatorChar) throws IOException {
		String headerLine = reader.readLine();
		if (headerLine == null) {
			throw new IOException("No header line found in CSV file");
		}
		String[] headerNames = headerLine.split(String.valueOf(separatorChar), -1);

		// Find the id and parent id columns
		int idColumnIndex = -1;
		int parentIdColumnIndex = -1;
		for (int i = 0; i < headerNames.length; i++) {
			if (headerNames[i].equals(idColumnName)) {
				idColumnIndex = i;
			} else if (headerNames[i].equals(parentIdColumnName)) {
				parentIdColumnIndex = i;
			}
		}
		if (idColumnIndex == -1 || parentIdColumnIndex == -1) {
			throw new IOException("Could not find the specified id or parent id column name in the file.");
		}

		Object[] headers = TreeUtils.removeEntries(headerNames, idColumnIndex, parentIdColumnIndex);

		return new CSVTableHeaderInfo(headers, idColumnIndex, parentIdColumnIndex);
	}
}
