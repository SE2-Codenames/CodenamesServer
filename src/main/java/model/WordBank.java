package model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;

public class WordBank {
    private static final String XML_PATH = "wordBank.xml";
    private final List<String> allWords;

    public WordBank() {
        this.allWords = loadWordsFromXML();
    }

    private List<String> loadWordsFromXML() {
        try {
            File file = new File(XML_PATH);
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            NodeList wordNodes = doc.getElementsByTagName("word");
            List<String> words = new ArrayList<>();

            for (int i = 0; i < wordNodes.getLength(); i++) {
                words.add(wordNodes.item(i).getTextContent());
            }
            return words;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load wordBank XML", e);
        }
    }

    public List<String> getRandomWords(int count) {
        if (count > allWords.size()) {
            throw new IllegalArgumentException("Not enough words in bank");
        }
        List<String> shuffledWords = new ArrayList<>(allWords);
        Collections.shuffle(shuffledWords);
        return shuffledWords.subList(0, count);
    }
}

