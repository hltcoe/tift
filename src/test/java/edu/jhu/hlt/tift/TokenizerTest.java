package edu.jhu.hlt.tift;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.jhu.hlt.concrete.Concrete.TextSpan;
import edu.jhu.hlt.concrete.Concrete.Token;
import edu.jhu.hlt.concrete.Concrete.TokenTagging;
import edu.jhu.hlt.concrete.Concrete.TokenTagging.TaggedToken;
import edu.jhu.hlt.concrete.Concrete.Tokenization;

public class TokenizerTest {

    private static final Logger logger = LoggerFactory.getLogger(TokenizerTest.class);
    
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testTokenizeToConcreteWhitespace() {
        String text = "hello world test tokens foo";
        int expectedTokenCount = 5;
        Tokenization ct = Tokenizer.WHITESPACE.tokenizeToConcrete(text, 0);
        List<Token> tokenList = ct.getTokenList();
        assertEquals(expectedTokenCount, tokenList.size());
        for (Token t : tokenList) {
            logger.info("Got token: {} with text: {}", t.getTokenIndex(), t.getText());
            TextSpan ts = t.getTextSpan();
            logger.info("Text span of this token: {} - {}", ts.getStart(), ts.getEnd());
        }
    }
    
    @Test
    public void testTokenizeToConcreteTwitter() {
        String text = "hello world test foo :-)";
        int expectedTokenCount = 5;
        Tokenization ct = Tokenizer.TWITTER.tokenizeToConcrete(text, 0);
        List<Token> tokenList = ct.getTokenList();
        assertEquals(expectedTokenCount, tokenList.size());
        for (Token t : tokenList) {
            logger.info("Got token: {} with text: {}", t.getTokenIndex(), t.getText());
            TextSpan ts = t.getTextSpan();
            logger.info("Text span of this token: {} - {}", ts.getStart(), ts.getEnd());
        }
        
        for (TokenTagging tt : ct.getPosTagsList()) {
            for (TaggedToken t : tt.getTaggedTokenList()) {
                logger.info("Got tagging: {} on token: {}", t.getTag(), t.getTokenIndex());
            }
        }
    }
    
    /*
    @Test
    public void testTokenizePTB() throws IOException {
        String docText = readFile("src/test/resources/AFP_ENG_20080819.0409.LDC2009T13", StandardCharsets.UTF_8);

        Tokenization ct = Tokenizer.PTB.tokenizeToConcrete(docText, 0);
        List<Token> tokenList = ct.getTokenList();
        for (Token t : tokenList) {
            logger.info("Got token: {} with text: {}", t.getTokenId(), t.getText());
            TextSpan ts = t.getTextSpan();
            logger.info("Text span of this token: {} - {}", ts.getStart(), ts.getEnd());
        }
        
        for (TokenTagging tt : ct.getPosTagsList()) {
            for (TaggedToken t : tt.getTaggedTokenList()) {
                logger.info("Got tagging: {} on token: {}", t.getTag(), t.getTokenId());
            }
        }
    }
    */

    @Test
    public void testTokenize() {
        String text = "hello world test tokens";
        List<String> tokens = Tokenizer.BASIC.tokenize(text);
        assertEquals(4, tokens.size());
    }

    static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }
}
