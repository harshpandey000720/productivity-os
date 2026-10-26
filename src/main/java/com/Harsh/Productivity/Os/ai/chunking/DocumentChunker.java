package com.Harsh.Productivity.Os.ai.chunking;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Component
public class DocumentChunker {
    private final int chunkSize;
    private final int overlap;
    public DocumentChunker(@Value("${ai.rag.chunking.max-segment-chars:800}")int chunkSize,@Value("${ai.rag.chunking.max-overlap-chars:100}")int overlap){
        if (chunkSize<=0) throw  new IllegalArgumentException("chunkSize must be positive");
        if(overlap<0||overlap>=chunkSize) throw new IllegalArgumentException("overlap must be non-negative and less than chunkSize");
        this.chunkSize = chunkSize;
        this.overlap = overlap;
    }
    public List<DocumentChunk> chunk(String document) {
        return chunk(document, Map.of());
    }
    public List<DocumentChunk> chunk(String document, Map<String, String> metadata){
        if (document == null || document.isBlank()) {
            return List.of();
        }
        Map<String, String> safeMetadata =
                metadata == null ? Map.of() : Map.copyOf(metadata);
        if (document==null||document.isBlank()) return List.of();
        String normalized=normalize(document);
        if(normalized.isEmpty()) return List.of();
        List<DocumentChunk> chunks=new ArrayList<>();
        int start=0;
        int index=0;
        while(start<normalized.length()){
            int proposedEnd=Math.min(start+chunkSize,normalized.length());
            int end=proposedEnd;
            if(proposedEnd<normalized.length()) {
                int boundary=findBoundary(normalized,start,proposedEnd);
                if(boundary>start) {
                    end=boundary;
                }
            }
            int contentStart=start;
            int contentEnd=end;
            while (contentStart<contentEnd&&Character.isWhitespace(normalized.charAt(contentStart))) {
                contentStart++;
            }
            while (contentEnd>contentStart&&Character.isWhitespace(normalized.charAt(contentEnd-1))) {
                contentEnd--;
            }
            if (contentStart<contentEnd) {
                chunks.add(new DocumentChunk(index++,normalized.substring(contentStart,contentEnd),contentStart,contentEnd,safeMetadata));
            }
            if(end>=normalized.length()) {
                break;
            }
            int nextStart=Math.min(contentEnd-overlap,start+1);
            if(nextStart<=start) throw new IllegalStateException("Chunker failed to make forward progress");
            start=nextStart;
        }
        return List.copyOf(chunks);
    }
    private String normalize(String text){
        return text
                .replace("\r\n","\n")
                .replace("\r","\n")
                .replaceAll("[\\t]+"," ")
                .trim();
    }
    private int findBoundary(String text,int start,int proposedEnd){
        int minimumBoundary=start+Math.max(1,chunkSize/2);
        int newline=text.lastIndexOf('\n',proposedEnd-1);
        if(newline>=minimumBoundary){
            return newline+1;
        }
        int sentence=findLastSentenceBoundary(text,start,proposedEnd);
        if(sentence>=minimumBoundary){
            return sentence;
        }
        int space=text.lastIndexOf(' ',proposedEnd-1);
        if(space>=minimumBoundary){
            return space;
        }
        return proposedEnd;
    }
    private int findLastSentenceBoundary(String text,int start,int end){
        for (int i = end-1; i >start ; i--) {
            char current=text.charAt(i);
            if((current=='.'||current=='!'||current=='?')&&(i + 1 == end || Character.isWhitespace(text.charAt(i + 1)))){
                return i+1;
            }
        }
        return -1;
    }
}
