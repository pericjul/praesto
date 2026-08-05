package ch.zhaw.praesto.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Vektor-Speicher der KI-Wissensbasis auf Basis von PostgreSQL + pgvector.
 *
 * Bewusst über {@link JdbcTemplate} statt JPA: Der Spaltentyp {@code vector} wird von
 * Hibernate/H2 nicht sauber unterstützt, und so bleibt das Feature unabhängig von der
 * ddl-auto-Einstellung der Prod-DB (CREATE TABLE IF NOT EXISTS).
 *
 * Alles ist fehlertolerant: Ist pgvector nicht verfügbar (z.B. Extension nicht aktiviert,
 * oder in Tests H2), schaltet sich der Store selbst ab ({@code available=false}) und der
 * KI-Coach nutzt automatisch die einfache Text-Variante als Fallback.
 */
@Service
@Slf4j
public class KnowledgeVectorStore {

    private final JdbcTemplate jdbc;
    private final boolean embeddingsEnabled;
    private final int dim;

    /** false, sobald pgvector nicht verfügbar ist -> Aufrufer nutzen den Fallback. */
    private boolean available = false;

    public KnowledgeVectorStore(JdbcTemplate jdbc,
                                @Value("${praesto.knowledge.embeddings-enabled:true}") boolean embeddingsEnabled,
                                @Value("${praesto.knowledge.embedding-dim:1536}") int dim) {
        this.jdbc = jdbc;
        this.embeddingsEnabled = embeddingsEnabled;
        this.dim = dim;
    }

    @PostConstruct
    void ensureSchema() {
        if (!embeddingsEnabled) {
            log.info("KI-Wissensbasis: Embeddings deaktiviert -> Vektorsuche aus, einfacher Text-Fallback aktiv.");
            return;
        }
        try {
            jdbc.execute("CREATE EXTENSION IF NOT EXISTS vector");
            jdbc.execute("""
                    CREATE TABLE IF NOT EXISTS knowledge_chunks (
                        id varchar(80) PRIMARY KEY,
                        source_id varchar(80) NOT NULL,
                        chunk_index int NOT NULL,
                        content text NOT NULL,
                        embedding vector(%d)
                    )
                    """.formatted(dim));
            jdbc.execute("CREATE INDEX IF NOT EXISTS idx_knowledge_chunks_source ON knowledge_chunks (source_id)");
            // HNSW-Index für schnelle Ähnlichkeitssuche (best-effort; ohne ihn geht's auch, nur langsamer).
            try {
                jdbc.execute("CREATE INDEX IF NOT EXISTS idx_knowledge_chunks_vec "
                        + "ON knowledge_chunks USING hnsw (embedding vector_cosine_ops)");
            } catch (Exception e) {
                log.info("HNSW-Index nicht angelegt (pgvector evtl. älter) – Suche funktioniert trotzdem: {}", e.getMessage());
            }
            available = true;
            log.info("KI-Wissensbasis: pgvector bereit (Dimension {}).", dim);
        } catch (Exception e) {
            available = false;
            log.warn("KI-Wissensbasis: pgvector NICHT verfügbar ({}). Die KI nutzt den einfachen Text-Fallback. "
                    + "Zum Aktivieren: Extension 'vector' in der PostgreSQL-Server-Konfiguration erlauben.", e.getMessage());
        }
    }

    public boolean isAvailable() {
        return available;
    }

    /** Ersetzt alle Häppchen einer Quelle durch die neuen (Content + Embedding). */
    public void replaceForSource(String sourceId, List<String> contents, List<float[]> embeddings) {
        if (!available) {
            return;
        }
        try {
            deleteForSource(sourceId);
            for (int i = 0; i < contents.size(); i++) {
                jdbc.update(
                        "INSERT INTO knowledge_chunks (id, source_id, chunk_index, content, embedding) "
                                + "VALUES (?, ?, ?, ?, ?::vector)",
                        sourceId + "#" + i, sourceId, i, contents.get(i), toVectorLiteral(embeddings.get(i)));
            }
        } catch (Exception e) {
            log.error("Wissens-Häppchen konnten nicht gespeichert werden (Quelle {}): {}", sourceId, e.getMessage());
        }
    }

    public void deleteForSource(String sourceId) {
        if (!available) {
            return;
        }
        try {
            jdbc.update("DELETE FROM knowledge_chunks WHERE source_id = ?", sourceId);
        } catch (Exception e) {
            log.error("Wissens-Häppchen konnten nicht gelöscht werden (Quelle {}): {}", sourceId, e.getMessage());
        }
    }

    /** Die {@code k} ähnlichsten Häppchen zur Anfrage (Kosinus-Distanz, kleinster Abstand zuerst). */
    public List<String> search(float[] queryEmbedding, int k) {
        if (!available || queryEmbedding == null) {
            return List.of();
        }
        try {
            return jdbc.queryForList(
                    "SELECT content FROM knowledge_chunks ORDER BY embedding <=> ?::vector LIMIT ?",
                    String.class, toVectorLiteral(queryEmbedding), k);
        } catch (Exception e) {
            log.error("Vektorsuche fehlgeschlagen: {}", e.getMessage());
            return List.of();
        }
    }

    /** pgvector erwartet das Literal-Format "[0.1,0.2,...]". */
    private String toVectorLiteral(float[] v) {
        StringBuilder sb = new StringBuilder(v.length * 8);
        sb.append('[');
        for (int i = 0; i < v.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(v[i]);
        }
        return sb.append(']').toString();
    }
}
