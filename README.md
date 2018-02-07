# bootloader
This is the bootloader for uploading firmware to the ROV over its RS-485 based tether.

Credit goes to jgillick's avr-multidrop-bootloader, which we reverse-engineered and modified for our purposes.

<h1>Bootloader Installation</h1>

To install the bootloader, connect the ROV computer to the surface computer using an ISP programmer. In the "ROV" directory run

<code>make clean</code>

<code>make</code>

<code>make program</code>

This will compile and upload the bootloader code to the ROV. You should see the status LED of the ROV blink 5x and then stop.

<h1>Uploading Code Using the Bootloader</h1>

Compile the ROV firmware and note the location of the resulting binary (.hex file). You could use the test_program, which blinks the ROV status LED, for example.

Compile the Uploader code (written in Java) with <code>javac -cp ".:./jars/jSerialComm-1.3.11.jar" Uploader.java</code>

To run the Uploader, do <code>java -cp ".:./jars/jSerialComm-1.3.11.jar" Uploader</code>

When prompted, provide the path to the hex file. When prompted, select /dev/ttyAMA0, the Raspberry Pi TTL Serial Port on its GPIO pins.

The code should upload, and when it finishes, the ROV computer will restart. This time, the onboard status LED will blink only twice, and the program will start immediately without first running the bootloader. You can power cycle the ROV computer, and the program will still run. If you are using the test_program code, the LED will blink one second on, one second off. To upload new code, you need to send <code>0xFF 0xFF 0xF5 0x00 0x00 0x00</code>. For example, you can use the command <code>printf "\xFF\xFF\xF5\x00\x00\x00" > /dev/ttyAMA0</code>.

<h1>How the bootloader works</h1>

1. When the ROV starts up, the bootloader runs. If the first EEPROM index (0x00) is set to the value 0x01, the bootloader runs the main firmware.

2. Otherwise, the ROV waits for new firmware.

3. After firmware is installed, the ROV sets EEPROM index 0x00 to 0x01 and resets (go to step 1).

<h1>What your code must do</h1>

1. When your code starts, it should set EEPROM index 0x00 to the value 0x01 to stop us from having to run the bootloader again.

2. Now do whatever the program should do. This includes receiving and processing any serial data and sending new data.

3. Upon receiving a command you choose, change the value in EEPROM index 0x00 to anything except 0x01 (e.g. 0x42 :D).

4. Reset the ROV computer in software and the bootloader will run this time.
