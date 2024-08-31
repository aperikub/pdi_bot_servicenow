package fr.aperikub.botSnow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Log {
    public static final int INFO_MESSAGE_TYPE = 3737;
    public static final int ERROR_MESSAGE_TYPE = 3437;
    public static final int WARNING_MESSAGE_TYPE = 3748;
    private Map<Integer,List<String>> messages;


    private Log(){
        messages = new HashMap<>();
        messages.put(INFO_MESSAGE_TYPE, Collections.synchronizedList(new ArrayList<>()));
        messages.put(ERROR_MESSAGE_TYPE,Collections.synchronizedList(new ArrayList<>()));
        messages.put(WARNING_MESSAGE_TYPE,Collections.synchronizedList(new ArrayList<>()));
    }

    private static class Holder{
        private final static  Log instance = new Log();
    }
    public void addInfoLog(String message){
        System.out.println(message);
        messages.get(INFO_MESSAGE_TYPE).add("INFO : "+
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)+" : " +message);
    }
    public void addErrorLog(String message){
        System.out.println(message);
        messages.get(ERROR_MESSAGE_TYPE).add("ERROR : "+
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)+" : " +message);
    }
    public void addWarningLog(String message){
        System.out.println(message);
        messages.get(WARNING_MESSAGE_TYPE).add("WARNING : "+
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)+" : " +message);
    }
    public List<String> getMessages(int type){
        return messages.get(type);
    }
    public void clearMessages(int type){
        messages.get(type).clear();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public static Log getInstance(){
        return Holder.instance;
    }


}
