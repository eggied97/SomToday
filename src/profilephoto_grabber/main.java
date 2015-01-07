package profilephoto_grabber;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;
import javax.swing.JSeparator;

import utils.SomToday_login;

public class main {
	
	static String JSESSIONID = null;
	static String username = null;
	static String password = null;
	
	static String afkorting = "csgliudger-elo";
	
	public static void main(String[] args) {
		Image image = null;
		
		int id = 135413651;
		int numberOfPhotosToDownload = 30000;
		int photosDownloaded = 0;
		final String baseURL = "https://"+ afkorting +".somtoday.nl/pasfoto/pasfoto_leerling.jpg?id=";
		
		/**
		 * First get the users login credentials through GUI / TUI
		 * Then get the logged in JSESSIONID (see utils/SomToday_login.java)
		 * Then start the while loop
		 * */
		
		username = "142681";
		password = "ED1929";
		
		JSESSIONID = SomToday_login.getLoggedInCookie(username, password);
		
		if(JSESSIONID == null){
			//terminate, no JSESSIONID (could be wron password)
			
			System.exit(0);
		}
		
		/**
		 * We first load the standart image which gets return when a non-existend SomToday-id gets requested.
		 * */
		BufferedImage img = null;
		try {
		    img = ImageIO.read(new File("images/std.jpg"));		    
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while(photosDownloaded <= numberOfPhotosToDownload){
	
			BufferedImage dwnldImg = null;
			
		
			try {
				URL url = new URL(baseURL + id);
	
				URLConnection con = url.openConnection();
				con.setDoOutput(true);
				con.setRequestProperty("Cookie", "JSESSIONID=" + JSESSIONID);		
				con.connect();
	
				
				InputStream is = con.getInputStream();
				
				//Get the photo
				dwnldImg = ImageIO.read(is);
				
				is.close();		
				
				//check if the pixel is the same color with the standart image, only should give false positif if the profile photo has the exact same
				//shade of gray as the standart photo :p
				if(dwnldImg.getRGB(54, 83) != img.getRGB(54, 83)){
					File outputfile = new File("D:/SomToday/images/"+id+".jpg");				
					ImageIO.write(dwnldImg, "jpg", outputfile);
				
					//increase download counter
					photosDownloaded++;
					
					System.out.println("Photo found, id : " + id);
				}
			
			
		
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			//increase id by one, so next photo gets loaded.
			id++;
			
		}
	}	
}
