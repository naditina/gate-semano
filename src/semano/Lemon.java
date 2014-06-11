package semano;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import eu.monnetproject.lemon.LemonModel;
import eu.monnetproject.lemon.LemonSerializer;

public class Lemon {

  /**
   * @param args
   */
  public static void main(String[] args) {
    String filename = "JAPE/dbpedia_en.nt";

    LemonSerializer s = LemonSerializer.newInstance();

    try {
      LemonModel model = s.read(new InputStreamReader(new  FileInputStream(new File(filename))));
      System.out.println(model);
      
    }
    catch(FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
        
  }
}
