package fr.aperikub.botSnow;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileHandler {
  public void saveDataInFile(){
    String filename = "log file"+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyyHHmm"))+".txt";
    File file = new File(filename);
    try {
      if (!file.exists()){

        file.createNewFile();

      }
      BufferedWriter writer = new BufferedWriter(new FileWriter(file,false));


      Log.getInstance().getMessages(Log.INFO_MESSAGE_TYPE).forEach((msg)->{
        try {
          writer.write(msg+"\n");

        }catch (IOException e) {
          Log.getInstance().addErrorLog("Error while saving info logs");

        }
      });
      Log.getInstance().clearMessages(Log.INFO_MESSAGE_TYPE);

      Log.getInstance().getMessages(Log.WARNING_MESSAGE_TYPE).forEach((msg)->{
        try {
          writer.write(msg+"\n");
        }catch (IOException e) {
          Log.getInstance().addErrorLog("Error while saving warn logs");

        }
      });

      Log.getInstance().clearMessages(Log.WARNING_MESSAGE_TYPE);

      Log.getInstance().getMessages(Log.ERROR_MESSAGE_TYPE).forEach((msg)->{
        try {
          writer.write(msg+"\n");
        }catch (IOException e) {
          Log.getInstance().addErrorLog("Error while saving error logs");

        }
      });

      Log.getInstance().clearMessages(Log.ERROR_MESSAGE_TYPE);

      writer.flush();
    } catch (IOException e) {
      Log.getInstance().addErrorLog("Error while saving logs : "+e.getMessage());
    }
  }
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    System.out.println("finalize called");
    saveDataInFile();

  }
}
