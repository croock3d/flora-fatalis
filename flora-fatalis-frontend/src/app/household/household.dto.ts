export interface MembershipView {
  householdId: string;
  label: string;
  ownedByMe: boolean;
  active: boolean;
  hasPartner: boolean;
  partnerDisplayName: string | null;
}

export interface InvitationView {
  invitationId: string;
  householdId: string;
  partnerDisplayName: string;
  invitedByMe: boolean;
}

export interface HouseholdStatusResponse {
  activeHouseholdId: string | null;
  households: MembershipView[];
  incomingInvitation: InvitationView | null;
  outgoingInvitation: InvitationView | null;
}

export interface SendInvitationRequest {
  inviteeEmail: string;
}
