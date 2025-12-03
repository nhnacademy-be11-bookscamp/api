package store.bookscamp.api.review.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.review.entity.Review;
import store.bookscamp.api.review.repository.ReviewRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;

    @Value("${google.gemini.review-api-key}")
    private String reviewApiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    @Scheduled(cron = "0 0 0 * * *")
    public void updateAiReviews() {
        List<Book> books = bookRepository.findAll();
        books.forEach(this::updateAiReviewForBook);
    }

    public void updateAiReviewForBook(Book book) {
        try {
            List<Review> reviews = reviewRepository.findAiReviewsByBookId(book.getId());
            List<Review> selectedReviews = selectReviews(reviews);

            if (selectedReviews.isEmpty()) {
                book.setAiReview(null);
                bookRepository.save(book);
                return;
            }

            String prompt = buildPrompt(book, selectedReviews);
            String aiText = callGeminiApi(prompt);

            if (aiText == null) {
                throw new ApplicationException(ErrorCode.AI_REVIEW_GENERATION_FAILED);
            }

            book.setAiReview(aiText);
            bookRepository.save(book);
            log.info("[AiReviewService] Book {} AI review updated", book.getId());
        } catch (ApplicationException ae) {
            log.error("[AiReviewService] Book {} AI review update failed: {}", book.getId(), ae.getMessage());
        } catch (Exception e) {
            log.error("[AiReviewService] Book {} AI review unexpected error", book.getId(), e);
            throw new ApplicationException(ErrorCode.AI_REVIEW_API_ERROR);
        }
    }

    private List<Review> selectReviews(List<Review> reviews) {
        if (reviews.isEmpty()) return Collections.emptyList();

        List<Review> result = new ArrayList<>();
        Map<Integer, List<Review>> scoreMap = reviews.stream()
                .collect(Collectors.groupingBy(Review::getScore, LinkedHashMap::new, Collectors.toList()));

        int[] scoreOrder = {5, 4, 3};
        for (int score : scoreOrder) {
            List<Review> byScore = scoreMap.getOrDefault(score, Collections.emptyList());
            for (Review r : byScore) {
                if (result.size() < 5) result.add(r);
            }
            if (result.size() >= 5) break;
        }

        return result.size() < 5 ? Collections.emptyList() : result;
    }

    private String buildPrompt(Book book, List<Review> reviews) {
        StringBuilder sb = new StringBuilder();
        sb.append("아래 도서와 리뷰를 참고하여 150자 내외로 한 줄 요약 리뷰를 생성해 주세요.\n");
        sb.append("도서 제목: ").append(book.getTitle()).append("\n");
        sb.append("도서 설명: ").append(book.getExplanation()).append("\n\n");
        sb.append("리뷰 목록:\n");
        reviews.forEach(r -> sb.append("- ").append(r.getContent()).append("\n"));
        sb.append("\n출력 형식: 한 줄 요약 리뷰만 출력하고 그 외 내용은 출력하지 마세요.");
        return sb.toString();
    }

    private String callGeminiApi(String prompt) {
        try {
            JSONObject body = new JSONObject()
                    .put("contents", new JSONArray()
                            .put(new JSONObject()
                                    .put("parts", new JSONArray()
                                            .put(new JSONObject().put("text", prompt)))));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_URL + "?key=" + reviewApiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject res = new JSONObject(response.body());
            JSONArray candidates = res.optJSONArray("candidates");
            if (candidates == null || candidates.isEmpty()) return null;

            String text = candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .optString("text", "")
                    .trim();

            return text.isEmpty() ? null : text;
        } catch (Exception e) {
            log.error("[AiReviewService] Gemini API 호출 실패", e);
            throw new ApplicationException(ErrorCode.AI_REVIEW_API_ERROR);
        }
    }

    public Book getBookById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.BOOK_NOT_FOUND));
    }
}
