package br.edu.fatecgru.view;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.TextArea;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.fazecast.jSerialComm.SerialPort;

import br.edu.fatecgru.DAO.TemperaturaDAO;
import br.edu.fatecgru.model.Temperatura;
import br.edu.fatecgru.util.ArduinoSerial;

public class TelaTemp extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtCelsius;
	private JTextField txtUmidade;
	private JTextField txtNome;
	private TextArea textArea;

	// --- Arduino ---
	private ArduinoSerial arduino = new ArduinoSerial();
	private Double ultimoCelsius = null; // ultima leitura recebida do Arduino
	private Double ultimaUmidade = null;
	private boolean atualizarPendente = false; // clicou em Atualizar antes de chegar a 1a leitura

	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("yyyy/MM/dd");

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TelaTemp frame = new TelaTemp();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public TelaTemp() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 510, 383);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		// fecha a porta serial ao fechar a janela
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				arduino.desconectar();
			}
		});

		JLabel lblNome = new JLabel("Nome:");
		lblNome.setBounds(15, 40, 44, 14);

		JLabel lblSensor = new JLabel("Banco de Dados de Temperatura");
		lblSensor.setBounds(161, 5, 157, 14);

		JLabel lblCelsius = new JLabel("°C:");
		lblCelsius.setBounds(79, 78, 16, 14);

		JLabel lblUmidade = new JLabel("Umidade:");
		lblUmidade.setBounds(50, 109, 45, 14);

		JButton btnAtualizar = new JButton("Atualizar");
		btnAtualizar.setBounds(99, 151, 91, 23);
		btnAtualizar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				atualizar();
			}
		});

		JButton btnConsultar = new JButton("Consultar");
		btnConsultar.setBounds(301, 151, 91, 23);
		btnConsultar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				consultar();
			}
		});

		txtCelsius = new JTextField();
		txtCelsius.setBounds(99, 75, 86, 20);
		txtCelsius.setColumns(10);
		txtCelsius.setEditable(false); // quem preenche e o Arduino (botao Atualizar)

		txtUmidade = new JTextField();
		txtUmidade.setBounds(99, 106, 86, 20);
		txtUmidade.setColumns(10);
		txtUmidade.setEditable(false); // quem preenche e o Arduino (botao Atualizar)

		txtNome = new JTextField();
		txtNome.setBounds(50, 37, 429, 20);
		txtNome.setColumns(10);
		contentPane.setLayout(null);
		contentPane.add(lblSensor);
		contentPane.add(lblNome);
		contentPane.add(lblUmidade);
		contentPane.add(lblCelsius);
		contentPane.add(btnAtualizar);
		contentPane.add(btnConsultar);
		contentPane.add(txtCelsius);
		contentPane.add(txtUmidade);
		contentPane.add(txtNome);

		textArea = new TextArea();
		textArea.setBounds(15, 180, 464, 160);
		textArea.setEditable(false);
		contentPane.add(textArea);

		JButton btnSalvar = new JButton("Salvar");
		btnSalvar.setBounds(200, 151, 91, 23);
		btnSalvar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				salvar();
			}
		});
		contentPane.add(btnSalvar);

		// tenta conectar no Arduino assim que a tela abre
		conectarArduino();
	}

	/**
	 * Procura o Arduino entre as portas seriais do PC e comeca a receber as leituras.
	 * Prefere uma porta cujo nome lembre Arduino/CH340/USB; senao usa a primeira.
	 */
	private void conectarArduino() {
		if (arduino.isConectado()) {
			return;
		}
		SerialPort[] portas = ArduinoSerial.listarPortas();
		if (portas.length == 0) {
			textArea.setText("Nenhuma porta serial encontrada.\nConecte o Arduino e clique em Atualizar.");
			return;
		}

		SerialPort escolhida = portas[0];
		for (SerialPort p : portas) {
			String nome = p.getDescriptivePortName().toLowerCase();
			if (nome.contains("arduino") || nome.contains("ch340") || nome.contains("usb")) {
				escolhida = p;
				break;
			}
		}

		boolean ok = arduino.conectar(escolhida, new ArduinoSerial.LeituraListener() {
			@Override
			public void aoReceber(final double celsius, final double umidade) {
				// a leitura chega em outra thread: passa para a thread do Swing
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						ultimoCelsius = celsius;
						ultimaUmidade = umidade;
						if (atualizarPendente) { // o usuario ja tinha clicado em Atualizar
							atualizarPendente = false;
							mostrarLeitura();
						}
					}
				});
			}
		});

		if (ok) {
			textArea.setText("Arduino conectado em " + escolhida.getSystemPortName()
					+ ".\nClique em Atualizar para ler a temperatura.");
		} else {
			textArea.setText("Nao foi possivel abrir " + escolhida.getSystemPortName()
					+ ".\nFeche o Monitor Serial da IDE do Arduino e clique em Atualizar.");
		}
	}

	/** Joga a ultima leitura do Arduino nos campos °C e Umidade. */
	private void mostrarLeitura() {
		txtCelsius.setText(String.format(Locale.US, "%.1f", ultimoCelsius));
		txtUmidade.setText(String.format(Locale.US, "%.1f", ultimaUmidade));
	}

	/** Botao Atualizar: pega a leitura atual do Arduino e mostra em txtCelsius e txtUmidade. */
	private void atualizar() {
		if (!arduino.isConectado()) {
			conectarArduino(); // tenta conectar de novo
			if (!arduino.isConectado()) {
				return;
			}
		}
		if (ultimoCelsius == null || ultimaUmidade == null) {
			// o Arduino reinicia ao abrir a porta e leva ~2s para a 1a leitura:
			// os campos sao preenchidos sozinhos assim que ela chegar
			atualizarPendente = true;
			textArea.setText("Aguardando a primeira leitura do Arduino...");
			return;
		}
		mostrarLeitura();
	}

	/** Botao Consultar: mostra no TextArea TODAS as temperaturas salvas no banco. */
	private void consultar() {
		try {
			List<Temperatura> lista = new TemperaturaDAO().listarTodos();
			if (lista.isEmpty()) {
				textArea.setText("Nenhuma temperatura salva no banco.");
				return;
			}
			StringBuilder sb = new StringBuilder();
			for (Temperatura t : lista) {
				sb.append("Nome: ").append(t.getNome())
						.append(" | ").append(String.format(Locale.US, "%.1f", t.getCelsius())).append(" °C")
						.append(" | ").append(String.format(Locale.US, "%.1f", t.getUmidade())).append(" %")
						.append(" | ").append(t.getDataTemperatura().format(FORMATO_DATA))
						.append("\n");
			}
			textArea.setText(sb.toString());
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Erro ao consultar: " + ex.getMessage());
		}
	}

	/** Botao Salvar: grava Nome, °C, Umidade e a data de hoje no banco. */
	private void salvar() {
		String nome = txtNome.getText().trim();
		if (nome.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Digite um nome para essa leitura.");
			return;
		}
		if (txtCelsius.getText().isEmpty() || txtUmidade.getText().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Clique em Atualizar para pegar a leitura do Arduino.");
			return;
		}

		try {
			// salva exatamente o que esta na tela (a leitura do ultimo clique em Atualizar)
			double celsius = Double.parseDouble(txtCelsius.getText());
			double umidade = Double.parseDouble(txtUmidade.getText());
			LocalDate hoje = LocalDate.now();

			new TemperaturaDAO().salvar(new Temperatura(nome, celsius, umidade, hoje));

			textArea.setText("Salvo: " + nome + " | " + celsius + " °C | " + umidade + " % | "
					+ hoje.format(FORMATO_DATA));
			txtNome.setText("");
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage());
		}
	}
}