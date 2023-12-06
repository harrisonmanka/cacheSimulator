import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Cache {

    private int hits;
    private int miss;
    private Set<Block> set;
    private Set<Block> alreadyHit;
    int count;

    public Cache(String file) throws FileNotFoundException {
        this.set = new HashSet<>();
        this.alreadyHit = new HashSet<>();
        this.count = 0;
        read(file);
    }

    private void read(String file) throws FileNotFoundException {
        File fileObj = new File(file);
        Scanner sc = new Scanner(fileObj);

        int sets = sc.useDelimiter("[^\\d]+").nextInt();
        int setSize = sc.useDelimiter("[^\\d]+").nextInt();
        int lines = sc.useDelimiter("[^\\d]+").nextInt();

        errorHandle(sets,setSize,lines);
        sc.nextLine();

        printHeader();

        while (sc.hasNextLine()) {
            String[] line = sc.nextLine().split(":");
            String cmd = line[0];
            int size = Integer.parseInt(line[1]);
            int address = Integer.parseInt(line[2], 16);

            int offsetSize = findOffsetSize(lines);
            int indexSize = findIndexSize(sets);
            int tagSize = lines - offsetSize - indexSize;

            int tag = findTag(address, tagSize, lines);
            int offset = findOffset(address, offsetSize);
            int index = findIndex(address, indexSize, offsetSize);

            if(address % size == 0){
                Block block = new Block(false, tag, index, offset, count);
                count++;
                boolean sizeCheck = alreadyHit.size() == setSize;
                switch(cmd){
                    case "read" -> {
                        boolean check = hasBlock(block);
                        boolean alrHit = checkForHit(block);
                        if(check && !alrHit){ //if in set --> hit
                            output(cmd, line[2], tag, index, offset, "hit", 0);
                            alreadyHit.add(block);
                            hits++;
                        }
                        else if(hasDirtyBit()){
                            block.isDirty = true;
                            output(cmd, line[2], tag, index, offset, "miss", 2);
                            miss++;
                        }
                        else{ //if not --> miss and memRef+1
                            output(cmd, line[2], tag, index, offset, "miss", 1);
                            miss++;
                        }
                        set.add(block);
                    }
                    case "write" -> {
                        boolean check = hasBlock(block);
                        if(check && !sizeCheck){ //if in set --> hit
                            output(cmd, line[2], tag, index, offset, "hit", 0);
                            alreadyHit.add(block);
                            hits++;
                        }
                        else if(sizeCheck){ //replace w/ LRU aka memRef == 2, because cache is full
                            block.isDirty = true;
                            output(cmd, line[2], tag, index, offset, "miss", 2);
                            miss++;
                            set.clear();
                            set.add(block);
                            alreadyHit.clear();
                            alreadyHit.add(block);
                        }
                        else{ //if not --> miss and memRef+1
                            output(cmd, line[2], tag, index, offset, "miss", 1);
                            miss++;
                        }
                    }
                }
            }
            else{
                System.out.println("Misalignment in the data.");
                System.exit(1);
            }
        }

        printSummary();
    }
    /**
     * Method for the error handle conditions in the handout
     * @param sets the number of sets
     * @param setSize the size of each set
     * @param lineSize the number of lines each set has
     */
    public void errorHandle(int sets, int setSize, int lineSize){
        if(sets > 8000){
            System.out.println("The number of sets has exceed 8k");
            System.exit(1);
        }
        if (setSize > 8) {
            System.out.println("The size of the set has exceed 8 elements");
            System.exit(1);
        }
        if (lineSize < 4){
            System.out.println("The size of the line is not large enough");
            System.exit(1);
        }
        if ((sets % 2) != 0) {
            System.out.println("The number of sets is not a power of 2");
            System.exit(1);
        }
        if ((lineSize % 2) != 0){
            System.out.println("The line size is not a power of 2");
            System.exit(1);
        }
    }

    public boolean hasDirtyBit(){
        boolean result = false;
        for(Block b : alreadyHit){
            if(b.isDirty){
                result = true;
            }
        }
        return result;
    }

    public boolean hasBlock(Block block){
        boolean result = false;
        for(Block b : set){
            if(b.index == block.index && b.tag == block.tag){
                result = true;
                break;
            }
        }
        return result;
    }

    public boolean checkForHit(Block block){
        boolean result = false;
        for(Block b : alreadyHit){
            if(b.index == block.index && b.tag == block.tag && b.offset == block.offset){
                result = true;
                break;
            }
        }
        return result;
    }

    public void output(String access, String address, int tag, int index, int offset, String result, int ref){
        access = String.format("%6s", access);
        address = String.format("%8s", address);
        String tagS = String.format("%7d", tag);
        String indexS = String.format("%5d", index);
        String offsetS = String.format("%6d", offset);
        result = String.format("%6s", result);
        String refS = String.format("%7d", ref);
        System.out.println(access + " " + address + " " + tagS + " " + indexS + " " + offsetS
                            + " " + result + " " + refS);
    }

    public void printHeader(){
        System.out.println("Access Address\t  Tag   Index Offset Result Memrefs");
        System.out.println("------ -------- ------- ----- ------ ------ -------");
    }

    public void printSummary(){
        double total = hits + miss;
        System.out.println();
        System.out.println("Simulation Summary Statistics");
        System.out.println("-----------------------------"); //19
        System.out.println("Total hits       : " + hits);
        System.out.println("Total misses     : " + miss);
        System.out.println("Total accesses   : " + (hits + miss));
        System.out.println("Hit ratio        : " + (hits / total));
        System.out.println("Miss ratio       : " + (miss / total));
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
