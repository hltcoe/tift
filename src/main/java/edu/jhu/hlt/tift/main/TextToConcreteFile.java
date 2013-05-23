/**
 * Copyright 2012-2013 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.tift.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.jhu.concrete.Concrete.Communication;
import edu.jhu.concrete.io.ProtocolBufferWriter;
import edu.jhu.hlt.tift.Tokenizer;

/**
 * @author max
 *
 */
public class TextToConcreteFile {
    
    private static final Logger logger = LoggerFactory.getLogger(TextToConcreteFile.class);
    
    /**
     * 
     */
    public TextToConcreteFile() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String... args) throws IOException {
        if (args.length != 4) {
            logger.info("Usage: TextToConcreteFile [tokenization_type] [path/to/text/file] [position_of_first_nonspace_char] " +
                        "[path/to/output/file]");
        }
        
        int startPos;
        try {
            startPos = Integer.parseInt(args[2]);
        } catch (NumberFormatException nfe) {
            throw new RuntimeException("3rd argument must be an integer; was: " + args[2], nfe);
        }
        
        String text = "";
        File input = new File(args[1]);
        Scanner sc = new Scanner(input);
        while (sc.hasNextLine())
            text += sc.nextLine();
        sc.close();

        Communication comm = Tokenizer.generateCommunicationWithSingleTokenization("chris_spanish_docs", 
                "chris_spanish_docs_" + input.getName(), Tokenizer.BASIC, text, startPos);
        
        ProtocolBufferWriter pbw = new ProtocolBufferWriter(new FileOutputStream(new File(args[3])));
        
        pbw.write(comm);
        pbw.close();
    }

}
