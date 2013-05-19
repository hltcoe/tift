/**
 * 
 */
package edu.jhu.hlt.tift;

/**
 * 2-tuple that contains a token and a tag.
 * 
 * @author max
 *
 */
public class TokenTagTuple {

    private final String token;
    private final String tag;
    
    /**
     * 
     */
    public TokenTagTuple(String token, String tag) {
        this.token = token;
        this.tag = tag;
    }

    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * @return the tag
     */
    public String getTag() {
        return tag;
    }

}