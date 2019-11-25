import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.ByteOrder;
import java.io.RandomAccessFile;

public class Registers {

    private int[] x;

    public Registers() {
        this.x = new int[32];
    }

    public int readRegister(int register) {
        return x[register];
    }

    public void writeRegister(int register, int value) {
        if (register != 0) {
            x[register] = value;
        }
    }

    public void printRegisters() {
        System.out.println("\nRegister content:\n");
        for (int i = 0;i < x.length; i++) {
            System.out.println("x" + i + ": " + x[i]);
        }
    }

    public void dumpRegisters() throws FileNotFoundException, IOException {
        ByteBuffer buffer = ByteBuffer.allocate(128);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i <x.length; i++) {
            buffer.putInt(x[i]);
        }
        buffer.position(0);
        try (RandomAccessFile writer = new RandomAccessFile("output.res", "rw");
        FileChannel channel = writer.getChannel()){
            channel.write(buffer);
        }
    }
}