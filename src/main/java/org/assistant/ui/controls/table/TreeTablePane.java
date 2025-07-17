package org.assistant.ui.controls.table;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

public class TreeTablePane<T> extends JScrollPane {

	TreeTable<T> table;

	public TreeTablePane(TreeNode rootNode, Object[] headers) {
		// Build the column model
		TableColumnModel columnModel = TableUtils.buildTableColumnModel(Arrays.asList(headers));

		// Create a TreeTableModelObjectArray from the root node and column model.
		TreeTableModelObjectArray treeTableModel = new TreeTableModelObjectArray(rootNode, columnModel);
		treeTableModel.setShowRoot(false);

		// Create a JTable and set the TreeTableModelObjectArray as its model
		table = new TreeTable<>();
		treeTableModel.bindTable(table);

		// Replace the default look and feel mouse click listener, so it sorts on every click,
		// and resizes the column on a double click to fit the contents, if the cursor is a resize cursor.
		AWTMouseListenerReplacer clickReplacer = new AWTMouseListenerReplacer(
			new HeaderSortResizeMouseClickListener(), MouseEvent.MOUSE_CLICKED, table.getTableHeader());
		clickReplacer.activate();

		// Place the table in a scroll pane
		setViewportView(table);
	}

	/**
	 * A class which intercepts AWTEvents before they are dispatched to any registered listeners for a component
	 * in order to replace the listeners for a particular type of mouse event.
	 * <p>
	 * This allows us to override default behaviour inside various Swing Look and Feels for one type of mouse event,
	 * while preserving all the other behaviours.
	 * <p>
	 * There can be a small performance impact in monitoring all AWTEvents globally, but used sparingly this allows
	 * a clean way to change default behaviour without re-implementing all mouse behaviours associated with a look
	 * and feel.
	 * <p>
	 * It also registers a HierarchyListener to automatically unregister the AWTEvent listener
	 * if the component it is monitoring becomes un-displayable.
	 * This listener is also removed if the AWTEventListener is manually removed.
	 */
	static class AWTMouseListenerReplacer implements AWTEventListener {

		/**
		 * The id of the mouse event we want to replace.
		 */
		protected final int eventReplacementId;
		/**
		 * The mouse listener which implements the new behaviour.
		 * If null, then no replacement listener will be invoked, but the original listener for that event will not run.
		 */
		protected final MouseListener replacementListener;
		/**
		 * The component to monitor AWTEvents for.
		 */
		protected JComponent component;

		/**
		 * Constructs an AWTMouseListenerReplacer.  You must call activate() after construction to make it active.
		 *
		 * @param replacementListener The mouse listener we want to replace the default behaviour with.
		 * @param eventReplacementId  The id of the mouse event we want to replace.
		 * @param component           The component to monitor mouse events for.
		 */
		public AWTMouseListenerReplacer(MouseListener replacementListener, int eventReplacementId, JComponent component) {
			this.eventReplacementId = eventReplacementId;
			this.replacementListener = replacementListener;
			this.component = component;
		}

		/**
		 * A HierarchyListener which watches to see if the component we are monitoring becomes
		 * un-displayable, and unregisters this class and itself as listeners if so.
		 */
		protected HierarchyListener removeListener = new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
					if (!component.isDisplayable()) {
						deactivate();
					}
				}
			}
		};

		/**
		 * Makes the replacement listener active, intercepting AWTEvents.
		 */
		public void activate() {
			Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK);
			component.addHierarchyListener(removeListener);
		}

		/**
		 * Makes the replacement listener inactive, no longer intercepting AWTEvents.
		 */
		public void deactivate() {
			Toolkit.getDefaultToolkit().removeAWTEventListener(this);
			component.removeHierarchyListener(removeListener);
		}

		@Override
		public void eventDispatched(AWTEvent event) {
			if (event.getSource() == component && event.getID() == eventReplacementId && event instanceof MouseEvent) {
				MouseEvent mouseEvent = (MouseEvent) event;
				if (shouldReplace(mouseEvent)) {
					handleWithReplacement(mouseEvent);
					mouseEvent.consume(); // prevent any other mouse listeners from consuming this event.
				}
			}
		}

		/**
		 * A method which subclasses can use to implement additional logic to determine if the event should be
		 * replaced by the new listener.  This class just returns true.
		 *
		 * @param event The event which might be replaced.
		 * @return true if the event should be replaced.
		 */
		protected boolean shouldReplace(MouseEvent event) {
			return true;
		}

		/**
		 * Dispatches the mouse event to the new replacement listener.
		 *
		 * @param event The event for the replacement listener to process.
		 */
		protected void handleWithReplacement(MouseEvent event) {
			if (replacementListener != null) {
				switch (event.getID()) {
					case MouseEvent.MOUSE_PRESSED:
						replacementListener.mousePressed(event);
						break;
					case MouseEvent.MOUSE_RELEASED:
						replacementListener.mouseReleased(event);
						break;
					case MouseEvent.MOUSE_CLICKED:
						replacementListener.mouseClicked(event);
						break;
					case MouseEvent.MOUSE_EXITED:
						replacementListener.mouseExited(event);
						break;
					case MouseEvent.MOUSE_ENTERED:
						replacementListener.mouseEntered(event);
						break;
				}
			}
		}
	}


	/**
	 * A class that implements a mouse click listener for JTableHeader components that will sort on every left
	 * mouse button click, unless the cursor is a resize cursor.  If a resize cursor, then a double click
	 * will resize the column to fit the contents of the rows.
	 */
	static class HeaderSortResizeMouseClickListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e)) {
				JTableHeader header = e.getSource() instanceof JTableHeader ? (JTableHeader) e.getSource() : null;
				if (header != null && header.isEnabled()) {
					if (!TableUtils.isResizeCursor(header.getCursor())) {
						TableUtils.toggleSortColumn(header.getTable(), header.columnAtPoint(e.getPoint()));
					} else if (e.getClickCount() == 2) {
						TableUtils.resizeColumnToFitContents(header.getTable(), header.columnAtPoint(e.getPoint()));
					}
				}
			}
		}
	}
}
