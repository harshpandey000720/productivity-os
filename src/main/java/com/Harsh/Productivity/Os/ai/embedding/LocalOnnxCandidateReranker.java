package com.Harsh.Productivity.Os.ai.embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.scoring.ScoringModel;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
public class LocalOnnxCandidateReranker implements CandidateReranker {
    private static final int MAX_CANDIDATES = 20;
    private final ScoringModel scoringModel;
    public LocalOnnxCandidateReranker(ScoringModel scoringModel) {
        this.scoringModel = scoringModel;
    }
    @Override
    public List<RerankedCandidate> rerank(String query, List<HybridSearchCandidate> candidates, int maxResults) {
        if(query==null||query.isBlank()){
            throw new IllegalArgumentException("query must not be blank");
        }
        if(candidates==null||candidates.isEmpty()){
            return List.of();
        }
        if(candidates.size()>MAX_CANDIDATES){
            throw new IllegalArgumentException("Reranking is limited to "+MAX_CANDIDATES+" candidates");
        }
        if(maxResults<1||maxResults>MAX_CANDIDATES){
            throw new IllegalArgumentException("maxResult must be between 1 and "+MAX_CANDIDATES);
        }
        List<TextSegment> passages=candidates.stream()
                .map(candidate -> TextSegment.from(candidate.content()))
                .toList();
        List<Double> scores=scoringModel.scoreAll(passages,query).content();
        if(scores==null||scores.size()!=candidates.size()){
            throw  new IllegalArgumentException("Reranker must return exactly one score per candidate");
        }
        List<ScoredCandidate> scored=new ArrayList<>();
        for (int i = 0; i < candidates.size(); i++) {
            Double score=scores.get(i);
            if(score==null||!Double.isFinite(score)){
                throw new IllegalArgumentException("Reranker returned an invalid score at index "+i);
            }
            scored.add(new ScoredCandidate(candidates.get(i),score));
        }
        scored.sort(
                Comparator.comparingDouble(ScoredCandidate::score).reversed()
                        .thenComparing(Comparator.comparingDouble(
                                (ScoredCandidate row)->
                                        row.candidate().rrfScore()
                                ).reversed()
                        )
                        .thenComparing(row->row.candidate().embeddingId())
        );
        List<RerankedCandidate> result=new ArrayList<>();
        int count=Math.min(maxResults,scored.size());
        for(int i=0;i<count;i++){
            ScoredCandidate row=scored.get(i);
            result.add(new RerankedCandidate(row.candidate() ,row.score(),i+1));
        }
        return List.copyOf(result);
    }
    private record ScoredCandidate(HybridSearchCandidate  candidate, double score) {}
}
