package store.bookscamp.api.address.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.address.controller.request.AddressCreateRequest;
import store.bookscamp.api.address.controller.request.AddressUpdateRequest;
import store.bookscamp.api.address.controller.response.AddressListResponse;
import store.bookscamp.api.address.service.AddressService;
import store.bookscamp.api.address.service.dto.AddressCreateDto;
import store.bookscamp.api.address.service.dto.AddressReadDto;
import store.bookscamp.api.address.service.dto.AddressUpdateRequestDto;

@RestController
@RequestMapping("/member/address")
@Tag(name = "주소 API", description = "Address CRUD API입니다")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    @Operation(summary = "create Address", description = "회원 주소 생성 API")
    public ResponseEntity<Void> createAddress(
            @Valid @RequestBody AddressCreateRequest addressCreateRequest,
            HttpServletRequest request) {

        Long memberId = Long.parseLong(request.getHeader("X-USER-ID"));

        AddressCreateDto addressCreateDto = AddressCreateRequest.toDto(addressCreateRequest);
        // 서비스 시그니처: createMemberAddress(Long memberId, AddressCreateDto dto)
        addressService.createMemberAddress(memberId, addressCreateDto);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "get Address List", description = "회원 주소 리스트 조회 API")
    public ResponseEntity<AddressListResponse> getAddresses(HttpServletRequest request) {
        Long memberId = Long.parseLong(request.getHeader("X-USER-ID"));
        List<AddressReadDto> addressDtos = addressService.getMemberAddresses(memberId);
        AddressListResponse response = AddressListResponse.from(addressDtos);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{addressId}")
    @Operation(summary = "update Address", description = "회원 주소 수정 API")
    public ResponseEntity<Void> updateAddress(
            HttpServletRequest request,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressUpdateRequest addressUpdateRequest) {

        Long memberId = Long.parseLong(request.getHeader("X-USER-ID"));
        AddressUpdateRequestDto addressUpdateDto = AddressUpdateRequest.toDto(addressUpdateRequest);
        addressService.updateMemberAddress(memberId, addressId, addressUpdateDto);

        // Location 헤더 굳이 쓸 필요 없으면 깔끔하게 OK만 반환
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{addressId}")
    @Operation(summary = "delete Address", description = "회원 주소 삭제 API")
    public ResponseEntity<Void> deleteAddress(
            HttpServletRequest request,
            @PathVariable Long addressId) {

        Long memberId = Long.parseLong(request.getHeader("X-USER-ID"));
        addressService.deleteMemberAddress(memberId, addressId);
        return ResponseEntity.noContent().build();
    }
}
