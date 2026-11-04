package com.Harsh.Productivity.Os.ai.embedding;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Service
public class KeywordDocumentService {

    private static final int MAX_TOP_K = 20;

    private static final String SEARCH_SQL = """
            WITH parsed_query AS (
                SELECT websearch_to_tsquery('english', ?) AS tsq
            )
            SELECT
                e.embedding_id::text AS embedding_id,
                e.text AS content,
                ts_rank_cd(
                    to_tsvector('english', coalesce(e.text, '')),
                    parsed_query.tsq
                ) AS score,
                e.metadata::text AS metadata_json
            FROM document_embeddings e
            CROSS JOIN parsed_query
            WHERE e.metadata ->> 'tenantId' = ?
              AND e.metadata ->> 'documentId' = ?
              AND to_tsvector('english', coalesce(e.text, '')) @@ parsed_query.tsq
            ORDER BY score DESC, e.embedding_id
            LIMIT ?
            """;

    private final JdbcTemplate jdbcTemplate;
    private final JsonMapper jsonMapper;

    public KeywordDocumentService(
            JdbcTemplate jdbcTemplate,
            JsonMapper jsonMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonMapper = jsonMapper;
    }

    public List<KeywordSearchResult> search(
            String tenantId,
            String documentId,
            String query,
            int topK
    ) {
        requireText(tenantId, "tenantId");
        requireText(documentId, "documentId");
        requireText(query, "query");

        if (topK < 1 || topK > MAX_TOP_K) {
            throw new IllegalArgumentException(
                    "topK must be between 1 and " + MAX_TOP_K
            );
        }

        return jdbcTemplate.query(
                SEARCH_SQL,
                (rs, rowNum) -> mapResult(rs),
                query,
                tenantId,
                documentId,
                topK
        );
    }

    private KeywordSearchResult mapResult(ResultSet rs) throws SQLException {
        try {
            Map<String, Object> metadata = jsonMapper.readValue(
                    rs.getString("metadata_json"),
                    new TypeReference<Map<String, Object>>() {}
            );

            return new KeywordSearchResult(
                    rs.getString("embedding_id"),
                    rs.getString("content"),
                    rs.getDouble("score"),
                    metadata
            );
        } catch (JacksonException e) {
            throw new IllegalStateException(
                    "Could not read chunk metadata returned by PostgreSQL",
                    e
            );
        }
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank"
            );
        }
    }
}