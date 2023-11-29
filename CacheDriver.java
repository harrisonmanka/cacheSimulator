import java.io.File;
import java.io.FileNotFoundException;

public class CacheDriver {

    public static void main(String[] args){
        if(args.length != 1){
            System.out.println("Usage: java CacheDriver <fileName>");
            System.exit(1);
        }
        else{
            Cache cache = new Cache();
            cache.readFile(args[0]);
        }
    }
}
