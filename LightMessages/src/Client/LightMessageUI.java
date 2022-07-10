package Client;

import java.awt.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import java.io.*;

import Commons.*;
import Commons.CommandV2.CommandTypeV2;

// TODO: See the memory leak
class LightMessageUI extends JFrame{
	public static enum UIState{
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
	private GridBagConstraints gbcMessagesPanel;

	private GridBagConstraints dgcNewMessage;
	private GridBagConstraints dgcNewMessageContents;

	private JScrollPane jspMessagesPanel;
	
	private JPanel jpControlsPanel;
	protected JTextArea jtaMessageBar;
	protected JButton jbtFileButton;

	protected JButton jbtSendButton;
	protected JButton jbtLoginButton;
	
	protected String userName;
	
	protected LightMessageSocket socket;

	//private final String imgPath = "../../img/";
	private final String imgPath = "../img/";
	//private final String imgPath = "./img/";

	private ImageIcon logOutIcon;
	private ImageIcon btnFileIcon;
	private ImageIcon sendIcon;
	private ImageIcon logoIcon;
	private ImageIcon logoImgIcon;
	
	public static void main(String[] args){
		new LightMessageUI("LightMessage", 500, 500);
	}
	
	public LightMessageUI(String name, int width, int height){
		super(name);
		
		this.titleName = name;
		
		this.setSize(width, height);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		
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
			this.closeUI(Logger.dumpException(ex));
		}
		
		this.setupUI(width, height);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}
	
