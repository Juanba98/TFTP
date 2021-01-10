package server_packet;
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
	
	//Ruta de los archivos
	public static final String DIR = ".\\lib\\server\\";
	

	//Ip del cliente
	private InetAddress clAddress;
	
	//Puerto del cliente
	private int clPort;
	
	//Tamaño maximo de los datagramas
	private static final int ECHOMAX = 516; 
	
	//Paquete de la peticion
	private Request_Packet request;
	
	//Socket 
	private DatagramSocket ds;
	
	//Datagrama a enviar 
	private DatagramPacket toSend;

	
	public ServerThread (DatagramPacket d) throws IOException {
		
		//Obtenemos el paquete del cliente
		this.request = new Request_Packet(d.getData());
		
		//Obtenemos la ip del cliente
		this.clAddress = d.getAddress();
		
		//Obtenemos el puerto del ciente
		this.clPort = d.getPort();
		
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

	
	private void WRQ() throws IOException {
		

		//Creamos el ACK0 y lo enviamos 
		ACK_Packet ack0 = new ACK_Packet((short)0);
		ack0.sendACK(ds,clAddress,clPort);
		
		new ReceiveData(ds, DIR+request.getFileName());
	}

	private void RRQ() {
		try {
			
			
			//Abrimos el archivo pedido por el cliente
			File file = new File(DIR+request.getFileName());
			
			//Para leer el archivo
			FileInputStream in = new FileInputStream(file);

			//Paquete de datos
			Data_Packet data;
			byte [] buffer ;
			short blockN = 1;

			//boleeano para la finalizacion del proceso
			boolean done = false;
			while(!done) {
				
				//Numero de intentos
				int tries = 0;
				
				
				int length = 0;
				buffer = new byte[ECHOMAX-4];
				//Longitud de los datos
				length = in.read(buffer);
				
				//Creamos el pquete de datos
				data = new Data_Packet(buffer, blockN, length);
				
				//Datagrama a enviar
				toSend = new DatagramPacket(data.getBuffer(), data.getBuffer().length,clAddress , clPort);
				ACK_Packet ack_packet;

				do{
					//Lo enviamos
					ds.send(toSend);
					//Establecemos el Time Out
					ds.setSoTimeout(3000);
					
					if(tries>0){
						System.out.println("RESEND----> " + data.toString());
					}else{
						System.out.println("----> " + data.toString());
					}
					
					//ACK que esperamos
					ack_packet = new ACK_Packet(blockN);
					tries++;
				}while(ack_packet.receiveACK(ds,clAddress,clPort)==null && tries < 3);
				ds.setSoTimeout(0);
				blockN++;

				//Hemos enviado los ultimos datos
				if (length < 512) {
					
					done = true;
					in.close();
				}

			}

		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}




}

