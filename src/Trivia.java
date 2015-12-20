import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import com.pokejava.*;

public class Trivia {
	
	private static String run = "";
	//Create firefox driver to drive the browser
	static FirefoxProfile profile = new FirefoxProfile(new File("C:/Users/Sevverre/AppData/Roaming/Mozilla/Firefox/Profiles/q9vftdqm.Selenium"));                  
	static WebDriver driver = new FirefoxDriver(profile);

	//Lists used
	static ArrayList<WebElement> posts = new ArrayList<WebElement>();
	static LinkedList<String> names = new LinkedList<String>();
	static LinkedList<String> messages = new LinkedList<String>();
	
	//Storage
	static ArrayList<WebElement> chat = new ArrayList<WebElement>();
	
	//Delay time in milliseconds
	static double max = 6000;
	static double min = 2500;
	
	//AFK Time
	static double afkMax = 1800000;
	static double afkMin = 300000;
	
	
	public static void main(String[] args) throws InterruptedException{		
		//Open Aus twitch page
		driver.get("http://www.twitch.tv/auslove");

		// Try to get textbox and ensure page loads
		 while (true) {
		   try {
			driver.findElement(By.className("chat_text_input"));
			break;
		// If there is no text sleep one second and try again
		   } catch (org.openqa.selenium.NoSuchElementException e) {
		     System.out.println("Waiting...");
		     Thread.sleep(1000);
		   }
		 }
		 
		//Creates textbox webElement
		WebElement textbox = driver.findElement(By.className("chat_text_input"));
		
		//Creates Scanner and confirms if you want to run program
		Scanner s = new Scanner(System.in);
		System.out.println("Y to start, N to quit."); //Asks user to begin scanning page for comments
		String confirmation = s.nextLine();
		
		
		if(confirmation.equals("y")){//Confirms execution
			boolean execute = run.equals(""); //Monitors whether user wants program to run
			
			while(execute){ //Needs to always run unless execute is changed
				
				//Initiates Elements
				try{
					getChat();
					separateChat();
					} catch(ArrayIndexOutOfBoundsException e){
					System.out.println("Waiting...");
				    Thread.sleep(1000);
				}
				
				//Searches to identify trivia post
				String TriviaQ = searchTriviaQ(names, messages); //Finds the last Trivia Post
				String answer = identifyQuestion(TriviaQ);
				
				//if there is a question
				if(!answer.equals("")){					
					//Resets and submits answers
					textbox.clear();
					textbox.sendKeys(answer);
					int delay = (int)(Math.random() * (max - min) + min);
					System.out.println(delay/1000 + " sec"); //display in sec
					Thread.sleep(delay);//Delays answer
					textbox.sendKeys(Keys.ENTER);//Submits answer
					
					Thread.sleep(5000);//Pauses to allow Triviabot to post
					getChat();//Retrieves newest posts
					separateChat();
					TriviaQ = searchTriviaQ(names, messages);
					
					//TODO Needs testing
					
					if(identifyQuestion(TriviaQ).equals("winner")){
						System.out.println("**************************************");
						System.out.println(TriviaQ + "\n" + answer);
						System.out.println("**************************************");
						int afkDelay = (int)(Math.random() * (afkMax - afkMin) + afkMin);
						System.out.println(afkDelay/60000 + " min"); //display in min
						Thread.sleep(afkDelay);
					}
				}
				
				//Resets last trivia post
				TriviaQ = "";
				
				//Clears all lists to avoid DOM element timeouts
				posts.clear();
				
				//System.out.print("*");
			}
			s.close();
		}
	}
	
	public static void getChat() throws InterruptedException{
		ArrayList<WebElement> temp = (ArrayList<WebElement>) driver.findElements(By.className("message-line"));
		//Clears all lists to avoid DOM element timeouts
		posts.clear();
		if(chat.size()>150)
			chat.remove(0);
				
		try{
			for(int i=0; i<temp.size(); i++){
				if(!chat.contains(temp.get(i))){
					posts.add(temp.get(i));
					chat.add(temp.get(i));
				}
			}
		}catch(Exception e){
			System.out.println("Waiting...");
		    Thread.sleep(1000);
		}
	}
	
	public static void separateChat() throws InterruptedException{
		//Clears all lists to avoid DOM element timeouts
		names.clear();
		messages.clear();
		
		for(int i = 0; i<posts.size(); i++){ //Writes components of posts to separate lists
			try{
			System.out.print(posts.get(i).findElement(By.className("from")).getText());
			names.add(posts.get(i).findElement(By.className("from")).getText());
			System.out.println(": " + posts.get(i).findElement(By.className("message")).getText());
			messages.add(posts.get(i).findElement(By.className("message")).getText());
			}catch(Exception e){
				System.out.println("Waiting...");
			    Thread.sleep(1000);
			}
		}
	}
	
	public static String searchTriviaQ(LinkedList<String> names, LinkedList<String> messages){
		int lastPost = names.lastIndexOf("PokeTrivia");
		if(lastPost >= 0){
			return messages.get(lastPost);
		}
		return "";
	}
	
	public static String identifyQuestion(String msg){
		if(msg.equals(""))
			return "";
		int firstSpace = msg.indexOf(" ");
		String type = msg.substring(0, firstSpace).toLowerCase();
//		if(type.equals("category"))
//			return findCategory(msg);
		if(type.equals("pokedex"))
			return findID(msg);
		if(type.equals("poke"))
			return findType(msg);
		if(msg.contains("Winner") && msg.contains("sevverre"))
			return "winner";
		return "";
	}
	
	private static String findType(String msg) {
		int index = msg.indexOf('#');
		int num = Integer.parseInt(msg.substring(index+1, index+4));
		Pokemon poke = new Pokemon(num);
		ArrayList<Type> types = poke.getTypes();
		String submit = types.get(0).getName().substring(6);
		if(types.size()>1)
			submit = types.get(0).getName().substring(6) + " " + types.get(1).getName().substring(6);
		return submit;
	}

	private static String findID(String msg) {
		int index = msg.lastIndexOf(' ');
		String name = msg.substring(index+1, msg.length()-1).toLowerCase();
		String answer = "";
		if(!name.equals("pokedex")){
			Pokemon poke = new Pokemon(name);
			answer = "#" + poke.getID();
		}else {
			index = msg.indexOf('#');
			int num = Integer.parseInt(msg.substring(index+1, index+4));
			Pokemon poke = new Pokemon(num);
			answer = poke.getName();
		}
		return answer;
	}

//	private static String findCategory(String msg) {
//		int index = msg.indexOf("Pokemon");
//		String species = msg.substring(26, index-1).toLowerCase();
//		for(int i=0; i<=720; i++){
//			Pokemon poke = new Pokemon(i);
//			if(poke.getSpecies().toLowerCase().contains(species))
//				return poke.getName();
//		}
//		return "";
//	}

}
