package br.edu.fatecgru.model;

import java.time.LocalDate;

public class Temperatura {
	private String nome;
	private double celsius;
	private double umidade;
	private LocalDate dataTemperatura;

	public Temperatura(String nome, double celsius, double umidade, LocalDate dataTemperatura) {
		this.nome = nome;
		this.celsius = celsius;
		this.umidade = umidade;
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

	public double getUmidade() {
		return umidade;
	}

	public void setUmidade(double umidade) {
		this.umidade = umidade;
	}

	public LocalDate getDataTemperatura() {
		return dataTemperatura;
	}

	public void setDataTemperatura(LocalDate dataTemperatura) {
		this.dataTemperatura = dataTemperatura;
	}
}