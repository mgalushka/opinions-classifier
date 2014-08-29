package com.maximgalushka.classifier.twitter.classify.python;

import java.io.*;
import java.util.Scanner;

/**
 * @author Maxim Galushka
 */
public class ClassifyTool {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) return;

        BufferedReader br = new BufferedReader(new FileReader(args[0]));
        PrintWriter out = new PrintWriter(
                new BufferedWriter(new FileWriter(args[1], true)));
        Scanner s = new Scanner(System.in);
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            System.out.print(">");
            Classification input = Classification.fromKey(s.next());
            out.printf("%s,%s\n", input.name().toLowerCase(), line);
            out.flush();
        }
        out.close();
    }
}
