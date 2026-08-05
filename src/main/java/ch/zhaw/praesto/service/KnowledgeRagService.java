package ch.zhaw.praesto.service;

import ch.zhaw.praesto.model.KnowledgeSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * RAG-Kern der KI-Wissensbasis: zerlegt Quellen in Häppchen, wandelt sie via
 * Azure-OpenAI-Embeddings in Vektoren um (einmal beim Hinzufügen) und liefert pro
 * Schülerfrage die relevantesten Häppchen zurück.
 *
 * Fehlertolerant: Ohne Embedding-Modell oder ohne pgvector ({@link KnowledgeVectorStore}
 * nicht verfügbar) tut der Service nichts und der Coach nutzt den einfachen Text-Fallback.
 */
@Service
@Slf4j
public class KnowledgeRagService {

    private final ObjectProvider<EmbeddingModel> embeddingModelProvider;
    private final KnowledgeVectorStore vectorStore;
    private final boolean embeddingsEnabled;
    private final int chunkSize;
    private final int chunkOverlap;
    private final int topK;

    public KnowledgeRagService(ObjectProvider<EmbeddingModel> embeddingModelProvider,
                               KnowledgeVectorStore vectorStore,
                               @Value("${praesto.knowledge.embeddings-enabled:true}") boolean embeddingsEnabled,
                               @Value("${praesto.knowledge.chunk-size:900}") int chunkSize,
                               @Value("${praesto.knowledge.chunk-overlap:150}") int chunkOverlap,
                               @Value("${praesto.knowledge.top-k:5}") int topK) {
        this.embeddingModelProvider = embeddingModelProvider;
        this.vectorStore = vectorStore;
        this.embeddingsEnabled = embeddingsEnabled;
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
        this.topK = topK;
    }

    /** Ist die Vektorsuche einsatzbereit (Modell + pgvector vorhanden)? */
    public boolean isActive() {
        return embeddingsEnabled && vectorStore.isAvailable() && embeddingModelProvider.getIfAvailable() != null;
    }

    /** Indexiert eine Quelle neu (löscht alte Häppchen). Best-effort – Fehler brechen nichts ab. */
    public void reindex(KnowledgeSource source) {
        if (source == null) {
            return;
        }
        if (!isActive() || !source.isEnabled() || source.getContent() == null || source.getContent().isBlank()) {
            // Nicht aktiv/aktiviert -> evtl. vorhandene Häppchen entfernen, damit nichts "hängen" bleibt.
            vectorStore.deleteForSource(source.getId());
            return;
        }
        try {
            List<String> chunks = chunk(source.getContent());
            // Titel jedem Häppchen voranstellen -> bessere Treffer & Kontext für die KI.
            List<String> withTitle = new ArrayList<>(chunks.size());
            for (String c : chunks) {
                withTitle.add((source.getTitle() != null ? source.getTitle() + ": " : "") + c);
            }
            List<float[]> vectors = embeddingModelProvider.getObject().embed(withTitle);
            vectorStore.replaceForSource(source.getId(), withTitle, vectors);
            log.info("KI-Wissen: Quelle '{}' indexiert ({} Häppchen).", source.getTitle(), chunks.size());
        } catch (Exception e) {
            log.error("KI-Wissen: Indexierung der Quelle '{}' fehlgeschlagen: {}", source.getTitle(), e.getMessage());
        }
    }

    public void remove(String sourceId) {
        vectorStore.deleteForSource(sourceId);
    }

    /**
     * Liefert die relevantesten Wissens-Häppchen zur Frage als fertigen Text (oder leer).
     * Leerer Rückgabewert -> der Aufrufer soll den einfachen Fallback verwenden.
     */
    public String retrieve(String query) {
        if (!isActive() || query == null || query.isBlank()) {
            return "";
        }
        try {
            float[] q = embeddingModelProvider.getObject().embed(query);
            List<String> hits = vectorStore.search(q, topK);
            if (hits.isEmpty()) {
                return "";
            }
            return String.join("\n\n---\n\n", hits);
        } catch (Exception e) {
            log.error("KI-Wissen: Abruf fehlgeschlagen: {}", e.getMessage());
            return "";
        }
    }

    /** Zerlegt Text in überlappende Häppchen, möglichst an Wortgrenzen. */
    private List<String> chunk(String text) {
        String t = text.strip();
        List<String> out = new ArrayList<>();
        if (t.length() <= chunkSize) {
            out.add(t);
            return out;
        }
        int start = 0;
        while (start < t.length()) {
            int end = Math.min(start + chunkSize, t.length());
            // Nicht mitten im Wort abschneiden: bis zum nächsten Leerzeichen zurückgehen.
            if (end < t.length()) {
                int space = t.lastIndexOf(' ', end);
                if (space > start + chunkSize / 2) {
                    end = space;
                }
            }
            out.add(t.substring(start, end).strip());
            if (end >= t.length()) {
                break;
            }
            start = Math.max(end - chunkOverlap, start + 1);
        }
        return out;
    }
}
