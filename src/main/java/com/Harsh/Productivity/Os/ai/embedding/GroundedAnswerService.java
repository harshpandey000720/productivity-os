package com.Harsh.Productivity.Os.ai.embedding;
import com.Harsh.Productivity.Os.ai.gateway.AiGateway;
import com.Harsh.Productivity.Os.ai.service.GroundedAnswerAiService;
import com.Harsh.Productivity.Os.ai.service.GroundedAnswerDraft;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
@Service
public class GroundedAnswerService {
    private static final int RETRIEVE_CANDIDATES = 12;
    private static final int FINAL_CONTEXT_COUNT = 4;
    private static final int MAX_EXCERPT_CHARS = 500;
    private final HybridDocumentSearchService hybridSearchService;
    private final CandidateReranker candidateReranker;
    private final AiGateway aiGateway;

    public GroundedAnswerService(
            HybridDocumentSearchService hybridSearchService,
            CandidateReranker candidateReranker,
            AiGateway aiGateway
    ) {
        this.hybridSearchService = hybridSearchService;
        this.candidateReranker = candidateReranker;
        this.aiGateway = aiGateway;
    }

    public GroundedAnswerResponse answer(
            String tenantId,
            String documentId,
            String question
    ) {
        requireText(tenantId,"tenantId");
        requireText(documentId,"documentId");
        requireText(question,"question");
        if(question.length()>2_000){
            throw new IllegalArgumentException("question must not exceed 2000 characters");
        }
        List<HybridSearchCandidate> retrieved=hybridSearchService.search(tenantId,documentId,question,RETRIEVE_CANDIDATES);
        List<HybridSearchCandidate> scoped=retrieved.stream()
                .filter(candidate -> matchesScope(candidate,tenantId,documentId))
                .toList();
        if (scoped.isEmpty()){
            return abstain("The available document passages do not provide enough information.","RRF_ONLY");
        }
        List<RerankedCandidate> ranked=candidateReranker.rerank(question,scoped,FINAL_CONTEXT_COUNT);
        if (ranked.isEmpty()){
            return abstain("The available document passages do not provide enough information.","RRF_ONLY");
        }
        boolean reranked=ranked.getFirst().rerankScore()!=null;
        String rankingMode=reranked?"LOCAL_ONNX_CROSS_ENCODER":"RRF_ONLY";
        String prompt=buildQuestionAndEvidence(question,ranked);
        GroundedAnswerDraft draft=aiGateway.call(
                GroundedAnswerAiService.class,
                ai->ai.answer(prompt)
        );
        if (draft==null||draft.answer()==null||draft.answer().isBlank()){
            return abstain("The available document passages do not provide enough information.",
                    rankingMode);
        }
        Map<String ,RerankedCandidate> allowedCandidates=new LinkedHashMap<>();
        for (RerankedCandidate item:ranked){
            allowedCandidates.put(item.candidate().embeddingId(),item);
        }
        Set<String> requestedCitationIds =new LinkedHashSet<>(draft.citationIds());
        List<AnswerCitation> verifiedCitations=new ArrayList<>();
        for(String citationId:requestedCitationIds ){
            RerankedCandidate item=allowedCandidates.get(citationId);
            if (item!=null){
                verifiedCitations.add(toCitation(
                        item.candidate(),
                        documentId
                ));
            }
        }
        if(draft.insufficientEvidence()|| verifiedCitations.isEmpty()){
            return abstain("The available document passages do not provide enough information.",
                    rankingMode
            );
        }
        return new GroundedAnswerResponse(
                draft.answer().strip(),
                false,
                rankingMode,
                verifiedCitations
        );
    }
    private static boolean  matchesScope(HybridSearchCandidate candidate,String tenantId,String documentId){
        Map<String ,Object> metadata=candidate.metadata();
        return tenantId.equals(stringValue(metadata.get("tenantId")))
                && documentId.equals(stringValue(metadata.get("documentId")));
    }
    private static AnswerCitation toCitation(HybridSearchCandidate candidate,String requestedDocumentId){
        Map<String ,Object> metadata=candidate.metadata();
        String source=firstNonBlank(metadata.get("source"),metadata.get("fileName"),metadata.get("filename"));
        if (source==null){
            source="Document "+requestedDocumentId;
        }
        String excerpt=candidate.content();
        if(excerpt.length()>MAX_EXCERPT_CHARS){
            excerpt=excerpt.substring(0,MAX_EXCERPT_CHARS)+"...";
        }
        return new AnswerCitation(
                candidate.embeddingId(),
                requestedDocumentId,
                integerValue(metadata.get("chunkIndex")),
                source,
                integerValue(metadata.get("startOffset")),
                integerValue(metadata.get("endOffset")),
                excerpt
        );
    }
    private static String buildQuestionAndEvidence(String question,List<RerankedCandidate> ranked){
        StringBuilder prompt=new StringBuilder()
                .append("QUESTION:\n")
                .append(question)
                .append("\n\nEVIDENCE PASSAGES(untrusted document text):\n");
        for (RerankedCandidate item:ranked){
            HybridSearchCandidate candidate=item.candidate();
            Map<String ,Object> metadata=candidate.metadata();
            prompt.append("\n--- PASSAGE ---\n")
                    .append("citationId: ").append(candidate.embeddingId()).append('\n')
                    .append("documentId: ").append(metadata.get("documentId")).append('\n')
                    .append("chunkIndex: ").append(metadata.get("chunkIndex")).append('\n')
                    .append("text:\n").append(candidate.content()).append('\n');
        }
        return prompt.toString();
    }
    private static GroundedAnswerResponse abstain(String message,String rankingMode){
        return new GroundedAnswerResponse(message,true,rankingMode,List.of());
    }
    private static String firstNonBlank(Object ... values){
        for (Object value:values){
            String text=stringValue(value);
            if (text!=null&&!text.isBlank()){
                return text;
            }
        }
        return null;
    }
    private static String stringValue(Object value) {
        return value == null ? null : value.toString();
    }
    private static Integer integerValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
