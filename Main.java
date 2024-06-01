import org.jsoup.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;


public class Main {
    final static int MAX_ITEMS = 5;

    private static String toString(InputStream inputStream) throws IOException
    {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream , "UTF-8"));
        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();
        while ((inputLine = bufferedReader.readLine()) != null)
            stringBuilder.append(inputLine);
        return stringBuilder.toString();
    }
    // To extract html source from url
    public static String fetchPageSource(String urlString) throws Exception
    {
        URI uri = new URI(urlString);
        URL url = uri.toURL();
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML , like Gecko) Chrome/108.0.0.0 Safari/537.36");
        return toString(urlConnection.getInputStream());
    }
    // To extract rss url from url
    public static String extractRssUrl(String url) throws IOException
    {
        org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
        return doc.select("[type='application/rss+xml']").attr("abs:href");
    }

    // To finding a webpage title from html source
    public static String extractPageTitle(String html)
    {
        try
        {
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            return doc.select("title").first().text();
        }
        catch (Exception e)
        {
            return "Error: no title tag found in page source!";
        }
    }

    // To extract rss content from RSS url
    public static void retrieveRssContent(String rssUrl)
     {
        try {
            String rssXml = fetchPageSource(rssUrl);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            StringBuilder xmlStringBuilder = new StringBuilder();
            xmlStringBuilder.append(rssXml);
            ByteArrayInputStream input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
            org.w3c.dom.Document doc = documentBuilder.parse(input);
            NodeList itemNodes = doc.getElementsByTagName("item");
            for (int i = 0; i < MAX_ITEMS; ++i) {
                Node itemNode = itemNodes.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) itemNode;
                    System.out.println("Title: " + element.getElementsByTagName("title").item(0).getTextContent());
                    System.out.println("Link: " + element.getElementsByTagName("link").item(0).getTextContent());
                    System.out.println("Description: " + element.getElementsByTagName("description").item(0).getTextContent());
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Error in retrieving RSS content for " + rssUrl + ": " + e.getMessage());
        }
     }

     // To check if an url exists in file or not
     public static boolean isUrlExists(String url, File datafile){
         try {
             FileReader fileReader = new FileReader(datafile);
             BufferedReader reader = new BufferedReader(fileReader);
             String line;
             while((line = reader.readLine()) != null){
                 // if statement to check not reading a blank line.
                 if(line.contains(";")) {
                     String[] eachLineSplit = line.split(";");
                     if (eachLineSplit[1].equals(url))
                         return true;
                 }
             }
             reader.close();
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
         return false;
     }

     // To add a new url to file
     public static void addUrl (File dataFile){
        Scanner scanner = new Scanner(System.in);
         System.out.println("Please enter website url to add:");
         String url = scanner.next();
         try {
             FileWriter fileWriter = new FileWriter(dataFile, true);
             BufferedWriter writer = new BufferedWriter(fileWriter);

             if (!isUrlExists(url,dataFile)) {
                 /* try/catch statement to checking the internet connection.
                 if it's connected, the method writes the website to our file.
                  */
                 try {
                     String html = fetchPageSource(url);
                     writer.write(extractPageTitle(html) + ";" + url + ";" + extractRssUrl(url) + "\n");
                     System.out.println("Added " + url + " successfully");
                 }
                 catch (UnknownHostException e) {
                     System.out.println("No internet connection");
                 }
             }

             else {
                 System.out.println(url + " is already exists");
             }
             writer.close();
         }catch (IllegalArgumentException e) {
             // if user enter a string that is not a website url
             System.out.println("Enter an url");
         }catch (Exception e){
             throw new RuntimeException(e);
         }
     }

     // To remove an existing url from file
     public static void removeUrl(File dataFile){
        System.out.println("Please enter website url to remove:");
        Scanner scanner = new Scanner(System.in);
        String urlToRemove = scanner.next();
         try {
             if (isUrlExists(urlToRemove,dataFile)) {
                 // reading all urls except the one we want to remove and put them on an array list
                 BufferedReader reader = new BufferedReader(new FileReader(dataFile));
                 String line;
                 ArrayList<String> allUrls = new ArrayList<String>();
                 while ((line = reader.readLine()) != null) {
                     if(line.contains(";")) {
                         String[] eachLineSplit = line.split(";");
                         if (eachLineSplit[1].equals(urlToRemove))
                             continue;
                         allUrls.add(eachLineSplit[1]);
                     }
                 }
                 reader.close();
                 // writing the urls in the array list, to the file.
                 BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile));

                 /* try/catch statement to checking the internet connection.
                 if it's connected, the method writes the website to our file.
                  */
                 try {
                     for (String url : allUrls) {
                         String html = fetchPageSource(url);
                         writer.write(extractPageTitle(html) + ";" + url + ";" + extractRssUrl(url) + "\n");
                     }
                 }catch (UnknownHostException e){
                     System.out.println("No internet connection");
                 }

                 writer.close();
                 System.out.println("Remove " + urlToRemove + " successfully");
             }
             else {
                 System.out.println(urlToRemove + " is not exists");
             }

         }catch (Exception e) {
             throw new RuntimeException(e);
         }
     }


     /* To showing the list of our webpages.
      It returns a list contains title, url and rss url of each website. */
     public static ArrayList<String[]> showUpdatesMenu(File dataFile){
         System.out.println("Show updates for:");
         System.out.println("[0] All webpages");

         ArrayList<String[]> listOfSites = new ArrayList<>();
         try {
             BufferedReader reader = new BufferedReader(new FileReader(dataFile));
             String line;
             while ((line = reader.readLine()) != null){
                 if(line.contains(";")) {
                     String[] eachLineSplit = line.split(";");
                     listOfSites.add(eachLineSplit);
                     System.out.println("[" + listOfSites.size() + "] " + eachLineSplit[0]);
                 }
             }
             reader.close();
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
         System.out.println("Enter -1 to return");
         return listOfSites;

     }


     // Showing last 5 updates from one or all of our websites
     public static void showUpdates(ArrayList<String[]> listOfSites, int updateInput) throws IOException {
        if (updateInput == 0)
            for (int i = 0; i < listOfSites.size(); i++){
                System.out.println(listOfSites.get(i)[0]);
                retrieveRssContent(extractRssUrl(listOfSites.get(i)[1]));
            }
        else {
            System.out.println(listOfSites.get(updateInput - 1)[0]);
            retrieveRssContent(extractRssUrl(listOfSites.get(updateInput - 1)[1]));
        }
     }

    public static void main(String[] args) throws IOException {

        File dataFile = new File("data.txt");
        int menuInput = 0;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Rss Reader!");

        // this while will execute until user Enter 4
        while(menuInput != 4){
            System.out.println("Type a valid number for your desire action:");
            System.out.println("[1] Show Updates");
            System.out.println("[2] Add Url");
            System.out.println("[3] Remove Url");
            System.out.println("[4] Exit");
            try {
                menuInput = scanner.nextInt();
            }catch (InputMismatchException e){
                System.out.println("Please enter a number not anything else");
                scanner.next();
                continue;
            }

            // Error for invalid input for menu
            if(menuInput > 4 || menuInput < 1 ){
                System.out.println("Please enter a valid number from 1 to 4");
            }

            else{
                // switch/case for our main menu.
                switch (menuInput){
                    case 1:
                        ArrayList<String[]> listOfSites = showUpdatesMenu(dataFile);
                        int updatesInput = scanner.nextInt();
                        if (updatesInput < -1 || updatesInput > listOfSites.size()){
                            System.out.println("You should enter a number from -1 to " + listOfSites.size());
                            break;
                        }
                        else if (updatesInput == -1)
                            break;
                        else
                            /* try/catch statement to checking the internet connection.
                            if it's connected, show updates method will be called.
                            */
                            try {
                                showUpdates(listOfSites, updatesInput);
                            }catch (UnknownHostException e){
                                System.out.println("No connection");
                            }

                        break;
                    case 2:
                        addUrl(dataFile);
                        break;
                    case 3:
                        removeUrl(dataFile);
                        break;
                }
            }
        }
        System.out.println("Good Luck!");
    }

}