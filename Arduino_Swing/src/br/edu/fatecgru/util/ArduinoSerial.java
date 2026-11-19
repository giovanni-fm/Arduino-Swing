package br.edu.fatecgru.util;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;

/**
 * Faz a leitura serial do Arduino (DHT11).
 * O Arduino envia uma linha por leitura no formato: "25.00;60.00\n"
 * (temperatura em °C ; umidade em %).
 */
public class ArduinoSerial {

	/** Quem quiser receber as leituras implementa esta interface. */
	public interface LeituraListener {
		void aoReceber(double celsius, double umidade);
	}

	// O jSerialComm as vezes detecta ARM64 por engano em PCs x64 (extrai a DLL errada).
	// Este bloco roda antes de qualquer uso do SerialPort e forca a arquitetura correta.
	static {
		// Extrai a DLL com nome aleatorio: evita "Acesso negado" ao sobrescrever uma DLL antiga travada.
		System.setProperty("jSerialComm.library.randomizeNativeName", "true");

		String arch = System.getProperty("os.arch", "").toLowerCase();
		if (arch.equals("amd64") || arch.equals("x86_64")) {
			System.setProperty("os.arch_full", "x86_64");
		}
	}

	private SerialPort porta;

	/** Lista as portas seriais disponiveis no computador (COM3, COM4, /dev/ttyUSB0...). */
	public static SerialPort[] listarPortas() {
		return SerialPort.getCommPorts();
	}

	/** Abre a porta e comeca a escutar. Retorna false se nao conseguiu abrir. */
	public boolean conectar(SerialPort p, final LeituraListener listener) {
		desconectar();
		porta = p;
		porta.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY); // mesmo baud do Arduino
		porta.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);

		if (!porta.openPort()) {
			return false;
		}

		// Listener que so dispara quando chega uma linha completa (terminada em \n)
		porta.addDataListener(new SerialPortMessageListener() {
			@Override
			public int getListeningEvents() {
				return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
			}

			@Override
			public byte[] getMessageDelimiter() {
				return new byte[] { (byte) '\n' };
			}

			@Override
			public boolean delimiterIndicatesEndOfMessage() {
				return true;
			}

			@Override
			public void serialEvent(SerialPortEvent event) {
				String linha = new String(event.getReceivedData()).trim();
				try {
					String[] partes = linha.split(";");
					double celsius = Double.parseDouble(partes[0].trim());
					double umidade = Double.parseDouble(partes[1].trim());
					listener.aoReceber(celsius, umidade);
				} catch (Exception e) {
					// linha invalida ("ERRO", lixo do boot do Arduino etc.): ignora
				}
			}
		});
		return true;
	}

	public void desconectar() {
		if (porta != null) {
			porta.removeDataListener();
			if (porta.isOpen()) {
				porta.closePort();
			}
			porta = null;
		}
	}

	public boolean isConectado() {
		return porta != null && porta.isOpen();
	}
}