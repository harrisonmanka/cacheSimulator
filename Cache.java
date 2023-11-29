import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class Cache {

    private int numSets;
    private int setSize;
    private int lineSize;
    private int hits;
    private int misses;
    private int accesses;
    private ArrayList<String> results;

    public Cache(){
        this.numSets = 0;
        this.setSize = 0;
        this.lineSize = 0;
        this.hits = 0;
        this.misses = 0;
        this.accesses = 0;
        this.results = new ArrayList<>();
    }

    public void readFile(String file){
        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            int index = 0;
            while((line = reader.readLine()) != null){
                String[] inputArr = line.split(":");
                if(index == 0){
                    this.numSets = Integer.parseInt(inputArr[inputArr.length-1]);
                }
                else if(index == 1){
                    this.setSize = Integer.parseInt(inputArr[inputArr.length-1]);
                }
                else if(index == 2){
                    this.lineSize = Integer.parseInt(inputArr[inputArr.length-1]);
                }
                else{
                    String instruction = inputArr[0];
                    int size = Integer.parseInt(inputArr[1]);
                    int address = Integer.parseInt(inputArr[2]);
                    simulate(instruction, size, address);
                }
                index++;
            }
        }
        catch(IOException e){
            System.out.println("File not found. Try again with a valid file.");
        }
    }

    public void simulate(String instruction, int size, int address){
        
    }

    private class Entry {
        String access;
        int address;
        int tag;
        int index;
        int offset;
        String result;
        int memrefs;
        
        public Entry(String access, int address, int tag, int index, int offset, String result, int memrefs){
            this.access = access;
            this.address = address;
            this.tag = tag;
            this.index = index;
            this.offset = offset;
            this.result = result;
            this.memrefs = memrefs;
        }

        public String toString(){
            String result ="";
            return result;
        }
        
    }
}