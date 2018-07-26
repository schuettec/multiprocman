package com.github.schuettec.multiprocman;

import static java.util.Arrays.asList;
import static java.util.Objects.isNull;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import com.github.schuettec.multiprocman.manager.Categories;
import com.github.schuettec.multiprocman.manager.Category;
import com.github.schuettec.multiprocman.manager.ProcessManager;
import com.github.schuettec.multiprocman.themes.ThemeUtil;

public class FindLauncherDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private JList<CategoryDecorator> list;
	private DefaultListModel<CategoryDecorator> model;

	private List<CategoryDecorator> allProcesses;

	AbstractAction disposeAction = new AbstractAction("Cancel") {

		@Override
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	};

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			FindLauncherDialog dialog = new FindLauncherDialog(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class CategoryDecorator {

		private Category category;
		private ProcessDescriptor processDescriptor;

		public CategoryDecorator(Category category, ProcessDescriptor processDescriptor) {
			super();
			this.category = category;
			this.processDescriptor = processDescriptor;
		}

		public ImageIcon getCategoryIcon() {
			return category.getIcon();
		}

		public String getCategoryName() {
			return category.getName();
		}

		public String getCategoryDescription() {
			return category.getDescription();
		}

		public ImageIcon getIcon() {
			return processDescriptor.getIcon();
		}

		public String getTitle() {
			return processDescriptor.getTitle();
		}

		public String getCommand() {
			return processDescriptor.getCommand();
		}

		public ProcessDescriptor getProcessDescriptor() {
			return processDescriptor;
		}

	}

	class ListCellRenderer extends DefaultListCellRenderer {
		private static final int WIDTH = 100;
		private static final int HEIGHT = 70;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
		    boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			CategoryDecorator processDescriptor = (CategoryDecorator) value;
			this.setText(processDescriptor.getTitle());
			this.setToolTipText(processDescriptor.getCommand());
			this.setIcon(processDescriptor.getIcon());

			JPanel container = new JPanel(new BorderLayout());
			container.add(this, BorderLayout.CENTER);
			ImageIcon categoryIcon = processDescriptor.getCategoryIcon();
			Image dimg = categoryIcon.getImage()
			    .getScaledInstance(16, 16, Image.SCALE_SMOOTH);
			JLabel label = new JLabel(processDescriptor.getCategoryName(), new ImageIcon(dimg), JLabel.RIGHT);
			container.add(label, BorderLayout.EAST);
			return container;
		}
	}

	/**
	 * Create the dialog.
	 */
	public FindLauncherDialog(Component parent) {
		ThemeUtil.setLookAndFeel();
		setTitle("Find launcher...");
		setIconImage(Resources.getApplicationIcon());
		setPreferredSize(new Dimension(280, 480));
		this.setSize(new Dimension(280, 480));
		setModal(true);
		this.setLocationRelativeTo(parent);

		String KEY = "closeDialog";
		this.getRootPane()
		    .getActionMap()
		    .put(KEY, disposeAction);
		InputMap im = this.getRootPane()
		    .getInputMap();
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), KEY);

		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		JLabel lblSearchForLauncher = new JLabel("Search launcher:");

		textField = new JTextField();
		textField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				refreshHits();
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					ListSelectionModel selectionModel = list.getSelectionModel();
					int selectedIndex = list.getSelectedIndex();
					selectionModel.clearSelection();
					selectedIndex++;
					if (selectedIndex >= list.getModel()
					    .getSize()) {
						selectedIndex = 0;
					}
					selectionModel.setSelectionInterval(selectedIndex, selectedIndex);
					list.ensureIndexIsVisible(list.getSelectedIndex());
				} else if (e.getKeyCode() == KeyEvent.VK_UP) {
					ListSelectionModel selectionModel = list.getSelectionModel();
					int selectedIndex = list.getSelectedIndex();
					selectionModel.clearSelection();
					selectedIndex--;
					if (selectedIndex < 0) {
						selectedIndex = list.getModel()
						    .getSize() - 1;
					}
					selectionModel.setSelectionInterval(selectedIndex, selectedIndex);
					list.ensureIndexIsVisible(list.getSelectedIndex());
				} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					startSelectedProcess();
				} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					dispose();
				}
			}
		});

		textField.setColumns(10);

		JScrollPane scrollPane = new JScrollPane();
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		    .addGroup(gl_contentPanel.createSequentialGroup()
		        .addComponent(lblSearchForLauncher)
		        .addPreferredGap(ComponentPlacement.UNRELATED)
		        .addComponent(textField, GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE))
		    .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE));
		gl_contentPanel.setVerticalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		    .addGroup(gl_contentPanel.createSequentialGroup()
		        .addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
		            .addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
		                GroupLayout.PREFERRED_SIZE)
		            .addComponent(lblSearchForLauncher))
		        .addPreferredGap(ComponentPlacement.RELATED)
		        .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)));

		this.model = new DefaultListModel<>();
		this.list = new JList<>(model);
		list.setCellRenderer(new ListCellRenderer());
		list.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {

			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					startSelectedProcess();
				} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					dispose();
				}
			}
		});
		list.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				if (e.getClickCount() > 1) {
					startSelectedProcess();
				}
			}

		});
		scrollPane.setViewportView(list);
		contentPanel.setLayout(gl_contentPanel);
		{

			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));

			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setPreferredSize(new Dimension(91, 23));
				okButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						startSelectedProcess();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						FindLauncherDialog.this.dispose();
					}

				});
				cancelButton.setPreferredSize(new Dimension(91, 23));
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		loadListModel();
		refreshHits();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				textField.requestFocus();
			}
		});

		setVisible(true);
	}

	private void refreshHits() {
		String search = textField.getText();
		this.model.clear();
		for (CategoryDecorator pd : allProcesses) {
			if (isNull(search) || search.isEmpty()) {
				model.addElement(pd);
			} else {
				if (pd.getCategoryDescription()
				    .contains(search)
				    || pd.getCategoryName()
				        .contains(search)
				    || pd.getTitle()
				        .contains(search)
				    || pd.getCommand()
				        .contains(search)) {
					model.addElement(pd);
				}
			}
		}
		list.repaint();
	}

	private void loadListModel() {
		this.allProcesses = new LinkedList<>();
		Categories categories = new Categories();
		Enumeration<Category> categoryElements = categories.elements();
		while (categoryElements.hasMoreElements()) {
			Category category = categoryElements.nextElement();
			DefaultListModel<ProcessDescriptor> processDescriptors = category.getProcessTemplates();
			Enumeration<ProcessDescriptor> processDescriptorElements = processDescriptors.elements();
			while (processDescriptorElements.hasMoreElements()) {
				ProcessDescriptor processDescriptor = processDescriptorElements.nextElement();
				allProcesses.add(new CategoryDecorator(category, processDescriptor));
			}
		}
	}

	private void startSelectedProcess() {
		if (list.getSelectedIndex() > -1) {
			CategoryDecorator selectedValue = list.getSelectedValue();
			ProcessManager.startAll(FindLauncherDialog.this, asList(selectedValue.getProcessDescriptor()));
			FindLauncherDialog.this.dispose();
		}
	}
}
