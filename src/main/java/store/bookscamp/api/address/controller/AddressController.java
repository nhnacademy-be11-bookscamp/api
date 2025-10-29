package store.bookscamp.api.address.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.address.controller.request.AddressCreateRequest;
import store.bookscamp.api.address.controller.response.AddressListResponse;
import store.bookscamp.api.address.service.dto.AddressCreateDto;
import store.bookscamp.api.address.service.AddressService;
import store.bookscamp.api.address.service.dto.AddressReadDto;

@RestController
@RequestMapping("/member/{username}/address")
@Tag(name = "Address API", description = "Address CRUD API입니다")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }


    @PostMapping
    @Tag(name = "Address API")
    @Operation(summary = "create Address", description = "회원 주소 생성 API")
    public ResponseEntity<Void> createAddress(
            @Valid @RequestBody AddressCreateRequest addressCreateRequest,
            @PathVariable String username) {
        AddressCreateDto addressCreateDto = AddressCreateRequest.toDto(addressCreateRequest);
        addressService.createMemberAddress(addressCreateDto, username);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    @Tag(name = "Address API")
    @Operation(summary = "get Address List", description = "회원 주소 리스트 조회 API")
    public ResponseEntity<AddressListResponse> getAddresses(@PathVariable String username) {
        List<AddressReadDto> addressDtos = addressService.getMemberAddresses(username);
        AddressListResponse response = AddressListResponse.from(addressDtos);
        return ResponseEntity.ok(response);
    }
}
