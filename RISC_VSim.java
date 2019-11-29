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
    private byte[] memory;

    public RISC_VSim() throws FileNotFoundException, IOException {
        this.registers = new Registers();
        this.pc = 0;
        this.debug = true;
        this.readProgram();
        this.memory =  new byte[1000000];
    }

    public void readProgram() throws FileNotFoundException, IOException {
        String fileName = "..\\cae-lab-master\\finasgmt\\tests\\task2\\branchcnt.bin";
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
                                registers.writeRegister(rd, registers.readRegister(rs1) >>> registers.readRegister(rs2));
                                if (debug) {System.out.println("srl x" + rd + " x" + rs1 + " x" + rs2);}
                            } else if (funct7 == 0x20) {
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
                    rs2 = (instruction >> 20) & 0x1F;
                    if (opcode == 0x23) {
                        offset = ((instruction >> 20) & 0xFE0) + rd;
                        switch (funct3) {
                            case 0x00:
                                memory[registers.readRegister(rs1) + (offset/8)] = (byte) (registers.readRegister(rs2) & 0xFF);
                                if (debug) {System.out.println("sb " + "x" + rs2 + " " + offset + "(" + rs1 + ")");}
                                break;
                            case 0x01:
                                memory[registers.readRegister(rs1) + (offset/8)] = (byte) (registers.readRegister(rs2) & 0xFF);
                                if (debug) {System.out.println("sb " + "x" + rs2 + " " + offset + "(" + rs1 + ")");}
                                break;
                            case 0x02:
                                break;
                        }
                    } else if (opcode == 0x63) { //SB-type
                        offset = (((instruction >> 31) & 0x01) == 1)
                            ? ((instruction >> 7) & 0x1E)
                            + ((instruction >> 20) & 0x7E0)
                            + ((instruction << 4) & 0x800)
                            + ((instruction >> 31) & 0x1000)
                            + 0xFFFFE000
                            : ((instruction >> 7) & 0x1E)
                            + ((instruction >> 20) & 0x7E0)
                            + ((instruction << 4) & 0x800)
                            + ((instruction >> 31) & 0x1000);
                        switch (funct3) {
                            case 0x00:
                                if (registers.readRegister(rs1) == registers.readRegister(rs2)) {
                                    pc = pc + (offset / 4) - 1; // Incremented at the end of the loop
                                }
                                if (debug) {System.out.println("beq " + "x" + rs1 + " x" + rs2 + " " + offset);}
                                break;
                            case 0x01:
                                if (registers.readRegister(rs1) != registers.readRegister(rs2)) {
                                    pc = pc + (offset / 4) - 1; 
                                }
                                if (debug) {System.out.println("bne " + "x" + rs1 + " x" + rs2 + " " + offset);}
                                break;
                            case 0x04:
                                if (registers.readRegister(rs1) < registers.readRegister(rs2)) {
                                    pc = pc + (offset / 4) - 1; 
                                }
                                if (debug) {System.out.println("blt " + "x" + rs1 + " x" + rs2 + " " + offset);}
                                break;
                            case 0x05:
                                if (registers.readRegister(rs1) >= registers.readRegister(rs2)) {
                                    pc = pc + (offset / 4) - 1; 
                                }
                                if (debug) {System.out.println("bge " + "x" + rs1 + " x" + rs2 + " " + offset);}
                                break;
                            case 0x06:
                                if (Integer.compareUnsigned(registers.readRegister(rs1), registers.readRegister(rs2)) < 0) {
                                    pc = pc + (offset / 4) - 1; 
                                }
                                if (debug) {System.out.println("bltu " + "x" + rs1 + " x" + rs2 + " " + offset);}
                                break;
                            case 0x07:
                                if (Integer.compareUnsigned(registers.readRegister(rs1), registers.readRegister(rs2)) >= 0) {
                                    pc = pc + (offset / 4) - 1;
                                }
                                if (debug) {System.out.println("bgeu " + "x" + rs1 + " x" + rs2 + " " + offset);}
                                break;
                        }
                    }
                    break;
                case 0x73:
                    if (debug) {System.out.println("ecall");}
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
        rv.registers.printRegisters();
    }
}