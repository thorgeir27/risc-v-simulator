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
        String fileName = "..\\cae-lab-master\\finasgmt\\tests\\task1\\shift.bin";
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
        if (debug) {System.out.println("Basic code:\n");}

        int instruction, opcode, rd, funct3, funct7, rs1, rs2, imm, offset;

        programLoop : while(true) {
            instruction = program[pc];
            /*opcode = instruction & 0x7f;
            rd = (instruction >> 7) & 0x01f;
            rs1 = (instruction >> 15) & 0x01f;
            imm = (instruction >> 20);
            immL = (instruction >> 12)  & 0xFFFFF; 12 bits ekki tíu!!
            */
            opcode = instruction & 0x7F;
            rd = (instruction >> 7) & 0x1F;
            funct3 = (instruction >> 12) & 0x07;
            rs1 = (instruction >> 15) & 0x1F;

            switch (opcode) {
                case 0x03: //I-type
                case 0x13: //
                    imm = (instruction >> 20);
                    switch (funct3) {
                        case 0x00:
                            if (opcode == 0x13) {
                                registers.writeRegister(rd, registers.readRegister(rs1) + imm);
                                if (debug) {System.out.println("addi x" + rd + " x" + rs1 + " " + imm);}
                            } else if (opcode == 0x03) {
                                System.out.println("LB");
                            }
                            break;
                        case 0x01:
                            if (opcode == 0x13) {
                                registers.writeRegister(rd, registers.readRegister(rs1)  << (imm & 0x1F));
                                if (debug) {System.out.println("slli x" + rd + " x" + rs1  + " " + (imm & 0x1F));}
                            } else if (opcode == 0x03) {
                                System.out.println("LH");
                            }
                            break;
                        case 0x02:
                            if (opcode == 0x13) {
                                System.out.println("SLTI");
                            } else if (opcode == 0x03) {
                                System.out.println("LW");
                            }
                            break;
                        case 0x03:
                            System.out.println("SLTIU");
                            break;
                        case 0x04:
                            if (opcode == 0x13) {
                                System.out.println("XORI");
                            } else if (opcode == 0x03) {
                                System.out.println("LBU");
                            }
                            break;
                        case 0x05:
                            if (opcode == 0x13) {
                                if ((imm >> 5) == 0x00) {
                                    registers.writeRegister(rd, registers.readRegister(rs1) >>> (imm & 0x1F));
                                    if (debug) {System.out.println("srli x" + rd + " x" + rs1 + " " + (imm & 0x1F));}
                                } else if ((imm >> 5) == 0x20) {
                                    registers.writeRegister(rd, registers.readRegister(rs1) >> (imm & 0x1F));
                                    if (debug) {System.out.println("srai x" + rd + " x" + rs1 + " " + (imm & 0x1F));}
                                }
                            } else if (opcode == 0x03) {
                                System.out.println("LHU");
                            }
                            break;
                        case 0x06:
                            System.out.println("ORI");
                            break;
                        case 0x07:
                            System.out.println("ANDI");
                            break;
                    }
                    break;
                case 0x33: // R-type
                    rs2 = (instruction >> 20) & 0x1F;
                    funct7 = (instruction >> 25) & 0x7F;
                    switch (funct3) {
                        case 0x00:
                            if (funct7 == 0x00) {
                                registers.writeRegister(rd, registers.readRegister(rs1) + registers.readRegister(rs2));
                                if (debug) {System.out.println("add x" + rd + " x" + rs1 + " x" + rs2);}
                            } else if (funct7 == 0x20) {
                                registers.writeRegister(rd, registers.readRegister(rs1) - registers.readRegister(rs2));
                                if (debug) {System.out.println("sub x" + rd + " x" + rs1 + " x" + rs2);}
                            }
                            break;
                        case 0x01:
                            System.out.println("SLL");
                            break;
                        case 0x02:
                            System.out.println("SLT");
                            break;
                        case 0x03:
                            System.out.println("SLTU");
                            break;
                        case 0x04:
                            System.out.println("XOR");
                            break;
                        case 0x05:
                            if (funct7 == 0x00) {
                                System.out.println("SRL");
                                registers.writeRegister(rd, registers.readRegister(rs1) >>> registers.readRegister(rs2));
                                if (debug) {System.out.println("srl x" + rd + " x" + rs1 + " x" + rs2);}
                            } else if (funct7 == 0x20) {
                                System.out.println("SRA");
                                registers.writeRegister(rd, rs1 >> registers.readRegister(rs2));
                                if (debug) {System.out.println("sra x" + rd + " x" + rs1 + " x" + rs2);}
                            }
                            break;
                        case 0x06:
                            System.out.println("OR");
                            break;
                        case 0x07:
                            System.out.println("AND");
                            break;
                    }
                    break;
                case 0x37:
                case 0x17: // U-type
                    imm = (instruction >>> 12);//  & 0xFFFFF;
                    if (opcode == 0x37) {
                        registers.writeRegister(rd, (imm << 12));
                        if (debug) {System.out.println("lui x" + rd + " " + imm);}
                    } else if (opcode == 0x6F) {
                        // TODO
                    }
                    break;
                case 0x23:
                case 0x63: // S-type
                    offset = rd;
                    if (opcode == 0x23) {
                        // TODO
                    } else if (opcode == 0x63) {
                        // TODO
                    }
                    break;
                case 0x73:
                    if (debug) {System.out.println("ecall");}
                    break programLoop;
                default:
                    if (debug) {System.out.println("Opcode " + opcode + " not yet implemented");}
                    break;
            }

            /*
            switch (opcode) {
                case 0x13:
                    registers.writeRegister(rd, registers.readRegister(rs1) + imm);
                    if (debug) {System.out.println("addi x" + rd + " x" + rs1 + " " + imm);}
                    break;
                case 0x33:
                    registers.writeRegister(rd, registers.readRegister(rs1) + registers.readRegister(imm));
                    if (debug) {System.out.println("add x" + rd + " x" + rs1 + " x" + imm);}
                    break;
                case 0x37:
                    registers.writeRegister(rd, (immL << 12));
                    if (debug) {System.out.println("lui x" + rd + " " + immL);}
                    break;
                case 0x73:
                    if (debug) {System.out.println("ecall");}
                    break programLoop;
                default:
                    if (debug) {System.out.println("Opcode " + opcode + " not yet implemented");}
                    break;
            }
            */
            pc++;
        }

    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        System.out.println("\n---RISC-V Simulator---\n");
        RISC_VSim rv = new RISC_VSim();
        rv.execute();
        rv.registers.dumpRegisters();
        rv.registers.printRegisters();
    }
}