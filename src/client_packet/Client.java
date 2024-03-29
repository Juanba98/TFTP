package client_packet;
/*
 * Garcia Pelaez Juan Bautista
 * Ing. Informatica 3ºB
 * Desarrollo de Servicios Telemáticos
 *
 */
import exception.ErrorReceivedException;
import packets.ACK_Packet;
import packets.Data_Packet;
import packets.Error_Packet;
import packets.Request_Packet;
import processes.ReceiveData;
import processes.SendData;

import java.io.*;
import java.net.*;
import java.util.Random;

public class Client {

//Codigos de operacion
public static final short OPRRQ		= 01;

//Direcctorio de los archivos
public static final String DIR = ".\\lib\\client\\";

//Puerto del servidor
private static final int servPort = 1234;

//Para la simulacion de errores
private static boolean errors=false;

private static boolean quit;

//Tiempo para el time out
private static final int timeOut = 3000;

//IP del servidor
private static InetAddress serverAddress;
//Socket
private static DatagramSocket socket;
//Datagrama a enviar
private static DatagramPacket toSend;

private static boolean verbose;

private static boolean saveFile;


public static void main(String[] args) throws ErrorReceivedException {

	if(args.length == 1){
		String s = args[0].toLowerCase();
		if(s.equals("true")){
			errors = true;
		}
	}


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

			quit = input.getQuit();

			//Si el usuario no quiere terminar
			if(!quit){

				//Creamos el paquete para la peticion
				res = new Request_Packet(input.getFilename(), input.getMode(), input.getOp());//Creamos el paquete para la peticion

				//Si es la primera peticion almacenamos la IP del servidor
				if(nRequest==0) {
					serverAddress = InetAddress.getByName(input.getIP_Serv());
				}

				verbose = input.getVerbose();
				//Lo pasamos a byte[]
				res.assemblePacket();

				saveFile = input.getSave();

				if(verbose){
					System.out.println("------------------------------------------------------> " + res.toString());

				}
			}


			return res;

		} catch (UnknownHostException e) {
			System.out.println("Error with the IP server. Try again");
			return request(nRequest);
		}


	}

	private static void WRQ(DatagramSocket socket, Request_Packet request) throws IOException {
		//Archivo a enviar
		File file = new File(DIR+request.getFileName());
		try {



			//ACK0
			ACK_Packet ack0 = new ACK_Packet((short) 0);
			ack0.receiveACK(socket, null, -1);
			//Obtenemos el puerto de la hebra
			int port = ack0.getPortAux();
			//Si el archivo no existe Error 1
			if(!file.exists()) {
				Error_Packet error_packet = new Error_Packet((short) 1, socket,serverAddress ,port);
				throw new ErrorReceivedException(error_packet);
			}
			new SendData(socket, serverAddress, port, file, errors, verbose);
		}catch (ErrorReceivedException e){

			e.printErrorMsg();
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	private static void RRQ(DatagramSocket socket,Request_Packet request){
		//Archivo a recibir
		if(saveFile){
			File file = new File(DIR+request.getFileName());
		}


        try {
            new ReceiveData(socket,serverAddress,-1,null,verbose,saveFile);
        } catch (ErrorReceivedException e) {
            e.printErrorMsg();
        }
    }

	private static DatagramPacket receiveACK_aux(DatagramSocket socket){


		DatagramPacket ack = //Datagrama a recibir
					new DatagramPacket(new byte[4], 4);

		try {
			socket.receive(ack);
			ACK_Packet ackP = new ACK_Packet(ack.getData());
			if(verbose) System.out.println("                                  <----  " + ackP.toString());
			return ack;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}


	}




}



