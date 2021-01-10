package packets;

/*
		 2 bytes    2 bytes
          -------------------
   ACK   | 04    |   Block #  |
          --------------------

*/


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
	
	public ACK_Packet(short blockNumber) {
		this.blockNumber = blockNumber;
		assemblePacket();
	}

	public ACK_Packet(byte[] buffer) {

			this.buffer = buffer;
			disassemble();
			
	}

	
	public short getOpCode() {
		return opCode;
	}

	public byte[] getBuffer(){return buffer;}

	public int getPacket_lenght(){return packet_lenght;}
	public short getBlockNumber() {
		return blockNumber;
	}

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
	public ACK_Packet receiveACK(DatagramSocket socket, InetAddress address, int port){

		try{
			DatagramPacket ack = //ACK a recibir
					new DatagramPacket(new byte[4], 4);

			socket.receive(ack);

			if(!ack.getAddress().equals(address) || ack.getPort()!= port){ //Comprobamos que el paquete recibido es de nuestro destinatario
				throw new IOException("Packet received from other entity");
			}

			ACK_Packet ackP = new ACK_Packet(ack.getData());//Tratamos el paquete recibido

			if(this.blockNumber!=ackP.getBlockNumber()){ //Comprobamos que sea el ACK esperado
				throw new IOException("Wrong ACK number recibed");
			}
			System.out.println("                                  <----  " + ackP.toString());

			return  ackP;

		}catch (SocketTimeoutException e){
			return null;
		} catch (IOException e) {
			System.out.println("EIII");
			e.printStackTrace();
			return null;
		}



	}
	public void sendACK(DatagramSocket socket ,InetAddress address, int port) throws IOException {


		DatagramPacket ack = //Datagrama a recibir
				new DatagramPacket(this.buffer,this.buffer.length, address, port);

		socket.send(ack);


		System.out.println("----> " + this.toString());
	}

	public String toString() {
		return "ACK " + blockNumber;
	}
}