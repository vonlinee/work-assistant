package org.assistant.control.treetable;

import org.assistant.ui.controls.table.TreeTableRow;
import org.assistant.ui.controls.table.TreeUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * An iterator for reading rows from a CSV file and populating {@code TreeUtils.TableTreeRow} objects.
 * <p>
 * This iterator processes each row from the given {@link BufferedReader}, parsing column values
 * into the appropriate fields of a {@code TreeUtils.TableTreeRow} instance. It supports differentiation
 * of ID and parent ID columns, storing remaining values into a separate array of other columns.
 * <p>
 * The iterator assumes the CSV file has a consistent number of columns per row.
 * It splits the input line based on a definable separator (defaulting to comma) and handles empty fields as empty strings.
 */
public class CSVTreeTableRowIterator implements Iterator<TreeTableRow> {

	String line;
	String separatorChar = ",";
	int idColumnIndex;
	int parentIdColumnIndex;
	BufferedReader reader;
	TreeTableRow row = new TreeTableRow();

	/**
	 * Constructs a CSVTableTreeRowIterator for processing rows from a given BufferedReader.
	 *
	 * @param reader              The BufferedReader instance to read CSV rows from.
	 * @param idColumnIndex       The index of the column representing the ID in the CSV data.
	 * @param parentIdColumnIndex The index of the column representing the parent ID in the CSV data.
	 * @throws IOException If an I/O error occurs while initializing the reader.
	 */
	public CSVTreeTableRowIterator(BufferedReader reader, int idColumnIndex, int parentIdColumnIndex) throws IOException {
		this.reader = reader;
		this.idColumnIndex = idColumnIndex;
		this.parentIdColumnIndex = parentIdColumnIndex;
	}

	/**
	 * Constructs a CSVTableTreeRowIterator for processing rows from a given BufferedReader
	 * with a specified separator character for splitting CSV data.
	 *
	 * @param reader              The BufferedReader instance to read CSV rows from.
	 * @param idColumnIndex       The index of the column representing the ID in the CSV data.
	 * @param parentIdColumnIndex The index of the column representing the parent ID in the CSV data.
	 * @param separatorChar       The character used to separate column values in the CSV data.
	 * @throws IOException If an I/O error occurs while initializing the reader.
	 */
	public CSVTreeTableRowIterator(BufferedReader reader, int idColumnIndex, int parentIdColumnIndex, char separatorChar) throws IOException {
		this(reader, idColumnIndex, parentIdColumnIndex);
		this.separatorChar = String.valueOf(separatorChar);
	}

	@Override
	public boolean hasNext() {
		try {
			line = reader.readLine();
		} catch (IOException e) {
			throw new RuntimeException("Problem reading data", e);
		}
		return line != null;
	}

	@Override
	public TreeTableRow next() {
		String[] values = line.split(separatorChar, -1);
		row.setId(values[idColumnIndex]);
		row.setParentId(values[parentIdColumnIndex]);
		row.setRowData(TreeUtils.removeEntries(values, idColumnIndex, parentIdColumnIndex));
		return row;
	}
}
