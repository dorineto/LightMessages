package Client;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import java.io.*;

import Commons.*;

class LightMessageUI extends JFrame{
	public enum UIState{
		LOGIN,
		CHAT
	}
	
	private UIState uiState = UIState.LOGIN;
	private String titleName = "";
	
	private JPanel jpCenterPanel;
	
	private JPanel jpLoginPanel;
	protected JTextField jtfUserName;
	
	private JPanel jpLogoutPanel;
	
	private JPanel jpMessagesPanel;
	private JScrollPane jspMessagesPanel;
	
	private JPanel jpControlsPanel;
	protected JTextArea jtaMessageBar;
	protected JButton jbtFileButton;
	
	protected String userName;
	
	protected LightMessageSocket socket;
	
	public static void main(String[] args){
		new LightMessageUI("LightMassage", 500, 500);
	}
	
	public LightMessageUI(String name, int width, int height){
		super(name);
		
		this.titleName = name;
		
		this.setSize(width, height);
		this.setResizable(false);
		
		this.socket = new LightMessageSocket(this);
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			socket.closeSocket();
		}));
		
		try
		{
			this.socket.setup();
		}
		catch(Exception ex)
		{
			this.closeUI(ex.getMessage());
		}
		
		this.setupUI(width, height);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		
		this.setVisible(true);
	}
	
	public void setupUI(int width, int height){
		this.jpCenterPanel = new JPanel();
		this.jpCenterPanel.setLayout(new BoxLayout(this.jpCenterPanel, BoxLayout.X_AXIS));
		
		// Start Setting Login Panel
		this.jpLoginPanel = new JPanel();
		this.jpLoginPanel.setLayout(new BoxLayout(this.jpLoginPanel, BoxLayout.Y_AXIS));
		
		this.jpLoginPanel.add(Box.createVerticalGlue());
		
		JLabel jlAux = new JLabel(this.titleName, JLabel.CENTER);
		jlAux.setFont(Font.decode("Arial-BOLD-25"));
		
		jlAux.setAlignmentX(Component.CENTER_ALIGNMENT);
		jlAux.setAlignmentY(Component.CENTER_ALIGNMENT);
		
		this.jpLoginPanel.add(jlAux);
		
		jlAux = new JLabel("Nome do usuário:", JLabel.CENTER);
		jlAux.setFont(Font.decode("Arial-PLAIN-15"));
		
		jlAux.setAlignmentX(Component.CENTER_ALIGNMENT);
		jlAux.setAlignmentY(Component.CENTER_ALIGNMENT);
		
		this.jpLoginPanel.add(jlAux);
		
		this.jtfUserName = new JTextField(100);
		
		Dimension dim = new Dimension(400,40);
		
		this.jtfUserName.setPreferredSize(dim);
		this.jtfUserName.setMaximumSize(dim);
		this.jtfUserName.setMinimumSize(dim);
		
		this.jtfUserName.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.jtfUserName.setAlignmentY(Component.CENTER_ALIGNMENT);
		
		this.jpLoginPanel.add(this.jtfUserName);
		
		JButton jbAux = new JButton("Entrar");
		jbAux.setFont(Font.decode("Arial-PLAIN-15"));
		jbAux.addActionListener(new loginBtnClick(this));
		
		jbAux.setAlignmentX(Component.CENTER_ALIGNMENT);
		jbAux.setAlignmentY(Component.CENTER_ALIGNMENT);
		
		this.jpLoginPanel.add(jbAux);
		
		this.jpLoginPanel.add(Box.createVerticalGlue());
		
		this.jpCenterPanel.add(this.jpLoginPanel, BorderLayout.CENTER);
		
		// End Setting Login Panel
		
		// Start Setting Logout Panel
		
		this.jpLogoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		jbAux = new JButton("Logout");
		jbAux.setFont(Font.decode("Arial-PLAIN-15"));
		jbAux.addActionListener(new logoutBtnClick(this));
		
		this.jpLogoutPanel.add(jbAux);
		
		this.jpLogoutPanel.setVisible(false);
		this.add(this.jpLogoutPanel, BorderLayout.NORTH);
	
		// End Setting Logout Panel
		
		// Start Setting Messages Panel
		
		this.jpMessagesPanel = new JPanel();
		this.jpMessagesPanel.setLayout(new BoxLayout(jpMessagesPanel, BoxLayout.Y_AXIS));
		this.jpMessagesPanel.setBackground(Color.LIGHT_GRAY);
		
		this.jspMessagesPanel = new JScrollPane(this.jpMessagesPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.jspMessagesPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		
		this.jspMessagesPanel.setVisible(false);
		this.jpCenterPanel.add(jspMessagesPanel);
		
		// Start Setting Control Panel
		this.jpControlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		this.jbtFileButton = new JButton("Arquivo");
		this.jbtFileButton.setFont(Font.decode("Arial-PLAIN-12"));
		this.jbtFileButton.addActionListener(new arquivoBtnClick(this));
		
		this.jpControlsPanel.add(jbtFileButton);
		
		this.jtaMessageBar = new JTextArea(3, 28);
		this.jtaMessageBar.setFont(Font.decode("Arial-PLAIN-13"));
		this.jtaMessageBar.setLineWrap(true);
		this.jtaMessageBar.setWrapStyleWord(true);
		
		this.jpControlsPanel.add(new JScrollPane(this.jtaMessageBar, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		
		jbAux = new JButton("Enviar");
		jbAux.setFont(Font.decode("Arial-PLAIN-12"));
		jbAux.addActionListener(new enviarBtnClick(this));
		
		this.jpControlsPanel.add(jbAux);
		
		this.jpControlsPanel.setVisible(false);
		this.add(this.jpControlsPanel, BorderLayout.SOUTH);
		// end Setting Control Panel
		
		this.add(this.jpCenterPanel, BorderLayout.CENTER);
	}
	
	public void changeState(UIState newState){
		if(newState == UIState.CHAT)
		{
			this.jpLoginPanel.setVisible(false);
			
			this.jpLogoutPanel.setVisible(true);
			this.jpControlsPanel.setVisible(true);
			this.jspMessagesPanel.setVisible(true);
		}
		else
		{
			this.jpControlsPanel.setVisible(false);
			this.jpLogoutPanel.setVisible(false);
			this.jspMessagesPanel.setVisible(false);
			
			this.jpLoginPanel.setVisible(true);
		}
		
		uiState = newState;
		this.update(this.getGraphics());
	}
	
	public void closeUI(String cause){
		JOptionPane.showMessageDialog(null, cause, "Erro", JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}
	
	public void createNewMessage(String useName, LocalDateTime datetime, String content, MessageDirection direction, commandType contentType){
		JPanel newMessage = new JPanel();
		
		newMessage.setLayout(new BoxLayout(newMessage, BoxLayout.Y_AXIS));
				
		newMessage.setAlignmentX(direction == MessageDirection.SENDING? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
		newMessage.setAlignmentY(Component.CENTER_ALIGNMENT);
		
		newMessage.setBackground(Color.WHITE);
		
		JLabel jlAux;
		if(!useName.trim().isEmpty()){
			jlAux = new JLabel(useName+":");
			jlAux.setFont(Font.decode("Arial-BOLD-13"));
			
			jlAux.setAlignmentY(Component.CENTER_ALIGNMENT);
			jlAux.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			newMessage.add(jlAux);
		}
		
		String labelContent = content;
		if(contentType == commandType.FILE){
			File file = new File(content);
			labelContent = (direction == MessageDirection.SENDING ? "Enviado" : "Recebido") + " - " + file.getName();
		}
		
		jlAux = new JLabel(labelContent);
		jlAux.setFont(Font.decode("Arial-PLAIN-13"));
		
		newMessage.add(jlAux);
		
		jlAux = new JLabel(datetime.format(DateTimeFormatter.ofPattern("HH:mm")));
		jlAux.setFont(Font.decode("Arial-PLAIN-13"));
		
		jlAux.setAlignmentY(Component.CENTER_ALIGNMENT);
		jlAux.setAlignmentX(Component.RIGHT_ALIGNMENT);
		
		newMessage.add(jlAux);
		this.jpMessagesPanel.add(newMessage);
		
		this.jspMessagesPanel.updateUI();
	}
	
	private class loginBtnClick implements ActionListener{
		public LightMessageUI container;
		
		public loginBtnClick(LightMessageUI container){
			this.container = container;
		}
		
		@Override
		public void actionPerformed(ActionEvent e){
			String useName = jtfUserName.getText();
			
			if(useName.trim().isEmpty()){
				JOptionPane.showMessageDialog(null, "O nome do usuário está vazio!", "Erro", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			userName = useName.trim();
			
			jtfUserName.setText("");
			
			container.changeState(UIState.CHAT);
		}
	}
	
	private class logoutBtnClick implements ActionListener{
		public LightMessageUI container;
		
		public logoutBtnClick(LightMessageUI container){
			this.container = container;
		}
		
		@Override
		public void actionPerformed(ActionEvent e){
			if(JOptionPane.showConfirmDialog(null, "Deseja realizar logout?", "Erro", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
				return;
			
			userName = "";
			
			jpMessagesPanel.removeAll();
			jtaMessageBar.setText("");
			
			jbtFileButton.setText("Arquivo");
			
			pathFileSelected = "";
			fileSelected = false;
			
			container.changeState(UIState.LOGIN);
		}
	}
	
	private class enviarBtnClick implements ActionListener{
		public LightMessageUI container;
		
		public enviarBtnClick(LightMessageUI container){
			this.container = container;
		}
		
		@Override
		public void actionPerformed(ActionEvent e){			
			String input = fileSelected? pathFileSelected : jtaMessageBar.getText();
			
			if(input.trim().isEmpty())
				return;
			
			LocalDateTime datetime = LocalDateTime.now();
			
			commandType type = fileSelected? commandType.FILE : commandType.TEXT;
			
			container.createNewMessage("", datetime, input, MessageDirection.SENDING, type);
			
			jtaMessageBar.setText("");
			
			if(fileSelected){
				jtaMessageBar.setEditable(true);
				jtaMessageBar.updateUI();
				
				jbtFileButton.setText("Arquivo");
				jbtFileButton.updateUI();
				
				pathFileSelected = "";
				fileSelected = false;
			}
			
			String ret = socket.processesSending(userName, input, type);
			if(!ret.isEmpty())
				JOptionPane.showMessageDialog(null, ret, "Erro", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	protected String pathFileSelected = "";
	protected boolean fileSelected = false;
	
	private class arquivoBtnClick implements ActionListener{
		public LightMessageUI container;
		
		public arquivoBtnClick(LightMessageUI container){
			this.container = container;
		}
		
		@Override
		public void actionPerformed(ActionEvent e){
			if(!fileSelected)
			{
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				
				if(JFileChooser.APPROVE_OPTION != jfc.showOpenDialog(null))
					return;
				
				File file = jfc.getSelectedFile();
				
				try
				{
					pathFileSelected = file.getAbsolutePath();
				}
				catch(Exception ex){
					JOptionPane.showMessageDialog(null, "Não foi possivel abrir o arquivo selecionado!", "Erro", JOptionPane.ERROR_MESSAGE);
				}
				
				jtaMessageBar.setEditable(false);
				jtaMessageBar.setText(file.getName());
				jtaMessageBar.updateUI();
				
				jbtFileButton.setText("Cancelar");
				jbtFileButton.updateUI();
				
				fileSelected = true;
			}
			else
			{
				if(JOptionPane.showConfirmDialog(null, "Deseja cancelar essa seleção?", "Erro", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
					return;
				
				jtaMessageBar.setEditable(true);
				jtaMessageBar.setText("");
				jtaMessageBar.updateUI();
				
				jbtFileButton.setText("Arquivo");
				jbtFileButton.updateUI();
				
				pathFileSelected = "";
				fileSelected = false;
			}
		}
	}
	
}