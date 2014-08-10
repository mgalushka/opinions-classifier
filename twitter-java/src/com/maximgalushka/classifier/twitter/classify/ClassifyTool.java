package com.maximgalushka.classifier.twitter.classify;

import java.io.*;
import java.util.Scanner;

/**
 * @author Maxim Galushka
 */
public class ClassifyTool {

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("D:\\projects\\classifier\\200.txt"));
        PrintWriter out = new PrintWriter(
                new BufferedWriter(new FileWriter("D:\\projects\\classifier\\200-out.txt", true)));
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
