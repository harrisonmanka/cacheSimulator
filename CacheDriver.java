import java.io.FileNotFoundException;
/**
* @author Harrison Manka
* @author Spencer Russell
* @version 12/6/2023
* This is the cache Driver class and is used to take in standerd input
*/
public class CacheDriver {
    /**
    * This is the main method of the cache driver
    * @param args is the argument of the file we are reading
    */
    public static void main(String[] args) throws FileNotFoundException {
        if(args.length != 1){
            System.out.println("Usage: java CacheDriver <fileName>");
            System.exit(1);
        }
        else{
            Cache cache = new Cache(args[0]);
            cache.read(args[0]);
        }
    }
}
