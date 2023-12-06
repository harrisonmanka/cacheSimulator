import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Cache {

    private int hits;
    private int miss;
    private Set<Block> set;
    int count;

    public Cache(String file) throws FileNotFoundException {
        this.set = new HashSet<Block>();
        this.count = 0;
        read(file);
    }

    private void read(String file) throws FileNotFoundException {
        File fileObj = new File(file);
        Scanner sc = new Scanner(fileObj);

        int sets = sc.useDelimiter("[^\\d]+").nextInt();
        int setSize = sc.useDelimiter("[^\\d]+").nextInt();
        int lines = sc.useDelimiter("[^\\d]+").nextInt();

        sc.nextLine();

        while (sc.hasNext()) {
            String[] line = sc.nextLine().split(":");
            String cmd = line[0];
            int size = Integer.parseInt(line[1]);
            int address = Integer.parseInt(line[2], 16);
            System.out.println(cmd + " " + size + " " + address);

            int offsetSize = findOffsetSize(lines);
            int indexSize = findIndexSize(sets);
            int tagSize = lines - offsetSize - indexSize;

            int tag = findTag(address, tagSize, lines);
            int offset = findOffset(address, offsetSize);
            int index = findIndex(address, indexSize, offsetSize);

            System.out.println("tag is: " + tag);
            System.out.println("index is: " + index);
            System.out.println("offset is: " + offset);
            System.out.println();

            if(address % size == 0){
                Block block = new Block(false, tag, index, offset, count);
                set.add(block);
                count++;
                switch(cmd){
                    case "read" -> {
                        if(count != 1){
                            boolean check = set.contains(block);
                            if(check){ //if in set --> hit
                                output(cmd, address, tag, index, offset, "hit", 0);
                            }
                            else{ //if not --> miss and memRef+1
                                output(cmd, address, tag, index, offset, "miss", 1);
                            }
                        }
                    }
                    case "write" -> {

                    }
                }
            }
            else{
                System.out.println("Misalignment in the data.");
                System.exit(1);
            }
        }
    }

    public void output(String access, int address, int tag, int index, int offset, String result, int ref){
        access = String.format("%6s", access);
        String addressS = String.format("%8d", address);
        String tagS = String.format("%7d", tag);
        String indexS = String.format("%5d", index);
        String offsetS = String.format("%6d", offset);
        result = String.format("%6s", result);
    }

    public int findOffsetSize(int lines){
        return (int) (Math.log(lines) / Math.log(2));
    }

    public int findIndexSize(int sets){
        return (int) (Math.log(sets) / Math.log(2));
    }

    public int findTag(int address, int tagSize, int lines){
        return (((1 << tagSize) - 1) & (address >> (lines - tagSize)));
    }

    public int findOffset(int address, int offsetSize){
        return (((1 << offsetSize) - 1) & address);
    }

    public int findIndex(int address, int indexSize, int offsetSize){
        return (((1 << indexSize) - 1) & (address >>  offsetSize));
    }

    public class Block{

        private boolean isDirty;
        private int tag;
        private int index;
        private int offset;
        private int timeStamp;

        public Block(boolean dirty, int tag, int index, int offset, int timeStamp){
            this.isDirty = dirty;
            this.tag = tag;
            this.index = index;
            this.offset = offset;
            this.timeStamp = timeStamp;
        }
    }
}