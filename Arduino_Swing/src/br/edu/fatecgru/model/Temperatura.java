package br.edu.fatecgru.model;

import java.time.LocalDate;

public class Temperatura {
	String nome;
	double celsius;
	LocalDate dataTemperatura;
	
	public Temperatura(String nome, double celsius, LocalDate dataTemperatura) {
		this.nome = nome;
		this.celsius = celsius;
		this.dataTemperatura = dataTemperatura;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public double getCelsius() {
		return celsius;
	}

	public void setCelsius(double celsius) {
		this.celsius = celsius;
	}

	public LocalDate getDataTemperatura() {
		return dataTemperatura;
	}

	public void setDataTemperatura(LocalDate dataTemperatura) {
		this.dataTemperatura = dataTemperatura;
	}
}
