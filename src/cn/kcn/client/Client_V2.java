package cn.kcn.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import cn.kcn.dao.UserDao;
import cn.kcn.domain.User;

public class Client_V2 {
	List<User> list;
	String name = null;
	String str = null;
	Socket s = null;
	DataOutputStream dos = null;
	private JFrame frame;  
    private JTextArea textArea;
    private JTextArea textArea2 = new JTextArea();
    private JTextField textField;
    private JPanel southPanel;//南边的面板  
    private JButton btn_send;//发送按钮
    private JScrollPane leftScroll;
    private JScrollPane rightScroll;   
    private JSplitPane centerSplit; 
    Date currentTime;
	SimpleDateFormat formatter;
	String dateString = null;
	DataInputStream dis = null;
	private boolean bConnected = false;
	Thread tRecv = new Thread(new RecvThread());//创建线程对象，先不开启线程
	
	List<String> userList = new ArrayList<String>();
    public void addList(){
    	//以下两行代码是让MyTask在1秒后开始执行并且每隔5秒执行一次
    	Timer timer = new Timer();
    	timer.schedule(new MyTask(),1000,5000);
    }
    class MyTask extends TimerTask{
    	@Override
    	public void run() {
    		UserDao userDao = new UserDao();
    		String userName = null;
    		String str = "\n";
    		try {
				list = userDao.findStateFor1();//查找数据库里State为1的用户
				//遍历在线用户集合
				for(int i=0;i<list.size();i++){
					User user = list.get(i);
					userName = user.getUsername();//获取在线用户名字
					userName = (userName+"\n");
					str+=userName;//将得到的在线用户姓名加上回车符后追加到str
					//System.out.println(str);
				}
				//System.out.println(str);
				textArea2.setText("\n"+str);//将在线用户姓名设置到setText上
			} catch (SQLException e) {
				e.printStackTrace();
			}
    	}
    }
	
	public void Client() {
		frame = new JFrame("客户端");//new一个窗体出来
		btn_send = new JButton("发送");
		textArea = new JTextArea();
		
		textArea.setEditable(false);  
        textArea.setForeground(Color.blue);
		
        textField = new JTextField();
    	textArea.setEditable(false);  
        textArea.setForeground(Color.blue); 
        
	    southPanel = new JPanel(new BorderLayout());
	    southPanel.add(textField,"Center");
	    southPanel.add(btn_send,"East");//将发送按钮添加到南边面板的东边
	    //左边为聊天窗口
	    leftScroll = new JScrollPane(textArea);
	    leftScroll.setBorder(new TitledBorder("聊天"));
	    //右边为在线用户
	    rightScroll = new JScrollPane(textArea2);
	    rightScroll.setBorder(new TitledBorder("在线用户"));
	    
	    centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll,  
                rightScroll);  
        centerSplit.setDividerLocation(450);//会使leftScro占450px  
		//icon图片   放在和该类同一个包下
        frame.setIconImage(Toolkit.getDefaultToolkit().createImage(Client_V2.class.getResource("a.jpg")));  
        frame.setLayout(new BorderLayout());
        frame.setSize(600,500);//设置窗体大小
        frame.add(centerSplit, "Center");
        frame.add(southPanel,"South");//添加南面的面板到整个窗体的南面
        frame.setLocationRelativeTo(null);//让窗体居中显示
        frame.setVisible(true);//显示窗体
       
        // 单击发送按钮时事件  
        btn_send.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent e) {  
            	send();  
            }  
        });  
    	// 点击红叉退出
		frame.addWindowListener(new WindowAdapter() {
			// 重写windowClosing方法，点击时候退出
			public void windowClosing(WindowEvent e) {
				disConnect();// 关闭窗体前断开连接释放资源
				System.exit(0);// 退出
			}
		});
		textField.addActionListener(new TfListener());
		
		connect();
		tRecv.start();//开启线程
	} 
	
	// 客户端连接服务器端
	public void connect() {
		try {
			s = new Socket("127.0.0.1", 8888);
			
			dos = new DataOutputStream(s.getOutputStream());// 创建输出流
			dis = new DataInputStream(s.getInputStream());// 创建输入流接收服务器端发来的消息
			//以下三行代码是得到当前时间
			currentTime = new Date();
			formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//格式化当前时间
			dateString = formatter.format(currentTime);//转成字符串
			bConnected = true;
			System.out.println(dateString+":"+name+"连到了服务器");
		} catch(ConnectException e){
			JOptionPane.showMessageDialog(frame, "服务器没有启动，请先启动服务器！", "错误",  
                    JOptionPane.ERROR_MESSAGE);
			System.exit(0);
System.out.println("服务器没有启动，请先启动服务器！");
		} catch(BindException e){
			System.out.println("端口被占用！");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// 断开连接，释放资源
	public void disConnect() {
		try {
			dos.close();
			dis.close();
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			UserDao userDao = new UserDao();
			try {
				userDao.change0State(name);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// 发送消息到服务器
	public void send() {
		str = textField.getText().trim();// 获取输入栏的消息
		
        if (str == null || str.equals("")) {  
            JOptionPane.showMessageDialog(frame, "消息不能为空！", "错误",  
                    JOptionPane.ERROR_MESSAGE);  
            return;  
        }  
		try {
			dos.writeUTF(name+"说："+str);// 将输入消息写出
			dos.flush();// 刷新
			textField.setText("");//清空输入
		} catch(SocketException e){
			JOptionPane.showMessageDialog(frame, "连接中断，可能是服务器出现故障了！", "错误",  
                    JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		} catch(NullPointerException e){
			JOptionPane.showMessageDialog(frame, "服务器还没有启动，您不能发送消息！", "错误",  
	                    JOptionPane.ERROR_MESSAGE);
			System.exit(0);
System.out.println("服务器没有启动，您还不能发送消息，请先启动服务器！");
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		new Client_V2();
	}
	
	//定义输入消息栏监听器类，监听敲回车
	private class TfListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			str = textField.getText().trim();// 获取输入栏的消息
			//textArea.append(dateString+"\n"+"我：" + str+"\n");// 将输入栏的内容带上时间设置到显示消息栏
			//textField.setText("");//清空输入
			send();// 将消息发给服务器
		}
		
		
	}
	
	//定义一个内部线程类用于接收服务器端发来的消息
	private class RecvThread implements Runnable{
		//重写run方法
		public void run() {
			try {
				while(bConnected){
					String str = dis.readUTF();
					System.out.println(str);
					textArea.append(dateString+"\n"+str+"\n");//追加
				}
			}catch(SocketException e){
				System.out.println("轻轻的我走了~");
				}catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			
		}
}
