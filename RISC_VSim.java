import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

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
    private Memory memory;

    public RISC_VSim(String[] args) throws FileNotFoundException, IOException {
        this.registers = new Registers();
        this.pc = 0;
        this.readProgram(args[0]);
        this.memory =  new Memory(10000000);
        if (args.length > 1) {this.debug = args[1].equals("0") ? false : true;}
        else {this.debug = true;}
    }

    public void readProgram(String fileName) throws FileNotFoundException, IOException {
        //String fileName = "..\\cae-lab-master\\finasgmt\\tests\\final\\final\\simple.bin";
        //Scanner scanner = new Scanner(System.in);
        //String fileName = scanner.nextLine();
        //scanner.close();
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

        int instruction, opcode, rd, funct3, funct7, rs1, rs2, imm, offset, temp;

        programLoop : while(true) {
            instruction = program[pc];

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
                                temp = memory.load(registers.readRegister(rs1) + imm, 8);
                                temp = ((temp >> 7) & 0x01) == 1 
                                    ? temp | 0xFFFFFF00
                                    : temp;
                                registers.writeRegister(rd, temp);
                                if (debug) {System.out.println("lb " + "x" + rd + " " + imm + "(x" + rs1 + ")");}
                            }
                            break;
                        case 0x01:
                            if (opcode == 0x13) {
                                registers.writeRegister(rd, registers.readRegister(rs1)  << (imm & 0x1F));
                                if (debug) {System.out.println("slli x" + rd + " x" + rs1  + " " + (imm & 0x1F));}
                            } else if (opcode == 0x03) {
                                //temp = memory.load(registers.readRegister(rs1) + imm + 15, 1) == 1
                                //    ? memory.load(registers.readRegister(rs1) + imm, 16) | 0xFFFF0000
                                //    : memory.load(registers.readRegister(rs1) + imm, 16);
                                temp = memory.load(registers.readRegister(rs1) + imm, 16);
                                temp = ((temp >> 15) & 0x01) == 1
                                    ? temp | 0xFFFF0000
                                    : temp;
                                registers.writeRegister(rd, temp);
                                if (debug) {System.out.println("lh " + "x" + rd + " " + imm + "(x" + rs1 + ")");}
                            }
                            break;
                        case 0x02:
                            if (opcode == 0x13) {
                                temp = registers.readRegister(rs1) < imm ? 1 : 0;
                                registers.writeRegister(rd, temp); 
                                if (debug) {System.out.println("slti x" + rd + " x" + rs1 + " " + imm);}
                            } else if (opcode == 0x03) {
                                registers.writeRegister(rd, memory.load(registers.readRegister(rs1) + imm, 32));
                                if (debug) {System.out.println("lw " + "x" + rd + " " + imm + "(x" + rs1 + ")");}
                            }
                            break;
                        case 0x03:
                            temp = Integer.compareUnsigned(registers.readRegister(rs1), imm) > 0 ? 1 : 0;
                            registers.writeRegister(rd, temp); 
                            if (debug) {System.out.println("sltiu x" + rd + " x" + rs1 + " " + imm);}
                            break;
                        case 0x04:
                            if (opcode == 0x13) {
                                registers.writeRegister(rd, registers.readRegister(rs1) ^ imm);
                                if (debug) {System.out.println("xori x" + rd + " x" + rs1 + " " + imm);}
                            } else if (opcode == 0x03) {
                                registers.writeRegister(rd, memory.load(registers.readRegister(rs1) + imm, 8));
                                if (debug) {System.out.println("lbu " + "x" + rd + " " + imm + "(x" + rs1 + ")");}

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
                                registers.writeRegister(rd, memory.load(registers.readRegister(rs1) + imm, 16));
                                if (debug) {System.out.println("lhu " + "x" + rd + " " + imm + "(x" + rs1 + ")");}
                            }
                            break;
                        case 0x06:
                            registers.writeRegister(rd, registers.readRegister(rs1) | imm);
                            if (debug) {System.out.println("ori x" + rd + " x" + rs1 + " " + imm);}
                            break;
                        case 0x07:
                            registers.writeRegister(rd, registers.readRegister(rs1) & imm);
                            if (debug) {System.out.println("andi x" + rd + " x" + rs1 + " " + imm);}
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
                            registers.writeRegister(rd, registers.readRegister(rs1)  << registers.readRegister(rs2));
                            if (debug) {System.out.println("sll x" + rd + " x" + rs1  + " x" + rs2);}
                            break;
                        case 0x02:
                            temp = registers.readRegister(rs1) < registers.readRegister(rs2) ? 1 : 0;
                            registers.writeRegister(rd, temp); 
                            if (debug) {System.out.println("slt x" + rd + " x" + rs1 + " x" + rs2);}
                            break;
                        case 0x03:
                            temp = Integer.compareUnsigned(registers.readRegister(rs1), registers.readRegister(rs2)) > 0 ? 1 : 0;
                            registers.writeRegister(rd, temp); 
                            if (debug) {System.out.println("sltu x" + rd + " x" + rs1 + " x" + rs2);}
                            break;
                        case 0x04:
                            registers.writeRegister(rd, registers.readRegister(rs1) ^ registers.readRegister(rs2));
                            if (debug) {System.out.println("xor x" + rd + " x" + rs1 + " x" + rs2);}
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
                            registers.writeRegister(rd, registers.readRegister(rs1) | registers.readRegister(rs2));
                            if (debug) {System.out.println("or x" + rd + " x" + rs1 + " x" + rs2);}
                            break;
                        case 0x07:
                            registers.writeRegister(rd, registers.readRegister(rs1) & registers.readRegister(rs2));
                            if (debug) {System.out.println("and x" + rd + " x" + rs1 + " x" + rs2);}
                            break;
                    }
                    break;
                case 0x37:
                case 0x6F:
                case 0x17: // U-type
                    imm = (instruction >>> 12);
                    if (opcode == 0x37) {
                        registers.writeRegister(rd, (imm << 12));
                        if (debug) {System.out.println("lui x" + rd + " " + imm);}
                    } else if (opcode == 0x6F) {
                        // JAL
                        offset = ((imm >> 19) & 0x01) == 1
                            ? ((imm << 13) & 0xFE000)
                            + ((imm << 5) &  0x1000)
                            + ((imm >> 8) &  0xFFE)
                            + ((imm << 1) & 0x100000)
                            | 0xFFE00000
                            : ((imm << 13) & 0xFE000)
                            + ((imm << 5) &  0x1000)
                            + ((imm >> 8) &  0xFFE)
                            + ((imm << 1) & 0x100000);
                        registers.writeRegister(rd, (pc + 1)*4);
                        pc = pc + (offset/4) -1;
                        if (debug) {System.out.println("jal x" + rd + " " + offset);}
                    } else if (opcode == 0x17) {
                        registers.writeRegister(rd, (imm << 12) + pc);
                        if (debug) {System.out.println("auipc x" + rd + " " + imm);}
                    }
                    break;
                case 0x23:
                case 0x63: // S-type
                    offset = rd;
                    rs2 = (instruction >> 20) & 0x1F;
                    if (opcode == 0x23) {
                        offset = (((instruction >> 31) & 0x01) == 1)
                            ? ((instruction >> 20) & 0xFE0) + rd - 4096
                            : ((instruction >> 20) & 0xFE0) + rd;
                        switch (funct3) {
                            case 0x00:
                                memory.store(registers.readRegister(rs1) + offset, 8, registers.readRegister(rs2));
                                if (debug) {System.out.println("sb " + "x" + rs2 + " " + offset + "(x" + rs1 + ")");}
                                break;
                            case 0x01:
                                memory.store(registers.readRegister(rs1) + offset, 16, registers.readRegister(rs2));
                                if (debug) {System.out.println("sh " + "x" + rs2 + " " + offset + "(x" + rs1 + ")");}
                                break;
                            case 0x02:
                                memory.store(registers.readRegister(rs1) + offset, 32, registers.readRegister(rs2));
                                if (debug) {System.out.println("sw " + "x" + rs2 + " " + offset + "(x" + rs1 + ")");}
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
                case 0x67:
                    imm = (instruction >> 20);
                    registers.writeRegister(rd, (pc+1)*4);
                    pc = ((registers.readRegister(rs1) + imm) / 4) - 1;
                    if ( debug) {System.out.println("jalr x" + rd + " x" + rs1 + " " + imm);}
                    break;
            }
            pc++;
            if (pc >= program.length) {break programLoop;}
        }

    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        if (args.length == 0) {
            System.out.println("No binary file provided");
            return;
        }
        System.out.println("\n---RISC-V Simulator---\n");
        RISC_VSim rv = new RISC_VSim(args);
        rv.execute();
        rv.registers.dumpRegisters();
        rv.registers.printRegisters();
    }
}