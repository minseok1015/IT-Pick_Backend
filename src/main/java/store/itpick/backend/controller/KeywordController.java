package store.itpick.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import store.itpick.backend.common.exception.KeywordException;
import store.itpick.backend.common.response.BaseResponse;
import store.itpick.backend.dto.keyword.SearchDTO;
import store.itpick.backend.dto.debate.DebateByKeywordDTO;
import store.itpick.backend.model.Keyword;
import store.itpick.backend.service.KeywordService;

import java.util.List;

import static store.itpick.backend.common.response.status.BaseExceptionResponseStatus.EMPTY_REFERENCE;
import static store.itpick.backend.common.response.status.BaseExceptionResponseStatus.NO_SEARCH_KEYWORD;

@RestController
@RequiredArgsConstructor
@RequestMapping("/keyword")
public class KeywordController {

    private final KeywordService keywordService;

    @GetMapping("/search/nobadge")
    public BaseResponse<List<String>> searchKeywords(@RequestParam String query) {
        List<String> keywords = keywordService.searchKeywords(query);
        if(keywords.isEmpty()){
            throw new KeywordException(NO_SEARCH_KEYWORD);
        }
        return new BaseResponse<>(keywords);
    }
    @GetMapping("/search")
    public BaseResponse<List<SearchDTO>> searchKeywordsWithBadge(@RequestParam String query) {
        List<SearchDTO> keywords = keywordService.searchKeywordsWithBadge(query);
        if(keywords.isEmpty()){
            throw new KeywordException(NO_SEARCH_KEYWORD);
        }
        return new BaseResponse<>(keywords);
    }
}
