package kr.ac.hansung.controller;

import kr.ac.hansung.dto.PasswordChangeDto;
import kr.ac.hansung.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/user/password")
    public String passwordForm(Model model) {
        model.addAttribute("passwordChangeDto", new PasswordChangeDto());
        return "user/password";
    }

    @PostMapping("/user/password")
    public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                 @ModelAttribute("passwordChangeDto") PasswordChangeDto dto,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        List<String> errors = validatePasswordChangeDto(dto);

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("passwordChangeDto", dto);
            return "user/password";
        }

        try {
            userService.changePassword(
                    userDetails.getUsername(),
                    dto.getCurrentPassword(),
                    dto.getNewPassword()
            );

            redirectAttributes.addFlashAttribute("successMessage", "비밀번호가 변경되었습니다.");
            return "redirect:/home";

        } catch (IllegalArgumentException e) {
            errors.add(e.getMessage());
            model.addAttribute("errors", errors);
            model.addAttribute("passwordChangeDto", dto);
            return "user/password";
        }
    }

    private List<String> validatePasswordChangeDto(PasswordChangeDto dto) {
        List<String> errors = new ArrayList<>();

        if (dto.getCurrentPassword() == null || dto.getCurrentPassword().isBlank()) {
            errors.add("현재 비밀번호를 입력하세요.");
        }

        if (dto.getNewPassword() == null || dto.getNewPassword().isBlank()) {
            errors.add("새 비밀번호를 입력하세요.");
        } else if (dto.getNewPassword().length() < 8) {
            errors.add("새 비밀번호는 8자 이상이어야 합니다.");
        }

        if (dto.getConfirmPassword() == null || dto.getConfirmPassword().isBlank()) {
            errors.add("새 비밀번호 확인을 입력하세요.");
        }

        if (dto.getNewPassword() != null
                && dto.getConfirmPassword() != null
                && !dto.getNewPassword().equals(dto.getConfirmPassword())) {
            errors.add("새 비밀번호가 일치하지 않습니다.");
        }

        return errors;
    }
}