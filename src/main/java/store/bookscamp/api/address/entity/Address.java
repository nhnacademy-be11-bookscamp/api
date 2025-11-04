package store.bookscamp.api.address.entity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import store.bookscamp.api.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Address {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String label; // 별칭

    @Column(nullable = false)
    private String roadNameAddress;

    @Column(nullable = false)
    private Integer zipCode;

    @Column(nullable = false)
    private boolean isDefault;

    private String detailAddress;

    public Address(Member member, String label, String roadNameAddress, Integer zipCode, boolean isDefault,
                   String detailAddress) {
        this.member = member;
        this.label = label;
        this.roadNameAddress = roadNameAddress;
        this.zipCode = zipCode;
        this.isDefault = isDefault;
        this.detailAddress = detailAddress;
    }

    public void updateAddress(String label,
                              String roadNameAddress,
                              Integer zipCode,
                              boolean isDefault,
                              String detailAddress) {
        this.label = label;
        this.roadNameAddress = roadNameAddress;
        this.zipCode = zipCode;
        this.isDefault = isDefault;
        this.detailAddress = detailAddress;
    }
}
