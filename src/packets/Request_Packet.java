package packets;
/*
           2 bytes    string   1 byte     string   1 byte
          -----------------------------------------------
   RRQ/  | 01/02 |  Filename  |   0  |    Mode    |   0  |
   WRQ    -----------------------------------------------

*/


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Request_Packet {
	private static final short OPRRQ		= 01;
	private static final short OPWRQ		= 02;
	private	 short	opCode;
	private  byte[]	buffer; 
	
	
	private  String fileName;
	private  String mode ;
	
	//Constructores
	public Request_Packet(String fileName, String mode, String op){
			
		this.fileName	= 	fileName;
		this.mode		=	mode;
		if(op.equals("RRQ")) this.opCode = OPRRQ;
		else this.opCode = OPWRQ;

		
	}
	
	
	public Request_Packet(byte[] buffer) {
		
		this.buffer	= buffer;
		disassemble();
	}
	
	public Request_Packet () {
		this(null,null,null);
	}
	
	
	//Getters
	public short getOpcode() {
		
		return	opCode;
	}
	
	public String getFileName() {
	
		return	fileName;
	}
	
	public String getMode() {
		
		return	mode;
	}
	
	public byte[] getBuffer() {
		
		return this.buffer;
	}
	
	
	//Setters 
	public void setOpcode(short opcode) {
		
		this.opCode = opcode;
	}
	
	public void setFileName(String fileName) {
	
		this.fileName = fileName;
	}
	
	public void setMode(String mode) {
		
		this.mode = mode;
	}
	
	public void setBuffer (byte[] buffer) {
		this.buffer = buffer;
	}
	
	
	public void assemblePacket()  {
		ByteArrayOutputStream resAux = new ByteArrayOutputStream();
		try {
		//Opcode, 2 bytes
		resAux.write((byte)((opCode>>8)&0xFF));
		resAux.write((byte)(opCode&0xFF));
		
		//File name, n bytes

			resAux.write(fileName.getBytes());



		//1 byte
		resAux.write((byte)0);
		
		
		//Mode, n bytes
		resAux.write(mode.getBytes());


		//1 byte
		resAux.write((byte)0);
		
		buffer = resAux.toByteArray();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void disassemble ()  {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(buffer));

		try {
			opCode		= in.readShort();

		fileName	= new String(readData(in));
		mode		= new String(readData(in));

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private byte[] readData (DataInputStream in)  {

		ByteArrayOutputStream resAux = new ByteArrayOutputStream();
		try {

			byte b = 0;

				b = in.readByte();


			while(b != 0) {

				resAux.write(b);
				b = in.readByte();


			}
				return resAux.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();

		}

		return null;
	}

	public void printData () {
		
		String op = "Error";
		if(opCode == OPRRQ) {
			op = "RRQ";
			
		}else if(opCode == OPWRQ) {
			op = "WRQ";
		}
		
		
		
		System.out.println("\tModo de operacion: " + op);
		System.out.println("\tNombre fichero: " + fileName);
		System.out.println("\tModo transmision: " + mode);
		
	}
	public String toString(){
		String op = "Error";
		if(opCode == OPRRQ) {
			op = "RRQ";

		}else if(opCode == OPWRQ) {
			op = "WRQ";
		}
		return op +" `" + fileName + "`" +" `"+mode+"`";
	}
}





































