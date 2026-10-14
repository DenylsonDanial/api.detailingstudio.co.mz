package mz.co.amaterasu.gestao.user.resource;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.base.entity.UserTenant;
import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import mz.co.amaterasu.gestao.exceptions.ValidationException;
import mz.co.amaterasu.gestao.user.dto.*;
import mz.co.amaterasu.gestao.user.dto.projection.PUserDTO;
import mz.co.amaterasu.gestao.user.dto.projection.PUserFindAllDTO;
import mz.co.amaterasu.gestao.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

	public static final String SERVER_ERROR = "Server error";

	private final UserService userService;


	@PreAuthorize("hasAuthority('create:users')")
	@PostMapping("/insert")
	public ResponseEntity<Object> insert(@RequestBody @Valid UserDTO user) throws ApiRequestException {

		PUserDTO dto;
		try {
			dto = userService.save(user);
		} catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
			throw new ApiRequestException(SERVER_ERROR);
		} catch (ValidationException e) {
			return ResponseEntity.badRequest().body(e.getErrors());
		}

		return ResponseEntity.ok().body(dto);
	}

	/** Definicao da palavra-passe pelo proprio utilizador. Preferir POST /auth/set-password no primeiro acesso. */
	@PostMapping("/setPassword/{id}")
	public ResponseEntity<Object> setPassword(@RequestBody @Valid PasswordSetDTO creds,
											  @PathVariable Long id,
											  @AuthenticationPrincipal UserDetails userDetails) throws ApiRequestException {
		return ResponseEntity.ok().body(userService.setPassword(creds, id, userDetails.getUsername()));
	}

	/** Reposicao pelo administrador: envia uma palavra-passe temporaria por email. */
	@PreAuthorize("hasAuthority('update:users')")
	@PostMapping("/resetPassword/{id}")
	public ResponseEntity<RequestSuccessResponse> resetPassword(@PathVariable Long id) {
		userService.resetPassword(id);
		return ResponseEntity.ok(new RequestSuccessResponse(
				"Palavra-passe temporaria enviada para o email do utilizador", HttpStatus.OK));
	}

	@PreAuthorize("hasAuthority('update:users')")
	@PutMapping("/update/{id}")
	public ResponseEntity<PUserDTO> update(@RequestBody @Valid UserDTO user, @PathVariable Long id, @RequestParam(required = false) boolean isAccessUpdate) throws  ApiRequestException {
		PUserDTO dto = userService.update(id, user, isAccessUpdate);
		return ResponseEntity.ok().body(dto);
	}

	@PreAuthorize("hasAuthority('read:users')")
	@GetMapping("/list")
	public Page<PUserFindAllDTO> findAll(Pageable pageable,
										 @RequestParam(required = false) String name,
										 @RequestParam(required = false) String status) {

		return userService.findAll(pageable, status, name);
	}

	@PreAuthorize("hasAuthority('read:users')")
	@GetMapping("/findById/{id}")
	public ResponseEntity<PUserDTO> findById(@PathVariable Long id) throws ApiRequestException {
		PUserDTO userDTO = null;
		try {
			userDTO = userService.findById(id);
		} catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | InstantiationException e) {
			throw new ApiRequestException(SERVER_ERROR);
		}
		return new ResponseEntity<>(userDTO, HttpStatus.OK);
	}

	@PreAuthorize("hasAuthority('read:users')")
	@GetMapping("/search")
	public ResponseEntity<PUserDTO> findByUsername(@RequestParam String username) throws ApiRequestException {
		PUserDTO userDTO = null;
		try {
			userDTO = userService.findByUsername(username);
		} catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
			throw new ApiRequestException(SERVER_ERROR);
		}
		return new ResponseEntity<>(userDTO, HttpStatus.OK);
	}

	@PreAuthorize("hasAuthority('delete:users')")
	@DeleteMapping(value="/deleteById/{id}")
	public ResponseEntity<RequestSuccessResponse> delete(@PathVariable Long id) throws ApiRequestException {
		RequestSuccessResponse result = userService.delete(id);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/loggedUserInfo")
	public ResponseEntity<PUserDTO> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) throws ApiRequestException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
		if (userDetails != null) {
			return new ResponseEntity<>(userService.findByUsername(userDetails.getUsername()), HttpStatus.OK); // Username or identifier
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	/** Empresas a que o utilizador autenticado tem acesso, para o seletor de empresa. */
	@GetMapping("/myCompanies")
	public ResponseEntity<List<UserTenant>> myCompanies(@AuthenticationPrincipal UserDetails userDetails) {
		return ResponseEntity.ok(userService.findTenantsByUsername(userDetails.getUsername()));
	}

}

