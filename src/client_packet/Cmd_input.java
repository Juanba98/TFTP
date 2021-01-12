package client_packet;
/*
 * Garcia Pelaez Juan Bautista
 * Ing. Informatica 3ºB
 * Desarrollo de Servicios Telemáticos
 *
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

public class Cmd_input {
    private static String IP_Serv ;
    private static String mode ;
    private static String op	;
    private static String filename ;
    private static boolean verbose = false;
    private static boolean quit ;
    private static int nRequest;
    private static boolean save = false;
    public Cmd_input (int nRequest) {
         IP_Serv = "";
         mode = "";
        op	= "";
        filename = "";
        verbose = false;
        quit = false;
        this.nRequest = nRequest;
        if(this.nRequest == 0) read_cmd0();
        else read_cmd1();

    }

    private static void read_cmd1() {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        try {
            while((op.equals("") || mode.equals("") || filename.equals("")) && !quit ) {
                System.out.print("tftp>");
                String temp = null;

                temp = stdIn.readLine();

                String[] tempAux = temp.split(" ");

                switch (tempAux[0].toLowerCase()) {

                    case "connect":
                        IP_Serv = tempAux[1];

                        break;

                    case "mode":
                        mode = tempAux[1];

                        break;

                    case "get":
                        op = "RRQ";



                            filename = tempAux[1];


                        break;

                    case "put":
                        op = "WRQ";



                            filename = tempAux[1];


                        break;

                    case "verbose":
                        verbose = !verbose;

                        break;


                    case "quit":
                        quit = true;


                        break;


                    case "save":
                        save = !save;

                        break;


                    default:
                        System.out.println("Error, try again");


                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void read_cmd0()  {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        try {
            while(( IP_Serv.equals("") ||op.equals("") || mode.equals("") || filename.equals("")) && !quit ) {
                System.out.print("tftp>");
                String temp = null;

                temp = stdIn.readLine();

                String[] tempAux = temp.split(" ");

                switch (tempAux[0].toLowerCase()) {

                    case "connect":
                        IP_Serv = tempAux[1];

                        break;

                    case "mode":
                        mode = tempAux[1];

                        break;

                    case "get":
                        op = "RRQ";

                        if (IP_Serv.equals("")) {

                            IP_Serv = tempAux[1].split(":")[0];
                            filename = tempAux[1].split(":")[1];

                        } else {

                            filename = tempAux[1];
                        }

                        break;

                    case "put":
                        op = "WRQ";

                        if (IP_Serv.equals("")) {

                            IP_Serv = tempAux[1].split(":")[0];
                            filename = tempAux[1].split(":")[1];

                        } else {

                            filename = tempAux[1];
                        }

                        break;

                    case "verbose":
                        verbose = !verbose;

                        break;


                    case "quit":
                        quit = true;


                        break;

                    case "save":
                        save = !save;

                        break;

                    default:
                        System.out.println("Error, try again");


                }
            }




        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String getIP_Serv() {
        return IP_Serv;
    }

    public static String getMode() {
        return mode;
    }

    public static String getOp() {
        return op;
    }

    public static String getFilename() {
        return filename;
    }

    public static boolean getVerbose() {
        return verbose;
    }

    public static boolean getQuit() {
        return quit;
    }

    public static boolean getSave() {
        return save;
    }
}
