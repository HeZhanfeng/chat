package cn.kcn.dao;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import cn.kcn.domain.User;
import cn.kcn.utils.DBUtils;

public class UserDao {
	// 登录
	public User login(String username, String password)
			throws SQLException {
		String sql = "select * from user where username=? and password=?";

		QueryRunner runner = new QueryRunner(DBUtils.getDataSource());
		// BeanHandler<User>(User.class)将结果集中的第一行数据封装到一个对应的JavaBean实例中
		return runner.query(sql, new BeanHandler<User>(User.class), username,
				password);
	}
	//查看在线用户
	public List<User> findStateFor1() throws SQLException{
		String sql = "select * from user where state=1";
		
		QueryRunner runner = new QueryRunner(DBUtils.getDataSource());
		return runner.query(sql, new BeanListHandler<User>(User.class));
	}
	public void changeState(String username) throws SQLException {

		String sql = "update user set state=1 where username=?;";
		QueryRunner runner = new QueryRunner(DBUtils.getDataSource());
		runner.update(sql, username);
	}
	public void change0State(String name) throws SQLException {

		String sql = "update user set state=0 where username=?;";
		QueryRunner runner = new QueryRunner(DBUtils.getDataSource());
		runner.update(sql, name); 
	}
}
