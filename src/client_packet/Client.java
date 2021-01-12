package client_packet;

import exception.ErrorReceivedException;
import packets.ACK_Packet;
import packets.Data_Packet;
import packets.Request_Packet;
import processes.ReceiveData;
import processes.SendData;

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
			e.printStackTrace();
		}

		return null;
	}




	private static void WRQ(DatagramSocket socket, Request_Packet request) throws IOException {

		//ACK0
		DatagramPacket ack0 = receiveACK_aux(socket);

		//Obtenemos el
		int port = ack0.getPort();

		new SendData(socket, serverAddress, port,DIR+request.getFileName(), errors, verbose );



	}

	private static void RRQ(DatagramSocket socket,Request_Packet request){


        try {
            new ReceiveData(socket,serverAddress,-1,DIR+request.getFileName(),verbose,saveFile);
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



