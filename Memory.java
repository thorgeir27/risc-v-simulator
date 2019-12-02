public class Memory {

    private int[] memory;

    public Memory(int size) {
        this.memory = new int[size*8];
    }
    public void store(int location, int size, int value) {
        for (int i = 0;i < size;i++) {
            memory[(location*8) + i] = (value >> i) & 0x01;
        }
    }
    public int load(int location, int size) {
        int value = 0;
        for (int i = 0; i < size;i++) {
            value = value + ((memory[(location*8) + i]) * ((int) Math.pow(2, i)));
        }
        return value;
    }
}