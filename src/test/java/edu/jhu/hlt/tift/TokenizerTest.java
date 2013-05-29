package edu.jhu.hlt.tift;

import static org.junit.Assert.*;


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
            logger.info("Got token: " + t.getTokenId() + " with text: " + t.getText());
            TextSpan ts = t.getTextSpan();
            logger.info("Text span of this token: " + ts.getStart() + " - " + ts.getEnd());
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
            logger.info("Got token: " + t.getTokenId() + " with text: " + t.getText());
            TextSpan ts = t.getTextSpan();
            logger.info("Text span of this token: " + ts.getStart() + " - " + ts.getEnd());
        }
        
        for (TokenTagging tt : ct.getPosTagsList()) {
            for (TaggedToken t : tt.getTaggedTokenList()) {
                logger.info("Got tagging: " + t.getTag() + " on token: " + t.getTokenId());
            }
        }
    }

    @Test
    public void testTokenize() {
        String text = "hello world test tokens";
        List<String> tokens = Tokenizer.BASIC.tokenize(text);
        assertEquals(4, tokens.size());
    }

}
