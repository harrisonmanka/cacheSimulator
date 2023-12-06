import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Class representing a cache simulator.
 *
 * @author Harrison Manka & Spencer Russell
 * @date 12/06/23
 */
public class Cache {

    /** Integer representing number of hits. */
    private int hits;

    /** Integer representing number of misses. */
    private int miss;

    /** Set holding blocks in the "cache". */
    private Set<Block> set;

    /** Set holding blocks that have currently been hit. */
    private Set<Block> alreadyHit;

    /** Integer representing a timestamp to use for LRU replacement policy. */
    int count;

    /**
     * Constructor that creates a Cache object and initializes our values to their
     * respected starting values.
     */
    public Cache(){
        this.set = new HashSet<>();
        this.alreadyHit = new HashSet<>();
        this.count = 0;
    }

    /**
     * Read method that reads in a file and conducts the simulation of a cache.
     *
     * @param file - file to read.
     * @throws FileNotFoundException - if file is not found within the directory.
     */
    public void read(String file) throws FileNotFoundException {
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
                        else if(hasDirtyBit()){ //checks if previous hit proceeded a replacement policy
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

    /**
     * Helper method that iterates through our set of alreadyHit blocks and returns
     * a boolean if one of those blocks contains a dirty bit.
     *
     * @return - boolean representing if there is a dirty bit contained.
     */
    public boolean hasDirtyBit(){
        boolean result = false;
        for(Block b : alreadyHit){
            if(b.isDirty){
                result = true;
            }
        }
        return result;
    }

    /**
     * Helper method to check if there was a previous miss by seeing if there is a
     * previous block matching index and the tag.
     *
     * @param block - block to reference when checking for equality.
     * @return - boolean representing if there is block of the same index and tag.
     */
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

    /**
     * Helper method to check if a block has already been hit.
     *
     * @param block - block to reference when checking for equality.
     * @return - boolean representing if there is a block of same index, offset, and tag.
     */
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

    /**
     * Helper method to format and print out our results.
     *
     * @param access - String representing our access type. (read & write).
     * @param address - String representing our address in base16.
     * @param tag - Integer representing our tag in base2.
     * @param index - Integer representing our index in base2.
     * @param offset - Integer representing our offset in base2.
     * @param result - String representing if we hit or miss within the cache.
     * @param ref - Integer representing how many times we referenced memeory.
     */
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

    /**
     * Helper method to print the header of our results.
     */
    public void printHeader(){
        System.out.println("Access Address\t  Tag   Index Offset Result Memrefs");
        System.out.println("------ -------- ------- ----- ------ ------ -------");
    }

    /**
     * Helper method to print out our cache simulation summary statistics.
     */
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

    /**
     * Helper method to calculate the offset size.
     *
     * @param lines - Integer representing the line size in bytes.
     * @return - An integer representing the offset size.
     */
    public int findOffsetSize(int lines){
        return (int) (Math.log(lines) / Math.log(2));
    }

    /**
     * Helper method to calculate the index size.
     *
     * @param sets - Integer representing the set size.
     * @return - An integer representing the index size.
     */
    public int findIndexSize(int sets){
        return (int) (Math.log(sets) / Math.log(2));
    }

    /**
     * Helper method to calculate the tag in base2.
     *
     * @param address - Integer representing our address in base2.
     * @param tagSize - Integer representing our tag size.
     * @param lines - Integer representing our line size.
     * @return - An integer representing our tag in base2.
     */
    public int findTag(int address, int tagSize, int lines){
        return (((1 << tagSize) - 1) & (address >> (lines - tagSize)));
    }

    /**
     * Helper method to calculate the offset in base2.
     *
     * @param address - Integer representing our address in base2.
     * @param offsetSize - Integer representing our offset size.
     * @return - An integer representing our offset in base2.
     */
    public int findOffset(int address, int offsetSize){
        return (((1 << offsetSize) - 1) & address);
    }

    /**
     * Helper method to calculate the index in base2.
     *
     * @param address - Integer representing our address in base2.
     * @param indexSize - Integer representing our index size.
     * @param offsetSize - Integer representing our offset size.
     * @return - An integer representing our index in base2.
     */
    public int findIndex(int address, int indexSize, int offsetSize){
        return (((1 << indexSize) - 1) & (address >>  offsetSize));
    }

    /**
     * Private inner class representing a "Block" within a cache.
     */
    public class Block{

        /** Boolean representing if a certain block is dirty or not. */
        private boolean isDirty;

        /** Integer representing our tag. */
        private int tag;

        /** Integer representing our index. */
        private int index;

        /** Integer representing our offset. */
        private int offset;

        /** Integer representing our timeStamp of a block to use
         * for a replacement policy if needed. */
        private int timeStamp;

        /**
         * Constructor to create a Block object with the given parameters.
         *
         * @param dirty - Boolean representing if a block is dirty.
         * @param tag - Integer representing the tag.
         * @param index - Integer representing the index.
         * @param offset - Integer representing the offset.
         * @param timeStamp - Integer representing the timeStamp.
         */
        public Block(boolean dirty, int tag, int index, int offset, int timeStamp){
            this.isDirty = dirty;
            this.tag = tag;
            this.index = index;
            this.offset = offset;
            this.timeStamp = timeStamp;
        }
    }
}
