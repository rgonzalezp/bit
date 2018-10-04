package utils;

import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Created by marvin on 5/5/16.
 */
public class Logger {
	private String name;

	public Logger(String name) {
		this.name = name;
	}

	public void log(String s) {
	    
		PrintWriter output = null;
	    
		try {
			output = new PrintWriter(new FileWriter("./data/log.txt",true));
			output.println("Client " + name + ": " + s);
			System.out.println("Client " + name + ": " + s);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		output.close();
	}
	
	public void logData(String s){
		PrintWriter output = null;
		    
			try {
				output = new PrintWriter(new FileWriter("./data/logData.txt",true));
				output.println(s);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			output.close();
		}
}