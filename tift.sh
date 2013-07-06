#!/bin/sh

mvn -q exec:java -Dexec.mainClass="edu.jhu.hlt.tift.Tokenizer" -Dexec.args="$1 $2"
