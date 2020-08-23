package org.yah.cli;

import java.util.Iterator;

public interface CommandLine extends Iterator<String> {

    String peek();

    String flush();

}
