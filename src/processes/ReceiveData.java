package processes;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


import packets.ACK_Packet;
import packets.Data_Packet;


public class ReceiveData {
	
	
	//Tamaño maximo de los datagramas
	private static final int ECHOMAX = 516; 

	
	

	public ReceiveData(DatagramSocket ds,String PATH) {

		
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
				ds.setSoTimeout(0);
				
				//Procesamos el paquete para obtener los datos
				Data_Packet data = new Data_Packet(inDP.getData(),inDP.getLength()-4);

				System.out.println("                                  <----  " + data.toString());
				

				//Si no se ha perdido ningun paquete
				if(data.getBlockNumber() == nAck){
					
					//Añadimos los datos obtenidos al mensajes
					msg.write(data.getData());
					
					//Creamos el ack a enviar
					ACK_Packet ack =  new ACK_Packet(nAck);
					
					//Enviamos el ACK
					ack.sendACK(ds,inDP.getAddress(),inDP.getPort());
					ds.setSoTimeout(1500);
					nAck++;

					//Ultimo paquete
					if( data.getData_length() < 512){

						done=true;
						System.out.println("\n******Fichero recibido******\n\n" + msg);
						
						//Almacenamos el mensaje recibido
						File file =  new File(PATH);
						BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(file));
						writer.write(msg.toByteArray());
						writer.close();

					}
				}else{
					done=true;
					throw new IOException("File transfer fail");
				}

			}
		}catch (IOException e){
		e.printStackTrace();
		}	
	}
	
	

}
