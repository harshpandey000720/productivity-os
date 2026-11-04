package com.Harsh.Productivity.Os.ai.embedding;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RrfFusionService {
    private final int rrfK;

    public RrfFusionService(@Value("${ai.rag.rrf.k:60}") int rrfK) {
        if (rrfK < 1) {
            throw new IllegalArgumentException("RRF must be at least 1");
        }
        this.rrfK = rrfK;
    }

    public List<HybridSearchCandidate> fuse(List<EmbeddingMatch<TextSegment>> vectorMatches, List<KeywordSearchResult> keywordMatches, int limit) {
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("limit must be between 1 and 100");
        }
        Map<String, CandidateAccumulator> byId = new HashMap<>();
        addVectorResults(byId, vectorMatches);
        addKeywordResults(byId, keywordMatches);
        Comparator<HybridSearchCandidate> ordering =
                Comparator.comparingDouble(HybridSearchCandidate::rrfScore)
                        .reversed()
                        .thenComparing(
                                HybridSearchCandidate::keywordRank,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
                        .thenComparing(
                                HybridSearchCandidate::vectorRank,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
                        .thenComparing(HybridSearchCandidate::embeddingId);
        return byId.values().stream()
                .map(CandidateAccumulator::toCandidate)
                .sorted(ordering)
                .limit(limit)
                .toList();
    }
    private void addVectorResults(
            Map<String, CandidateAccumulator> byId,
            List<EmbeddingMatch<TextSegment>> matches
    ) {
        Set<String> seenInVector = new HashSet<>();

        for (int index = 0; index < matches.size(); index++) {
            EmbeddingMatch<TextSegment> match = matches.get(index);
            String id = requireId(match.embeddingId());
            int rank = index + 1;

            // Ignore a repeated ID within the same ranked list.
            if (!seenInVector.add(id)) {
                continue;
            }

            TextSegment segment = match.embedded();
            Map<String, Object> metadata = segment.metadata() == null
                    ? Map.of()
                    : segment.metadata().toMap();

            CandidateAccumulator candidate =
                    byId.computeIfAbsent(id, CandidateAccumulator::new);

            candidate.mergeContent(segment.text());
            candidate.mergeMetadata(metadata);
            candidate.addVector(rank, match.score(), rrfK);
        }
    }
    private void addKeywordResults(Map<String, CandidateAccumulator> byId, List<KeywordSearchResult> matches) {
        Set<String> seenInKeyword  = new HashSet<>();
        for(int index = 0; index < matches.size(); index++) {
            KeywordSearchResult match = matches.get(index);
            String id=requireId(match.embeddingId());
            int rank=index+1;
            if(!seenInKeyword.add(id)) {
                continue;
            }
            CandidateAccumulator candidate = byId.computeIfAbsent(id,CandidateAccumulator::new);
            candidate.mergeContent(match.content());
            candidate.mergeMetadata(match.metadata());
            candidate.addKeyword(rank, match.score(), rrfK);
        }
    }
    private static String requireId(String id){
        if (id == null||id.isBlank()) {
            throw new IllegalArgumentException("Search result embedding ID must not be blank");
        }
        return id;
    }
    private static final class CandidateAccumulator {
        private final String embeddingId;
        private final Map<String ,Object> metadata=new LinkedHashMap<>();
        private String content;
        private double rrfScore;
        private Integer vectorRank;
        private Integer keywordRank;
        private Double vectorScore;
        private Double keywordScore;
        private CandidateAccumulator(String embeddingId) {
            this.embeddingId = embeddingId;
        }
        private void mergeContent(String incomingContent) {
            if (incomingContent == null) {
                throw new IllegalArgumentException("Search result content must not be null");
            }
            if(content==null){
                content = incomingContent;
            } else if (!content.equals(incomingContent)) {
                throw new IllegalStateException("Different chunk text returned for embedding ID"+embeddingId);
            }
        }
        private void mergeMetadata(Map<String ,Object> incomingMetadata) {
            if (incomingMetadata != null) {
                incomingMetadata.forEach(metadata::putIfAbsent);
            }
        }
        private void addVector(int rank,double score,int k){
            vectorRank = rank;
            vectorScore = score;
            rrfScore+=1.0/(k+rank);
        }
        private void addKeyword(int rank,double score,int k){
            keywordRank = rank;
            keywordScore = score;
            rrfScore+=1.0/(k+rank);
        }
        private HybridSearchCandidate toCandidate() {
            return new HybridSearchCandidate(
                    embeddingId,
                    content,
                    rrfScore,
                    vectorRank,
                    keywordRank,
                    vectorScore,
                    keywordScore,
                    metadata
            );
        }
    }
}
