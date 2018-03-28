import java.util.Scanner;
import com.fazecast.jSerialComm.*;
import java.io.File;

public class Uploader {
        public static final byte[] toBootloaderBytes = new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xF0};
        public static final byte[] startBytes = new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xF1, (byte)0x00, (byte)0x00, (byte)0x00};
        public static final byte[] endBytes = new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xF4, (byte)0x00, (byte)0x00, (byte)0x00};
        public static final byte[] footerBytes = new byte[]{(byte)0x00,(byte)0x00};
        public static final byte[] setHeaderBytes = new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xF2, (byte)0x01};
        public static final byte[] dataHeaderBytes = new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xF3};
        public static void main(String args[]) throws Exception {
                //prompt for input for filename and setup Scanner
                Scanner input = new Scanner(System.in);
		String filepath;
		if(args.length >= 1) {
			filepath = args[0];
		} else {
			System.out.print("Enter filepath to upload: ");
                      	filepath = input.nextLine().trim();
		}
                Scanner infile = null;
                try {
                        infile = new Scanner(new File(filepath));
                } catch(Exception e) {
                        System.out.println("Bad filepath " + filepath);
                        System.exit(0);
                }
                //List Serial Ports and prompt for which to connect over
                SerialPort[] portList = SerialPort.getCommPorts();
                for(int i = 0; i < portList.length; i++) {
                        System.out.println(i + " " + portList[i].getSystemPortName());
                }
		int portNum;
		if(args.length >= 2) {
			portNum = Integer.parseInt(args[1]);
		} else {
			System.out.print("Enter number of serial port to use for upload: ");
			portNum = Integer.parseInt(input.nextLine().trim());
		}
                SerialPort port = portList[portNum];
                System.out.println("Opening " + port.getSystemPortName());
                if(!port.openPort()) {
                        System.out.println("Failed to open port " + port.getSystemPortName());
                        System.exit(0);
                }
                while(!port.isOpen());
                port.setBaudRate(115200);
		port.setNumDataBits(8);
		port.setNumStopBits(SerialPort.ONE_STOP_BIT);
		port.setParity(SerialPort.NO_PARITY);
                Thread.sleep(10);
		//get into bootloading mode
		/*System.out.println("Entering ROV Bootloader Mode.\nYou should see the current program on the ROV cease and the LED blink 5x");
		port.writeBytes(toBootloaderBytes, 3);
		System.out.println("Press enter if it worked");
		while(!input.hasNextLine());
		Thread.sleep(500);*/
                //Start upload
                System.out.println("Starting upload of " + filepath);
                print(startBytes);
                port.writeBytes(startBytes, 6);
                Thread.sleep(10);
                System.out.println("Began program transmission");
                String data = "";
                int pageAddress = 0;
                byte[] dataBuffer = new byte[128];
                byte[] sendBuffer = new byte[150];
                //for every record in the hex file
                while(infile.hasNext()) {
                        String line = infile.nextLine();
                        //the record is 0x00 for data and 0x01 for an end of file
                        int record = Integer.parseInt(line.substring(7,9), 16);
                        data += line.substring(9, (line.length() - 2));
                        //If the record amounts to a full page or we have reached end of file
                        if(data.length() > 255 || record == 1) {
                                System.out.println("Wrote page " + (pageAddress+1) + " of 128");
                                print(setHeaderBytes);
                                port.writeBytes(setHeaderBytes, 4);
                                byte[] b = new byte[1];
                                b[0] = (byte)pageAddress;
                                print(b);
                                port.writeBytes(b, 1);
                                print(footerBytes);
                                port.writeBytes(footerBytes, 2);
				Thread.sleep(1000);
                                //Two hexadecimal characters per byte of data
                                int length = data.length() / 2;
                                for(int i = 0, j =0; i < data.length() && j < dataBuffer.length; i+=2) {
                                        dataBuffer[j] = (byte)Integer.parseInt(
                                                data.substring(i, i+2), 16);
                                        j++;
                                }
				Thread.sleep(10);
                                print(dataHeaderBytes);
                                port.writeBytes(dataHeaderBytes, 3);
                                b[0] = (byte)length;
                                print(b);
                                port.writeBytes(b, 1);
                                print(dataBuffer);
                                port.writeBytes(dataBuffer, dataBuffer.length);
                                print(footerBytes);
                                port.writeBytes(footerBytes, 2);
                                for(int i = 0; i < dataBuffer.length; i++) {
                                     dataBuffer[i] = 0;
                                } 
                                //reset variables
                                data = "";
                                pageAddress++;
                                Thread.sleep(1000);
                        }
                }
		Thread.sleep(1000);
                System.out.println("Sending bootloader end");
                print(endBytes);
                port.writeBytes(endBytes, 6);
                System.out.println("All done, exiting (Hope it worked)");
        }
        public static void print(byte[] b) {
                for(byte x : b) {
                        System.out.print(" x" + Integer.toHexString(x & 0xFF));
                }
                System.out.print("\n");
        }
}
