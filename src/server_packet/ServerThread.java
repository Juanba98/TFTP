package server_packet;
import exception.ErrorReceivedException;
import processes.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import packets.*;
/*
 * Garcia Pelaez Juan Bautista
 * Ing. Informatica 3ºB
 * Desarrollo de Servicios Telemáticos
 *
 */
public class ServerThread extends Thread {
	
	//Codigos de operacion
    private static final short OPRRQ		= 01;

	//Ip del cliente
	private InetAddress clAddress;
	
	//Puerto del cliente
	private int clPort;

	//Paquete de la peticion
	private Request_Packet request;
	
	//Socket
	private DatagramSocket ds;

	//Ruta del archivo
	private static final String DIR = ".\\lib\\server\\";
	private String PATH;

	//Verbose
	private boolean verbose;

	//SaveFile
    private boolean saveFile;

    //Errors
    private boolean errors;

	
	public ServerThread (DatagramPacket d, boolean errors, boolean verbose, boolean saveFile) throws IOException {


		//Obtenemos el paquete del cliente
		this.request = new Request_Packet(d.getData());
		
		//Obtenemos la ip del cliente
		this.clAddress = d.getAddress();
		
		//Obtenemos el puerto del ciente
		this.clPort = d.getPort();

		this.PATH = DIR+request.getFileName();

		//Creamos un nuevo socket para la hebra
		ds =  new  DatagramSocket();

		//Parametros
		this.errors = errors;
		this.verbose = verbose;
		this.saveFile = saveFile;
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


			//Creamos el ACK0 y lo enviamos
			ACK_Packet ack0 = new ACK_Packet((short) 0);
			ack0.sendACK(ds, clAddress, clPort);
			new ReceiveData(ds,clAddress,clPort,file,verbose,saveFile);

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


		new SendData(ds,clAddress,clPort,file,errors,verbose);
	}




}

