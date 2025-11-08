package store.bookscamp.api.book.feign;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import store.bookscamp.api.book.controller.request.RerankerRequest;
import store.bookscamp.api.book.controller.response.RerankerResponse;
import store.bookscamp.api.common.config.FeignConfig;

@FeignClient(name = "rerankerClient", url = "http://reranker.java21.net",configuration = FeignConfig.class)
public interface RerankerClient {

    @PostMapping("/rerank")
    List<RerankerResponse> rerank(@RequestBody RerankerRequest request);

}
