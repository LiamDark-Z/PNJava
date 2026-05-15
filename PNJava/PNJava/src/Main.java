//Credit to https://stackoverflow.com/questions/5526690/convert-a-raw-negative-rgb-int-value-back-to-a-3-number-rgb-value
//https://stackoverflow.com/questions/601274/how-do-i-properly-load-a-bufferedimage-in-java
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class Main {
    File inputIMG = null;
    Path directory = null;
    int width, height = 0;
    URL url = null;
    BufferedImage image = null;
    ArrayList<Pixel> pixels = new ArrayList<>();
    ArrayList<String> output = new ArrayList<>();

    public void main(String[] args){        
        //Checks if arguements are passed (found in launch.json)
        if(args.length==0){
            System.err.println("No command line arguement provided");
            System.exit(1);
        }else{
            //File iteration, Credit: https://stackoverflow.com/questions/3154488/how-do-i-iterate-through-the-files-in-a-directory-and-its-sub-directories-in-ja
            directory = Paths.get(args[0]);
            try{
                Files.walk(directory).forEach(path -> addFile(path.toFile()));
            }catch(IOException e){
                System.err.println("Directory could not be read");
            }

        }
    }

    public void addFile(File file){
        //Checks that a file is of .vm filetype

        // Walks through input folder and parses all files that end with .png
        String filename = file.getName();
        if(filename.endsWith(".Png")||filename.endsWith(".png")){
            inputIMG = file;
            try{
                image = ImageIO.read(inputIMG);
            }catch(IOException e){
                System.err.println("Cannot read IMG type");
                System.exit(1);
            }
            addToToken();
            printFile();
        }
    }

    public void addToToken(){
        //resets token and tokenid arraylists for new file
        pixels.clear();
        width = image.getWidth();
        height = image.getHeight();
        for(int h = 0;h<height;h++){
            for(int w = 0;w<width-1;w++){
                if(!((((image.getRGB(w,h)>>16)&0xFF)==255)&(((image.getRGB(w,h)>>8)&0xFF)==255)&(((image.getRGB(w,h)&0xFF))==255))){
                    pixels.add(new Pixel(((image.getRGB(w,h)>>16)&0xFF),((image.getRGB(w,h)>>8)&0xFF),((image.getRGB(w,h)&0xFF))));
                }
            }
        }
    }

    public void printFile(){
        //Creates a new PNG parses using the current list of pixels
        PNGParser parser = new PNGParser(pixels);
        parser.parseList();
        //Attempts to create and assign the new file
        File outputASM = null;
        try{
            outputASM = new File(directory.toString(),inputIMG.getName().replace(".png","").replace(".Png","")+".vm");
            outputASM.delete();
            if(outputASM.createNewFile()){
                System.out.println("Output file located at: "+outputASM.getAbsoluteFile());
            }
        }
        catch(IOException e){
             System.err.println("could not create file");
        }

        //Attempts to create the fileWriter for FileIO
        FileWriter Writer = null;
        try {
            Writer = new FileWriter(outputASM.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Could not write to file");
        }
        for(String line: parser.getList()){
            try{
                Writer.write(""+line);
                Writer.write("\n");
            }catch(IOException e){
                System.err.println("Could not write line");
            }
        }

        //Closes Writer class (why does it need so many try/catch's)
        try{
            Writer.close();
        }catch(IOException e){
            System.err.println("Could not write line");
        }
            
    }

}

//https://docs.oracle.com/en/database/oracle/oracle-database/26/jaxml/index.html?oracle/xml/parser/v2/XMLTokenizer.html
//https://docs.oracle.com/javase/8/docs/api/java/util/StringTokenizer.html 
//https://www.geeksforgeeks.org/java/java-dom-parser-1/
//https://docs.oracle.com/javase/8/docs/api/java/io/StreamTokenizer.html
//https://www.geeksforgeeks.org/java/java-io-streamtokenizer-class-set-1/
//https://www.baeldung.com/java-streamtokenizer
//https://www.baeldung.com/xstream-serialize-object-to-xml