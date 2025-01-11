package io.dy.gamecenter.api.models;

import io.dy.gamecenter.api.constants.Enums;
import lombok.Data;
import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Document(collection = "users")
@Data
public class UserModel extends TimeModel implements UserDetails {

    @Id
    private String id;

    @Field("user_id")
    private String userId;

    private String username;

    private String password;

    @Field("display_name")
    private String displayName;

    @Field("avatar_url")
    private String avatarUrl;

    @Field("social_id")
    private String socialId;

    @Field("social_type")
    private int socialType;

    private String email;

    private List<String> roles;



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    public UserModel() {
    }

    public UserModel(String username, String password) {
        String uuid = UUID.randomUUID().toString();
        this.username = username;
        this.password = password;
        this.userId = uuid;
        this.displayName = "DEFAULT DEFAULT";
        this.avatarUrl = "https://assets.dy.io/assets/default.png";
        this.email = "";
        this.socialId = uuid;
        this.socialType = Enums.SocialTypeLogin.AUTH_LOGIN.getValue();
        this.roles = new ArrayList<>(Arrays.asList("USER"));
        this.setCreatedAt(DateTime.now().toDate());
        this.setUpdatedAt(DateTime.now().toDate());
    }
}
