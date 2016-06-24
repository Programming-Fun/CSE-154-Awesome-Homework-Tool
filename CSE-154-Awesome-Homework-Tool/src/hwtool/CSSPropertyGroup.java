/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hwtool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Zach
 */
public class CSSPropertyGroup implements Comparable<CSSPropertyGroup>{
    Set<String> comments;
    protected Set<String> selectors, attributes;
    protected String rawSelectors, rawAttributes;
    
    public CSSPropertyGroup(){
        this.selectors = new TreeSet<>();
        this.attributes = new TreeSet<>();
        this.comments = new TreeSet<>();
        rawSelectors = "";
        rawAttributes = "";
    }
    
    public CSSPropertyGroup(String selectors, String attributes, String comment){
        this(selectors, attributes);
        if(!comment.isEmpty() && !containsComment(comment))
            this.comments.add(this.comments.size()+comment);
    }
    
    public CSSPropertyGroup(String selectors, String attributes){
        this();
        this.rawSelectors = selectors.trim().replaceAll("[\n\t]", "")
                .replaceAll(" {2,}", " ");
        this.rawAttributes = attributes.trim().replaceAll("[^;]^", ";\n")
                .replaceAll("[\n\t]", "");
        processSelectors(selectors);
        processAttributes(attributes);
    }
    
    public CSSPropertyGroup(CSSPropertyGroup... groups){
        this(Arrays.asList(groups));
    }
    
    public CSSPropertyGroup(List<CSSPropertyGroup> groups){
        this();
        for(CSSPropertyGroup group : groups){
            this.selectors.addAll(group.selectors);
            this.attributes.addAll(group.attributes);
            for(String temp : group.comments)
                if(!containsComment(temp))
                    comments.add(temp);
        }
        rebuildRaw();
    }
    
    private void rebuildRaw(){
        for(String s : selectors)
            rawSelectors += s;
        for(String s : attributes)
            rawAttributes += s;
    }
    
    public void addComments(Set<String> add){
        for(String temp : add){
            addComment(temp);
        }
    }
    
    public void addComment(String add){
        if(!add.isEmpty() && !containsComment(add))
            comments.add(comments.size() + add);
    }   
    
    private boolean containsComment(String add){
        for(String temp : comments){
            if(temp.substring(1).matches(add))
                return true;
        }
        return false;
    }
    
    private void processSelectors(String data){
        String[] temp = data.split(",\\s*");
        selectors.addAll(Arrays.asList(temp));
    }
    
    private void processAttributes(String data){
        data = data.replaceAll(":( +)",":")
                .replaceAll("^[ \\t]+", "");
        String[] temp = data.split("\n");
        attributes.addAll(Arrays.asList(temp));
    }
    
    public Set<CSSPropertyGroup> splitAttributes(){
        Set<CSSPropertyGroup> split = new TreeSet<>();
        boolean added = false;
        for(String attr : attributes){
            if(!attr.isEmpty()){
                CSSPropertyGroup group = new CSSPropertyGroup(rawSelectors, attr);
                if(!added){
                    group.addComments(comments);
                    added = true;
                }
                split.add(group);
            }
        }
        return split;
    }

    @Override
    public int compareTo(CSSPropertyGroup o) {
        String tempRawSelectors = this.rawSelectors.replaceAll("[\\W]", "");
        String tempRawAttributes = this.rawAttributes.replaceAll("[\\W]", "");
        String otherTempRawSelectors = o.rawSelectors.replaceAll("[\\W]", "");
        String otherTempRawAttributes = o.rawAttributes.replaceAll("[\\W]", "");
        int res = tempRawSelectors.compareTo(otherTempRawSelectors);
        if(res == 0)
            res = tempRawAttributes.compareTo(otherTempRawSelectors);
        return res;
    }
    
    @Override
    public String toString(){
        ArrayList<String> commentsTemp = new ArrayList<>();
        for(String temp : comments){
            Integer index = Integer.valueOf(""+temp.charAt(0));
            commentsTemp.add(index, temp.substring(2));
        }
        StringBuilder prettyComments = new StringBuilder();
        for(String temp : commentsTemp){
            prettyComments.append(temp);
            prettyComments.append("\n");
        }
        StringBuilder prettyAttributes = new StringBuilder();
        StringBuilder prettySelectors = new StringBuilder();
        
        Pattern pattern = Pattern.compile("(\\*|=|~|(\\|)|\\$|\\^|\\+|>)+");
        Iterator<String> it = selectors.iterator();
        if(it.hasNext()){
            prettySelectors.append(it.next());
            while(it.hasNext()){
                String selector = it.next().replaceAll("\\s+", "");
                Matcher matcher = pattern.matcher(selector);
                while(matcher.find()){
                    String match = selector.substring(matcher.start(), matcher.end());
                    match = " " + match + " ";
                    selector = selector.substring(0, matcher.start()) + match +
                            selector.substring(matcher.end());
                    
                }
                //hackish cause it didnt work even though the regex detects |=
                prettySelectors.append(", ");
                prettySelectors.append(selector);
            }
        }
        
        String spaces = "";
        for(int i = 0; i < HwTool.spaces; i++)
            spaces += " ";
        
        it = attributes.iterator();
        while(it.hasNext()){
            prettyAttributes.append(spaces);
            String attr = it.next();
            attr = attr.replaceFirst(":", ": ").replaceAll("http(s{0,1}): ", "https:");
            prettyAttributes.append(attr);
            prettyAttributes.append("\n");
        }
        
        String pretty = prettySelectors.toString() + " {\n" + 
                prettyAttributes.toString() + "}\n";
        if(!comments.isEmpty())
            pretty = prettyComments.toString().trim() + "\n" + pretty;
        return pretty;
        
    }
}
