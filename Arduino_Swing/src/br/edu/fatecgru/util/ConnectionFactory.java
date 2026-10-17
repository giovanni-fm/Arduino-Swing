package br.edu.fatecgru.util;

import java.sql.Connection;

public class ConnectionFactory {
	public static Connection getConnection() throws Exception {
		try {
			Class.forName(null);
			String url= ""; //url do banco de dados
			String login= ""; //login do bd
			String senha = ""; //senha do bd
			return DriverManager.getConnection(url, login, senha);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public static void closeConnection(Connection conn) throws Exception {
		close(conn);
	}
	
	private static void close(Connection conn) 
		throws Exception {
			try {
				conn.close();
			} catch (Exception e) {
				throw new Exception(e.getMessage());
			}
		}
	
}
