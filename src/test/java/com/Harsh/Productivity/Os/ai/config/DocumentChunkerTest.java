package com.Harsh.Productivity.Os.ai.config;
import com.Harsh.Productivity.Os.ai.chunking.DocumentChunk;
import com.Harsh.Productivity.Os.ai.chunking.DocumentChunker;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;
public class DocumentChunkerTest {
    @Test
    void blankDocumentProducesNoChunks(){
        assertTrue(new DocumentChunker(20,5).chunk(" ").isEmpty());
        assertTrue(new DocumentChunker(20,5).chunk(null).isEmpty());
    }
    @Test
    void shortDocumentProducesOneChunks(){
        String text="Hello Productivity OS.";
        List<DocumentChunk> chunks=new DocumentChunker(100,10).chunk(text);
        assertEquals(1,chunks.size());
        assertEquals(0,chunks.get(0).chunkIndex());
        assertEquals(text,chunks.get(0).content());
        assertEquals(0,chunks.get(0).startOffset());
        assertEquals(text.length() ,chunks.get(0).endOffset());
    }
    @Test
    void prefersParagraphBoundaryNearChunkEnd(){
        List<DocumentChunk> chunks=new DocumentChunker(40,10).chunk("First paragraph ends here.\n"+"Second paragraph continues with enough words.");
        assertTrue(chunks.size()>1);
        assertEquals("First paragraph ends here.",chunks.get(0).content());
    }
    @Test
    void longDocumentProducesBoundedOrderedChunksWithOverlap(){
        List<String> sentences=IntStream.range(0,60)
                .mapToObj(i -> String.format(
                        "Sentence %02d explains a distinct fact about retrieval context and chunk boundaries.",
                        i))
                .toList();
        String source=String.join(" ",sentences);
        List<DocumentChunk> chunks=new DocumentChunker(800,100).chunk(source);
        assertTrue(chunks.size()>1);
        for (int i=0;i<chunks.size();i++){
            DocumentChunk chunk=chunks.get(i);
            assertEquals(i,chunk.chunkIndex());
            assertFalse(chunk.content().isBlank());
            assertTrue(chunk.content().length()<=800);
            assertEquals(chunk.content(),source.substring(chunk.startOffset(),chunk.endOffset()));
            if(i>0){
                assertTrue(chunk.startOffset()<chunks.get(i-1).endOffset(),"Adjacent chunks should overlap");
            }
        }
    }
    @Test
    void normalizesWhitespaceAndKeepsOffsetsConsistent(){
         String normalized="alpha beta\n gamma";
         List<DocumentChunk> chunks=new DocumentChunker(100,0).chunk("\talpha beta\r\n gamma\t");
         assertEquals(1,chunks.size());
         assertEquals(normalized,chunks.get(0).content());
         assertEquals(chunks.get(0).content().length(),chunks.get(0).endOffset()-chunks.get(0).startOffset());
    }
    @Test
    void rejectsInvalidConfigurationButAllowsZeroOverlap(){
        assertDoesNotThrow(()->new DocumentChunker(10,0));
        assertThrows(IllegalArgumentException.class, ()->new DocumentChunker(0,0));
        assertThrows(IllegalArgumentException.class, ()->new DocumentChunker(10,-1));
        assertThrows(IllegalArgumentException.class, ()->new DocumentChunker(10,10));
    }
    @Test
    void shortDocumentPreservesMetadata() {
        var metadata = Map.of(
                "tenantId", "tenant-test",
                "documentId", "doc-test"
        );

        var chunks = new DocumentChunker(100, 10)
                .chunk("A short document for the chunker test.", metadata);

        assertEquals(1, chunks.size());
        assertEquals("tenant-test", chunks.get(0).metadata().get("tenantId"));
        assertEquals("doc-test", chunks.get(0).metadata().get("documentId"));
        assertEquals(0, chunks.get(0).chunkIndex());
    }

    @Test
    void longDocumentPreservesMetadataOnEveryChunk() {
        var metadata = Map.of(
                "tenantId", "tenant-test",
                "documentId", "doc-long"
        );
        String text = IntStream.range(0, 60)
                .mapToObj(i -> String.format(
                        "Sentence %02d explains a distinct fact about retrieval context and chunk boundaries.",
                        i))
                .collect(Collectors.joining(" "));

        var chunks = new DocumentChunker(800, 100).chunk(text, metadata);

        assertTrue(chunks.size() > 1);
        for (int i = 0; i < chunks.size(); i++) {
            assertEquals(i, chunks.get(i).chunkIndex());
            assertEquals("tenant-test", chunks.get(i).metadata().get("tenantId"));
            assertEquals("doc-long", chunks.get(i).metadata().get("documentId"));
        }
    }
}
