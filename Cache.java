import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;

public class Cache {

    private int numSets;
    private int setSize;
    private int lineSize;
    private int hits;
    private int misses;
    private int accesses;

    public Cache(){
        this.numSets = 0;
        this.setSize = 0;
        this.lineSize = 0;
        this.hits = 0;
        this.misses = 0;
        this.accesses = 0;
    }

    public void readFile(String file){
        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while(line = reader.readLine() != null){
                String[] inputArr = line.split(":");
            }
        }
        catch(FileNotFoundException e){
            System.out.println("File not found. Try again with a valid file.");
        }
    }
}