package exception;

import packets.Error_Packet;

public class ErrorReceivedException extends Exception{
    private String msg;

    public ErrorReceivedException (Error_Packet error_packet){
        super(error_packet.getErrorMsg());
        this.msg = error_packet.getErrorMsg();

   }

   public void printErrorMsg(){
        System.out.println("Error: " + msg);
   }
}
