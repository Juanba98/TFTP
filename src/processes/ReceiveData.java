package processes;
/*
 * Garcia Pelaez Juan Bautista
 * Ing. Informatica 3ºB
 * Desarrollo de Servicios Telemáticos
 *
 */
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import exception.ErrorReceivedException;
import packets.ACK_Packet;
import packets.Data_Packet;
import packets.Error_Packet;



public class ReceiveData {

	//Tamaño maximo de los datagramas
	private static final int ECHOMAX = 516; 

	public ReceiveData(DatagramSocket ds, InetAddress transAddress, int transPort,File file, boolean verbose, boolean saveFile) throws ErrorReceivedException {

		
		short nAck = 1;
		try {

			//booleano para la finalizacion del proceso
			boolean done= false;
			
			//Mensaje a recibir 
			ByteArrayOutputStream msg = new ByteArrayOutputStream();

			while(!done){

				//Paquete para recibir los datos
				DatagramPacket inDP =  new DatagramPacket(new byte[ECHOMAX],ECHOMAX);
				ds.receive(inDP);

				//Para el cliente puesto que no sabemos el puerto de la hebra
				if(transPort == -1){
					transPort = inDP.getPort();
				}

				//Comprobamos que el paquete recibido es del emisor correcto
				if(!inDP.getAddress().equals(transAddress) || inDP.getPort()!= transPort){
					Error_Packet error_packet = new Error_Packet((short)5,ds,transAddress,transPort);
					DatagramPacket toSend = new DatagramPacket(error_packet.getBuffer(), error_packet.getBuffer().length, transAddress, transPort);
					ds.send(toSend);
					throw new ErrorReceivedException(error_packet);
				}
				ds.setSoTimeout(0);

				if(inDP.getLength()<ECHOMAX ){
					Error_Packet error_packet = new Error_Packet(inDP.getData());
					if(error_packet.getErrorMsg()!=null){

						throw new ErrorReceivedException(error_packet);
					}

				}

				//Procesamos el paquete para obtener los datos
				Data_Packet data = new Data_Packet(inDP.getData(),inDP.getLength()-4);

				if(verbose){
					System.out.println("                                  <----  " + data.toString());
				}

				//Si no se ha perdido ningun paquete
				if(data.getBlockNumber() == nAck){
					
					//Añadimos los datos obtenidos al mensajes
					msg.write(data.getData());
					
					//Creamos el ack a enviar
					ACK_Packet ack =  new ACK_Packet(nAck);
					
					//Enviamos el ACK
					ack.sendACK(ds,inDP.getAddress(),inDP.getPort());
					if(verbose)System.out.println("----> " + ack.toString());
					ds.setSoTimeout(1500);
					nAck++;

					//Ultimo paquete
					if( data.getData_length() < 512){

						done=true;
						System.out.println("\n******Fichero recibido******");

						//Si se quiere guardar en un archivo
						if(saveFile){

							//Almacenamos el mensaje recibido
							BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(file));
							writer.write(msg.toByteArray());
							writer.close();

						}else{

							//Mostramos el contenido del fichero por pantalla
							System.out.println(msg);
						}

					}

				}else{
					done=true;
					Error_Packet error_packet =  new Error_Packet((short)0,"Block number not escpected",ds,transAddress,transPort);
					throw new ErrorReceivedException(error_packet);

				}
			}

		}catch (IOException e){
			e.printStackTrace();
		}	
	}

}
