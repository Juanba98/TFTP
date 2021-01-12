package packets;
/*
 * Garcia Pelaez Juan Bautista
 * Ing. Informatica 3ºB
 * Desarrollo de Servicios Telemáticos
 *

  		  2 bytes    2 bytes       n bytes
          ---------------------------------
   DATA  | 03    |   Block #  |    Data    |
          ---------------------------------

*/


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

public class Data_Packet {

	private final short opCode = 03;
	private short blockNumber;
	private byte[] data;
	private int data_length;
	private byte[] buffer;
	
	public Data_Packet(byte[] data,short blockNumber,int length) throws IOException {
		this.blockNumber = blockNumber;
		this.data = Arrays.copyOf(data,length);
		this.data_length = length;
		this.buffer = new byte[4+length];
		assemblePacket();


	}
	
	public Data_Packet (byte[] buffer, int data_length) throws IOException {
		this.buffer = buffer;
		this.data_length = data_length;
		disassemble();
	}


	public int getBlockNumber() {
		return blockNumber;
	}
	public byte[] getData() {
		return data;
	}
	public byte[] getBuffer() {
		return buffer;
	}
	public int getData_length(){return data_length;}


	public void assemblePacket() throws IOException {


		ByteArrayOutputStream resAux = new ByteArrayOutputStream();

		//Opcode, 2 bytes
		resAux.write((byte)((opCode>>8)&0xFF));
		resAux.write((byte)(opCode&0xFF));

		//Block number, 2 bytes
		resAux.write((byte)((blockNumber>>8)&0xFF));
		resAux.write((byte)(blockNumber&0xFF));

		//Data MAX 512 bytes
		for (int i = 0 ; i <data_length;i++) {
			resAux.write(data[i]);
		}


		resAux.close();

		buffer = resAux.toByteArray();

	}
	
	
	private void disassemble () throws IOException {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(buffer));
		in.readByte(); 
		in.readByte(); 
		
		blockNumber = in.readShort();

		data	= readData(in);
		
	}
	
	
	
	private byte[] readData (DataInputStream in) throws IOException {
		

		ByteArrayOutputStream resAux = new ByteArrayOutputStream();


		for(int i = 0; i<data_length; i++){

			resAux.write(in.readByte());
		}

		resAux.close();
		return resAux.toByteArray();
	}


	public  String toString() {
		return "DATA " + blockNumber + " " + data_length + " bytes";
	}
}
