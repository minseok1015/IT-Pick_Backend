package store.itpick.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import store.itpick.backend.common.exception.ReferenceException;
import store.itpick.backend.dto.rank.RankResponseDTO;
import store.itpick.backend.model.CommunityPeriod;
import store.itpick.backend.model.Keyword;
import store.itpick.backend.model.Reference;
import store.itpick.backend.repository.CommunityPeriodRepository;
import store.itpick.backend.repository.KeywordRepository;

import java.util.List;
import java.util.Optional;

import static store.itpick.backend.common.response.status.BaseExceptionResponseStatus.NO_Search_REFERENCE;


//관련자료 조회하는 Service
@Service
public class RankService {
    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private CommunityPeriodRepository communityPeriodRepository;

    public RankResponseDTO getReferenceByKeyword(String community, String period, String keyword) {
        List<String> communitiesToCheck = "total".equals(community)
                ? List.of("naver", "zum", "nate")
                : List.of(community);

        for (String comm : communitiesToCheck) {
            Optional<Keyword> keywordOptional;

            if ("weekly".equals(period)) {
                // weekly 기간에 대한 처리
                keywordOptional = keywordRepository.findTop1ByKeywordAndCommunityOrderByUpdatedAtDesc(keyword, comm);
            } else {
                // 다른 기간에 대한 처리
                Optional<CommunityPeriod> communityPeriodOptional = communityPeriodRepository.findByCommunityAndPeriod(comm, period);

                if (communityPeriodOptional.isPresent()) {
                    CommunityPeriod communityPeriod = communityPeriodOptional.get();
                    keywordOptional = keywordRepository.findTop1ByKeywordAndCommunityOrderByUpdatedAtDesc(keyword, comm);
                } else {
                    continue;
                }
            }

            if (keywordOptional.isPresent()) {
                Keyword latestKeyword = keywordOptional.get();
                Reference reference = latestKeyword.getReference();

                RankResponseDTO response = new RankResponseDTO();
                response.setKeywordId(latestKeyword.getKeywordId());
                response.setKeyword(latestKeyword.getKeyword());
                response.setSearchLink(reference.getSearchLink());
                response.setNewsTitle(reference.getNewsTitle());
                response.setImageUrl(reference.getNewsImage());
                response.setNewsContent(reference.getNewsContent());
                response.setNewsLink(reference.getNewsLink());

                isValidReference(reference.getNewsTitle());

                return response;
            }
        }

        return null;
    }
    private void isValidReference(String newsTitle){
        if(newsTitle.equals("No search")){
            throw new ReferenceException(NO_Search_REFERENCE);
        }
    }



}
