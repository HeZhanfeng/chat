package cn.kcn.client;

import javax.swing.*;
import cn.kcn.dao.UserDao;
import cn.kcn.domain.User;
import cn.kcn.server.Server_V2;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

public class Login implements ActionListener {

	JFrame f;
	JButton b1, b2;
	JTextField j1, j2;
	private JLabel label, lablel2, label3;

	Login() {

		f = new JFrame("登录");
		f.setSize(470, 330);// 设置大小
		Container p = f.getContentPane();// 加载面板
		p.setLayout(null);// 改成流式布局

		label = new JLabel("请登录");
		label.setBounds(200, 10, 55, 25);
		p.add(label);

		lablel2 = new JLabel("用户名：");
		lablel2.setBounds(150, 50, 55, 25);
		p.add(lablel2);

		j1 = new JTextField();
		j1.setBounds(200, 50, 100, 25);
		p.add(j1);

		label3 = new JLabel("密码：");
		label3.setBounds(150, 90, 55, 25);
		p.add(label3);

		j2 = new JTextField();
		j2.setBounds(200, 90, 100, 25);
		p.add(j2);

		b1 = new JButton("登录");// 创建按钮
		b1.setBounds(150, 150, 60, 30);
		p.add(b1);
		b1.addActionListener(this);// 添加监听机制

		b2 = new JButton("重置");// 创建按钮
		b2.setBounds(240, 150, 60, 30);
		p.add(b2);
		b2.addActionListener(this);// 添加监听机制

		f.setVisible(true);// 显示窗体
		f.setLocationRelativeTo(null);// 窗体居中
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// 关闭
	}

	public void actionPerformed(ActionEvent e) {
		// 监听到事件
		if (e.getSource() == b1) {
			String username = j1.getText().trim();
			String password = j2.getText().trim();
			// System.out.println(username+"..."+password);
			User user = new User();
			try {
				user = new UserDao().login(username, password);
				if (user == null) {
					JOptionPane.showMessageDialog(null, "用户名或密码错误!", "警告",
							JOptionPane.OK_OPTION);
				} else {
					UserDao userDao = new UserDao();
					userDao.changeState(username);// 登录成功修改数据库state字段状态为1
					f.dispose();// 关闭登录窗体
					Client_V2 c = new Client_V2();
					c.name = username;
					c.addList();// 启动客户端前调用添加在线列表方法
					c.Client();// 启用客户端
					Server_V2.name = username;
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} else {
			j1.setText("");
			j2.setText("");
		}
	}

	public static void main(String[] args) {
		new Login();
	}
}
