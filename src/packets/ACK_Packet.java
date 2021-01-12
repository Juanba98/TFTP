package packets;

/*
 * Garcia Pelaez Juan Bautista
 * Ing. Informatica 3ºB
 * Desarrollo de Servicios Telemáticos
 *


		 2 bytes    2 bytes
          -------------------
   ACK   | 04    |   Block #  |
          --------------------

*/


import exception.ErrorReceivedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;


public class ACK_Packet {
	private final short opCode = 04;
	private short blockNumber;
	private final int packet_lenght = 4;
	private byte[] buffer;
	private int portAux;

	public ACK_Packet(short blockNumber) {
		this.blockNumber = blockNumber;
		assemblePacket();
	}

	public ACK_Packet(byte[] buffer) {

			this.buffer = buffer;
			disassemble();
			
	}

	


	public short getBlockNumber() {
		return blockNumber;
	}
	public int getPortAux(){return portAux;}


	public void assemblePacket() {
		ByteArrayOutputStream resAux = new ByteArrayOutputStream();
		
		//Opcode, 2 bytes
		resAux.write((byte)((opCode>>8)&0xFF));
		resAux.write((byte)(opCode&0xFF));
		
		//Block number, 2 bytes
		resAux.write((byte)((blockNumber>>8)&0xFF));
		resAux.write((byte)(blockNumber&0xFF));
	
		
		buffer = resAux.toByteArray();
		

	}
	
	private void disassemble()  {

		DataInputStream in = new DataInputStream(new ByteArrayInputStream(buffer));

		try {
			in.readShort();
			blockNumber = in.readShort();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public ACK_Packet receiveACK(DatagramSocket socket, InetAddress address, int port) throws ErrorReceivedException {

		try{
			//ACK a recibir
			DatagramPacket ack =  new DatagramPacket(new byte[256],256);

			//Recibimos el datagrama
			socket.receive(ack);
			if(ack.getLength() > packet_lenght){
				Error_Packet error_packet = new Error_Packet(ack.getData());
				throw new ErrorReceivedException(error_packet);
			}
			//Condicion para recibir el ACK0
			if(port != -1 && !address.equals(null)){

				//Comprobamos que el ACK se ha recibido del emisor adecuado
				if(!ack.getAddress().equals(address) || ack.getPort()!= port){
					Error_Packet error_packet = new Error_Packet((short)5,socket,ack.getAddress(),ack.getPort());
					DatagramPacket toSend = new DatagramPacket(error_packet.getBuffer(), error_packet.getBuffer().length, ack.getAddress(), ack.getPort());
					socket.send(toSend);
					throw new ErrorReceivedException(error_packet);
				}
			}else{
				portAux = ack.getPort();
			}

			//Tratamos el paquete recibido
			ACK_Packet ackP = new ACK_Packet(ack.getData());

			//Comprobamos que sea el ACK esperado
			if(this.blockNumber!=ackP.getBlockNumber()){
				Error_Packet error_packet = new Error_Packet((short)1,"ACK number not expected",socket,ack.getAddress(),ack.getPort());
				DatagramPacket toSend = new DatagramPacket(error_packet.getBuffer(), error_packet.getBuffer().length,ack.getAddress(), ack.getPort());
				socket.send(toSend);
				throw new ErrorReceivedException(error_packet);
			}


			return  ackP;

		}catch (SocketTimeoutException e){
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}



	}
	public void sendACK(DatagramSocket socket ,InetAddress address, int port) throws IOException {


		DatagramPacket ack = //Datagrama a recibir
				new DatagramPacket(this.buffer,this.buffer.length, address, port);

		socket.send(ack);

	}

	public String toString() {
		return "ACK " + blockNumber;
	}
}
