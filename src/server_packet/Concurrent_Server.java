package server_packet;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.IOException;


public class Concurrent_Server {
	
	public static final int DATAP_LENGTH = 516; //2 (opcode) + 2 (block) + 512 (data)
	
	//Puerto del servidor
	public static int port = 1234;
	
	public static void main(String[] args) {
		
		DatagramSocket ds = null;
		
		try {
			
			
			//BufferedReader stdin =  new BufferedReader(new InputStreamReader(System.in));
			
			
			/*do {
				System.out.println("Introduce el puerto en el que escuchar:");
				tftpPort = Integer.parseInt(stdin.readLine());
				
			}while(tftpPort < 0);
			*/
			
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
				
				System.out.println("***Nueva peticion de servicio***");

				//Iniciamos una hebra 
				(new ServerThread(datagram)).start();
				
			}
		} catch (IOException e) {
			System.err.println("Error E/S en: " + e.getMessage());
		}finally {
				if (ds != null)
				ds.close();
			}
		}

	
		
		
		
		
}
