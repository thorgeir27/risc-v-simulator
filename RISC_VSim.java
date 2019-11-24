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
    private boolean debug;

    public RISC_VSim() throws FileNotFoundException, IOException {
        this.registers = new Registers();
        this.pc = 0;
        this.debug = true;
        this.readProgram();
    }

    public void readProgram() throws FileNotFoundException, IOException {
        String fileName = "..\\cae-lab-master\\finasgmt\\tests\\task1\\addpos.bin";
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

    public void execute() {
        System.out.println("Executing...\n");

        int instruction;
        int opcode;
        int rd;
        int rs1;
        int imm;

        programLoop : while(true) {
            instruction = program[pc];
            opcode = instruction & 0x7f;
            rd = (instruction >> 7) & 0x01f;
            rs1 = (instruction >> 15) & 0x01f;
            imm = (instruction >> 20);

            switch (opcode) {
                
                case 0x13:
                    registers.writeRegister(rd, registers.readRegister(rs1) + imm);
                    if (debug) {System.out.println("addi x" + rd + " x" + rs1 + " " + imm);}
                    break;
                case 0x33:
                    registers.writeRegister(rd, registers.readRegister(rs1) + registers.readRegister(imm));
                    if (debug) {System.out.println("add x" + rd + " x" + rs1 + " x" + imm);}
                    break;
                case 0x73:
                    if (debug) {System.out.println("ecall 10");}
                    break programLoop;
                default:
                    if (debug) {System.out.println("Opcode " + opcode + " not yet implemented");}
                    break;
            }
            pc++;

        }

    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        System.out.println("\n---RISC-V Simulator---\n");
        RISC_VSim rv = new RISC_VSim();
        rv.execute();
        rv.registers.dumpRegisters();
    }
}