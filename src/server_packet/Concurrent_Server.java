package server_packet;
/*
* Garcia Pelaez Juan Bautista
* Ing. Informatica 3ºB
* Desarrollo de Servicios Telemáticos
*
*/

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.IOException;


public class Concurrent_Server {
	public static boolean errors = false;
	public static boolean verbose = true;
	public static boolean saveFile = true;
	public static final int DATAP_LENGTH = 516; //2 (opcode) + 2 (block) + 512 (data)
	
	//Puerto del servidor
	public static int port = 1234;
	
	public static void main(String[] args) {
		if(args.length == 3){

			String s = args[0].toLowerCase();
			if(s.equals("true")){
				errors = true;
			}

			s = args[1].toLowerCase();
			if(s.equals("false")){
			 	verbose = false;
			}

			s = args[2].toLowerCase();
			if(s.equals("false")){
				saveFile = false;
			}
		}

		DatagramSocket ds = null;
		
		try {

			//Creamos el socket en el puerto tftpPort
			ds = new DatagramSocket(port);
			
			byte[] buffer = new byte[DATAP_LENGTH];
			DatagramPacket datagram = new DatagramPacket(buffer,
			buffer.length);
			System.out.println("------ Server Started ------");

			while (true) {
				//Limpiamos el buffer
				datagram.setLength(DATAP_LENGTH);
				
				//Recibimos el paquete
				ds.receive(datagram);
				
				System.out.println("***New Client***");

				//Iniciamos una hebra 
				(new ServerThread(datagram,errors,verbose,saveFile)).start();
				
			}

		} catch (IOException e) {
			System.err.println("Error E/S en: " + e.getMessage());

		}finally {
				if (ds != null)
				ds.close();
			}
		}
}
