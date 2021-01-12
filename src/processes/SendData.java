package processes;

import packets.ACK_Packet;
import packets.Data_Packet;
import packets.Error_Packet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.net.InetAddress;
import java.util.Random;

public class SendData {
    //Tamaño maximo de los datagramas
    private static final int ECHOMAX = 516;

    //Simulacion de errores
    private static Random prob = new Random();

    public SendData(DatagramSocket ds, InetAddress dstAddres, int dstPort, String PATH, boolean errors, boolean verbose) throws IOException {
        DatagramPacket toSend;
        try {
            //Abrimos el archivo a enviar
            File file = new File(PATH);

            //Para leer el archivo
            FileInputStream in = new FileInputStream(file);

            //Paquete de datos
            Data_Packet data;
            byte [] buffer ;
            short blockN = 1;

            //boleeano para la finalizacion del proceso
            boolean done = false;

            while(!done) {

                //Numero de intentos
                int tries = -1;

                //516 - 4 bytes
                buffer = new byte[ECHOMAX-4];

                //Longitud de los datos
                int length = in.read(buffer);



                //Creamos el paquete de datos
                data = new Data_Packet(buffer, blockN, length);

                //Datagrama a enviar
                toSend = new DatagramPacket(data.getBuffer(), data.getBuffer().length, dstAddres, dstPort);

                //Paquete para recibir los ACK
                ACK_Packet ack_packet;

                boolean received = false;

                do{
                    tries++;

                    //Si tenemos activada la simulación de fallos
                    if(errors){

                        //Probabilidad de fallo de 1/20
                        if( prob.nextInt(20)!=1 ) {
                            //Lo enviamos
                            ds.send(toSend);
                        }

                    }else{
                        ds.send(toSend);
                    }

                    //Establecemos el timeout en 1s = 1000ms
                    ds.setSoTimeout(1000);

                    if(verbose){

                        //Si no es el primer intento
                        if(tries>0){
                            System.out.println("RESEND----> " + data.toString());

                            //Si es el primer intento
                        }else{
                            System.out.println("----> " + data.toString());
                        }
                    }

                    //Recibimos el ACK
                    ack_packet = new ACK_Packet(blockN);
                    if(ack_packet.receiveACK(ds,dstAddres,dstPort)!=null){

                        received = true;

                        if(verbose) {
                            System.out.println("                                  <----  " + ack_packet.toString());
                        }

                    }

                //Mientras no haya recibido el paquete y no se hayan superado el nuemero de intentos
                }while( !received && tries <= 3);
                ds.setSoTimeout(0);

                if(!received){
                    System.out.println("Error max tries reached");
                    done = true;

                //Si se ha recibido el paquete
                }else{

                    //Aumentamos el numero de secuencia
                    blockN++;

                    //Si es el ultimo paquete
                    if (length < 512) done = true;

                }

            }

        } catch (FileNotFoundException e) {
            Error_Packet error_packet = new Error_Packet((short)1);
            toSend = new DatagramPacket(error_packet.getBuffer(), error_packet.getBuffer().length, dstAddres, dstPort);
            ds.send(toSend);
            System.out.println(error_packet.getErrorMsg());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
