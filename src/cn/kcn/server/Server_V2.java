package cn.kcn.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Server_V2 {
	public static String name;
	boolean started = false;// 服务器是否开启
	ServerSocket ss = null;// 服务器端监听的socket
	Date currentTime = null;
	SimpleDateFormat formatter = null;
	String dateString = null;
	List<Clients> clients = new ArrayList<Clients>();// List集合用来保存每一个客户端信息

	public void start() {
		int port = 8888;// 端口号为8888
		try {
			ss = new ServerSocket(port);// 监听8888端口
			started = true;// 为true表示已经启动
		} catch (BindException e) {
			System.out.println(port + "端口被占用！\n您可以尝试关闭相关程序并重新启动服务器！");
			System.exit(0);// 端口被占用是退出程序
		} catch (IOException e) {
			e.printStackTrace();// 打印异常堆栈
		}

		try {
			while (started) {// 判断是否启动，如果启动了不断接受客户端的连接
				Socket s = ss.accept();// 接收客户端请求
				// 以下三行代码是得到当前时间
				currentTime = new Date();
				formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 格式化当前时间
				dateString = formatter.format(currentTime);// 转成字符串

				Clients c = new Clients(s);// 接收到客户端就new一个线程
				new Thread(c).start();// 开启线程执行run方法
				System.out.println("有客户连了进来");
				clients.add(c);// 每个连接的客户端都添加到List集合里去
			}
		} catch (IOException e) {// 捕获到异常关闭
			e.printStackTrace();
		} finally {
			try {
				ss.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	// 服务器端程序入口
	public static void main(String[] args) {
		// 创建服务器端类调用start方法开始执行
		new Server_V2().start();
	}

	class Clients implements Runnable {
		Socket s;
		private DataInputStream dis = null;// 获取数据的管道
		private DataOutputStream dos = null;// 输出流管道
		private boolean bConnected = false;// 客户端和服务器端是否已经连上

		public Clients(Socket s) {
			this.s = s;// 将s初始化
			try {
				dis = new DataInputStream(s.getInputStream());// 初始化dis
				dos = new DataOutputStream(s.getOutputStream());// 初始化dos
				bConnected = true;// 连上了将bConnected改成true
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// 将接收到的消息写出
		public void send(String str) {
			try {
				dos.writeUTF(str);
			} catch (IOException e) {
				clients.remove(this);
				System.out.println("对方退出了！我将从List里移除他！");
				// e.printStackTrace();
			}
		}

		// 重写run方法
		public void run() {
			try {
				while (bConnected) {// 判断是否连上,连上了就把数据打印出来
					String str = dis.readUTF();
					System.out.println(dateString + "\n" + str);
					// 循环遍历将接受到的消息发给每一个客户端
					for (int i = 0; i < clients.size(); i++) {
						Clients c = clients.get(i);
						c.send(str);
						// System.out.println(str);
					}
				}
			} catch (IOException e) {// 捕获到异常关闭
				// e.printStackTrace();
				System.out.println("客户端关闭");
			} finally {
				try {
					if (dis != null) {
						dis.close();
					}
					if (dos != null) {
						dos.close();
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

		}
	}
}