package edu.jhu.hlt.tift;

import static org.junit.Assert.*;


import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TokenizerTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testTokenizeToConcrete() {
        //fail("Not yet implemented");
    }

    @Test
    public void testTokenize() {
        String text = "hello world test tokens";
        List<String> tokens = Tokenizer.BASIC.tokenize(text);
        assertEquals(4, tokens.size());
    }

}
