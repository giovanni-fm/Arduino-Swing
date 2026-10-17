package br.edu.fatecgru.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import br.edu.fatecgru.model.Temperatura;
import br.edu.fatecgru.util.ConnectionFactory;

public class TemperaturaDAO {
	private Connection conn;
	private PreparedStatement ps;
	private ResultSet rs;
	private Temperatura temperatura;
	
	public TemperaturaDAO() throws Exception { //chama a classe ConnectionFactory
		try {
			this.conn = ConnectionFactory.getConnection();
		} catch (Exception e) {
			throw new Exception("erro: \n" + e.getMessage());
		}
	}
}
