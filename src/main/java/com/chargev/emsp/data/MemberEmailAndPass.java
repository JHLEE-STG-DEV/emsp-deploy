package com.chargev.emsp.data;

public record MemberEmailAndPass(
        String email,
        String memberPass
) {
    /**
     * @param email
     * @param memberPass
     */
    public MemberEmailAndPass(String email, String memberPass) {
        this.email = email;
        this.memberPass = memberPass;
    }
}
