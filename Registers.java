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
}