package model.Card;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WordBankTest {

    private WordBank wordBank;
    private List<String> mockWords;

    @BeforeEach
    void setUp() {
        // Setup mock word list
        mockWords = List.of("apple", "banana", "cherry", "date", "elderberry");
        wordBank = new WordBank();

        // Inject test words instead of loading from XML
        setPrivateField(wordBank, "allWords", new ArrayList<>(mockWords));
    }

    @Test
    void testGetRandomWords_NormalCase() {
        List<String> result = wordBank.getRandomWords(3);

        assertEquals(3, result.size());
        assertTrue(mockWords.containsAll(result));
        // Verify no duplicates
        assertEquals(3, new HashSet<>(result).size());
    }

    @Test
    void testGetRandomWords_AllWords() {
        List<String> result = wordBank.getRandomWords(mockWords.size());

        assertEquals(mockWords.size(), result.size());
        assertTrue(mockWords.containsAll(result));
        // Verify shuffled
        assertNotEquals(mockWords, result);
    }

    @Test
    void testGetRandomWords_TooManyWords() {
        int invalidCount = mockWords.size() + 1;  // Calculate problematic value first
        assertThrows(IllegalArgumentException.class,
                () -> wordBank.getRandomWords(invalidCount));  // Single throwing call
    }

    @Test
    void testGetRandomWords_ZeroWords() {
        List<String> result = wordBank.getRandomWords(0);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetRandomWords_Shuffled() {
        // Run multiple times to verify randomness
        Set<List<String>> results = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            results.add(wordBank.getRandomWords(mockWords.size()));
        }

        // Very likely to get different orders
        assertTrue(results.size() > 1);
    }

    //allows tests to modify private fields of an object for tests
    private void setPrivateField(Object obj, String fieldName, Object value) {
        try {
            // 1. Get the Field object for the specified field name
            Field field = obj.getClass().getDeclaredField(fieldName);

            // 2. Override Java's access control checks
            field.setAccessible(true);

            // 3. Actually set the field's value
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testConstructorLoadsWords() {
        WordBank bank = new WordBank();
        assertFalse(bank.getRandomWords(1).isEmpty());
    }

    @Test
    void testLoadWordsFromXML_Failure() {
        //Rename XML file temporarily to make it "unavailable"
        File originalFile = new File("wordBank.xml");
        File tempFile = new File("wordBank.xml.bak");

        boolean renamed = originalFile.renameTo(tempFile);
        assertTrue(renamed, "Failed to rename the XML file for testing.");

        try {
            //fail to load the XML
            Exception exception = assertThrows(RuntimeException.class, WordBank::new);
            assertTrue(exception.getMessage().contains("Failed to load wordBank XML"));
        } finally {
            //Restore original XML
            boolean restored = tempFile.renameTo(originalFile);
            assertTrue(restored, "Failed to restore the original XML file after testing.");
        }
    }
}
