package store.bookscamp.api.address.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.address.controller.request.AddressCreateRequest;
import store.bookscamp.api.address.controller.response.AddressListResponse;
import store.bookscamp.api.address.controller.request.AddressUpdateRequest;
import store.bookscamp.api.address.service.AddressService;
import store.bookscamp.api.address.service.dto.AddressCreateDto;
import store.bookscamp.api.address.service.dto.AddressReadDto;
import store.bookscamp.api.address.service.dto.AddressUpdateRequestDto;

@RestController
@RequestMapping("/member/{username}/address")
@Tag(name = "주소 API", description = "Address CRUD API입니다")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    @Operation(summary = "create Address", description = "회원 주소 생성 API")
    public ResponseEntity<Void> createAddress(
            @Valid @RequestBody AddressCreateRequest addressCreateRequest,
            @PathVariable String username) {
        AddressCreateDto addressCreateDto = AddressCreateRequest.toDto(addressCreateRequest);
        addressService.createMemberAddress(addressCreateDto, username);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "get Address List", description = "회원 주소 리스트 조회 API")
    public ResponseEntity<AddressListResponse> getAddresses(@PathVariable String username) {
        List<AddressReadDto> addressDtos = addressService.getMemberAddresses(username);
        AddressListResponse response = AddressListResponse.from(addressDtos);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{addressId}")
    @Operation(summary = "update Address", description = "회원 주소 수정 API")
    public ResponseEntity<Void> updateAddress(
            @PathVariable String username,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressUpdateRequest addressUpdateRequest) {

        AddressUpdateRequestDto addressUpdateDto = AddressUpdateRequest.toDto(addressUpdateRequest);
        addressService.updateMemberAddress(username, addressId, addressUpdateDto);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/member/" + username + "/address");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{addressId}")
    @Operation(summary = "delete Address", description = "회원 주소 삭제 API")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable String username,
            @PathVariable Long addressId) {

        addressService.deleteMemberAddress(username, addressId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
