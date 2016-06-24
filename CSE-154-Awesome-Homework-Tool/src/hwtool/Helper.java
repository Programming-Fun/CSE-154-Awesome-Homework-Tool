/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hwtool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.System.in;
import static java.lang.System.out;
import java.util.Scanner;

class Helper{
    protected String username = "", pass = "", defaultPath = "";
    protected Scanner scan;
    InputStream input;
    public Helper(){
        scan = new Scanner(in);
        input = getClass().getResourceAsStream("homeworktool.txt");
    }
    
    public void getCredentials() throws IOException{
        out.println("CSE 154 Homework Tool (C) Zach Bryant");
        Scanner creds = new Scanner(input);
        boolean hasUser = false, hasPass = false, hasDir = false;
        if(creds.hasNextLine()){
            username = creds.nextLine().trim().toLowerCase();
            if(username.contains("\\")){
                defaultPath = username;
                username = "";
            }
            if(creds.hasNextLine()){
                pass = creds.nextLine().trim();
            }
            if(creds.hasNextLine())
                defaultPath = creds.nextLine().trim();
            hasUser = !username.isEmpty();
            hasPass = !pass.isEmpty();
            hasDir = !defaultPath.isEmpty();
            out.printf("user: \t\t%s%npass: \t\t%s%nworking dir:\t%s%n", 
                    (hasUser ? username.charAt(0) + 
                    username.substring(1).replaceAll("[\\w\\d\\s]", "*") : "n/a"), 
                    (hasPass ? "hidden" : "n/a"), 
                    (hasDir ? defaultPath : "n/a"));
        }
    }
    
    public String getAnswer(){
        String answer = scan.nextLine().trim();
        while(answer.isEmpty()){
            answer = scan.nextLine().trim();
        }
        return answer;
    }
    
    public void listFiles(File file, String level, boolean all){
        out.print(level.replaceAll("-", " ") + file.getName());
        if(file.isDirectory()){
            File[] files = file.listFiles();
            if(files != null){
                out.println(" \t\t\t[" + files.length + " children]");
                if(all || level.isEmpty())
                    for(File temp : files){
                        listFiles(temp, level+"-", all);
                    }
            }
            else
                out.println(" [0 children]");
        }
        else
            out.println();
    }
    
    public void showCommands(){
        out.println();
        out.println("<'def' | full path> <command> [a|n|o|r|t|u]");
        out.println("<required> [optional]");
        out.println("Commands:");
        out.println("\tlist, ls [all]: will show files directly inside the directory given. Will recurse if passed param all.");
        out.println("NOTE: Leaving the command blank will assume file transfer. Leaving options blank will trigger prompts for all operations.");
        out.println();
        out.println("File upload options:");
        out.println("\t-a: do everything except restore.");
        out.println("\t-n: no backup after optimization");
        out.println("\t-o: rearranges code to be in standard format, fixes general errors (only CSS).");
        out.println("\t-r: restore a file to it's backup. note: this option overrides ALL other options.");
        out.println("\t-t: submit files to be graded and save html receipt.");
        out.println("\t-u: upload files/directories to webster.");
        out.println();
    }
}