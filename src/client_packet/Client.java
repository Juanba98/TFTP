package client_packet;

import packets.ACK_Packet;
import packets.Data_Packet;
import packets.Request_Packet;
import processes.ReceiveData;

import java.io.*;
import java.net.*;
import java.util.Random;

public class Client {

//Codigos de operacion
public static final short OPRRQ		= 01;
public static final short OPWRQ		= 02;
public static final short OPDATA	= 03;
public static final short OPACK		= 04;
public static final short OPERROR 	= 05;

//Longitud maxima del datagrama
public static final int DATAGRAM_LENGTH = 516;

//Direcctorio de los archivos
public static final String DIR = ".\\lib\\client\\";

//Puerto del servidor
private static final int servPort = 1234;

//Para la simulacion de errores
private static final boolean errors=false;
private static Random prob = new Random();

private static boolean quit;

//Tiempo para el time out
private static final int timeOut = 3000;

//IP del servidor
private static InetAddress serverAddress;
//Socket
private static DatagramSocket socket;
//Datagrama a enviar
private static DatagramPacket toSend;


public static void main(String[] args) {


	try {
		
		//Creamos el socket para el enviar el paquete
		socket = new DatagramSocket();
		//Numero de peticiones hechas
		int nRequest = 0;

		do{
			//Creamos la peticion
			Request_Packet request_packet = request(nRequest);

			//Si el cliente no quiere acabar con la conexion
			if(!quit){
				//Datagrama donde enviamos la peticion
				toSend = new DatagramPacket(request_packet.getBuffer(),
						request_packet.getBuffer().length, serverAddress, servPort);
				//Enviamos el paquete
				socket.send(toSend);
				//Establecemos el timeOut
				socket.setSoTimeout(timeOut);


				if (request_packet.getOpcode() == OPRRQ) RRQ(socket, request_packet);
				else WRQ(socket, request_packet);
				nRequest++;

				System.out.println("*********Done***********");
			}



		} while (!quit) ;


		System.out.println("Bye ...");


		//Cerramos el socket
		socket.close();

	} catch (SocketException e) {

		e.printStackTrace();

	} catch (IOException e) {
		e.printStackTrace();
	}
}
	/*
	* Para crear el paquete de peticion
	* nRequest =  numero de peticiones, por parte del cliente, que se han realizado en la ejecucion del programa
	* */
	private static Request_Packet request(int nRequest){

		try {
			Request_Packet res = null;

			//Leemos por pantalla
			Cmd_input input =  new Cmd_input(nRequest);

			quit = input.isQuit();

			//Si el usuario no quiere terminar
			if(!quit){

				//Creamos el paquete para la peticion
				res = new Request_Packet(input.getFilename(), input.getMode(), input.getOp());//Creamos el paquete para la peticion

				//Si es la primera peticion almacenamos la IP del servidor
				if(nRequest==0) {
					serverAddress = InetAddress.getByName(input.getIP_Serv());
				}

				//Lo pasamos a byte[]
				res.assemblePacket();

				System.out.println("------------------------------------------------------> " + res.toString());
			}


			return res;

		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		return null;
	}




	private static void WRQ(DatagramSocket socket, Request_Packet request) {

		//ACK0
		DatagramPacket ack0 = receiveACK_aux(socket);

		//Obtenemos el
		int port = ack0.getPort();

		try {
			
			//Abrimos el archivo a enviar
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
				int tries = -1;
				
				buffer = new byte[512];
				int length = 0;
				//Longitud de los datos
				length = in.read(buffer);
				
				//Creamos el pquete de datos
				data = new Data_Packet(buffer, blockN, length);
				//Datagrama a enviar
				toSend = new DatagramPacket(data.getBuffer(), data.getBuffer().length, serverAddress , port);
				ACK_Packet ack_packet;

				do{
					tries++;
					if(errors){

						if( prob.nextInt(20)!=0 ) {
							//Lo enviamos
							socket.send(toSend);
						}
					}else{
						socket.send(toSend);
					}
					socket.setSoTimeout(300);

					if(tries>0){
						System.out.println("RESEND----> " + data.toString());
					}else{
						System.out.println("----> " + data.toString());
					}

					ack_packet = new ACK_Packet(blockN);

				}while(ack_packet.receiveACK(socket,serverAddress,port)==null && tries <= 3);
				socket.setSoTimeout(0);
				blockN++;

				if (length < 512) done = true;
				if(tries > 3){
					System.out.println("Error max tries reached");
					done = true;
				}
		}

		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void RRQ(DatagramSocket socket,Request_Packet request) throws IOException {

	
		new ReceiveData(socket, DIR+request.getFileName());
	}




	private static DatagramPacket receiveACK_aux(DatagramSocket socket){


		DatagramPacket ack = //Datagrama a recibir
					new DatagramPacket(new byte[4], 4);

		try {
			socket.receive(ack);
			ACK_Packet ackP = new ACK_Packet(ack.getData());
			System.out.println("                                  <----  " + ackP.toString());
			return ack;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}


	}




}



