package com.cs5248.team01.persistent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;

import org.apache.log4j.Logger;

public class DBCall {

	Logger logger = Logger.getLogger(DBCall.class.getSimpleName());

	String host = "jdbc:mysql://localhost:3306/team01";
	String username = "root";
	String password = "password";

	protected PreparedStatement statement;

	protected Connection con;
	protected int parameterIndex;

	public DBCall() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		this.con = DriverManager.getConnection(host, username, password);
		this.con.setAutoCommit(true);
	}

	public DBCall createStatement(String sql) throws SQLException {
		try {
			statement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			parameterIndex = 1;
			return this;
		} catch (SQLException e) {
			this.close();
			throw e;
		}
	}

	public DBCall setString(String s) throws SQLException {
		statement.setString(parameterIndex, s);
		parameterIndex++;

		return this;
	}

	public DBCall setInt(int i) throws SQLException {
		statement.setInt(parameterIndex, i);
		parameterIndex++;

		return this;
	}

	public DBCall setDate(Time dt) throws SQLException {
		statement.setTime(parameterIndex, dt);
		parameterIndex++;

		return this;
	}

	public Integer executeInsert() throws SQLException {
		try {
			statement.executeUpdate();
			ResultSet rs = statement.getGeneratedKeys();

			rs.next();
			return rs.getInt(1);
		} catch (SQLException e) {
			throw e;
		} finally {
			this.close();
		}

	}

	public void executeUpdate() throws SQLException {
		try {
			statement.executeUpdate();
		} catch (SQLException e) {
			throw e;
		} finally {
			this.close();
		}
	}

	public <T> T executeQuery(ResultSetMapper<T> mapper) throws SQLException {
		try {
			ResultSet rs = statement.executeQuery();

			return mapper.map(rs);
		} catch (SQLException e) {
			throw e;
		} finally {
			this.close();
		}

	}

	public interface ResultSetMapper<T> {
		public T map(ResultSet rs) throws SQLException, RuntimeException;
	}

	public void close() {
		try {
			this.statement.close();
		} catch (Exception e) {

		}
		try {
			this.con.close();
		} catch (Exception e) {

		}
	}
}
