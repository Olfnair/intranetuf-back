/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest.security;

/**
 *
 * @author Florian
 */
public class AuthToken {
    private long nonce;
    private long userId;
    private long roleId;

    public AuthToken() {
    }

    public AuthToken(long nonce, long userId, long roleId) {
        this.nonce = nonce;
        this.userId = userId;
        this.roleId = roleId;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }
    
    @Override
    public String toString() {
        // TODO : compresser, chiffrer, signer
        return "token";
    }
}
