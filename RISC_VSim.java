import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * RISC-V Instruction Set Simulator
 * 
 * @author Þorgeir Sigurðarson
 *
 */

public class RISC_VSim {
    private Registers registers;
    private int[] program;
    private int pc;

    public void readProgram() throws FileNotFoundException, IOException {
        
        String fileName = "C:\\Users\\thorg\\Desktop\\Skóli\\Haust19\\Computer Architecture\\cae-lab-master\\finasgmt\\tests\\task1\\addpos.bin";
        File file = new File(fileName);
        int[] instructions = new int[(int) file.length()];
        int i = 0;

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            int singleInt;
      
            while((singleInt = fileInputStream.read()) != -1) {
                instructions[i] = singleInt;
                i++;
            }
        }

        program = new int[instructions.length / 4];

        for(i = 0;i < program.length;i++) {
            program[i] = instructions[i*4]
                + (instructions[i*4+1] << 8)
                + (instructions[i*4+2] << 16)
                + (instructions[i*4+3] << 24);
        }
    }


    public static void main(String[] args) throws FileNotFoundException, IOException {
        RISC_VSim rv = new RISC_VSim();
        rv.readProgram();
        int[] ba = rv.program;

    }
}