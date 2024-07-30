package likelion.hufsglobal.lgtu.runwithmate.utils;

import lombok.RequiredArgsConstructor;

import java.security.Principal;

@RequiredArgsConstructor
public class UserPrincipal implements Principal {
    private final String userId;

    @Override
    public String getName() {
        return userId;
    }
}