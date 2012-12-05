import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * 漢語大詞典を MVC 風に書いてみる
 * このクラスは controller
 */
public class PseudoHYDC3 extends MouseAdapter implements ActionListener, TreeSelectionListener {

	public final static void main(String[] sArgs) {
		try {
//			Locale.setDefault(Locale.TRADITIONAL_CHINESE);
//			System.out.println(Locale.getDefault());
//			Locale.setDefault(new Locale("zh", "tw"));
			new PseudoHYDC3();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.toString());
			System.exit(1);
		}
	}
	
	PseudoHYDC3View hydc3View;
	PseudoHYDC3Model hydc3Model;

	/**
	 * constructor
	 */
	public PseudoHYDC3() throws Exception {
		hydc3Model = new PseudoHYDC3Model(this);
		hydc3View = new PseudoHYDC3View(this);
	}
	
	public DefaultMutableTreeNode getRadicalTree() throws SQLException {
		return hydc3Model.getRadicalTree();
	}

	public void searchWithCharacter() throws SQLException {
		String[] sCharacters = hydc3Model.searchWithCharacter(hydc3View.getInputText());
		hydc3View.setSelectingList(sCharacters);
	}
	
	public void searchWithWord() throws SQLException {
		String[] sWords = hydc3Model.searchWithWord(hydc3View.getInputText());
		hydc3View.setSelectingList(sWords);
	}

	public void searchWithReading() throws SQLException {
		String[] sCharacters = hydc3Model.searchWithReading(hydc3View.getInputText());
		hydc3View.setSelectingList(sCharacters);
	}

	public void searchWithCode() throws SQLException {
		String sCode = hydc3View.getInputText();
		int code = 0;
		if (sCode.matches("(0[Xx]|U\\+)[0-9A-Fa-f]+")) {
			code = Integer.parseInt(sCode.substring(2), 16);
		} else if (sCode.matches("[0-9]+")) {
			code = Integer.parseInt(sCode);
		} else if (sCode.matches("[0-9A-Fa-f]+")) {
			code = Integer.parseInt(sCode, 16);
		}
		String[] sCharacters = hydc3Model.searchWithCode(code);
		hydc3View.setSelectingList(sCharacters);
	}

	public void searchWithRadical(String sCharacters) {
		String[] sCharacters2 = new String[sCharacters.length()];
		for (int i = 0;  i < sCharacters2.length;  ++i) {
			sCharacters2[i] = sCharacters.substring(i, i + 1);
		}
		hydc3View.setSelectingList(sCharacters2);
	}

	public void searchWithStrokes() throws SQLException {
		int fromStrokes = hydc3View.getFromStrokes();
		int toStrokes = hydc3View.getToStrokes();
		if (fromStrokes <= toStrokes) {
			String[] sCharacters = hydc3Model.searchWithStrokes(fromStrokes, toStrokes);
			hydc3View.setSelectingList(sCharacters);
		}
	}

	public void setDescriptionByCharacter(Clob clob) throws SAXException, IOException, SQLException {
		hydc3View.hydc2ClobToStyledDocument(clob);
	}

	public void setDescriptionByCharacterAndIndex(Clob clob) throws SAXException, IOException, SQLException {
		hydc3View.hydc1ClobToStyledDocument(clob);
	}
	
	public void setDescriptionByWord(Clob clob) throws SAXException, IOException, SQLException {
		hydc3View.hydc9ClobToStyledDocument(clob);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		Cursor cursor = hydc3View.getCursor();
		hydc3View.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		String sCommand = event.getActionCommand();
		try {
			if ("searchWithCharacter".equals(sCommand)) {
				searchWithCharacter();
			} else if ("searchWithWord".equals(sCommand)) {
				searchWithWord();
			} else if ("searchWithReading".equals(sCommand)) {
				searchWithReading();
			} else if ("searchWithCode".equals(sCommand)) {
				searchWithCode();
			} else if ("searchWithStrokes".equals(sCommand)) {
				searchWithStrokes();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		hydc3View.setCursor(cursor);
	}

	@Override
	public void valueChanged(TreeSelectionEvent event) {
		TreePath path = event.getPath();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
		if (!node.isLeaf()) {
			return;
		}
		Cursor cursor = hydc3View.getCursor();
		hydc3View.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		String s = (String)node.getUserObject();
//		int partsStrokes = Integer.parseInt(s.split(" ")[0]);
		String sCharacters = s.split(" ")[2];
		searchWithRadical(sCharacters);
		hydc3View.setCursor(cursor);
	}
	
	@Override
	public void mouseClicked(MouseEvent event) {
		if (event.getClickCount() == 2 && event.getSource() instanceof JList) {
			Cursor cursor = hydc3View.getCursor();
			hydc3View.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			String s = hydc3View.getSelected();
			try {
				hydc3View.clearDescriptionTextPane();
				if (s.matches(".[0-9]+ .+")) {
					int index = Integer.parseInt(s.split(" ")[0].substring(1));
					s = s.substring(0, 1);
					hydc3Model.getDescriptionsByCharacterAndIndex(s, index);
				} else if (s.length() == 1) {
					hydc3Model.getDescriptionsByCharacter(s);
				} else {
					hydc3Model.getDescriptionsByWord(s);
				}
				hydc3View.setDescriptionTextPaneTop();
			} catch (Exception e) {
				e.printStackTrace();
			}
			hydc3View.setCursor(cursor);
		} else if (event.getSource() instanceof JTextPane) {
			JTextPane textPane = (JTextPane)event.getSource();
			Point point = new Point(event.getX(), event.getY());
			int position = textPane.viewToModel(point);
			DefaultStyledDocument document = (DefaultStyledDocument)textPane.getDocument();
			Element element = document.getCharacterElement(position);
			AttributeSet attributeSet = element.getAttributes();
			String sLink = (String)attributeSet.getAttribute("link");
			if (sLink != null) {
				Cursor cursor = hydc3View.getCursor();
				hydc3View.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				try {
					if (sLink.matches(".")) {
						hydc3View.clearDescriptionTextPane();
						hydc3Model.getDescriptionsByCharacter(sLink);
						hydc3View.setDescriptionTextPaneTop();
					} else if (sLink.matches(".[0-9]+")) {
						int index = Integer.parseInt(sLink.substring(1));
						hydc3View.clearDescriptionTextPane();
						hydc3Model.getDescriptionsByCharacterAndIndex(sLink.substring(0, 1), index);
						hydc3View.setDescriptionTextPaneTop();
					} else {
						HashMap<String, Integer> wordPositionMap = hydc3View.getWordPositionMap();
						Integer iPosition = wordPositionMap.get(sLink);
						if (iPosition != null) {
							hydc3View.setDescriptionTextPaneCaretPosition(iPosition);
						} else {
							hydc3View.clearDescriptionTextPane();
							hydc3Model.getDescriptionsByWord(sLink);
							hydc3View.setDescriptionTextPaneTop();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				hydc3View.setCursor(cursor);
			}
		}
	}

	Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
	Cursor textCursor = new Cursor(Cursor.TEXT_CURSOR);

	@Override
	public void mouseMoved(MouseEvent event) {
		JTextPane textPane = (JTextPane)event.getSource();
		Point point = new Point(event.getX(), event.getY());
		int position = textPane.viewToModel(point);
		DefaultStyledDocument document = (DefaultStyledDocument)textPane.getDocument();
		Element element = document.getCharacterElement(position);
		AttributeSet attributeSet = element.getAttributes();
		String sLink = (String)attributeSet.getAttribute("link");
		if (sLink != null) {
			textPane.setCursor(handCursor);
		} else {
			textPane.setCursor(textCursor);
		}
	}
}

/**
 * view class
 * @author beu
 */
class PseudoHYDC3View {

	PseudoHYDC3 controller;
	JFrame frame;
	JTextField inputTextField;
	JTextField fromStrokesTextField;
	JTextField toStrokesTextField;
	JList selectingList;
	JTextPane descriptionTextPane;

	/**
	 * constructor
	 * @param controller is the parent object
	 * @throws Exception
	 */
	public PseudoHYDC3View(PseudoHYDC3 controller) throws Exception {
		this.controller = controller;
		{
			String sFontName = "DFKai-SB2"/*"PMingLiU"*/;
			if (System.getProperty("hydc3.fontName") != null) {
				sFontName = System.getProperty("hydc3.fontName");
			}
			UIManager.put("TextPane.font",
					new Font(sFontName,
					UIManager.getFont("TextPane.font").getStyle(),
					UIManager.getFont("TextPane.font").getSize()));
			UIManager.put("List.font",
					new Font(sFontName,
					UIManager.getFont("List.font").getStyle(),
					UIManager.getFont("List.font").getSize()));
			UIManager.put("TextField.font",
					new Font(sFontName,
					UIManager.getFont("TextField.font").getStyle(),
					UIManager.getFont("TextField.font").getSize()));
		}
		frame = new JFrame("漢語大詞典もどき");
		frame.setPreferredSize(new Dimension(800, 600));
		{
			JMenuBar menuBar = new JMenuBar();
			{
				JMenu menu = new JMenu("File");
				{
					JMenuItem menuItem = new JMenuItem("Exit");
					menuItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent event) {
							System.exit(0);
						}
					});
					menu.add(menuItem);
				}
				menuBar.add(menu);
			}
			{
				JMenu menu = new JMenu("Help");
				{
					JMenuItem menuItem = new JMenuItem("About...");
					menuItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent event) {
							JOptionPane.showMessageDialog(frame, "漢語大詞典もどき  version 0.1  by Beu", "about", JOptionPane.INFORMATION_MESSAGE);
						}
					});
					menu.add(menuItem);
				}
				menuBar.add(menu);
			}
			frame.setJMenuBar(menuBar);
			JPanel rootPanel = new JPanel(new BorderLayout());
			{
				JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
				{
					JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
					{
						JTabbedPane tabbedPane = new JTabbedPane();
						{
							JPanel panel = new JPanel();
							BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
							panel.setLayout(layout);
							{
								JPanel panel2 = new JPanel();
								{
									inputTextField = new JTextField(8);
									Font font = inputTextField.getFont();
									font = font.deriveFont(font.getSize2D() * 3);
									inputTextField.setFont(font);
									panel2.add(inputTextField);
								}
								panel.add(panel2);
							}
							{
								JPanel panel2 = new JPanel();
								{
									JButton button = new JButton("字");
									button.setActionCommand("searchWithCharacter");
									button.addActionListener(controller);
									panel2.add(button);
								}
								{
									JButton button = new JButton("詞");
									button.setActionCommand("searchWithWord");
									button.addActionListener(controller);
									panel2.add(button);
								}
								{
									JButton button = new JButton("音");
									button.setActionCommand("searchWithReading");
									button.addActionListener(controller);
									panel2.add(button);
								}
								{
									JButton button = new JButton("碼");
									button.setActionCommand("searchWithCode");
									button.addActionListener(controller);
									panel2.add(button);
								}
								panel.add(panel2);
							}
							tabbedPane.addTab("輸入", panel);
						}
						{
							DefaultMutableTreeNode root = controller.getRadicalTree();
							JTree tree = new JTree(root);
							Font font = tree.getFont();
							font = font.deriveFont(font.getSize2D() * 1.5F);
							tree.setFont(font);
							tree.addTreeSelectionListener(controller);
							JScrollPane scrollPane = new JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
							tabbedPane.addTab("部首", scrollPane);
						}
						{
							JPanel panel = new JPanel();
							BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
							panel.setLayout(layout);
							{
								JPanel panel2 = new JPanel();
								panel2.add(new JLabel("從"));
								{
									fromStrokesTextField = new JTextField("1", 2);
									Font font = fromStrokesTextField.getFont();
									font = font.deriveFont(font.getSize2D() * 2);
									fromStrokesTextField.setFont(font);
									fromStrokesTextField.setHorizontalAlignment(JTextField.RIGHT);
									panel2.add(fromStrokesTextField);
								}
								panel2.add(new JLabel("到"));
								{
									toStrokesTextField = new JTextField("10", 2);
									Font font = toStrokesTextField.getFont();
									font = font.deriveFont(font.getSize2D() * 2);
									toStrokesTextField.setFont(font);
									toStrokesTextField.setHorizontalAlignment(JTextField.RIGHT);
									panel2.add(toStrokesTextField);
								}
								panel.add(panel2);
							}
							{
								JPanel panel2 = new JPanel();
								{
									JButton button = new JButton("査");
									button.setActionCommand("searchWithStrokes");
									button.addActionListener(controller);
									panel2.add(button);
								}
								panel.add(panel2);
							}
							tabbedPane.addTab("畫數", panel);
						}
						splitPane2.add(tabbedPane, JSplitPane.TOP);
					}
					{
						selectingList = new JList();
						Font font = selectingList.getFont();
						font = font.deriveFont(font.getSize2D() * 2);
						selectingList.setFont(font);
						selectingList.addMouseListener(controller);
						JScrollPane scrollPane = new JScrollPane(selectingList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
						scrollPane.setBorder(new TitledBorder("候補"));
						splitPane2.add(scrollPane, JSplitPane.BOTTOM);
					}
					splitPane.add(splitPane2, JSplitPane.LEFT);
				}
				{
					descriptionTextPane = new JTextPane();
					Font font = descriptionTextPane.getFont();
					font = font.deriveFont(font.getSize2D() * 1.5F);
					descriptionTextPane.setFont(font);
					descriptionTextPane.addMouseListener(controller);
					descriptionTextPane.addMouseMotionListener(controller);
					JScrollPane scrollPane = new JScrollPane(descriptionTextPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					splitPane.add(scrollPane, JSplitPane.RIGHT);
				}
				rootPanel.add(splitPane, BorderLayout.CENTER);
			}
			{
				JPanel panel = new JPanel(new BorderLayout());
				{
					JLabel label = new JLabel("status");
					panel.add(label, BorderLayout.WEST);
				}
				rootPanel.add(panel, BorderLayout.SOUTH);
			}
			frame.setContentPane(rootPanel);
		}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	public String getInputText() {
		return inputTextField.getText();
	}

	public int getFromStrokes() {
		int strokes = 0;
		try {
			strokes = Integer.parseInt(fromStrokesTextField.getText());
		} catch (Exception e) {
			fromStrokesTextField.setText("0");
		}
		return strokes;
	}
	
	public int getToStrokes() {
		int strokes = 0;
		try {
			strokes = Integer.parseInt(toStrokesTextField.getText());
		} catch (Exception e) {
			toStrokesTextField.setText("0");
		}
		return strokes;
	}

	public void setSelectingList(String[] sCharacters) {
		selectingList.removeAll();
		selectingList.setListData(sCharacters);
	}
	
	public String getSelected() {
		return (String)selectingList.getSelectedValue();
	}

	public void clearDescriptionTextPane() throws BadLocationException {
//		descriptionTextPane.removeAll();
		StyledDocument document = descriptionTextPane.getStyledDocument();
		document.remove(0, document.getLength());
	}
	
	public void setDescriptionTextPaneTop() {
		descriptionTextPane.setCaretPosition(0);
	}
	
	public void setDescriptionTextPaneCaretPosition(int position) {
		descriptionTextPane.setCaretPosition(position);
	}
	
	public void setCursor(Cursor cursor) {
		frame.setCursor(cursor);
	}
	
	public Cursor getCursor() {
		return frame.getCursor();
	}

	public void hydc1ClobToStyledDocument(Clob clob) throws SAXException, IOException, SQLException {
		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		xmlReader.setContentHandler(new HYDC1ContentHandler());
		xmlReader.parse(new InputSource(clob.getCharacterStream()));
	}

	public void hydc2ClobToStyledDocument(Clob clob) throws SAXException, IOException, SQLException {
		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		xmlReader.setContentHandler(new HYDC2ContentHandler());
		xmlReader.parse(new InputSource(clob.getCharacterStream()));
	}

	public void hydc9ClobToStyledDocument(Clob clob) throws SAXException, IOException, SQLException {
		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		xmlReader.setContentHandler(new HYDC9ContentHandler());
		xmlReader.parse(new InputSource(clob.getCharacterStream()));
	}

	HashMap<String, Integer> wordPositionMap = null;
	public HashMap<String, Integer> getWordPositionMap() {
		return wordPositionMap;
	}
	
	class HYDC1ContentHandler extends HYDCContentHandler {
		
		String sText = null;
		SimpleAttributeSet attributeSet = null;

		@Override
		public void path(Timing timing, String sPath, String s) {
			sText = null;
			if (sPath.equals("/ZI/ZMLB") && timing == Timing.START) {

			} else if (sPath.equals("/ZI/ZMLB/ZM") && timing == Timing.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setFontSize(attributeSet, 72);
				sText = s;
			} else if (sPath.equals("/ZI/ZMLB/XH") && timing == Timing.CHARACTERS) {
				attributeSet = null;
				sText = s + "\n";
			} else if (sPath.equals("/ZI/ZMLB/YD") && timing == Timing.START) {
				
			} else if (sPath.equals("/ZI/ZMLB/YD/PYLB/PY") && timing == Timing.CHARACTERS) {
				attributeSet = null;
				sText = "拼音:" + s + " ";
			} else if (sPath.equals("/ZI/ZMLB/YD/PYLB/ZY") && timing == Timing.CHARACTERS) {
				sText = "注音:" + s + " ";
			} else if (sPath.equals("/ZI/ZMLB/YD/PYLB") && timing == Timing.END) {
				sText = "\n";
			} else if (sPath.equals("/ZI/ZMLB/YD/GY") && timing == Timing.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setFontSize(attributeSet, 24);
				sText = s;
			} else if (sPath.equals("/ZI/ZMLB/YD") && timing == Timing.END) {
				attributeSet = null;
				sText = "\n\n";
			} else if (sPath.equals("/ZI/ZMLB/GL") && timing == Timing.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setFontSize(attributeSet, 24);
				sText = s + "\n";
			} else if (sPath.equals("/ZI/ZMLB/ZMSY") && timing == Timing.START) {
				
			} else if (sPath.equals("/ZI/ZMLB/ZMSY/SYLB") && timing == Timing.START) {
				attributeSet = null;
			} else if (sPath.equals("/ZI/ZMLB/ZMSY/SYLB/XH") && timing == Timing.CHARACTERS) {
				attributeSet = null;
				sText = "(" + s + ") ";
			} else if (sPath.equals("/ZI/ZMLB/ZMSY/SYLB/SY") && timing == Timing.CHARACTERS) {
				sText = s + "\n";
			} else if (sPath.equals("/ZI/ZMLB/ZMSY/SYLB/LZ") && timing == Timing.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setFontSize(attributeSet, 24);
				sText = s + "\n";
			} else if (sPath.equals("/ZI/ZMLB/ZMSY/SYLB") && timing == Timing.END) {
				attributeSet = null;
				sText = "\n";
			} else if (sPath.equals("/ZI/CI/CMLB") && timing == Timing.START) {
				attributeSet = null;
			} else if (sPath.equals("/ZI/CI/CMLB/CM/CY") && timing == Timing.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setForeground(attributeSet, Color.BLUE);
				StyleConstants.setUnderline(attributeSet, true);
				attributeSet.addAttribute("link", s);
				sText = s;
			} else if (sPath.equals("/ZI/CI/CMLB/CM/LJXH") && timing == Timing.CHARACTERS) {
				attributeSet = null;
				sText = " ";
			} else if (sPath.equals("/ZI/CI/CMLB") && timing == Timing.END) {
				sText = "\n\n";
			} else if (sPath.equals("/ZI/CI/CM/CY") && timing == Timing.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setFontSize(attributeSet, 36);
				sText = "【" + s + "】\n";
				wordPositionMap.put(s, document.getLength());
			} else if (sPath.equals("/ZI/CI/CM/LJXH") && timing == Timing.CHARACTERS) {
				// ignore
			} else if (sPath.equals("/ZI/CI/CM/CMSY/GL") && timing == Timing.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setFontSize(attributeSet, 24);
				sText = s + "\n";
			} else if (sPath.equals("/ZI/CI/CM/CMSY/SYLB") && timing == Timing.START) {
				attributeSet = null;
			} else if (sPath.equals("/ZI/CI/CM/CMSY/SYLB/XH") && timing == Timing.CHARACTERS) {
				sText = "(" + s + ") ";
			} else if (sPath.equals("/ZI/CI/CM/CMSY/SYLB/SY") && timing == Timing.CHARACTERS) {
				sText = s + "\n";
			} else if (sPath.equals("/ZI/CI/CM/CMSY/SYLB/LZ") && timing == Timing.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setFontSize(attributeSet, 24);
				sText = s + "\n";
			} else if (sPath.equals("/ZI/CI/CM/CMSY") && timing == Timing.END) {
				attributeSet = null;
				sText = "\n";
			}
			if (sText != null) {
				insertString(sText, attributeSet == null ? attributeSet0 : attributeSet);
			}
		}
	}
	
	class HYDC2ContentHandler extends HYDCContentHandler {

		String sText = null;
		SimpleAttributeSet attributeSet = null;
		
		@Override
		public void path(Timing timing, String sPath, String s) {
			sText = null;
			if (sPath.equals("/ZTM/ZI/ZM") && timing == Timing.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setFontSize(attributeSet, 72);
				sText = s + "\n";
			} else if (sPath.equals("/ZTM/ZI/ZMLB") && timing == Timing.START) {
				attributeSet = null;
				sText = "\n";
			} else if (sPath.equals("/ZTM/ZI/ZMLB/XH") && timing == Timing.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setFontSize(attributeSet, 48);
				sText = s + ".\n";
			} else if (sPath.equals("/ZTM/ZI/ZMLB/YD/PYLB/PY") && timing == Timing.CHARACTERS) {
				attributeSet = null;
				sText = "拼音:" + s + " ";
			} else if (sPath.equals("/ZTM/ZI/ZMLB/YD/PYLB/ZY") && timing == Timing.CHARACTERS) {
				sText = "注音:" + s + " ";
			} else if (sPath.equals("/ZTM/ZI/ZMLB/YD/PYLB/DY") && timing == Timing.CHARACTERS) {
				sText = "\n";
			} else if (sPath.equals("/ZTM/ZI/ZMLB/YD/GY") && timing == Timing.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setFontSize(attributeSet, 24);
				sText = s;
			} else if (sPath.equals("/ZTM/ZI/ZMLB/YD") && timing == Timing.END) {
				sText = "\n";
			} else if (sPath.equals("/ZTM/ZI/ZMLB/GL") && timing == Timing.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setFontSize(attributeSet, 24);
				sText = s;
			} else if (sPath.equals("/ZTM/ZI/ZMLB/ZMSY") && timing == Timing.START) {
				attributeSet = null;
				sText = "\n";
			} else if (sPath.equals("/ZTM/ZI/ZMLB/ZMSY/SYLB/XH") && timing == Timing.CHARACTERS) {
				attributeSet = null;
				sText = "(" + s + ") ";
			} else if (sPath.equals("/ZTM/ZI/ZMLB/ZMSY/SYLB/SY") && timing == Timing.CHARACTERS) {
				sText = s + "\n";
			} else if (sPath.equals("/ZTM/ZI/ZMLB/ZMSY/SYLB/LZ") && timing == Timing.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setFontSize(attributeSet, 24);
				sText = s + "\n";
			} else if (sPath.equals("/ZTM/ZI/ZMLB/ZMSY/SYLB") && timing == Timing.END) {
				attributeSet = null;
				sText = "\n";
			} else if (sPath.equals("/ZTM/CI/CMLB/CM/CY") && timing == Timing.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setForeground(attributeSet, Color.BLUE);
				StyleConstants.setUnderline(attributeSet, true);
				attributeSet.addAttribute("link", s);
				sText = s;
			} else if (sPath.equals("/ZTM/CI/CMLB/CM/LJXH") && timing == Timing.CHARACTERS) {
				attributeSet = null;
				sText = " ";
			} else if (sPath.equals("/ZTM/CI/CMLB") && timing == Timing.END) {
				attributeSet = null;
				sText = "\n\n";
			} else if (sPath.equals("/ZTM/CI/CM/CY") && timing == Timing.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setFontSize(attributeSet, 48);
				sText = "【" + s + "】\n";
				wordPositionMap.put(s, document.getLength());
			} else if (sPath.equals("/ZTM/CI/CM/LJXH") && timing == Timing.CHARACTERS) {
			} else if (sPath.equals("/ZTM/CI/CM/CMSY/GL") && timing == Timing.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setFontSize(attributeSet, 24);
				sText = s + "\n";
			} else if (sPath.equals("/ZTM/CI/CM/CMSY/SYLB/XH") && timing == Timing.CHARACTERS) {
				attributeSet = null;
				sText = "(" + s + ") ";
			} else if (sPath.equals("/ZTM/CI/CM/CMSY/SYLB/SY") && timing == Timing.CHARACTERS) {
				attributeSet = null;
				sText = s + "\n";
			} else if (sPath.equals("/ZTM/CI/CM/CMSY/SYLB/LZ") && timing == Timing.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setFontSize(attributeSet, 24);
				sText = s + "\n";
			} else if (sPath.equals("/ZTM/CI/CM/CMSY") && timing == Timing.END) {
				attributeSet = null;
				sText = "\n";
			}
			if (sText != null) {
				insertString(sText, attributeSet == null ? attributeSet0 : attributeSet);
			}
		}
	}

	class HYDC9ContentHandler extends HYDCContentHandler {

		String sText = "";
		SimpleAttributeSet attributeSet = null;

		@Override
		public void path(Timing timing, String path, String s) {
			sText = "";
			if (sPath.equals("/CM/CY") && timing == Timing.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setFontSize(attributeSet, 48);
				sText = "【" + s + "】\n";
			} else if (sPath.equals("/CM/CMSY/GL") && timing == Timing.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setFontSize(attributeSet, 24);
				sText = s + "\n";
			} else if (sPath.equals("/CM/CMSY/SYLB") && timing == Timing.START) {
				attributeSet = null;
				sText = "\n";
			} else if (sPath.equals("/CM/CMSY/SYLB/XH") && timing == Timing.CHARACTERS) {
				attributeSet = null;
				sText = "(" + s + ") ";
			} else if (sPath.equals("/CM/CMSY/SYLB/SY") && timing == Timing.CHARACTERS) {
				attributeSet = null;
				sText = s + "\n";
			} else if (sPath.equals("/CM/CMSY/SYLB/LZ") && timing == Timing.CHARACTERS) {
				attributeSet = new SimpleAttributeSet(attributeSet0);
				StyleConstants.setFontSize(attributeSet, 24);
				sText = s + "\n";
			} else if (sPath.equals("/CM/CMSY") && timing == Timing.END) {
				attributeSet = null;
				sText = "\n";
			}
			if (sText != null) {
				insertString(sText, attributeSet == null ? attributeSet0 : attributeSet);
			}
		}
	}

	public static enum Timing {START, END, CHARACTERS};

	abstract class HYDCContentHandler implements ContentHandler {

		StyledDocument document;
		SimpleAttributeSet attributeSet0;

		public HYDCContentHandler() {
			wordPositionMap = new HashMap<String, Integer>();
			document = descriptionTextPane.getStyledDocument();
			attributeSet0 = new SimpleAttributeSet();
			StyleConstants.setFontSize(attributeSet0, 32);
		}

		@Override
		public void startDocument() throws SAXException {}
		@Override
		public void endDocument() throws SAXException {}

		String sPath = "";

		@Override
		public void startElement(String sUri, String sLocalName, String name, Attributes attributes) throws SAXException {
			sPath += "/" + sLocalName;
			path(Timing.START, sPath, null);
		}
		@Override
		public void endElement(String sUri, String sLocalName, String sName) throws SAXException {
			path(Timing.END, sPath, null);
			sPath = sPath.substring(0, sPath.lastIndexOf("/" + sLocalName));
		}
		@Override
		public void characters(char[] characters, int start, int length) throws SAXException {
			path(Timing.CHARACTERS, sPath, new String(characters, start, length));
		}

		public abstract void path(Timing timing, String sPath, String s);

		@Override
		public void startPrefixMapping(String prefix, String uri) throws SAXException {}
		@Override
		public void endPrefixMapping(String prefix) throws SAXException {}
		@Override
		public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
		@Override
		public void processingInstruction(String target, String data) throws SAXException {}
		@Override
		public void setDocumentLocator(Locator locator) {}
		@Override
		public void skippedEntity(String name) throws SAXException {}
		
		public void insertString(String sText, SimpleAttributeSet attributeSet) {
			int i = sText.indexOf("<u");
			if (i < 0) {
				i = sText.indexOf("<a");
				if (i < 0) {
					try {
						document.insertString(document.getLength(), sText, attributeSet);
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
					return;
				} else {
					// <a>
					if (i > 0) {
						insertString(sText.substring(0, i), attributeSet);
						sText = sText.substring(i);
					}
					i = sText.indexOf("'");
					sText = sText.substring(i + 1);
					i = sText.indexOf("'");
					String sLink = sText.substring(0, i);
					i = sText.indexOf(">");
					sText = sText.substring(i + 1);
					i = sText.indexOf("</a>");
					SimpleAttributeSet attributeSet2 = new SimpleAttributeSet(attributeSet);
					StyleConstants.setForeground(attributeSet2, Color.BLUE);
					StyleConstants.setUnderline(attributeSet2, true);
					attributeSet2.addAttribute("link", sLink);
					insertString(sText.substring(0, i), attributeSet2);
					sText = sText.substring(i + "</a>".length());
					if (sText.length() > 0) {
						insertString(sText, attributeSet);
					}
				}
			} else {
				// <u>
				if (i > 0) {
					insertString(sText.substring(0, i), attributeSet);
					sText = sText.substring(i);
				}
				i = sText.indexOf("</u>");
				SimpleAttributeSet attributeSet2 = new SimpleAttributeSet(attributeSet);
				StyleConstants.setForeground(attributeSet2, Color.RED);
				insertString(sText.substring("<u>".length(), i), attributeSet2);
				sText = sText.substring(i + "</u>".length());
				if (sText.length() > 0) {
					insertString(sText, attributeSet);
				}
			}
		}
	}
}


/**
 * Model class
 * @author beu
 */
class PseudoHYDC3Model {
	
	final static String DATABASE_PATH = "database";
//	final static String DATABASE_PATH = "jar:(/inetpub/wwwroot/comp/PseudoHYDC3/PseudoHYDC3.jar)database";

	PseudoHYDC3 controller;
	DatabaseConnector connector = new DatabaseConnector();
	Connection connection;

	/**
	 * constructor
	 * @param controller is the parent object
	 * @throws Exception
	 */
	public PseudoHYDC3Model(PseudoHYDC3 controller) throws Exception {
		this.controller = controller;
		connection = connector.connect(DATABASE_PATH);
	}

	final static String RADICALS
			= "一丨丶丿乙亅二亠人儿入八冂冖冫几"
			+ "凵刀力勹匕匚匸十卜卩厂厶又口囗土"
			+ "士夂夊夕大女子宀寸小尢尸屮山巛工"
			+ "己巾干幺广廴廾弋弓彐彡彳心戈戶手"
			+ "支攴文斗斤方无日曰月木欠止歹殳毋"
			+ "比毛氏气水火爪父爻爿片牙牛犬玄玉"
			+ "瓜瓦甘生用田疋疒癶白皮皿目矛矢石"
			+ "示禸禾穴立竹米糸缶网羊羽老而耒耳"
			+ "聿肉臣自至臼舌舛舟艮色艸虍虫血行"
			+ "衣襾見角言谷豆豕豸貝赤走足身車辛"
			+ "辰辵邑酉釆里金長門阜隶隹雨靑非面"
			+ "革韋韭音頁風飛食首香馬骨高髟鬥鬯"
			+ "鬲鬼魚鳥鹵鹿麥麻黃黍黑黹黽鼎鼓鼠"
			+ "鼻齊齒龍龜龠";
	final static String HYDC_RADICALS
			= "一丨丶丿乙　　亠人儿　八冂冖冫几"
			+ "凵刀力勹匕匚　十卜卩厂厶又口囗土"
			+ "　夂　夕大女子宀寸小尢尸屮山巛工"
			+ "己巾干幺广廴廾弋弓彐彡彳心戈戶手"
			+ "支攴文斗斤方旡日　月木欠止歹殳毋"
			+ "比毛氏气水火爪父　爿片牙牛犬　王"	// 玉異畫數
			+ "瓜瓦甘生　田疋疒癶白皮皿目矛矢石"
			+ "示　禾穴立竹米糸缶网羊羽老而耒耳"
			+ "聿肉臣自至臼舌　舟艮色艸虍虫血　"
			+ "衣襾見角言谷豆豕豸貝赤走足身車辛"
			+ "辰辵邑酉釆里金長門阜隶隹雨青非面"
			+ "革韋韭音頁風飛食首香馬骨高髟鬥　"
			+ "鬲鬼魚鳥鹵鹿麥麻黃黍黑黹黽鼎鼓鼠"
			+ "鼻齊齒龍龜龠";
	final static int[] RADICAL_STROKES = {
		1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4,
		4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
		4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 4/*王5玉*/,
		5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
		5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
		6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
		6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
		7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9,
		9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10,
		10, 10, 11, 11, 11, 11, 11, 11, 12, 12, 12, 12, 13, 13, 13, 13,
		14, 14, 15, 16, 16, 17
	};
	/**
	 * get 部首 tree view
	 * @returns root tree node object
	 * @throws SQLException
	 */
	public DefaultMutableTreeNode getRadicalTree() throws SQLException {
		TreeMap<Integer, String> strokesAndRadicalsMap = new TreeMap<Integer, String>();
		for (int i = 0;  i < RADICAL_STROKES.length;  ++i) {
			String sRadical = "" + HYDC_RADICALS.charAt(i);
			if (sRadical.equals("　")) {
				continue;
			}
			int strokes = RADICAL_STROKES[i];
			String sRadicals = strokesAndRadicalsMap.get(strokes);
			if (sRadicals == null) {
				strokesAndRadicalsMap.put(strokes, sRadicals = "");
			}
			strokesAndRadicalsMap.put(strokes, sRadicals + sRadical);
		}
		String sSql = "SELECT parts_strokes, unicode FROM HYDC3 "
				+ "WHERE bushou = ? "
				+ "ORDER BY parts_strokes, unicode";
		PreparedStatement statement = connection.prepareStatement(sSql);

		DefaultMutableTreeNode root = new DefaultMutableTreeNode("畫數 / 部首 / 畫數");
		for (Integer strokes: strokesAndRadicalsMap.keySet()) {
			String sRadicals = strokesAndRadicalsMap.get(strokes);
			DefaultMutableTreeNode strokesNode = new DefaultMutableTreeNode(strokes.toString() + " : " + sRadicals);
			for (int i = 0;  i < sRadicals.length();  ++i) {
				String sRadical = sRadicals.substring(i, i + 1);
				DefaultMutableTreeNode radicalNode = new DefaultMutableTreeNode(sRadical);

				TreeMap<Integer, String> partsStrokesAndCharactersMap = new TreeMap<Integer, String>();
				statement.setString(1, sRadical);
				ResultSet resultSet = statement.executeQuery();
				while (resultSet.next()) {
					short partsStrokes = resultSet.getShort("parts_strokes");
					int unicode = resultSet.getInt("unicode");
					String sCharacters = partsStrokesAndCharactersMap.get((Integer)(int)partsStrokes);
					if (sCharacters == null) {
						partsStrokesAndCharactersMap.put((Integer)(int)partsStrokes, sCharacters = "");
					}
					sCharacters += new String(new int[]{unicode}, 0, 1);
					partsStrokesAndCharactersMap.put((Integer)(int)partsStrokes, sCharacters);
				}
				resultSet.close();
				strokesNode.add(radicalNode);
				for (Integer partsStrokes: partsStrokesAndCharactersMap.keySet()) {
					String sCharacters = partsStrokesAndCharactersMap.get(partsStrokes);
					DefaultMutableTreeNode partsStrokesNode = new DefaultMutableTreeNode(partsStrokes.toString() + " : " + sCharacters);
					radicalNode.add(partsStrokesNode);
				}
				strokesNode.add(radicalNode);
			}
			root.add(strokesNode);
		}

		statement.close();
		return root;
	}
	
	public String[] searchWithCharacter(String sCharacters) throws SQLException {
		String sSql = "SELECT COUNT(*) FROM hydc3 "
				+ "WHERE unicode = ?";
		PreparedStatement statement = connection.prepareStatement(sSql);
		TreeSet<String> sCharacterSet = new TreeSet<String>();
		for (int i = 0;  i < sCharacters.length();  ++i) {
			String sCharacter = sCharacters.substring(i, i + 1);
			statement.setInt(1, sCharacter.codePointAt(0));
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				int count = resultSet.getInt(1);
				if (count > 0) {
					sCharacterSet.add(sCharacter);
				}
			}
			resultSet.close();
		}
		statement.close();
		return sCharacterSet.toArray(new String[sCharacterSet.size()]);
	}
	
	public String[] searchWithWord(String sWord) throws SQLException {
		if (sWord == null || sWord.equals("")) {
			return new String[0];
		}
		TreeSet<String> sWordSet = new TreeSet<String>();
		String sSql = "SELECT ci FROM hydc9 "
			+ "WHERE ci LIKE '%'||?||'%' ";
		PreparedStatement statement = connection.prepareStatement(sSql);
		statement.setString(1, sWord);
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next()) {
			String sCi = resultSet.getString("ci");
			sWordSet.add(sCi);
		}
		resultSet.close();
		statement.close();
		return sWordSet.toArray(new String[sWordSet.size()]);
	}
	
	public String[] searchWithReading(String sReading) throws SQLException {
		if (sReading == null || sReading.equals("")) {
			return new String[0];
		}
		TreeSet<String> sCharacterSet = new TreeSet<String>();
		String sSql = "SELECT hydc3.unicode, hydc4.index, hydc4.pinyin FROM hydc4 AS hydc4, hydc3 AS hydc3 "
				+ "WHERE (LCASE(hydc4.pinyin) = LCASE(?) OR hydc4.zhuyin = ?) "
				+ "AND hydc3.gbk = hydc4.gbk";
		PreparedStatement statement = connection.prepareStatement(sSql);
		int i = 1;
		statement.setString(i++, sReading);
		statement.setString(i++, sReading);
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next()) {
			int unicode = resultSet.getInt("unicode");
			short index = resultSet.getShort("index");
			String sPinyin = resultSet.getString("pinyin");
			sCharacterSet.add(new String(new int[] {unicode}, 0, 1) + Short.toString(index) + " (" + sPinyin + ")");
		}
		resultSet.close();
		statement.close();
		sSql = "SELECT hydc3.unicode, hydc6.index, hydc6.pinyin FROM hydc6 AS hydc6, hydc3 AS hydc3 "
				+ "WHERE (UCASE(hydc6.pinyinA) = UCASE(?) OR UCASE(hydc6.pinyinA || hydc6.sisheng) = UCASE(?) "
				+ "OR LCASE(hydc6.pinyin) = LCASE(?) OR hydc6.zhuyin = ?) "
				+ "AND hydc3.gbk = hydc6.gbk";
		statement = connection.prepareStatement(sSql);
		i = 1;
		statement.setString(i++, sReading);
		statement.setString(i++, sReading);
		statement.setString(i++, sReading);
		statement.setString(i++, sReading);
		resultSet = statement.executeQuery();
		while (resultSet.next()) {
			int unicode = resultSet.getInt("unicode");
			short index = resultSet.getShort("index");
			String sPinyin = resultSet.getString("pinyin");
			sCharacterSet.add(new String(new int[] {unicode}, 0, 1) + Short.toString(index) + " (" + sPinyin + ")");
		}
		resultSet.close();
		statement.close();
		return sCharacterSet.toArray(new String[sCharacterSet.size()]);
	}

	public String[] searchWithCode(int code) throws SQLException {
		TreeSet<String> sCharacterSet = new TreeSet<String>();
		String sSql = "SELECT unicode FROM hydc3 "
				+ "WHERE unicode = ? OR gbk = ? OR big5 = ?";
		PreparedStatement statement = connection.prepareStatement(sSql);
		int i = 1;
		statement.setInt(i++, code);
		statement.setInt(i++, code);
		statement.setInt(i++, code);
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next()) {
			int unicode = resultSet.getInt("unicode");
			sCharacterSet.add(new String(new int[] {unicode}, 0, 1));
		}
		resultSet.close();
		statement.close();
		return sCharacterSet.toArray(new String[sCharacterSet.size()]);
	}
	
	public String[] searchWithStrokes(int fromStrokes, int toStrokes) throws SQLException {
		TreeSet<String> sCharacterSet = new TreeSet<String>();
		String sSql = "SELECT unicode FROM hydc3 "
				+ "WHERE strokes >= ? AND strokes <= ?";
		PreparedStatement statement = connection.prepareStatement(sSql);
		int i = 1;
		statement.setInt(i++, fromStrokes);
		statement.setInt(i++, toStrokes);
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next()) {
			int unicode = resultSet.getInt("unicode");
			sCharacterSet.add(new String(new int[] {unicode}, 0, 1));
		}
		resultSet.close();
		statement.close();
		return sCharacterSet.toArray(new String[sCharacterSet.size()]);
	}
	
	public void getDescriptionsByCharacter(String sCharacter) throws SQLException, SAXException, IOException {
		String sSql = "SELECT hydc2.description FROM hydc2 AS hydc2, hydc3 AS hydc3 "
				+ "WHERE hydc2.gbk = hydc3.gbk AND hydc3.unicode = ?";
		PreparedStatement statement = connection.prepareStatement(sSql);
		statement.setInt(1, sCharacter.codePointAt(0));
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next()) {
			controller.setDescriptionByCharacter(resultSet.getClob("description"));
		}
		resultSet.close();
		statement.close();
	}
	
	public void getDescriptionsByCharacterAndIndex(String sCharacter, int index) throws SQLException, SAXException, IOException {
		String sSql = "SELECT hydc1.description FROM hydc1 AS hydc1, hydc3 AS hydc3 "
				+ "WHERE hydc1.gbk = hydc3.gbk AND hydc3.unicode = ? AND hydc1.index = ?";
		PreparedStatement statement = connection.prepareStatement(sSql);
		int i = 1;
		statement.setInt(i++, sCharacter.codePointAt(0));
		statement.setInt(i++, index);
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next()) {
			controller.setDescriptionByCharacterAndIndex(resultSet.getClob("description"));
		}
		resultSet.close();
		statement.close();
	}
	
	public void getDescriptionsByWord(String sWord) throws SQLException, SAXException, IOException {
		String sSql = "SELECT description FROM hydc9 "
				+ "WHERE ci = ?";
		PreparedStatement statement = connection.prepareStatement(sSql);
		statement.setString(1, sWord);
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next()) {
			controller.setDescriptionByWord(resultSet.getClob("description"));
		}
		resultSet.close();
		statement.close();
	}
}

/**
 * general embedded database (JavaDB) connector class
 * @author beu
 */
class DatabaseConnector {

	static String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	static String DATABASE_URL_PREFIX = "jdbc:derby:";
	
	static {
		try {
			Class.forName(DRIVER).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	Set<Connection> connectionSet = new HashSet<Connection>();

	/**
	 * connect to the specified embedded database
	 * @param sDatabase is the path to embedded database
	 * @returns new Connection object
	 * @throws SQLException
	 */
	public Connection connect(String sDatabase) throws SQLException {
		Connection connection = DriverManager.getConnection(DATABASE_URL_PREFIX + sDatabase);
		connectionSet.add(connection);
		return connection;
	}
	
	/**
	 * disconnect the specified connection
	 * @param connection is a Connection object to disconnect
	 * @throws SQLException
	 */
	public void disconnect(Connection connection) throws SQLException {
		connectionSet.remove(connection);
		connection.close();
	}

	/**
	 * clean connected connection up
	 */
	@Override
	public void finalize() {
		for (Connection connection: connectionSet) {
			connectionSet.remove(connection);
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
