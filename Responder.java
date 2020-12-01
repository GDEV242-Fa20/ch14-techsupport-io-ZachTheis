import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;

/**
 * The responder class represents a response generator object.
 * It is used to generate an automatic response, based on specified input.
 * Input is presented to the responder as a set of words, and based on those
 * words the responder will generate a String that represents the response.
 *
 * Internally, the reponder uses a HashMap to associate words with response
 * strings and a list of default responses. If any of the input words is found
 * in the HashMap, the corresponding response is returned. If none of the input
 * words is recognized, one of the default responses is randomly chosen.
 * 
 * @author Zach Theis
 * @version 2020.11.30
 */
public class Responder
{
    // Used to map key words to responses.
    private HashMap<String, String> responseMap;
    // Default responses to use if we don't recognise a word.
    private ArrayList<String> defaultResponses;
    // The name of the file containing the default responses.
    private static final String FILE_OF_DEFAULT_RESPONSES = "default.txt";
    private Random randomGenerator;

    /**
     * Construct a Responder
     */
    public Responder()
    {
        responseMap = new HashMap<>();
        defaultResponses = new ArrayList<>();
        fillResponseMap();
        fillDefaultResponses();
        randomGenerator = new Random();
    }
    
    /**
     * Catches a key with no paired response value.
     */
    private class NoMatchingResponseException extends Exception
    {
        private String key;
        
        /**
         * Store the detail in error.
         * @param key The key with no matching response.
         */
        public NoMatchingResponseException(String key)
        {
            this.key = key;
        }
        
        /**
         * @return The key with no paired response.
         */
        public String getKey()
        {
            return key;
        }
        
        /**
         * @return A diagnostic statement containing the unpaired key.
         */
        public String toString()
        {
            return "No response has been paired with " + key;
        }
    }
    
    /**
     * Catches a null key.
     */
    private class NullKeyException extends Exception
    {
        private String response;
        
        /**
         * Store the details in error.
         * @param response The response paired to a null key.
         */
        public NullKeyException(String response)
        {
            this.response = response;
        }
        
        /**
         * @return The unpaired response.
         */
        public String getResponse()
        {
            return response;
        }
        
        /**
         * @return A diagnostic statement containing the unpaired response.
         */
        public String toString()
        {
            return "There was no key paired with the response: " + response;
        }
    }
        

    /**
     * Generate a response from a given set of input words.
     * 
     * @param words  A set of words entered by the user
     * @return       A string that should be displayed as the response
     */
    public String generateResponse(HashSet<String> words)
    {
        Iterator<String> it = words.iterator();
        while(it.hasNext()) {
            String word = it.next();
            String response = responseMap.get(word);
            if(response != null) {
                return response;
            }
        }
        // If we get here, none of the words from the input line was recognized.
        // In this case we pick one of our default responses (what we say when
        // we cannot think of anything else to say...)
        return pickDefaultResponse();
    }

    /**
     * Enter all the known keywords and their associated responses
     * into our response map.
     * 
     * This satisfies Lab 12 #2.
     * @throws NullKeyException If a null key is entered.
     * @throws NoMatchingResponseException If a key is paired with a null or empty response.
     */
    private void fillResponseMap()
    {
        Path path = Paths.get("Response Map.txt");
        Charset charset = Charset.forName("US-ASCII");
        try(BufferedReader reader = Files.newBufferedReader(path, charset))
        {
            ArrayList<String> keys = new ArrayList<>();
            String response = "";
            String line = reader.readLine();
            
            while(line != null)
            {
                if(keys.size() == 0)
                {
                    String[]inputLine = line.split(", ");
                    for(String key : inputLine)
                    {
                        keys.add(key);
                    }
                }
                else if(line.trim().equals(""))
                {
                    for(String key : keys)
                    {
                        if(key == null)
                        {
                            throw new NullKeyException(key);
                        }
                        else if(response == null || response.equals(""))
                        {
                            throw new NoMatchingResponseException(response);
                        }
                        responseMap.put(key, response);
                    }
                    keys.clear();
                    response = "";
                }
                else
                {
                    response += line.trim().replace("\n", " ");
                }
                line = reader.readLine();
            }
        }
        catch(FileNotFoundException e)
        {
            System.out.println("The response file was not found");
        }
        catch(NoMatchingResponseException e)
        {
            System.out.println(e);
        }
        catch(NullKeyException e)
        {
            System.out.println(e);
        }
        catch(IOException e)
        {
            System.out.println("There was a problem opening the response file.");
        }
    }

    /**
     * Build up a list of default responses from which we can pick
     * if we don't know what else to say.
     * 
     * This satisfies Lab 12, #1.
     */
    private void fillDefaultResponses()
    {
        Charset charset = Charset.forName("US-ASCII");
        Path path = Paths.get(FILE_OF_DEFAULT_RESPONSES);
        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
            String line = reader.readLine();
            String response = "";
            while(line != null) 
            {
                if(line.trim().equals(""))
                {
                    defaultResponses.add(response);
                    response = "";
                }
                else
                {
                    response += line.replace("\n", " ");
                }
                line = reader.readLine();
            }
        }
        catch(FileNotFoundException e) {
            System.err.println("Unable to open " + FILE_OF_DEFAULT_RESPONSES);
        }
        catch(IOException e) {
            System.err.println("A problem was encountered reading " +
                               FILE_OF_DEFAULT_RESPONSES);
        }
        // Make sure we have at least one response.
        if(defaultResponses.size() == 0) {
            defaultResponses.add("Could you elaborate on that?");
        }
    }

    /**
     * Randomly select and return one of the default responses.
     * @return     A random default response
     */
    private String pickDefaultResponse()
    {
        // Pick a random number for the index in the default response list.
        // The number will be between 0 (inclusive) and the size of the list (exclusive).
        int index = randomGenerator.nextInt(defaultResponses.size());
        return defaultResponses.get(index);
    }
}
