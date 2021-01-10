package packets;

/*
  		  2 bytes    2 bytes       n bytes
          ---------------------------------
   DATA  | 03    |   Block #  |    Data    |
          ---------------------------------

*/


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

public class Data_Packet {
	private static final int MAX_LENGTH = 516;
	
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
		//this.data	= new byte[MAX_LENGTH];
		//this.data_length = MAX_LENGTH;
		disassemble();
	}

	public short getOpCode() {
		return opCode;
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
	public void setBlockNumber(short blockNumber) {
		this.blockNumber = blockNumber;
	}

	

	public void setData(byte[] data) {
		this.data = data;
	}

	public void assemblePacket() throws IOException {

		//ArrayList<Byte> resAux =  new ArrayList<Byte>();
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

		/*for (byte b : data) {
			//System.out.println(b);
			resAux.add(b);
		}*/
		resAux.close();
		//System.out.println("Estamos");
		buffer = resAux.toByteArray();
		//System.out.println("Salimos");
	}
	
	
	private void disassemble () throws IOException {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(buffer));
		in.readByte(); 
		in.readByte(); 
		
		blockNumber = in.readShort();

		data	= readData(in);
		//System.out.println("VAAMOS");
		//System.out.println("XXXX-----" + new String(data));
		//System.out.println(buffer.length);
		//System.out.println(data_length);
		//System.out.println("---------------------------------------------------------------");
		
	}
	
	
	
	private byte[] readData (DataInputStream in) throws IOException {
		

		ByteArrayOutputStream resAux = new ByteArrayOutputStream();
		//System.out.println("VAAMOS");

		for(int i = 0; i<data_length; i++){
			//System.out.println("VAAMOS " + i);
			resAux.write(in.readByte());
		}
		//System.out.println("VAAMOS");
		//System.out.println();

		resAux.close();
		return resAux.toByteArray();
	}


	public  String toString() {
		return "DATA " + blockNumber + " " + data_length + " bytes";
	}
}
