# RISC-V Simulator
Simulator that can execute RISC-V programs (RV32I)

compile:
```
javac RISC_VSim.java Registers.java Memory.java
```

run:
```
java RISC_VSim program.bin
```
Runs the program program.bin and dumps register content in output.res after execution. 

Optionally, running:
```
java RISC_VSim program.bin 0
```
runs the program without printing out each instruction