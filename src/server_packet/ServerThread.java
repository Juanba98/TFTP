package server_packet;
import exception.ErrorReceivedException;
import processes.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import packets.*;
public class ServerThread extends Thread {
	
	//Codigos de operacion
	public static final short OPRRQ		= 01;
	public static final short OPWRQ		= 02;
	public static final short OPDATA	= 03;
	public static final short OPACK		= 04;
	public static final short OPERROR 	= 05;
	

	

	//Ip del cliente
	private InetAddress clAddress;
	
	//Puerto del cliente
	private int clPort;
	
	//Tama�o maximo de los datagramas
	private static final int ECHOMAX = 516; 
	
	//Paquete de la peticion
	private Request_Packet request;
	
	//Socket 
	private DatagramSocket ds;

	//Ruta del archivo
	public static final String DIR = ".\\lib\\server\\";
	private String PATH;


	//Verbose
	private final boolean verbose = true;
	
	public ServerThread (DatagramPacket d) throws IOException {


		//Obtenemos el paquete del cliente
		this.request = new Request_Packet(d.getData());
		
		//Obtenemos la ip del cliente
		this.clAddress = d.getAddress();
		
		//Obtenemos el puerto del ciente
		this.clPort = d.getPort();

		this.PATH = DIR+request.getFileName();

		//Creamos un nuevo socket para la hebra
		ds =  new  DatagramSocket(); 
	}
		
		
		
	public void run() {
		try {


			System.out.println("Cliente con ip "+ clAddress +" y puerto "+ clPort);
			System.out.println("\n                                   <---- " + request.toString());

			if (request.getOpcode() == OPRRQ) {
				RRQ();
			} else {
				WRQ();
			}


		} catch (Exception e) {}

	}

	
	private void WRQ() throws IOException{
		//Archivo a recibir
		File file = new File(PATH);
		try {


			//Si ya existe  Error 6
			if (file.exists()) {

				Error_Packet error_packet = new Error_Packet((short) 6, ds, clAddress, clPort);
				throw new ErrorReceivedException(error_packet);
			}
			//Si no es un archivo Error 4
			if (!file.isFile()) {
				Error_Packet error_packet = new Error_Packet((short) 4, ds, clAddress, clPort);

				throw new ErrorReceivedException(error_packet);
			}

			//Creamos el ACK0 y lo enviamos
			ACK_Packet ack0 = new ACK_Packet((short) 0);
			ack0.sendACK(ds, clAddress, clPort);
			new ReceiveData(ds,clAddress,clPort,file,true,true);
		}catch (ErrorReceivedException e) {
			e.printErrorMsg();
		}

	}

	private void RRQ() throws IOException, ErrorReceivedException {
		//Archivo a enviar
		File file = new File(PATH);

		//Si el archivo no existe Error 1
		if(!file.exists()){
			Error_Packet error_packet =  new Error_Packet((short)1,ds,clAddress,clPort);
			throw new ErrorReceivedException(error_packet);
		}
		//Si es un directorio Error 2
		if (file.isDirectory()){
			Error_Packet error_packet = new Error_Packet((short)2,ds,clAddress,clPort);
			throw new ErrorReceivedException(error_packet);
		}

		//Si no es un archivo Error 4
		if(!file.isFile()){
			Error_Packet error_packet =  new Error_Packet((short)4,ds,clAddress,clPort);
			throw new ErrorReceivedException(error_packet);
		}





		new SendData(ds,clAddress,clPort,file,false,true);
	}




}

