import java.io.FileNotFoundException;

public class CacheDriver {

    public static void main(String[] args) throws FileNotFoundException {
        if(args.length != 1){
            System.out.println("Usage: java CacheDriver <fileName>");
            System.exit(1);
        }
        else{
            Cache cache = new Cache(args[0]);
            //cache.read(args[0]);
        }
    }
}
