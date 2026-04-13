package tn.english.school.claimservice.dto;

import tn.english.school.claimservice.enums.ClaimType;

public record ClaimTypeSuggestionResponse(ClaimType suggestedType, int confidence) {}