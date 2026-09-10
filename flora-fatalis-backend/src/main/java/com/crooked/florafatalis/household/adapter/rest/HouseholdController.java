package com.crooked.florafatalis.household.adapter.rest;

import com.crooked.florafatalis.household.application.port.in.AcceptHouseholdInvitationUseCase;
import com.crooked.florafatalis.household.application.port.in.AcceptHouseholdInvitationUseCase.AcceptHouseholdInvitationCommand;
import com.crooked.florafatalis.household.application.port.in.GetHouseholdStatusUseCase;
import com.crooked.florafatalis.household.application.port.in.GetHouseholdStatusUseCase.HouseholdOverview;
import com.crooked.florafatalis.household.application.port.in.GetHouseholdStatusUseCase.InvitationView;
import com.crooked.florafatalis.household.application.port.in.GetHouseholdStatusUseCase.MembershipView;
import com.crooked.florafatalis.household.application.port.in.LeaveHouseholdUseCase;
import com.crooked.florafatalis.household.application.port.in.LeaveHouseholdUseCase.LeaveHouseholdCommand;
import com.crooked.florafatalis.household.application.port.in.RejectHouseholdInvitationUseCase;
import com.crooked.florafatalis.household.application.port.in.RejectHouseholdInvitationUseCase.RejectHouseholdInvitationCommand;
import com.crooked.florafatalis.household.application.port.in.SendHouseholdInvitationUseCase;
import com.crooked.florafatalis.household.application.port.in.SendHouseholdInvitationUseCase.SendHouseholdInvitationCommand;
import com.crooked.florafatalis.household.application.port.in.SwitchActiveHouseholdUseCase;
import com.crooked.florafatalis.household.application.port.in.SwitchActiveHouseholdUseCase.SwitchActiveHouseholdCommand;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.HouseholdInvitationId;
import com.crooked.florafatalis.shared.application.port.out.CurrentUserProvider;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/household")
@RequiredArgsConstructor
class HouseholdController {

  private final SendHouseholdInvitationUseCase sendHouseholdInvitationUseCase;
  private final AcceptHouseholdInvitationUseCase acceptHouseholdInvitationUseCase;
  private final RejectHouseholdInvitationUseCase rejectHouseholdInvitationUseCase;
  private final LeaveHouseholdUseCase leaveHouseholdUseCase;
  private final SwitchActiveHouseholdUseCase switchActiveHouseholdUseCase;
  private final GetHouseholdStatusUseCase getHouseholdStatusUseCase;
  private final CurrentUserProvider currentUserProvider;

  @PostMapping("/invite")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void invite(@RequestBody InviteRequest request) {
    sendHouseholdInvitationUseCase.send(
        new SendHouseholdInvitationCommand(
            currentUserProvider.currentUserId(), request.inviteeEmail()));
  }

  @PostMapping("/invitations/{id}/accept")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void accept(@PathVariable String id) {
    acceptHouseholdInvitationUseCase.accept(
        new AcceptHouseholdInvitationCommand(
            new HouseholdInvitationId(UUID.fromString(id)), currentUserProvider.currentUserId()));
  }

  @PostMapping("/invitations/{id}/reject")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void reject(@PathVariable String id) {
    rejectHouseholdInvitationUseCase.reject(
        new RejectHouseholdInvitationCommand(
            new HouseholdInvitationId(UUID.fromString(id)), currentUserProvider.currentUserId()));
  }

  @PostMapping("/{id}/switch")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void switchActive(@PathVariable String id) {
    switchActiveHouseholdUseCase.switchTo(
        new SwitchActiveHouseholdCommand(
            new HouseholdId(UUID.fromString(id)), currentUserProvider.currentUserId()));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void leave(@PathVariable String id) {
    leaveHouseholdUseCase.leave(
        new LeaveHouseholdCommand(
            new HouseholdId(UUID.fromString(id)), currentUserProvider.currentUserId()));
  }

  @GetMapping("/status")
  HouseholdStatusResponse status() {
    HouseholdOverview overview =
        getHouseholdStatusUseCase.getStatus(currentUserProvider.currentUserId());
    return new HouseholdStatusResponse(
        overview.activeHouseholdId(),
        overview.households(),
        overview.incomingInvitation(),
        overview.outgoingInvitation());
  }

  record InviteRequest(String inviteeEmail) {}

  record HouseholdStatusResponse(
      UUID activeHouseholdId,
      List<MembershipView> households,
      InvitationView incomingInvitation,
      InvitationView outgoingInvitation) {}
}