	public void setupUI(int width, int height){
		try{
			this.logOutIcon = new ImageIcon(ImageIO.read(new File(imgPath + "logOut_25_25.png")), "Logout");
			this.btnFileIcon = new ImageIcon(ImageIO.read(new File(imgPath + "fileIcon_50_50.png")), "Arquivo");
			this.sendIcon = new ImageIcon(ImageIO.read(new File(imgPath + "sendIcon_50_50.png")), "Enviar");
			this.logoIcon = new ImageIcon(ImageIO.read(new File(imgPath + "logoIcon.png")), "Logo");
			this.logoImgIcon = new ImageIcon(ImageIO.read(new File(imgPath + "logoImg.png")), "Logo");
		}
		catch(IOException ex) {}

		if(logoIcon != null)
			this.setIconImage(logoIcon.getImage());

		this.jpCenterPanel = new JPanel();
		this.jpCenterPanel.setLayout(new BoxLayout(this.jpCenterPanel, BoxLayout.X_AXIS));
		
		// Start Setting Login Panel
		this.jpLoginPanel = new JPanel();

		this.jpLoginPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbcjpLoginPanel = new GridBagConstraints();
		gbcjpLoginPanel.gridx = 0;
		gbcjpLoginPanel.gridy = 0;
		gbcjpLoginPanel.weightx = 1.0;

		gbcjpLoginPanel.insets = new Insets(0,0,(int)Math.ceil(this.getHeight() * .1),0);


		JLabel jlAux;
		if(logoImgIcon != null){
			jlAux = new JLabel(logoImgIcon, JLabel.CENTER);
			jlAux.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		}
		else{
			jlAux = new JLabel(this.titleName, JLabel.CENTER);
			jlAux.setFont(Font.decode("Arial-BOLD-25"));
		}
		
		this.jpLoginPanel.add(jlAux, gbcjpLoginPanel);
		gbcjpLoginPanel.gridy += 1;
		
		gbcjpLoginPanel.insets.set(0, 0, 15, 0);

		jlAux = new JLabel("Usuário:", JLabel.CENTER);
		jlAux.setFont(Font.decode("Arial-PLAIN-16"));
		
		this.jpLoginPanel.add(jlAux, gbcjpLoginPanel);
		gbcjpLoginPanel.gridy += 1;
		
		this.jtfUserName = new JTextField(100);
		jtfUserName.setFont(Font.decode("Arial-PLAIN-16"));
		jtfUserName.addKeyListener(new userNameKey(this));
		
		Dimension dim = new Dimension(400,40);
		
		this.jtfUserName.setPreferredSize(dim);
		this.jtfUserName.setMaximumSize(dim);
		this.jtfUserName.setMinimumSize(dim);
		
		this.jpLoginPanel.add(this.jtfUserName, gbcjpLoginPanel);
		gbcjpLoginPanel.gridy += 1;
		
		this.jbtLoginButton = new JButton("Login");
		this.jbtLoginButton.setFont(Font.decode("Arial-PLAIN-17"));
		this.jbtLoginButton.addActionListener(new loginBtnClick(this));
		this.jbtLoginButton.setMinimumSize(new Dimension(100, 40));
		
		this.jpLoginPanel.add(jbtLoginButton, gbcjpLoginPanel);
		gbcjpLoginPanel.gridy += 1;
		
		this.jpCenterPanel.add(this.jpLoginPanel, BorderLayout.CENTER);
		
		// End Setting Login Panel
		
		// Start Setting Logout Panel
		
		this.jpLogoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		JButton jbAux;

		if(logOutIcon != null){
			jbAux = new JButton(logOutIcon);
			jbAux.setPreferredSize(new Dimension(35, 35));
		}
		else
			jbAux = new JButton("Logout");

		jbAux.setToolTipText("Logout");

		jbAux.setFont(Font.decode("Arial-PLAIN-15"));
		jbAux.addActionListener(new logoutBtnClick(this));
		
		this.jpLogoutPanel.add(jbAux);
		
		this.jpLogoutPanel.setVisible(false);
		this.add(this.jpLogoutPanel, BorderLayout.NORTH);
	
		// End Setting Logout Panel
		
		// Start Setting Messages Panel
		
		this.jpMessagesPanel = new JPanel();
		this.jpMessagesPanel.setLayout(new GridBagLayout());
		
		this.gbcMessagesPanel = new GridBagConstraints();
		this.gbcMessagesPanel.fill = GridBagConstraints.HORIZONTAL;
		this.gbcMessagesPanel.gridx = 0;
		this.gbcMessagesPanel.gridy = 0;

		this.gbcMessagesPanel.weightx = 1.0;
		this.gbcMessagesPanel.weighty = 1.0;
		this.gbcMessagesPanel.ipady = 15;
		this.gbcMessagesPanel.anchor = GridBagConstraints.NORTH;

		this.dgcNewMessage = new GridBagConstraints();
		this.dgcNewMessage.weightx = 1.0;
		this.dgcNewMessage.weighty = 1.0;		
		this.dgcNewMessage.insets = new Insets(0,10,0, 10);

		this.dgcNewMessageContents = new GridBagConstraints();
		this.dgcNewMessageContents.fill = GridBagConstraints.HORIZONTAL;
		this.dgcNewMessageContents.weightx = 1.0;
		this.dgcNewMessageContents.weighty = 1.0;
		this.dgcNewMessageContents.gridx = 0;
		this.dgcNewMessageContents.gridy = 0;
		this.dgcNewMessageContents.insets = new Insets(1,5,1, 5);

		this.jpMessagesPanel.setBackground(Color.LIGHT_GRAY);

		
		this.jspMessagesPanel = new JScrollPane(this.jpMessagesPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.jspMessagesPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

		this.jspMessagesPanel.setVisible(false);
		this.jpCenterPanel.add(jspMessagesPanel);
		
		// Start Setting Control Panel
		this.jpControlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

		if(btnFileIcon != null){
			this.jbtFileButton = new JButton(btnFileIcon);
			this.jbtFileButton.setPreferredSize(new Dimension(60, 60));
		}
		else
			this.jbtFileButton = new JButton("Arquivo");
		
		this.jbtFileButton.setToolTipText("Arquivo");
		this.jbtFileButton.setFont(Font.decode("Arial-PLAIN-12"));
		this.jbtFileButton.addActionListener(new arquivoBtnClick(this));
		
		this.jpControlsPanel.add(jbtFileButton);
		
		this.jtaMessageBar = new JTextArea(3, 28);
		this.jtaMessageBar.setToolTipText("Barra de mensagem");
		this.jtaMessageBar.setFont(Font.decode("Arial-PLAIN-13"));
		this.jtaMessageBar.setLineWrap(true);
		this.jtaMessageBar.setWrapStyleWord(true);
		this.jtaMessageBar.addKeyListener(new messageBarKey(this));		

		this.jpControlsPanel.add(new JScrollPane(this.jtaMessageBar, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		
		if(sendIcon != null){
			this.jbtSendButton = new JButton(sendIcon);
			this.jbtSendButton.setPreferredSize(new Dimension(60, 60));
		}
		else
			this.jbtSendButton = new JButton("Enviar");

		this.jbtSendButton.setToolTipText("Enviar");
		this.jbtSendButton.setFont(Font.decode("Arial-PLAIN-12"));
		this.jbtSendButton.addActionListener(new enviarBtnClick(this));
		
		this.jpControlsPanel.add(jbtSendButton);
		
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
	
	private String formatLabelContent(String contentToFormat, int wrapLenght){
		StringBuilder formatedContent = new StringBuilder(""); 

		if(contentToFormat.length() > wrapLenght){

			String auxString = "";
			int endIndex = 0;
			int startIndex = 0;
			for(int i = 0; i < (int)Math.ceil(contentToFormat.length() / wrapLenght); i++){
				startIndex = endIndex;
				endIndex = (i + 1) * wrapLenght;
				endIndex = contentToFormat.indexOf(" ", endIndex) > -1?  contentToFormat.indexOf(" ", endIndex) + 1 : endIndex;

				if(endIndex < contentToFormat.length()){
					auxString = contentToFormat.substring(startIndex, endIndex);
					auxString = contentToFormat.substring(startIndex, endIndex).replace("\n", "<br/>");
					auxString = auxString.trim() + "<br/>";
				}
				else
					auxString = contentToFormat.substring(startIndex, contentToFormat.length());

				formatedContent.append(auxString);
			}

		}
		else 
			formatedContent.append(contentToFormat.replace("\n", "<br/>").trim());

		return "<html><body>" + formatedContent.toString() + "</body></html>";
	}

	public void createNewMessage(String useName, LocalDateTime datetime, String content, MessageDirection direction, CommandV2.CommandTypeV2 contentType){
		JPanel backgroundPanel = new JPanel();
		
		backgroundPanel.setLayout(new GridBagLayout());
		backgroundPanel.setBackground(Color.LIGHT_GRAY);
		
		this.dgcNewMessageContents.gridy = 0;

		JLabel jlAux;
		if(!useName.trim().isEmpty()){
			jlAux = new JLabel(useName+":");
			jlAux.setFont(Font.decode("Arial-BOLD-13"));
			
			jlAux.setHorizontalAlignment(SwingConstants.LEFT);

			backgroundPanel.add(jlAux, this.dgcNewMessageContents);
			this.dgcNewMessageContents.gridy += 1;
		}
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new GridBagLayout());

		contentPanel.setBackground(Color.WHITE);
		contentPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		GridBagConstraints gbcContenPanel = new GridBagConstraints();
		gbcContenPanel.gridy = 0;
		gbcContenPanel.weightx = 1.0;
		gbcContenPanel.weighty = 1.0;
		gbcContenPanel.insets = new Insets(15,15,15, 15);

		String labelContent = content;
		if(contentType == CommandV2.CommandTypeV2.FILE){			
			File file = new File(content);

			if(btnFileIcon != null){
				JLabel lblfileIcon = new JLabel(btnFileIcon);

				gbcContenPanel.insets.set(15, 15, 5, 15);

				contentPanel.add(lblfileIcon, gbcContenPanel);
				gbcContenPanel.gridy += 1;

				gbcContenPanel.insets.set(5, 15, 15, 15);

				labelContent = file.getName();	
			}
			else
				labelContent = (direction == MessageDirection.SENDING ? "Enviado" : "Recebido") + " - " + file.getName();
		}

		jlAux = new JLabel(formatLabelContent(labelContent, 50));
		jlAux.setFont(Font.decode("Arial-PLAIN-13"));

		contentPanel.add(jlAux,gbcContenPanel);
		gbcContenPanel.gridy += 1;

		backgroundPanel.add(contentPanel,this.dgcNewMessageContents);
		this.dgcNewMessageContents.gridy += 1;
		
		jlAux = new JLabel(datetime.format(DateTimeFormatter.ofPattern("HH:mm")));
		jlAux.setFont(Font.decode("Arial-PLAIN-13"));
		
		jlAux.setHorizontalAlignment(SwingConstants.RIGHT);

		backgroundPanel.add(jlAux,this.dgcNewMessageContents);

		JPanel newMessage = new JPanel();
		newMessage.setLayout(new GridBagLayout());

		newMessage.setBackground(Color.LIGHT_GRAY);

		this.dgcNewMessage.anchor = direction == MessageDirection.SENDING? GridBagConstraints.EAST : GridBagConstraints.WEST;
		newMessage.add(backgroundPanel, this.dgcNewMessage);

		this.jpMessagesPanel.add(newMessage, this.gbcMessagesPanel);
		this.gbcMessagesPanel.gridy += 1;

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
			
			CommandV2.CommandTypeV2 type = fileSelected? CommandV2.CommandTypeV2.FILE : CommandV2.CommandTypeV2.TEXT;
			
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

	private class messageBarKey implements KeyListener{
		public LightMessageUI container;
		
		public messageBarKey(LightMessageUI container){
			this.container = container;
		}
		
		@Override
		public void keyTyped(KeyEvent e){
			if(((int)e.getKeyChar()) == KeyEvent.VK_ENTER && e.isShiftDown()){
				container.jtaMessageBar.append("\n");
				return;
			}

			if (((int)e.getKeyChar()) == KeyEvent.VK_ENTER && !e.isShiftDown()){
				container.jbtSendButton.doClick();
				return;
			}
		}

		@Override
		public void keyPressed(KeyEvent e){}

		@Override
		public void keyReleased(KeyEvent e){}
			
	}

	private class userNameKey implements KeyListener{
		public LightMessageUI container;
		
		public userNameKey(LightMessageUI container){
			this.container = container;
		}
		
		@Override
		public void keyTyped(KeyEvent e){
			if (((int)e.getKeyChar()) == KeyEvent.VK_ENTER && !e.isShiftDown()){
				container.jbtLoginButton.doClick();
				return;
			}
		}

		@Override
		public void keyPressed(KeyEvent e){}

		@Override
		public void keyReleased(KeyEvent e){}
	}
	
}