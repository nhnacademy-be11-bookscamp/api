package store.bookscamp.api.book.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import store.bookscamp.api.book.entity.BookDocument;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookAnswerService {

    @Value("${google.gemini.api-key}")
    private String geminiApiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";


    // Gemini LLM 호출 - 도서 기반 추천문 생성
    public Map<String, Object> generateAnswer(String query, List<BookDocument> docs) {
        Map<String,Object> result=new HashMap<>();
        try {
            // 프롬프트 구성
            String prompt = buildPrompt(query, docs);

            // 요청 본문
            JSONObject body = new JSONObject()
                    .put("contents", new JSONArray()
                            .put(new JSONObject()
                                    .put("parts", new JSONArray()
                                            .put(new JSONObject().put("text", prompt)))));

            // HTTP 요청
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_URL + "?key=" + geminiApiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject res = new JSONObject(response.body());
            log.info("[BookAnswerService] response: '{}'", response.body());

            JSONArray candidates = res.optJSONArray("candidates");
            if (candidates == null || candidates.isEmpty()) {
                result.put("result", "추천 결과를 생성하지 못했습니다.");
                return result;
            }

            // Gemini content.parts는 1개 뿐임 → 내부에 JSON 문자열이 들어있음
            String rawText = candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .optString("text", "");


            log.info("[BookAnswerService] rawText: '{}'", rawText);

            // ```json ... ``` 제거
            String cleaned = rawText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            log.info("[BookAnswerService] cleaned rawText = {}", cleaned);

            String[] list = cleaned.split("\n");
            String[] idStrList = (list[0].split(","));
            List<Long> idList= new ArrayList<>();
            for (int i = 0; i < idStrList.length; i++) {
                String num =idStrList[i].trim();
                idList.add(Long.parseLong(num));
                log.info("[BookAnswerService] idList "+i+" : '{}'", idList.get(i));
            }
            List<String> recList = new ArrayList<>();
            for (int i = 1; i < list.length; i++) {
                String text = list[i];
                int idx = text.indexOf(","); // 첫 번째 콤마의 위치 찾기
                String rec = text.substring(idx + 1);
                recList.add(rec);
                log.info("[BookAnswerService] recList "+i+" : '{}'", recList.get(i-1));
            }
            result.put("idList",idList);
            result.put("recList", recList);
            return result;

        } catch (Exception e) {
            log.error("[Gemini] 응답 생성 실패", e);
            result.put("result","AI 응답 생성 중 오류가 발생했습니다." );
            return result;
        }
    }

    // 사용자 검색어 + 도서 리스트 기반으로 프롬프트 생성
    private String buildPrompt(String userQuery, List<BookDocument> books) {
        StringBuilder sb = new StringBuilder();
        sb.append("사용자가 다음과 같은 검색을 했습니다: '").append(userQuery).append("'\n");
        sb.append("아래는 관련된 도서 목록입니다.\n");
        sb.append("해당 도서 목록에서 연관이 있는 도서를 분류해주고, 각 도서를 분석하여 연관도가 높은 3등까지는 추천 순위를 매기고 추천 이유를 설명해주세요.\n\n");

        int rank = 1;
        for (BookDocument b : books) {
            sb.append(rank++).append(". [").append(b.getTitle()).append("]\n");
            if (b.getId()!= null)
                sb.append("id: ").append(b.getId()).append("\n");
            if (b.getTitle() != null)
                sb.append("제목: ").append(b.getTitle()).append("\n");
            if (b.getExplanation() != null)
                sb.append("요약: ").append(b.getExplanation()).append("\n\n");
        }

        sb.append("출력형식은 아래와 같습니다.\n");
        sb.append("연관이 있는 도서는 연관도 순위로 정렬하여 도서 id 전체를 가지고 옵니다.");
        sb.append("추가적으로 연관도 순위 3등까지는 '순위,추천 이유' 형식으로 출력합니다. 출력 예를 제외한 문장은 출력하지 않습니다."
                + "출력 예: 3,18,10,14\n"
                + "        1,이 책은~~\n");


        return sb.toString();
    }
}
