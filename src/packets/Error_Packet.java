package packets;


/*
	       2 bytes  2 bytes        string    1 byte
          ----------------------------------------
   ERROR | 05    |  ErrorCode |   ErrMsg   |   0  |
          ----------------------------------------
*/





import exception.ErrorReceivedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class Error_Packet {
	private final short opCode = 05;
	private short errorCode;
	private String errorMsg;
	private byte[] buffer;



	private final String[] errorArra = {" Not defined, see error message (if any)",
									"File not found","Access violation","Disk full or allocation exceeded",
									"Illegal TFTP operation","Unknown transfer ID","File already exists",
									"No such user"};


	public Error_Packet (byte[] buffer) throws IOException {
		this.buffer =  buffer;
		disassemble();


	}

	public Error_Packet(short errorCode, String msg, DatagramSocket socket, InetAddress dstAddres, int dstPort) throws IOException {
		this.errorCode = errorCode;
		this.errorMsg  = msg;
		assemblePacket();
		DatagramPacket toSend = new DatagramPacket(this.getBuffer(), this.getBuffer().length, dstAddres, dstPort);
		socket.send(toSend);
	}

	public Error_Packet(short errorCode,  DatagramSocket socket,InetAddress dstAddres, int dstPort) throws IOException, ErrorReceivedException {
		this.errorCode = errorCode;
		this.errorMsg  = errorArra[errorCode];
		assemblePacket();
		DatagramPacket toSend = new DatagramPacket(this.getBuffer(), this.getBuffer().length, dstAddres, dstPort);
		socket.send(toSend);
		throw new ErrorReceivedException(this);

	}

	//Gettes and setters
	public short getOpCode() {
		return opCode;
	}

	public short getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(short errorCode) {
		this.errorCode = errorCode;
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	public String[] getErrorArra() {
		return errorArra;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}





	public void assemblePacket() throws IOException {

		ByteArrayOutputStream resAux = new ByteArrayOutputStream();

		//Opcode, 2 bytes
		resAux.write((byte)((opCode>>8)&0xFF));
		resAux.write((byte)(opCode&0xFF));

		//Error code, 2 bytes
		resAux.write((byte)((errorCode>>8)&0xFF));
		resAux.write((byte)(errorCode&0xFF));

		//Error Msg

        resAux.write(errorMsg.getBytes());


        resAux.write((byte)0);


		buffer = resAux.toByteArray();


	}





	private void disassemble() throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(buffer));

        //Opcode
        if (in.readShort() == opCode) {
            //Error code
            errorCode = in.readShort();

            System.out.println();
            //Error Msg
            errorMsg = new String(readData(in));


            in.close();


        }
    }

	private byte[] readData (DataInputStream in) throws IOException {

		ByteArrayOutputStream resAux = new ByteArrayOutputStream();

		while(in.available()!=0) {
			resAux.write(in.readByte());
		}


		return resAux.toByteArray();
	}
}
