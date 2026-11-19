package br.edu.fatecgru.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
	
	public void salvar(Temperatura temp) throws Exception {
		if (temp == null)
			throw new Exception("O valor passado nao pode ser nulo");
		try {
			String SQL = "INSERT INTO tbTemp"
					+ "(celsius, nome, umidade, dataTemperatura) values(?, ?, ?, ?)";
			ps = conn.prepareStatement(SQL);
			ps.setDouble(1, temp.getCelsius());
			ps.setString(2, temp.getNome());
			ps.setDouble(3, temp.getUmidade());
			ps.setDate(4, java.sql.Date.valueOf(temp.getDataTemperatura()));
			ps.executeUpdate();
		} catch (SQLException sqle) {
			throw new Exception("Erro ao inserir dados " + sqle);
		} finally {
			ConnectionFactory.closeConnection(conn, ps);
		}
		
	}
	
	public List<Temperatura> listarTodos() throws Exception {
		List<Temperatura> lista = new ArrayList<Temperatura>();
		try {
			String SQL = "SELECT nome, celsius, umidade, dataTemperatura "
					+ "FROM tbTemp ORDER BY dataTemperatura, id";
			ps = conn.prepareStatement(SQL);
			rs = ps.executeQuery();
			while (rs.next()) {
				lista.add(new Temperatura(rs.getString("nome"),
						rs.getDouble("celsius"),
						rs.getDouble("umidade"),
						rs.getDate("dataTemperatura").toLocalDate()));
			}
			return lista;
		} catch (SQLException sqle) {
			throw new Exception("Erro ao consultar dados " + sqle);
		} finally {
			ConnectionFactory.closeConnection(conn, ps, rs);
		}
	}
}