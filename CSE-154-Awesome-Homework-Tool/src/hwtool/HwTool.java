package hwtool;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import static com.jcraft.jsch.ChannelSftp.SSH_FX_FAILURE;
import static com.jcraft.jsch.ChannelSftp.SSH_FX_NO_SUCH_FILE;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.lang.System.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

/**
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class HwTool {
    protected Set<CSSPropertyGroup> groups;
    public static int spaces = 2;
    protected String headComment;
    private JSch jsch;
    private static Session session;
    private static Channel channel;
    private static ChannelSftp sftpChannel;
    public static boolean doall;
    private static Helper help;
    
    public static void main(String[] args) throws Exception{
        help = new Helper();
        help.getCredentials();
        help.showCommands();
        out.println("Type \"help\" for this list of options.");
        out.println("Type \"quit\" to quit.");
        
        out.println("<file path> [-options..]");
        String target = help.getAnswer();
        out.println();
        while(!target.startsWith("quit")){
            doCommand(target);
            out.println("<file path> [options..]");
            target = help.getAnswer();
            out.println();
        }
        close();
    }
    
    public static void doCommand(String target) throws Exception{
        String[] parts = target.split(" ");
        String filePath = parts[0];
        switch(filePath.toLowerCase()){
            case "help": help.showCommands();
                break;
            default:
                if(filePath.startsWith("def"))
                    filePath = help.defaultPath + filePath.substring(3);
                HashSet<Character> options = new HashSet<>();
                File file = new File(filePath);
                if(!file.exists() || !file.canRead()){
                    out.println(target + " cannot be read or does not exist");
                    break;
                }
                if(parts.length > 1){
                    if(parts[1].equalsIgnoreCase("list") || parts[1].equalsIgnoreCase("ls")){
                        boolean all = false;
                        if(parts.length > 2 && parts[2].equalsIgnoreCase("all"))
                            all = true;
                        help.listFiles(file, "" , all);
                        break;
                    }
                    for(int i = 1; i < parts.length; i++){
                        char[] chars = parts[i].toCharArray();
                        for(char c : chars)
                            if(Character.isLetter(c))
                                options.add(Character.toLowerCase(c));
                    }
                }
                doall = options.contains('a');
                if(options.contains('r'))
                    restore();
                else
                    process(file, options);   
        }
    }
    
    public static void restore(){
        
    }
    
    public static void process(File file, HashSet<Character> options) throws Exception{
        if(file == null)
            return;
        if(file.isDirectory()){
            File[] files = file.listFiles();
            if(files != null)
                for(File temp : files){
                    process(temp, options);
                }
        }
        else{
            if(file.getAbsolutePath().contains("OLD_") || 
                    file.getAbsolutePath().contains("Homework Turnin"))
                return;
            out.println("+--Processing "+file.getName()+"--+");
            String answer;
            HwTool css = new HwTool(spaces);
            if(options.isEmpty()){
                help.showCommands();
                out.println("What would you like to do?");
                String[] opts = help.getAnswer().split(" ");
                for(int i = 0; i < opts.length; i++){
                    char[] chars = opts[i].toCharArray();
                    for(char c : chars)
                        if(Character.isLetter(c))
                            options.add(Character.toLowerCase(c));
                }
            }
            if(options.contains('o') || doall){
                if(file.getAbsolutePath().endsWith(".css")){
                        out.println("How many spaces per indentation? (Enter to skip, n to cancel)");
                        answer = help.scan.nextLine().toLowerCase();
                        if(!answer.startsWith("n")){
                            if(!answer.isEmpty()){
                                try{
                                    spaces = Integer.valueOf(answer);
                                }catch(Exception e){}
                            }
                            if(spaces < 2)
                                spaces = 2;

                            out.printf("Using %d spaces.%n", spaces);
                            css.readFile(file);
                            css.optimize().printCSS(file, !options.contains('n'));
                            out.println("Successful optimization.");
                        }
                }
                else
                    out.println("WARNING: Cannot beautify non CSS file!");
            }
            if(doall || options.contains('u')){
                out.println();
                css.withAuth(help.username, help.pass)
                    .sftpCSS("webster.cs.washington.edu", 22, 
                            "/www/html/students/%s/%s", file.getAbsolutePath());
            }
            if(doall || options.contains('t')){
                turnin();
            }
            out.println();
        }
    }
    
    public HwTool(int spaces){
        if(spaces < 0 || spaces > 4){
            out.println("That's bad style. Shame on you. \n\t\tI'm using 2 spaces.");
        }
        else
            this.spaces = spaces;
    }
    
    public HwTool readURL(URL url) throws IOException, Exception{
        if(!url.toString().endsWith(".css"))
            return this;
        InputStreamReader isReader = new InputStreamReader(url.openConnection().getInputStream());
        BufferedReader buffer = new BufferedReader(isReader);

        StringBuilder response = new StringBuilder();
        String line = "";
        while((line = buffer.readLine()) != null){
            if(!line.trim().isEmpty()){
                response.append(line);
                response.append("\n");
            }
        }
        buffer.close();
        isReader.close();
        String resp = response.toString().trim();
        if(!resp.startsWith("/*")){
            out.println("Your CSS is missing the header comment. You will lose points without this.");
            resp = "/*\n\t" + help.username +" CSE 154\n\t" + new Date() + "\n\t@TODO Change this auto-generated comment.\n*/" + resp;
        }
        headComment = resp.substring(0, resp.indexOf("*/")+2);
        resp = resp.substring(resp.indexOf("*/")+2);
        populateGroups(resp);
        return this;
    }
    
    
    public HwTool readFile(File file) throws FileNotFoundException, IOException, Exception{
        if(!file.exists())
            throw new FileNotFoundException(file.getAbsolutePath());
        readURL(file.toURI().toURL());
        return this;
    }
    
    private void populateGroups(String css){
        this.groups = new TreeSet<>();
        css = css.replaceAll("\\*\\/\\s*\\/\\*", "");
        String[] rawGroups = css.split("\\}");
        for(String rawGroup : rawGroups){
            if(!rawGroup.replaceAll("\\/\\*.*\\*\\/","").trim().isEmpty()){
                String[] temp = rawGroup.split("\\{");
                String[] temp2 = temp[0].split("\\*\\/");
                
                CSSPropertyGroup group = new CSSPropertyGroup(
                        temp2[temp2.length - 1], temp[1]);
                if(temp2.length > 1)
                    group.addComment(temp2[0] + "*/");
                groups.add(group);
            }
        }
    }
    
    public HwTool optimize(){
        Map<String, CSSPropertyGroup> splitAttrs = new HashMap<>();
        out.println("working..");
        for(CSSPropertyGroup group : groups)
            for(CSSPropertyGroup splitGroup : group.splitAttributes()){
                if(splitAttrs.containsKey(splitGroup.rawAttributes)){
                    splitAttrs.put(splitGroup.rawAttributes, 
                            new CSSPropertyGroup(splitGroup, 
                                    splitAttrs.get(splitGroup.rawAttributes)));
                }
                else
                    splitAttrs.put(splitGroup.rawAttributes, splitGroup);
                out.print(".");
            }
        groups.clear();
        out.print(".");
        for(CSSPropertyGroup group : splitAttrs.values())
            groups.add(group);
        //optimizing duplicate selectors
        Map<String, CSSPropertyGroup> common = new HashMap<>();
        for(CSSPropertyGroup group : groups){
            if(common.containsKey(group.rawSelectors))
                common.put(group.rawSelectors, new CSSPropertyGroup(group, 
                        common.get(group.rawSelectors)));
            else
                common.put(group.rawSelectors, group);
            out.print(".");
        }
        out.println();
        groups.clear();
        groups.addAll(common.values());
        
        return this;
    }
    
    public HwTool printCSS(){
        out.println(headComment);
        for(CSSPropertyGroup group : groups)
            out.print(group);
        return this;
    }
    
    public HwTool printCSS(String filename, boolean doBackup){
        File file = new File(filename);
        String bName = filename.replace(file.getName(), "OLD_"+file.getName());
        File backup = new File(bName);
        try{
            if(doBackup){
                Files.move(file.toPath(), backup.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                out.println("Created backup at "+backup.getAbsolutePath());
                out.println();
            }
            PrintWriter writer = new PrintWriter(filename);
            writer.println(headComment);
            for(CSSPropertyGroup group : groups){
                writer.print(group);
            }
            writer.close();
        }catch(IOException e){
            if(doBackup)
                out.println("WARNING: Encountered an error while saving backup.");
        }
        return this;
    }
    
    public HwTool printCSS(File file, boolean doBackup){
        return printCSS(file.getAbsolutePath(), doBackup);
    }
    
    public HwTool withAuth(String username, String pass){
        help.username = username;
        help.pass = pass;
        return this;
    }
    
    public HwTool sftpCSS(String host, Integer port, String username, 
            String pass, String workingDirectory, String filename) throws FileNotFoundException, IOException{
        return withAuth(username, pass).sftpCSS(host, port, workingDirectory, filename);
    }
    
    public HwTool sftpCSS(String host, Integer port, 
            String workingDirectory, String filename) throws FileNotFoundException, IOException{
        String base = "/www/html/students/";
        if(jsch == null)
            jsch = new JSch();
        if(port == null || port < 0)
            port = 22;
        out.printf("Preparing connection to %s:%s%n", host, port);
        File upload = new File(filename);
        String folder = upload.getCanonicalPath()
                .replace(help.defaultPath, "").replaceAll("\\\\", "/")
                .replace(upload.getName(), "").replaceAll("(^/|/$)","").trim();        
        workingDirectory = String.format(workingDirectory, help.username, folder);
        try{
            if(session == null){
                session = jsch.getSession(help.username, host, port);
                session.setConfig("StrictHostKeyChecking", "no");
                session.setPassword(help.pass);
            }
            if(!session.isConnected())
                session.connect();
            else{
                out.println("Resuming session with host.");
            }
            if(channel == null)
                channel = session.openChannel("sftp");
            if(!channel.isConnected() || channel.isClosed()){
                channel.connect();
                out.println("Channel opened and connected.");
            }
            else
                out.println("Channel resumed.");
            
            sftpChannel = (ChannelSftp)channel;
            try{
                sftpChannel.cd(workingDirectory);
            }catch(SftpException e){
                switch(e.id){
                    case SSH_FX_FAILURE:
                        sftpChannel.rm(workingDirectory);
                        break;
                    case SSH_FX_NO_SUCH_FILE:
                        String temp = workingDirectory.replace(folder, "");
                        sftpChannel.cd(temp);
                        mkdirs(sftpChannel, workingDirectory);
                        //}
                        
                        sftpChannel.cd(workingDirectory);
                        break;
                }
            }
            InputStream input = new FileInputStream(upload);
            sftpChannel.put(input, upload.getName());
            out.printf("%s/%s uploaded successfully.%n", 
                    workingDirectory.replace("base","../"), upload.getName());
            input.close();
        }
        catch(JSchException |SftpException e){
            jsch = new JSch();
            session = null;
            channel = null;
            //sftpCSS(host, port, workingDirectory, filename);
            out.println("FATAL: Begin error log.");
            out.println("FATAL: File transfer failed.");
            out.printf("FATAL: Host: %s:%d%n", host, port);
            out.println("FATAL: Working dir: " + workingDirectory);
            out.println("FATAL: Target file name: " + filename);
            out.println();
            e.printStackTrace();
        }
        
        return this;
    }
    
    public static void turnin(){
        //@TODO
    }
    
    public static void mkdirs(ChannelSftp ch, String path) {
    try {
        String[] folders = path.split("/");
        if (folders[0].isEmpty()) folders[0] = "/";
        String fullPath = folders[0];
        for (int i = 1; i < folders.length; i++) {
            Vector ls = ch.ls(fullPath);
            boolean isExist = false;
            for (Object o : ls) {
                if (o instanceof LsEntry) {
                    LsEntry e = (LsEntry) o;
                    if (e.getAttrs().isDir() && e.getFilename().equals(folders[i])) {
                        isExist = true;
                    }
                }
            }
            if (!isExist && !folders[i].isEmpty()) {
                ch.mkdir(fullPath + folders[i]); 
            }
            fullPath = fullPath + folders[i] + "/";
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
    
    public static void close(){
        if(sftpChannel != null){
            sftpChannel.exit();
            out.println("SFTP channel exited");
            channel.disconnect();
            out.println("Channel disconnected");
            session.disconnect();
            out.println("Session disconnected.");
        }
    }
    
}
