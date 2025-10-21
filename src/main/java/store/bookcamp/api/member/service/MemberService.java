package store.bookcamp.api.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store.bookcamp.api.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    //public
}
