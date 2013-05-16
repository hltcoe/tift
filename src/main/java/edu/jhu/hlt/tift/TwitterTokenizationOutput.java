/**
 * 
 */
package edu.jhu.hlt.tift;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Wrapper around the data structure returned by the twitter tokenizer.
 * 
 * @author max
 *
 */
public class TwitterTokenizationOutput {

    private final List<String> tokens;
    private final List<String> tokenTags;
    private final List<Integer> offsets;
    
    /**
     * 
     */
    public TwitterTokenizationOutput(List<String> tokens, List<String> tokenTags, List<Integer> offsets) {
        this.tokens = tokens;
        this.tokenTags = tokenTags;
        this.offsets = offsets;
    }
    
    public TwitterTokenizationOutput(String[][] twitterTokenizerOutput) {
        this.tokens = Arrays.asList(twitterTokenizerOutput[0]);
        this.tokenTags = Arrays.asList(twitterTokenizerOutput[1]);
        String[] offsetStrings = twitterTokenizerOutput[2];
        this.offsets = new ArrayList<>(offsetStrings.length);
        for (String offset : offsetStrings)
            this.offsets.add(Integer.parseInt(offset));
    }

    /**
     * @return the tokens
     */
    public List<String> getTokens() {
        return tokens;
    }

    /**
     * @return the tokenTags
     */
    public List<String> getTokenTags() {
        return tokenTags;
    }

    /**
     * @return the offsets
     */
    public List<Integer> getOffsets() {
        return offsets;
    }
    
    
}
