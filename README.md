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
